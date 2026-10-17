package com.donopot.voiddynastybridge;

import com.google.gson.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.UUID;
import static java.nio.file.StandardOpenOption.*;

/** World-scoped journal; intent markers deliberately fail closed across partial saves. */
public final class BridgeWorld {
    public int schema=2;
    public String world=UUID.randomUUID().toString(), pair="", campaign="", checkpoint="";
    public String crate="", dimension="", delivery="", phase="IDLE", hash="";
    public int x,y,z;
    public int repeat_version=2, completed_count;
    public String last_receipt_id="";
    public int catalog_version=1;
    public String selected_mission_id=CargoCatalog.DEFAULT,delivery_mission_id=CargoCatalog.DEFAULT;
    public boolean paired;
    public static final Gson JSON=new Gson();
    public static BridgeWorld read(Path file) throws IOException {
        if(!Files.exists(file)) return new BridgeWorld();
        if(Files.size(file)>16384) throw new IOException("Journal trop grand");
        String json=Files.readString(file);
        BridgeWorld s=JSON.fromJson(json,BridgeWorld.class);
        if(s!=null&&!JsonParser.parseString(json).getAsJsonObject().has("repeat_version"))s.repeat_version=0;
        if(s!=null&&s.phase!=null&&!JsonParser.parseString(json).getAsJsonObject().has("catalog_version")&&!s.phase.equals("IDLE")&&!s.phase.equals("SETTLED"))throw new IOException("Terminer la livraison precedente avant mise a jour");
        if(s==null || s.schema!=2 || !uuid(s.world) || s.phase==null
            || !java.util.Set.of("IDLE","DRAFT","RESERVING","RESERVED","SETTLING","SETTLED","RECOVERY_REQUIRED").contains(s.phase)) throw new IOException("Journal invalide");
        if(s.paired && (!uuid(s.pair)||!uuid(s.campaign)||!uuid(s.checkpoint)))throw new IOException("Identite invalide");
        if(!s.delivery.isEmpty() && (!uuid(s.delivery)||s.hash.length()!=64))throw new IOException("Livraison invalide");
        s.upgrade();
        return s;
    }
    public void upgrade()throws IOException{
        if(catalog_version!=1)throw new IOException("Catalogue inconnu");
        CargoCatalog.get(selected_mission_id);CargoCatalog.get(delivery_mission_id);
        if(repeat_version==0){
            if(!phase.equals("IDLE")&&!phase.equals("SETTLED"))throw new IOException("Terminer la livraison en 0.4.1 avant migration");
            completed_count=phase.equals("SETTLED")?1:0;last_receipt_id=completed_count==1?delivery:"";repeat_version=2;
        }
        if(repeat_version!=2||completed_count<0||completed_count>256||last_receipt_id==null
            ||(completed_count==0?!last_receipt_id.isEmpty():!uuid(last_receipt_id)))throw new IOException("Historique invalide");
        if(phase.equals("SETTLED")&&(completed_count==0||!delivery.equals(last_receipt_id)))throw new IOException("Recu final invalide");
        if(phase.equals("IDLE")&&(completed_count!=0||!delivery.isEmpty()))throw new IOException("Etat initial invalide");
    }
    public void save(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        Path temp=file.resolveSibling(file.getFileName()+".tmp");
        byte[] bytes=JSON.toJson(this).getBytes(StandardCharsets.UTF_8);
        try(var out=FileChannel.open(temp,CREATE,TRUNCATE_EXISTING,WRITE)) {ByteBuffer b=ByteBuffer.wrap(bytes);while(b.hasRemaining())out.write(b);out.force(true);}
        Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
    }
    public static boolean uuid(String s) {try{return s!=null && UUID.fromString(s).toString().equals(s);}catch(Exception e){return false;}}
}
