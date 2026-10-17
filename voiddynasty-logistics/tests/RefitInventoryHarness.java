package com.donopot.voiddynastybridge.refit.tests;
import com.donopot.voiddynastybridge.refit.*;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.core.component.*;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.*;
import java.util.List;
import net.minecraft.world.phys.*;

public final class RefitInventoryHarness {
    private static int count;
    private static void check(boolean test,String message){count++;if(!test)throw new AssertionError(message);}
    private static int total(SimpleContainer c){int out=0;for(int i=0;i<c.getContainerSize();i++)out+=c.getItem(i).getCount();return out;}
    public static void run(String pack) throws Exception {
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
        var shipTypes=FleetPadScreen.class.getDeclaredField("SHIPS");shipTypes.setAccessible(true);
        check(java.util.Arrays.equals((Object[])shipTypes.get(null), com.donopot.voiddynasty.ShipType.values()),
                "Pad exposes every ship type from the actual base mod without a fixed list");
        // No world/datapack is started. Bind explicit fixture defaults through the engine's public API.
        var defaults=DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build();
        for(var item:new Item[]{Items.IRON_INGOT,Items.GOLD_INGOT,Items.DIAMOND})item.builtInRegistryHolder().bindComponents(defaults);
        var meta=JsonParser.parseString(Files.readString(Path.of(pack,"pack.mcmeta"))).getAsJsonObject().get("pack");
        var section=PackMetadataSection.forPackType(PackType.CLIENT_RESOURCES).codec().parse(JsonOps.INSTANCE,meta).getOrThrow();
        check(section.description().getString().contains("VOID DYNASTY"),"Pack metadata accepted by the actual Minecraft codec");
        var storage=new SimpleContainer(81);
        for(int i=0;i<81;i++)storage.setItem(i,new ItemStack(Items.IRON_INGOT,i%64+1));
        int before=total(storage);var page=new PagedContainer(storage,false);
        check(page.getContainerSize()==54&&page.maxRow()==3,"81 physical slots retain four row offsets");
        page.row(3);check(page.getItem(0).getCount()==28&&page.getItem(53).getCount()==17,"Last page maps to original slots 27..80");
        var taken=page.removeItem(53,8);check(taken.getCount()==8&&storage.getItem(80).getCount()==9,"Removal affects selected physical slot");
        check(total(storage)+taken.getCount()==before&&storage.getItem(0).getCount()==1,"Conservation and no alias into first page");
        page.setItem(0,new ItemStack(Items.DIAMOND,4));check(storage.getItem(27).is(Items.DIAMOND),"Insertion affects selected physical slot");
        var slot=new PageSlot(page,0,8,18);check(slot.getContainerSlot()==0&&slot.getItem().is(Items.DIAMOND),"Packet slot stays zero while physical storage is offset");
        page.row(999);check(page.row()==3,"Clamp upper row");page.row(-4);check(page.row()==0,"Clamp negative row");
        var visible=new SimpleContainer(54);var client=new PagedContainer(visible,true);page.row(3);
        for(int i=0;i<4;i++)new PageWord(client,i).set((short)new PageWord(page,i).get());
        visible.setItem(0,new ItemStack(Items.GOLD_INGOT,7));
        check(client.row()==3&&client.maxRow()==3,"Server capacity and row reach the client");
        check(client.getItem(0).is(Items.GOLD_INGOT),"Client stays a 54-slot visible window instead of indexing a hidden page");
        check(visible.getContainerSize()==54,"No client storage expansion or alternate inventory");
        var large=new PagedContainer(new SimpleContainer(70000),false);
        for(int i=0;i<4;i++)client.word(i,(short)large.word(i));
        check(client.capacity()==70000,"Capacity survives signed 16-bit vanilla data packets");
        var partial=new PagedContainer(new SimpleContainer(7),false);
        check(partial.exists(6)&&!partial.exists(7)&&partial.getItem(53).isEmpty(),"Short final window rejects nonexistent slots");
        for(int[] size:new int[][]{{320,240},{426,240},{640,360},{854,480},{1920,1080}}){
            var layout=PadLayout.of(size[0],size[1]);check(layout.left()>=0&&layout.left()+layout.width()<=size[0],"Pad horizontal bounds");
            check(66+(layout.rows()-1)*22+20<layout.footer(),"Pad list does not overlap navigation");
        }
        check(PadLayout.name("Frégate_01")&&!PadLayout.name("bad\nfleet delete x")&&!PadLayout.name("a b")&&!PadLayout.name(""),"Command names reject whitespace and command injection");
        var origin=new Vec3(0,1,0);var wall=new Vec3(0,1,12);
        var near=new AABB(-.4,0,3,.4,2,4);var far=new AABB(-.4,0,8,.4,2,9);var behind=new AABB(-.4,0,13,.4,2,14);
        check(LaserRay.closest(origin,wall,List.of(far,near)).index()==1,"Laser chooses nearest target independent of entity iteration order");
        check(LaserRay.closest(origin,wall,List.of(behind)).index()==-1,"Laser cannot select an entity behind a solid wall");
        check(LaserRay.closest(origin,wall,List.of(new AABB(1,0,3,2,2,4))).index()==-1,"Off-axis entity is not damaged");
        check(LaserRay.closest(origin,wall,List.of(new AABB(-.4,0,12,.4,2,13))).index()==-1,"Block wins at the exact impact boundary");
        System.out.println("PASS "+count+" real-game inventory and pad layout assertions");
    }
}
