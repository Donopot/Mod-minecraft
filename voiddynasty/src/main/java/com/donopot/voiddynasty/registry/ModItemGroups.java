package com.donopot.voiddynasty.registry;

import com.donopot.voiddynasty.VoidDynastyMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Onglet créatif « Void Dynasty » regroupant tout l'arsenal du mod.
 */
public final class ModItemGroups {

    public static final CreativeModeTab MAIN = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("voiddynasty", "main"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.voiddynasty"))
                    .icon(() -> new ItemStack(ModItems.BOLTGUN))
                    .displayItems((parameters, output) -> {
                        // Armes à distance
                        output.accept(ModItems.BOLTGUN);
                        output.accept(ModItems.BOLT_PISTOL);
                        output.accept(ModItems.HEAVY_BOLTER);
                        output.accept(ModItems.LAS_GUN);
                        output.accept(ModItems.LAZ_PISTOL);
                        output.accept(ModItems.PLASMA_PISTOL);
                        output.accept(ModItems.PLASMA_INCINERATOR);
                        output.accept(ModItems.STALKER_BOLT_RIFLE);
                        output.accept(ModItems.SHOTGUN);
                        output.accept(ModItems.FLAMETHROWER);
                        output.accept(ModItems.MELTA_GUN);
                        output.accept(ModItems.HUNTER_KILLER_MISSILE);
                        // Mêlée
                        output.accept(ModItems.CHAINSWORD);
                        output.accept(ModItems.POWERSWORD);
                        output.accept(ModItems.POWER_HAMMER);
                        // Outils (foreuse laser + servo-crâne)
                        output.accept(ModItems.LASER_DRILL);
                        output.accept(ModItems.SERVO_SKULL);
                        // Armure Space Marine
                        output.accept(ModArmor.HELMET);
                        output.accept(ModArmor.CHESTPLATE);
                        output.accept(ModArmor.LEGGINGS);
                        output.accept(ModArmor.BOOTS);
                        // Armure Terminator (élite)
                        output.accept(ModArmor.TERMINATOR_HELMET);
                        output.accept(ModArmor.TERMINATOR_CHESTPLATE);
                        output.accept(ModArmor.TERMINATOR_LEGGINGS);
                        output.accept(ModArmor.TERMINATOR_BOOTS);
                        // Spawn eggs
                        output.accept(ModItems.CARNIFEX_SPAWN_EGG);
                        output.accept(ModItems.CARNIFEX_LEVIATHAN_SPAWN_EGG);
                        output.accept(ModItems.LICTOR_SPAWN_EGG);
                        output.accept(ModItems.LEMAN_RUSS_SPAWN_EGG);
                        output.accept(ModItems.NORN_EMISSARY_SPAWN_EGG);
                        output.accept(ModItems.GREAT_UNCLEAN_ONE_SPAWN_EGG);
                        output.accept(ModItems.LORD_OF_CHANGE_SPAWN_EGG);
                        output.accept(ModItems.KEEPER_OF_SECRETS_SPAWN_EGG);
                        output.accept(ModItems.KHORNE_JUGGERNAUT_SPAWN_EGG);
                        output.accept(ModItems.LARGE_KHORNE_DAEMON_SPAWN_EGG);
                        output.accept(ModItems.PLAGUETOAD_SPAWN_EGG);
                        output.accept(ModItems.GHAZGHKULL_SPAWN_EGG);
                        output.accept(ModItems.TYRANID_WARRIOR_SPAWN_EGG);
            output.accept(ModItems.CULTIST_1_SPAWN_EGG);
            output.accept(ModItems.CULTIST_2_SPAWN_EGG);
            output.accept(ModItems.CULTIST_3_SPAWN_EGG);
            output.accept(ModItems.CULTIST_4_SPAWN_EGG);
            output.accept(ModItems.CULTIST_BOSS_SPAWN_EGG);
            output.accept(ModItems.CHAOS_MARINE_1_SPAWN_EGG);
            output.accept(ModItems.CHAOS_MARINE_2_SPAWN_EGG);
            output.accept(ModItems.CHAOS_MARINE_3_SPAWN_EGG);
            output.accept(ModItems.CHAOS_CAPTAIN_SPAWN_EGG);
            output.accept(ModItems.CHAOS_RAPTOR_SPAWN_EGG);
            output.accept(ModItems.SERVITOR_1_SPAWN_EGG);
            output.accept(ModItems.SERVITOR_2_SPAWN_EGG);
            output.accept(ModItems.SERVITOR_3_SPAWN_EGG);
            output.accept(ModItems.CHAOS_DREADNOUGHT_SPAWN_EGG);
            output.accept(ModItems.CHAOS_HELLBRUTE_SPAWN_EGG);
            output.accept(ModItems.CHAOS_FORGEFIEND_SPAWN_EGG);
            output.accept(ModItems.DAEMON_PRINCE_SPAWN_EGG);
            output.accept(ModItems.CHAOS_HELLDRAKE_SPAWN_EGG);
                        // Munitions
                        output.accept(ModItems.BOLT_SHELL);
                        output.accept(ModItems.HEAVY_BOLT_SHELL);
                        output.accept(ModItems.STALKER_BOLT_ROUND);
                        output.accept(ModItems.SHOTGUN_SHELL);
                        output.accept(ModItems.LAS_CELL);
                        output.accept(ModItems.PLASMA_CELL);
                        output.accept(ModItems.MELTA_CELL);
                        output.accept(ModItems.PROMETHIUM);
                        output.accept(ModItems.MISSILE_AMMO);
                        // Matériaux
                        output.accept(ModItems.ADAMANTIUM_PLATE);
                        output.accept(ModItems.WIRES);
                        output.accept(ModItems.WAX_SEAL);
                        output.accept(ModItems.PLASMA_COIL);
                        output.accept(ModItems.WARHEAD);
                        output.accept(ModItems.FOCUS_LENS);
                        // Céramite (blindage) + matériaux de base
                        output.accept(ModItems.TITANIUM_INGOT);
                        output.accept(ModItems.SILICON_CARBIDE);
                        output.accept(ModItems.CERAMITE_INGOT);
                        output.accept(ModItems.BLUE_CERAMITE_INGOT);
                        output.accept(ModItems.RED_CERAMITE_INGOT);
                        output.accept(ModItems.BLACK_CERAMITE_INGOT);
                        output.accept(ModItems.GREEN_CERAMITE_INGOT);
                        output.accept(ModItems.DARK_GREEN_CERAMITE_INGOT);
                        output.accept(ModItems.PINK_CERAMITE_INGOT);
                        output.accept(ModItems.WHITE_CERAMITE_INGOT);
                        output.accept(ModItems.YELLOW_CERAMITE_INGOT);
                        output.accept(ModItems.LIGHT_BLUE_CERAMITE_INGOT);
                        // Blocs décoratifs (props)
                        output.accept(ModBlocks.BARREL_ITEM);
                        output.accept(ModBlocks.AMMO_CRATE_ITEM);
                        output.accept(ModBlocks.CANNON_SHELL_ITEM);
                        output.accept(ModBlocks.SANDBAGS_ITEM);
                        output.accept(ModBlocks.GOTHIC_WALL_ITEM);
                        // Blocs céramite
                        output.accept(ModBlocks.CERAMITE_ORE_ITEM);
                        output.accept(ModBlocks.DEEPSLATE_CERAMITE_ORE_ITEM);
                        output.accept(ModBlocks.CERAMITE_BLOCK_ITEM);
                        output.accept(ModBlocks.BLUE_CERAMITE_BLOCK_ITEM);
                        output.accept(ModBlocks.RED_CERAMITE_BLOCK_ITEM);
                        output.accept(ModBlocks.BLACK_CERAMITE_BLOCK_ITEM);
                        output.accept(ModBlocks.GREEN_CERAMITE_BLOCK_ITEM);
                        output.accept(ModBlocks.DARK_GREEN_CERAMITE_BLOCK_ITEM);
                        output.accept(ModBlocks.PINK_CERAMITE_BLOCK_ITEM);
                        output.accept(ModBlocks.WHITE_CERAMITE_BLOCK_ITEM);
                        output.accept(ModBlocks.YELLOW_CERAMITE_BLOCK_ITEM);
                        output.accept(ModBlocks.LIGHT_BLUE_CERAMITE_BLOCK_ITEM);
                        // Machine de récupération
                        output.accept(ModBlocks.MINING_RIG_ITEM);
                        // Machines de raffinage Prométhium
                        output.accept(ModBlocks.PROMETHIUM_FURNACE_ITEM);
                        output.accept(ModBlocks.PROMETHIUM_BLAST_FURNACE_ITEM);
                        output.accept(ModBlocks.PROMETHIUM_SMOKER_ITEM);
                        output.accept(ModBlocks.PROMETHIUM_BREWING_STAND_ITEM);
                        // Super conteneurs
                        output.accept(ModBlocks.SUPER_CHEST_ITEM);
                        output.accept(ModBlocks.SUPER_CONTAINER_ITEM);
            output.accept(ModBlocks.CRATE_ITEM);
            output.accept(ModBlocks.CRATE_GREEN_ITEM);
            output.accept(ModBlocks.CRATE_RED_ITEM);
            output.accept(ModBlocks.CRATE_SHUT_ITEM);
            output.accept(ModBlocks.GOTHIC_PANEL_ITEM);
            output.accept(ModBlocks.CORPSE_1_ITEM);
            output.accept(ModBlocks.CORPSE_2_ITEM);
            output.accept(ModBlocks.CORPSE_3_ITEM);
            output.accept(ModBlocks.CORPSE_4_ITEM);
            output.accept(ModBlocks.CORPSE_5_ITEM);
            output.accept(ModBlocks.CORPSE_6_ITEM);
            output.accept(ModBlocks.BLOOD_STAIN_ITEM);
            output.accept(ModBlocks.BLOOD_ARROW_ITEM);
            output.accept(ModBlocks.BLAST_MARK_ITEM);
            output.accept(ModBlocks.BLOOD_GOD_ITEM);
            output.accept(ModBlocks.SPENT_SHELL_ITEM);
            output.accept(ModBlocks.BATTERY_PACK_ITEM);
            output.accept(ModBlocks.WAX_SEAL_PROP_ITEM);
            output.accept(ModBlocks.CORPSE_STARCH_ITEM);
            output.accept(ModBlocks.LAZ_CANNON_ITEM);
            output.accept(ModBlocks.MULTIMELTA_TURRET_ITEM);
                        // Items historiques
                        output.accept(VoidDynastyMod.CONTROL_PAD);
                        output.accept(VoidDynastyMod.CANON);
                        output.accept(VoidDynastyMod.ENERGY_WAVE);
                        output.accept(VoidDynastyMod.GELLAR_SHARD);
                        output.accept(VoidDynastyMod.VOID_DYNASTY_ITEM);
                        output.accept(VoidDynastyMod.VOID_DYNASTY_BLOCK_ITEM);
                    })
                    .build()
    );

    private ModItemGroups() {
    }

    /** Le simple chargement de cette classe enregistre l'onglet. */
    public static void init() {
    }
}
