package com.donopot.voiddynasty;

import com.donopot.voiddynasty.network.FleetListRequestPayload;
import com.donopot.voiddynasty.network.ShipInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pad de contrôle de la flotte : liste des vaisseaux du joueur + actions.
 *
 * Budget vertical (panneau de 320 px, tout doit tenir avec de la marge) :
 *    12 titre            23 compteur         38..56  fleches
 *    60..116 liste       119 indicateur       131/153/175 boutons d'action
 *   196 label           206 champ nom         226/244 grille des types
 *   264 type choisi     284..302 fermer
 *
 * - Les types viennent de ShipType : tout vaisseau ajouté à l'énumération apparaît
 *   automatiquement dans la grille (6 emplacements : 3 colonnes x 2 lignes).
 * - La liste utilise une fenêtre glissante (FleetListWindow) : la sélection reste
 *   visible quel que soit le nombre de vaisseaux (jusqu'à 10).
 */
@Environment(EnvType.CLIENT)
public class ControlPadScreen extends Screen {
    private static final int BG_W = 256, BG_H = 320;
    private static final int GOLD = 0xFFC89B67;
    private static final int GREY = 0xFF9a9aa5;
    private static final int DIM = 0xFF6a6a75;
    /** Marqueur de version : sert a verifier quel jar est reellement charge. */
    private static final String MARQUE = "pad v6 · grille";

    private static final int LIGNES = 5;
    private static final int LISTE_Y = 60, LISTE_DY = 12;
    private static final int INDIC_Y = 119;
    private static final int ACTION_Y = 131, ACTION_DY = 22, ACTION_W = 100, ACTION_H = 18;
    private static final int NOM_Y = 196, CHAMP_Y = 206;
    private static final int GRILLE_Y = 226, GRILLE_DY = 18, GRILLE_W = 66, GRILLE_H = 16;
    private static final int MAX_GRILLE = 6;
    private static final int CHOISI_Y = 264, FERMER_Y = 284;

    private static final ShipType[] TYPES = ShipType.values();
    private int left, top;
    private List<ShipInfo> ships = new ArrayList<>();
    private int selected = 0;
    private int addTypeIndex = 0;
    private EditBox nameField;
    private static ControlPadScreen instance;

    public static ControlPadScreen getInstance() {
        return instance;
    }

    public ControlPadScreen() {
        super(Component.literal("Flotte"));
        instance = this;
    }

    /** Nom court pour tenir dans un bouton de 66 px ("Frégate impériale" -> "Frégate"). */
    private static String court(ShipType t) {
        return courtTexte(t.displayName);
    }

    private static String courtTexte(String n) {
        int i = n.indexOf(' ');
        String s = i > 0 ? n.substring(0, i) : n;
        return s.length() > 10 ? s.substring(0, 10) : s;
    }

    private static String coupe(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    @Override
    protected void init() {
        this.left = (this.width - BG_W) / 2;
        this.top = (this.height - BG_H) / 2;

        addRenderableWidget(Button.builder(Component.literal("◀"), b -> sel(-1)).bounds(left + 24, top + 38, 20, 18).build());
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> sel(+1)).bounds(left + 212, top + 38, 20, 18).build());

        addRenderableWidget(Button.builder(Component.literal("Déployer"), b -> action("deploy"))
                .bounds(left + 24, top + ACTION_Y, ACTION_W, ACTION_H).build());
        addRenderableWidget(Button.builder(Component.literal("Ranger"), b -> action("stow"))
                .bounds(left + 132, top + ACTION_Y, ACTION_W, ACTION_H).build());
        addRenderableWidget(Button.builder(Component.literal("Téléporter…"), b -> action("teleport"))
                .bounds(left + 24, top + ACTION_Y + ACTION_DY, ACTION_W, ACTION_H).build());
        addRenderableWidget(Button.builder(Component.literal("Renommer"), b -> action("rename"))
                .bounds(left + 132, top + ACTION_Y + ACTION_DY, ACTION_W, ACTION_H).build());
        addRenderableWidget(Button.builder(Component.literal("Supprimer"), b -> action("delete"))
                .bounds(left + 24, top + ACTION_Y + 2 * ACTION_DY, ACTION_W, ACTION_H).build());
        addRenderableWidget(Button.builder(Component.literal("Ajouter"), b -> action("add"))
                .bounds(left + 132, top + ACTION_Y + 2 * ACTION_DY, ACTION_W, ACTION_H).build());

        this.nameField = new EditBox(this.font, left + 24, top + CHAMP_Y, 208, 16, Component.empty());
        this.nameField.setMaxLength(32);
        addRenderableWidget(this.nameField);

        int n = Math.min(TYPES.length, MAX_GRILLE);
        for (int i = 0; i < n; i++) {
            final int ci = i;
            int col = i % 3, row = i / 3;
            addRenderableWidget(Button.builder(Component.literal(court(TYPES[i])), b -> addTypeIndex = ci)
                    .bounds(left + 24 + col * 72, top + GRILLE_Y + row * GRILLE_DY, GRILLE_W, GRILLE_H).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Fermer"), b -> onClose())
                .bounds(left + 73, top + FERMER_Y, 110, 18).build());

        ClientPlayNetworking.send(new FleetListRequestPayload());
    }

    public void updateShips(List<ShipInfo> ships) {
        this.ships = ships;
        if (ships.isEmpty()) selected = -1;
        else if (selected < 0 || selected >= ships.size()) selected = 0;
    }

    private void sel(int d) {
        if (ships.isEmpty()) return;
        selected = (selected + d + ships.size()) % ships.size();
    }

    private void action(String op) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        String name = nameField.getValue().trim().replace(' ', '_');
        String cmd = "";
        if (op.equals("add")) {
            cmd = "fleet add " + TYPES[addTypeIndex].name().toLowerCase() + " " + name;
        } else if (ships.isEmpty() || selected < 0 || selected >= ships.size()) {
            return;
        } else {
            ShipInfo s = ships.get(selected);
            switch (op) {
                case "deploy" -> cmd = "fleet deploy " + s.name();
                case "stow" -> cmd = "fleet stow " + s.name();
                case "teleport" -> {
                    mc.setScreenAndShow(new TeleportDestinationScreen(s.name(), mc.player.getBlockX(), mc.player.getBlockZ(), this));
                    return;
                }
                case "rename" -> cmd = "fleet rename " + s.name() + " " + name;
                case "delete" -> cmd = "fleet delete " + s.name();
                default -> { }
            }
        }
        if (!cmd.isEmpty()) mc.player.connection.sendCommand(cmd);
        this.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.fill(RenderPipelines.GUI, 0, 0, this.width, this.height, 0xFF0a0a12);
        g.fill(RenderPipelines.GUI, left, top, left + BG_W, top + BG_H, 0xFF14141f);
        g.fill(RenderPipelines.GUI, left, top, left + BG_W, top + 1, GOLD);
        g.fill(RenderPipelines.GUI, left, top, left + 1, top + BG_H, GOLD);
        g.fill(RenderPipelines.GUI, left + BG_W - 1, top, left + BG_W, top + BG_H, GOLD);
        g.fill(RenderPipelines.GUI, left, top + BG_H - 1, left + BG_W, top + BG_H, GOLD);

        g.centeredText(this.font, "FLOTTE", left + BG_W / 2, top + 12, GOLD);
        g.centeredText(this.font, ships.size() + "/" + FleetManager.MAX_SHIPS_PER_PLAYER + " vaisseaux · "
                + TYPES.length + " types · " + MARQUE, left + BG_W / 2, top + 23, GREY);

        if (ships.isEmpty()) {
            g.centeredText(this.font, "Aucun vaisseau", left + BG_W / 2, top + 64, GREY);
            g.centeredText(this.font, "Nom + type → Ajouter", left + BG_W / 2, top + 78, DIM);
        } else {
            int debut = FleetListWindow.debut(selected, ships.size(), LIGNES);
            int nb = FleetListWindow.compte(ships.size(), LIGNES);
            for (int j = 0; j < nb; j++) {
                int i = debut + j;
                ShipInfo s = ships.get(i);
                String etat = s.deployed() ? "@" + s.x() + "," + s.y() + "," + s.z() : "— rangé";
                String line = (i == selected ? "▶ " : "  ") + s.name() + " (" + courtTexte(s.typeDisplay()) + ") " + etat;
                g.text(this.font, coupe(line, 37), left + 24, top + LISTE_Y + j * LISTE_DY,
                        i == selected ? GOLD : GREY);
            }
            g.centeredText(this.font, FleetListWindow.indicateur(selected, ships.size(), LIGNES),
                    left + BG_W / 2, top + INDIC_Y, DIM);
        }

        g.text(this.font, "Nom (ajout / renommage)", left + 24, top + NOM_Y, GREY);

        // encadre en or le type selectionne (memes constantes que la grille)
        int sel = Math.min(addTypeIndex, MAX_GRILLE - 1);
        int bx = left + 24 + (sel % 3) * 72, by = top + GRILLE_Y + (sel / 3) * GRILLE_DY;
        g.fill(RenderPipelines.GUI, bx - 1, by - 1, bx + GRILLE_W + 1, by, GOLD);
        g.fill(RenderPipelines.GUI, bx - 1, by + GRILLE_H, bx + GRILLE_W + 1, by + GRILLE_H + 1, GOLD);
        g.fill(RenderPipelines.GUI, bx - 1, by - 1, bx, by + GRILLE_H + 1, GOLD);
        g.fill(RenderPipelines.GUI, bx + GRILLE_W, by - 1, bx + GRILLE_W + 1, by + GRILLE_H + 1, GOLD);

        g.text(this.font, coupe("Type choisi : " + TYPES[addTypeIndex].displayName
                + " (" + TYPES[addTypeIndex].name().toLowerCase() + ")", 38), left + 24, top + CHOISI_Y, GREY);

        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        instance = null;
        super.onClose();
    }
}
