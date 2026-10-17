package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.menu.SuperChestMenu;
import com.donopot.voiddynasty.network.ScrollContainerPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Écran des super conteneurs : 6 rangées + inventaire joueur, avec défilement
 * vertical à la molette (comme l'inventaire créatif).
 */
@Environment(EnvType.CLIENT)
public class SuperChestScreen extends AbstractContainerScreen<SuperChestMenu> {

    public SuperChestScreen(SuperChestMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, 176, 222);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // fond simple (la parente dessine ensuite les slots + labels)
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + 17, 0xFF2b2b31);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.menu.getMaxScrollRow() > 0) {
            int delta = (int) Math.signum(verticalAmount);
            int newRow = Math.max(0, Math.min(this.menu.getMaxScrollRow(), this.menu.getScrollRow() - delta));
            if (newRow != this.menu.getScrollRow()) {
                this.menu.setScrollRow(newRow);
                ClientPlayNetworking.send(new ScrollContainerPayload(this.menu.containerId, newRow));
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
