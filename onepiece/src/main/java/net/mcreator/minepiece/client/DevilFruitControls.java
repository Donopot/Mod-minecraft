package net.mcreator.minepiece.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

/** Stable binding IDs preserve existing options.txt choices. Defaults use SDL scancodes. */
public final class DevilFruitControls {
    public static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(Identifier.fromNamespaceAndPath("minepiece","controls"));
    public static final KeyMapping SKILL=key("skill",InputConstants.KEY_G);
    public static final KeyMapping TRANSFORM=key("transform",InputConstants.KEY_V);
    public static final KeyMapping CYCLE=key("cycle",InputConstants.KEY_B);
    public static final KeyMapping HUD=key("hud",InputConstants.KEY_H);
    public static boolean hudVisible=true;
    private static final TagKey<net.minecraft.world.item.Item> RANGED=TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("voiddynasty","ranged_weapons"));
    private DevilFruitControls() {}
    private static KeyMapping key(String name,int code) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.minepiece."+name,InputConstants.Type.KEYBOARD,code,CATEGORY));
    }
    public static void register() { loadHud(); }
    public static boolean reloadHasPriority(Minecraft client) {
        KeyMapping reload=KeyMapping.get("key.voiddynasty.reload");
        return reload!=null && !SKILL.isUnbound() && SKILL.same(reload) && client.player!=null
                && client.player.getMainHandItem().is(RANGED);
    }
    public static void toggleHud() {
        hudVisible=!hudVisible;
        try {java.nio.file.Files.writeString(file(),"{\"visible\":"+hudVisible+"}\n");}
        catch(java.io.IOException error) {org.slf4j.LoggerFactory.getLogger("MinePieceDevilFruit").warn("HUD preference could not be saved",error);}
    }
    private static java.nio.file.Path file() {return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("opworldlike-hud.json");}
    private static void loadHud() {
        try {if(java.nio.file.Files.isRegularFile(file())) hudVisible=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(file())).getAsJsonObject().get("visible").getAsBoolean();}
        catch(RuntimeException|java.io.IOException error) {hudVisible=true;org.slf4j.LoggerFactory.getLogger("MinePieceDevilFruit").warn("Invalid HUD preference; using visible HUD",error);}
    }
    public static void drain() {for(KeyMapping key:new KeyMapping[]{SKILL,TRANSFORM,CYCLE,HUD}) while(key.consumeClick()) {}}
}
