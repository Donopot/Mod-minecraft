package com.donopot.voiddynastybridge;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.storage.LevelResource;

/** All game access and state transitions run on the integrated server thread. */
public final class DeliveryService implements AutoCloseable {
    public static final String MISSION=CargoCatalog.HASH;
    public final MinecraftServer server;
    private final Path file;
    private BridgeWorld s;
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"VoidDynasty delivery");t.setDaemon(true);return t;});
    private volatile Socket socket;
    private volatile boolean closed;
    private boolean inFlight, connected, sending;
    private final DeliveryInteraction interaction=new DeliveryInteraction();
    private String availability="Connexion en cours";
    private long nextPoll, received;
    private JsonObject remote;
    private String message="Connexion a Rogue Trader...";
    public record View(String status,String availability,String world,String campaign,String zone,String phase,String delivery,int iron,int reserved,int reserve,int completed,boolean paired,boolean connected,boolean canShip,boolean hasCrate,String mission,int required,int nativeCount,boolean canSelect,String missionId){}
    private volatile View view=new View("Chargement","Connexion en cours","","","","IDLE","",0,0,0,0,false,false,false,false,"Composants de vaisseau",64,0,false,CargoCatalog.DEFAULT);
    public View view(){return view;}
    public DeliveryService(MinecraftServer server){this.server=server;file=server.getWorldPath(LevelResource.ROOT).resolve("data/voiddynasty-bridge.json");
        try{s=BridgeWorld.read(file);s.save(file);if(Set.of("RESERVING","SETTLING").contains(s.phase))recover("Ecriture interrompue: checkpoint requis");}
        catch(Exception e){s=new BridgeWorld();s.phase="RECOVERY_REQUIRED";message="Journal illisible; conserver les fichiers et restaurer un checkpoint";}
    }
    public boolean locked(){return !Set.of("IDLE","DRAFT","SETTLED").contains(s.phase)||closed;}
    public boolean matches(CargoBlockEntity c){return c.world.equals(s.world)&&c.crate.equals(s.crate)&&c.getBlockPos().equals(new BlockPos(s.x,s.y,s.z))&&c.getLevel()!=null&&c.getLevel().dimension().identifier().toString().equals(s.dimension);}
    private CargoBlockEntity cargo(){
        if(s.crate.isEmpty())return null;
        ServerLevel level=server.getLevel(ResourceKey.create(Registries.DIMENSION,Identifier.parse(s.dimension)));
        if(level==null)throw new IllegalStateException("Dimension introuvable");
        BlockPos p=new BlockPos(s.x,s.y,s.z);level.getChunkAt(p);
        if(!(level.getBlockEntity(p) instanceof CargoBlockEntity c)||!matches(c))throw new IllegalStateException("Conteneur absent, copie ou deplace");
        return c;
    }
    private CargoCatalog.Mission activeMission(){return CargoCatalog.get(s.delivery_mission_id);}
    private void verifyCargo(CargoBlockEntity c){
        if(c==null){if(!s.delivery.isEmpty())throw new IllegalStateException("Conteneur manquant");return;}
        boolean ok=switch(s.phase){case "IDLE"->c.reserved==0&&c.phase.equals("IDLE")&&c.delivery.isEmpty();case "DRAFT"->c.reserved==0&&(s.completed_count==0?c.phase.equals("IDLE")&&c.delivery.isEmpty():c.phase.equals("SETTLED")&&c.delivery.equals(s.last_receipt_id));case "RESERVED"->c.reserved==activeMission().count()&&c.reservedItem.equals(activeMission().item())&&c.mission.equals(s.delivery_mission_id)&&c.phase.equals("RESERVED")&&c.delivery.equals(s.delivery);case "SETTLED"->c.reserved==0&&c.phase.equals("SETTLED")&&c.delivery.equals(s.delivery);default->false;};
        if(!ok)throw new IllegalStateException("Divergence conteneur / registre: checkpoint requis");
    }
    private void persist() throws IOException{s.save(file);}
    private void recover(String why){sending=false;connected=false;s.phase="RECOVERY_REQUIRED";message=why;try{persist();}catch(Exception ignored){} }
    private boolean safe(){return !closed&&server.isSingleplayer()&&!server.isPublished()&&!s.phase.equals("RECOVERY_REQUIRED");}
    public void tick(){
        if(!safe()){connected=false;publish();return;}
        try{
            CargoBlockEntity c=cargo();verifyCargo(c);
            if(System.currentTimeMillis()-received>6000)connected=false;
            if(!inFlight&&System.currentTimeMillis()>=nextPoll){nextPoll=System.currentTimeMillis()+2000;request("inspect");}
        }catch(Exception e){recover(e.getMessage());}
        publish();
    }
    private void publish(){
        CargoBlockEntity c=null;try{c=cargo();}catch(Exception ignored){}
        boolean fresh=connected&&System.currentTimeMillis()-received<6000;
        var selected=CargoCatalog.get(s.selected_mission_id);
        int iron=0;try{if(c!=null)iron=c.count(selected.item());}catch(Exception ignored){}
        boolean ready=remote!=null&&text(remote,"code").equals("OK")&&remote.get("available").getAsBoolean();
        if(s.phase.equals("SETTLED"))message="Derniere livraison confirmee ("+s.completed_count+" au total)";
        String reason=availability;
        if(ready){
            if(s.completed_count>=256)reason="Registre plein : nouvelle version necessaire";
            else if(interaction.mutationBusy())reason="Enregistrement de la livraison en cours";
            else if(!s.paired)reason="Lier le monde a la campagne";
            else if(s.phase.equals("IDLE")||s.phase.equals("SETTLED"))reason=iron>=selected.count()?"Pret : une cargaison native "+selected.label():"Deposer encore "+(selected.count()-iron)+" unites";
            else reason="Livraison en cours; Reprendre si necessaire";
        }
        view=new View(message,fresh?reason:"Rogue Trader deconnecte ou sans reponse recente",s.world,remote==null?"":text(remote,"campaign_name"),remote==null?"":text(remote,"zone"),s.phase,s.delivery,iron,c==null?0:c.reserved,remote==null||text(remote,"campaign_id").isEmpty()?-1:remote.get("reserve").getAsInt(),s.completed_count,s.paired,fresh,
            safe()&&s.completed_count<256&&DeliveryInteraction.canShip(fresh,ready,s.paired,s.phase,iron,selected.count(),interaction.mutationBusy()),c!=null,selected.label()+" : "+selected.count()+" "+selected.material(),selected.count(),remote==null?0:remote.get("native_count").getAsInt(),safe()&&!interaction.mutationBusy()&&(s.phase.equals("IDLE")||s.phase.equals("SETTLED")),selected.id());
    }
    public void action(UUID playerId,String action){
        if(!server.isSameThread())throw new IllegalStateException("Server thread required");
        try{
            if(!safe())throw new IllegalStateException("Pont indisponible");
            if(inFlight){if(interaction.defer(playerId,action))return;throw new IllegalStateException("Operation en cours");}
            if(action.startsWith("ship:")){
                if(!action.substring(5).equals(s.selected_mission_id))throw new IllegalStateException("Mission modifiee : confirmer a nouveau");
                action="ship";
            }
            ServerPlayer player=server.getPlayerList().getPlayer(playerId);if(player==null)throw new IllegalStateException("Joueur absent");
            if(action.equals("previous_mission")||action.equals("next_mission")){
                if(!s.phase.equals("IDLE")&&!s.phase.equals("SETTLED"))throw new IllegalStateException("Terminer la livraison avant de changer de mission");
                int index=0;while(!CargoCatalog.ALL.get(index).id().equals(s.selected_mission_id))index++;
                index=Math.floorMod(index+(action.equals("next_mission")?1:-1),CargoCatalog.ALL.size());
                s.selected_mission_id=CargoCatalog.ALL.get(index).id();persist();publish();return;
            }
            if(action.equals("create")){
                if(!s.crate.isEmpty())throw new IllegalStateException("Un depot existe deja dans ce monde");
                BlockPos p=player.blockPosition().relative(player.getDirection(),2);
                ServerLevel level=player.level();if(!level.isEmptyBlock(p)||!level.isInWorldBounds(p))throw new IllegalStateException("Il faut une case libre deux blocs devant toi");
                s.crate=UUID.randomUUID().toString();s.dimension=level.dimension().identifier().toString();s.x=p.getX();s.y=p.getY();s.z=p.getZ();
                // An intent without its corresponding chunk is detectable on restart.
                persist();if(!level.setBlockAndUpdate(p,BridgeMod.CARGO.defaultBlockState()))throw new IOException("Pose impossible");
                CargoBlockEntity c=(CargoBlockEntity)level.getBlockEntity(p);c.world=s.world;c.crate=s.crate;c.setChanged();
                server.saveEverything(false,true,true);persist();message="Depot cree: clic droit pour deposer le fer";
            }else{
                CargoBlockEntity c=cargo();verifyCargo(c);
                if(c==null||player.level()!=c.getLevel()||player.distanceToSqr(s.x+0.5,s.y+0.5,s.z+0.5)>64)throw new IllegalStateException("Approche-toi du depot (8 blocs maximum)");
                if(action.equals("open")){player.openMenu(c);
                }else if(action.equals("pair")){
                    if(!connected||remote==null)throw new IllegalStateException("Rogue Trader indisponible");
                    if(s.paired)throw new IllegalStateException("Monde deja lie");
                    if(s.campaign.isEmpty()){s.campaign=text(remote,"campaign_id");s.pair=UUID.randomUUID().toString();s.checkpoint=UUID.randomUUID().toString();persist();}
                    request("propose_pair");
                }else if(action.equals("ship")){
                    if(!view.canShip())throw new IllegalStateException("Materiaux, lien et campagne disponibles requis");
                    player.closeContainer();s.delivery_mission_id=s.selected_mission_id;s.delivery=UUID.randomUUID().toString();s.hash=fingerprint(s);s.phase="DRAFT";persist();sending=true;request("prepare");
                }else if(action.equals("resume")){
                    if(!s.paired||(!s.phase.equals("DRAFT")&&!s.phase.equals("RESERVED")))throw new IllegalStateException("Aucune livraison reprenable");
                    sending=true;request("status");
                }
            }
        }catch(IOException e){recover("Echec de sauvegarde: cargaison verrouillee");}
        catch(Exception e){message=e.getMessage();}
        publish();
    }
    private JsonObject envelope(String type){
        JsonObject q=new JsonObject();q.addProperty("protocol_version",4);q.addProperty("type",type);q.addProperty("message_id",UUID.randomUUID().toString());
        q.addProperty("session_id",remote==null?"":text(remote,"session_id"));q.addProperty("campaign_id",s.campaign);q.addProperty("world_id",s.world);q.addProperty("pair_id",s.pair);q.addProperty("checkpoint_id",s.checkpoint);
        q.addProperty("delivery_id",s.delivery);q.addProperty("known_state",s.phase);q.addProperty("mission_hash",MISSION);q.addProperty("payload_hash",s.hash);var m=s.phase.equals("IDLE")?CargoCatalog.get(s.selected_mission_id):activeMission();q.addProperty("mission_id",m.id());q.addProperty("item_id",m.item());q.addProperty("count",m.count());q.addProperty("completed_count",s.completed_count);q.addProperty("last_receipt_id",s.last_receipt_id);return q;
    }
    private void request(String type){
        JsonObject q=envelope(type);inFlight=true;interaction.started(type);if(!type.equals("inspect"))message=switch(type){case "prepare"->"Preparation...";case "commit"->"Livraison en cours; sauvegarde Rogue Trader...";case "propose_pair"->"Valide le monde affiche dans Rogue Trader (Ctrl+F10)";default->"Rapprochement des recus...";};
        worker.submit(()->{
            JsonObject response=null;String error=null;
            try{response=rpc(q);}catch(Exception e){error="Connexion interrompue; utiliser Reprendre avec le meme recu";}
            final JsonObject reply=response;final String failure=error;
            if(!closed)server.execute(()->{if(closed)return;inFlight=false;var click=interaction.completed();
                if(failure!=null){connected=false;availability=failure;message=failure;sending=false;publish();return;}
                try{accept(type,reply);}catch(Exception e){recover(e.getMessage());}publish();
                if(click!=null&&!inFlight&&safe())action(click.player(),click.action());});
        });
    }
    private void accept(String request,JsonObject r) throws Exception{
        remote=r;received=System.currentTimeMillis();String code=text(r,"code");connected=true;
        availability=DeliveryInteraction.pauseMessage(code);
        boolean paused=code.equals("PAUSED_MODE")||code.equals("PAUSED_COMBAT");
        if(code.equals("OK"))availability=r.get("available").getAsBoolean()?"Envoi disponible":"Envoi temporairement suspendu";
        if(code.equals("PAIR_APPROVAL_REQUIRED")){message="Dans Rogue Trader: Ctrl+F10 puis Lier cette campagne au monde affiche";return;}
        if(!code.equals("OK")&&!paused){
            if(Set.of("RECOVERY_REQUIRED","PAYLOAD_MISMATCH","PERSISTENCE_FAILED").contains(code))recover(code+": restaurer un checkpoint appaire");
            else{message=availability.isEmpty()?"Rogue Trader: "+code:availability;sending=false;}return;
        }
        if(!s.campaign.isEmpty()&&!s.campaign.equals(text(r,"campaign_id")))throw new IOException("Campagne differente");
        if(!s.pair.isEmpty()){
            if(!s.pair.equals(text(r,"pair_id"))||!s.world.equals(text(r,"world_id"))||!s.checkpoint.equals(text(r,"checkpoint_id"))){
                if(!s.paired&&text(r,"pair_id").isEmpty()){message="Appairage a confirmer dans Rogue Trader";return;}
                throw new IOException("Divergence de paire/checkpoint");
            }
            if(!s.paired){s.paired=true;persist();message="Campagne et monde lies; choisir une mission";}
        }else if(!text(r,"pair_id").isEmpty()){connected=false;message="Cette campagne est deja liee a un monde; charger le monde correspondant";return;}
        else{message="Connexion etablie; creer le depot puis lier la campagne";return;}
        var result=DeliveryReconciliation.inspect(s,r);
        if(result==DeliveryReconciliation.Result.COMMITTED){
            CargoBlockEntity c=cargo();verifyCargo(c);s.phase="SETTLING";persist();c.settle(s.delivery);server.saveEverything(false,true,true);
            s.completed_count++;s.last_receipt_id=s.delivery;s.phase="SETTLED";persist();sending=false;
            message="Livraison confirmee : une cargaison native "+activeMission().label();return;
        }
        if(paused){sending=false;return;}
        if(sending&&s.phase.equals("DRAFT")){
            if(result==DeliveryReconciliation.Result.PREPARED){
                CargoBlockEntity c=cargo();verifyCargo(c);if(c.count(activeMission().item())<activeMission().count()){sending=false;message="Preparation conservee; completer les materiaux puis Reprendre";return;}
                for(ServerPlayer p:server.getPlayerList().getPlayers())p.closeContainer();
                s.phase="RESERVING";persist();c.reserve(s.delivery,activeMission());server.saveEverything(false,true,true);s.phase="RESERVED";persist();request("commit");
            }else if(result==DeliveryReconciliation.Result.NEED_PREPARE)request("prepare");else throw new IOException("Etat distant incompatible");
        }else if(sending&&s.phase.equals("RESERVED")&&result==DeliveryReconciliation.Result.PREPARED)request("commit");
        else if(!s.phase.equals("SETTLED"))message=s.phase.equals("IDLE")?"Mission repetable : une cargaison native par envoi":"Cargaison conservee; Reprendre pour poursuivre";
    }

    private JsonObject rpc(JsonObject q) throws Exception{
        Path config=Path.of(System.getenv("LOCALAPPDATA"),"VoidDynastyBridge","diagnostic.local.json");
        if(Files.size(config)>4096)throw new IOException();JsonObject local=JsonParser.parseString(Files.readString(config)).getAsJsonObject();q.addProperty("token",text(local,"token"));
        try(Socket c=new Socket()){
            socket=c;c.connect(new InetSocketAddress("127.0.0.1",28765),3000);c.setSoTimeout(130000);
            c.getOutputStream().write((q+"\n").getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream data=new ByteArrayOutputStream();int b;while((b=c.getInputStream().read())!=10){if(b<0||data.size()>=4095)throw new IOException();data.write(b);}
            return DeliveryWire.reply(data.toString(StandardCharsets.UTF_8),text(q,"message_id"));
        }finally{socket=null;}
    }
    public static String fingerprint(BridgeWorld s)throws Exception{
        var m=CargoCatalog.get(s.delivery_mission_id);
        String json="{\"campaign_id\":\""+s.campaign+"\",\"cargo\":[{\"count\":"+m.count()+",\"item_id\":\""+m.item()+"\"}],\"mission_id\":\""+m.id()+"\",\"mission_version\":3,\"native_cargo_blueprint\":\""+m.blueprint()+"\",\"pair_id\":\""+s.pair+"\",\"previous_delivery_id\":\""+s.last_receipt_id+"\",\"sequence\":"+(s.completed_count+1)+",\"world_id\":\""+s.world+"\"}";
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.getBytes(StandardCharsets.UTF_8)));
    }
    private static String text(JsonObject o,String k){return o.has(k)&&!o.get(k).isJsonNull()?o.get(k).getAsString():"";}
    @Override public void close(){closed=true;worker.shutdownNow();try{if(socket!=null)socket.close();}catch(IOException ignored){} }
}
