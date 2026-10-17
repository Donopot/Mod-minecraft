package com.donopot.voiddynasty;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class ClientScreenOpener {
    private ClientScreenOpener() {}

    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        Identifier itemTex = Identifier.fromNamespaceAndPath("voiddynasty", "textures/item/control_pad.png");
        Identifier guiTex = Identifier.fromNamespaceAndPath("voiddynasty", "textures/gui/control_pad.png");
        boolean itemOk = !mc.getResourceManager().getResourceStack(itemTex).isEmpty();
        boolean guiOk = !mc.getResourceManager().getResourceStack(guiTex).isEmpty();
        VoidDynastyMod.LOGGER.info("[voiddynasty] diag textures: item={} gui={}", itemOk, guiOk);
        mc.setScreenAndShow(new ControlPadScreen());
    }
}
