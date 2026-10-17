package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.component.ModDataComponents;
import com.donopot.voiddynasty.weapon.WeaponDefinition;
import com.donopot.voiddynasty.weapon.WeaponItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * HUD des armes : munitions dans le chargeur + nom de la munition requise.
 * Affiche « 28 / 30 » surmontant le type de munition (ex. « Cartouche de bolter »).
 */
@Environment(EnvType.CLIENT)
public final class BolterHud {

    private BolterHud() {
    }

    public static void init() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("voiddynasty", "boltgun_ammo"),
                BolterHud::renderAmmoHud
        );
    }

    private static void renderAmmoHud(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof WeaponItem weapon)) return;

        WeaponDefinition def = weapon.definition();
        if (def.ammo() == null) return; // mêlée : pas de HUD de munitions

        int ammo = stack.getOrDefault(ModDataComponents.AMMO, 0);
        String count = ammo + " / " + def.magazineSize();
        String ammoName = def.ammo().getName(ItemStack.EMPTY).getString();

        int x = mc.getWindow().getGuiScaledWidth() / 2 + 8;
        int y = mc.getWindow().getGuiScaledHeight() - 48;

        g.text(mc.font, count, x, y, 0xFFFFFFFF);
        g.text(mc.font, ammoName, x, y + 10, 0xFFAAAAAA);
    }
}
