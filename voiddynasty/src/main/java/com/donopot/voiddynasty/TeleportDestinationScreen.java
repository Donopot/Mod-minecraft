package com.donopot.voiddynasty;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/**
 * Choix de la destination de téléportation d'un vaisseau :
 * coordonnées saisies, position du joueur, ou point visé (jusqu'à 200 blocs devant).
 * On revient au pad en fermant, pour enchaîner les actions.
 */
@Environment(EnvType.CLIENT)
public class TeleportDestinationScreen extends Screen {
    private static final int BG_W = 240, BG_H = 200;
    private static final int GOLD = 0xFFC89B67;
    private static final int GREY = 0xFF9a9aa5;
    private static final int DIM = 0xFF6a6a75;

    private final String ship;
    private final int startX, startZ;
    private final Screen parent;
    private int left, top;
    private EditBox xField, zField;

    public TeleportDestinationScreen(String ship, int startX, int startZ, Screen parent) {
        super(Component.literal("Destination"));
        this.ship = ship;
        this.startX = startX;
        this.startZ = startZ;
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.left = (this.width - BG_W) / 2;
        this.top = (this.height - BG_H) / 2;

        this.xField = new EditBox(this.font, left + 24, top + 60, 84, 18, Component.empty());
        this.xField.setMaxLength(9);
        this.xField.setValue(Integer.toString(this.startX));
        addRenderableWidget(this.xField);

        this.zField = new EditBox(this.font, left + 132, top + 60, 84, 18, Component.empty());
        this.zField.setMaxLength(9);
        this.zField.setValue(Integer.toString(this.startZ));
        addRenderableWidget(this.zField);

        addRenderableWidget(Button.builder(Component.literal("Ma position"), b -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                xField.setValue(Integer.toString(mc.player.getBlockX()));
                zField.setValue(Integer.toString(mc.player.getBlockZ()));
            }
        }).bounds(left + 24, top + 88, 90, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Point visé"), b -> {
            int[] p = visee();
            xField.setValue(Integer.toString(p[0]));
            zField.setValue(Integer.toString(p[1]));
        }).bounds(left + 126, top + 88, 90, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Téléporter le vaisseau"), b -> valider())
                .bounds(left + 24, top + 116, 192, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Retour"), b -> retourAuPad())
                .bounds(left + 65, top + 142, 110, 20).build());
    }

    /** Point visé : on avance depuis les yeux jusqu'à 200 blocs et on s'arrête au premier bloc solide. */
    private int[] visee() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return new int[]{this.startX, this.startZ};
        Vec3 oeil = mc.player.getEyePosition(1.0f);
        Vec3 dir = mc.player.getViewVector(1.0f);
        for (double d = 1.0; d <= 200.0; d += 0.5) {
            Vec3 p = oeil.add(dir.scale(d));
            if (!mc.level.getBlockState(BlockPos.containing(p)).isAir()) {
                return new int[]{(int) Math.floor(p.x), (int) Math.floor(p.z)};
            }
        }
        Vec3 p = oeil.add(dir.scale(200.0));
        return new int[]{(int) Math.floor(p.x), (int) Math.floor(p.z)};
    }

    private void valider() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int x = entier(this.xField.getValue(), this.startX);
        int z = entier(this.zField.getValue(), this.startZ);
        mc.player.connection.sendCommand("fleet teleport " + this.ship + " " + x + " " + z);
        retourAuPad();
    }

    private void retourAuPad() {
        Minecraft mc = Minecraft.getInstance();
        if (this.parent != null) mc.setScreenAndShow(this.parent);
        else super.onClose();
    }

    private static int entier(String s, int defaut) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaut;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.fill(RenderPipelines.GUI, 0, 0, this.width, this.height, 0xFF0a0a12);
        g.fill(RenderPipelines.GUI, left, top, left + BG_W, top + BG_H, 0xFF14141f);
        g.fill(RenderPipelines.GUI, left, top, left + BG_W, top + 1, GOLD);
        g.fill(RenderPipelines.GUI, left, top, left + 1, top + BG_H, GOLD);
        g.fill(RenderPipelines.GUI, left + BG_W - 1, top, left + BG_W, top + BG_H, GOLD);
        g.fill(RenderPipelines.GUI, left, top + BG_H - 1, left + BG_W, top + BG_H, GOLD);

        g.centeredText(this.font, "DESTINATION DU TÉLÉPORT", left + BG_W / 2, top + 14, GOLD);
        g.centeredText(this.font, this.ship, left + BG_W / 2, top + 28, GREY);
        g.text(this.font, "X", left + 26, top + 48, GREY);
        g.text(this.font, "Z", left + 134, top + 48, GREY);
        g.centeredText(this.font, "Saisis les coordonnées, ou prends-les", left + BG_W / 2, top + 176, DIM);
        g.centeredText(this.font, "de ta position ou de ton point de visée", left + BG_W / 2, top + 187, DIM);

        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
