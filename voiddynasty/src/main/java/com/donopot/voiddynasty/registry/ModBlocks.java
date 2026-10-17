package com.donopot.voiddynasty.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import com.donopot.voiddynasty.machine.MiningRigBlock;
import com.donopot.voiddynasty.machine.PromethiumBlastFurnaceBlock;
import com.donopot.voiddynasty.machine.PromethiumBrewingStandBlock;
import com.donopot.voiddynasty.machine.PromethiumFurnaceBlock;
import com.donopot.voiddynasty.machine.PromethiumSmokerBlock;
import com.donopot.voiddynasty.machine.SuperChestBlock;
import com.donopot.voiddynasty.machine.SuperContainerBlock;

import java.util.function.Function;

/**
 * Blocs décoratifs (props) du mod, convertis depuis le pack Astartes.
 */
public final class ModBlocks {

    public static final String NAMESPACE = "voiddynasty";

    public static final Block BARREL = block("barrel", SoundType.METAL);
    public static final Item BARREL_ITEM = blockItem("barrel", BARREL);

    public static final Block AMMO_CRATE = block("ammo_crate", SoundType.METAL);
    public static final Item AMMO_CRATE_ITEM = blockItem("ammo_crate", AMMO_CRATE);

    public static final Block CANNON_SHELL = block("cannon_shell", SoundType.METAL);
    // Munition du char : sa classe propre lui donne le comportement « tirer quand on est assis
    // dans le Leman Russ ». Sans cela, ce serait un bloc comme un autre.
    public static final Item CANNON_SHELL_ITEM = shellItem("cannon_shell", CANNON_SHELL);

    public static final Block SANDBAGS = block("sandbags", SoundType.WOOL);
    public static final Item SANDBAGS_ITEM = blockItem("sandbags", SANDBAGS);

    public static final Block GOTHIC_WALL = block("gothic_wall", SoundType.STONE);
    public static final Item GOTHIC_WALL_ITEM = blockItem("gothic_wall", GOTHIC_WALL);

    // ------------------------------------------------------------------
    // Props décoratifs (phase C) — convertis depuis le pack Astartes par
    // tools/convert_astartes_props.py. Modèles multi-textures : les BLOCS les supportent
    // nativement, contrairement aux objets (pas de cuisson en atlas ici).
    // ------------------------------------------------------------------
    public static final Block CRATE = block("crate", SoundType.METAL);
    public static final Item CRATE_ITEM = blockItem("crate", CRATE);

    public static final Block CRATE_GREEN = block("crate_green", SoundType.METAL);
    public static final Item CRATE_GREEN_ITEM = blockItem("crate_green", CRATE_GREEN);

    public static final Block CRATE_RED = block("crate_red", SoundType.METAL);
    public static final Item CRATE_RED_ITEM = blockItem("crate_red", CRATE_RED);

    public static final Block CRATE_SHUT = block("crate_shut", SoundType.METAL);
    public static final Item CRATE_SHUT_ITEM = blockItem("crate_shut", CRATE_SHUT);

    public static final Block GOTHIC_PANEL = block("gothic_panel", SoundType.STONE);
    public static final Item GOTHIC_PANEL_ITEM = blockItem("gothic_panel", GOTHIC_PANEL);

    public static final Block CORPSE_1 = block("corpse_1", SoundType.WOOL);
    public static final Item CORPSE_1_ITEM = blockItem("corpse_1", CORPSE_1);

    public static final Block CORPSE_2 = block("corpse_2", SoundType.WOOL);
    public static final Item CORPSE_2_ITEM = blockItem("corpse_2", CORPSE_2);

    public static final Block CORPSE_3 = block("corpse_3", SoundType.WOOL);
    public static final Item CORPSE_3_ITEM = blockItem("corpse_3", CORPSE_3);

    public static final Block CORPSE_4 = block("corpse_4", SoundType.WOOL);
    public static final Item CORPSE_4_ITEM = blockItem("corpse_4", CORPSE_4);

    public static final Block CORPSE_5 = block("corpse_5", SoundType.WOOL);
    public static final Item CORPSE_5_ITEM = blockItem("corpse_5", CORPSE_5);

    public static final Block CORPSE_6 = block("corpse_6", SoundType.WOOL);
    public static final Item CORPSE_6_ITEM = blockItem("corpse_6", CORPSE_6);

    public static final Block BLOOD_STAIN = block("blood_stain", SoundType.WOOL);
    public static final Item BLOOD_STAIN_ITEM = blockItem("blood_stain", BLOOD_STAIN);

    public static final Block BLOOD_ARROW = block("blood_arrow", SoundType.WOOL);
    public static final Item BLOOD_ARROW_ITEM = blockItem("blood_arrow", BLOOD_ARROW);

    public static final Block BLAST_MARK = block("blast_mark", SoundType.WOOL);
    public static final Item BLAST_MARK_ITEM = blockItem("blast_mark", BLAST_MARK);

    public static final Block BLOOD_GOD = block("blood_god", SoundType.WOOL);
    public static final Item BLOOD_GOD_ITEM = blockItem("blood_god", BLOOD_GOD);

    public static final Block SPENT_SHELL = block("spent_shell", SoundType.METAL);
    public static final Item SPENT_SHELL_ITEM = blockItem("spent_shell", SPENT_SHELL);

    public static final Block BATTERY_PACK = block("battery_pack", SoundType.METAL);
    public static final Item BATTERY_PACK_ITEM = blockItem("battery_pack", BATTERY_PACK);

    public static final Block WAX_SEAL_PROP = block("wax_seal_prop", SoundType.WOOL);
    public static final Item WAX_SEAL_PROP_ITEM = blockItem("wax_seal_prop", WAX_SEAL_PROP);

    public static final Block CORPSE_STARCH = block("corpse_starch", SoundType.WOOL);
    public static final Item CORPSE_STARCH_ITEM = blockItem("corpse_starch", CORPSE_STARCH);

    public static final Block LAZ_CANNON = block("laz_cannon", SoundType.METAL);
    public static final Item LAZ_CANNON_ITEM = blockItem("laz_cannon", LAZ_CANNON);

    public static final Block MULTIMELTA_TURRET = block("multimelta_turret", SoundType.METAL);
    public static final Item MULTIMELTA_TURRET_ITEM = blockItem("multimelta_turret", MULTIMELTA_TURRET);

    // Céramite (minerai + blocs de lingots)
    public static final Block CERAMITE_ORE = metalBlock("ceramite_ore", SoundType.STONE, 3.0f);
    public static final Item CERAMITE_ORE_ITEM = blockItem("ceramite_ore", CERAMITE_ORE);
    // Variante « ardoise » (decision de Donopot 5b) : le filon genere dans les DEUX materiaux
    // (stone_ore_replaceables et deepslate_ore_replaceables) mais posait toujours le meme bloc — en
    // profondeur on trouvait donc un minerai a texture de pierre. Proprietes des minerais d'ardoise
    // du jeu : durete 4,5 et resistance 3,0.
    public static final Block DEEPSLATE_CERAMITE_ORE = deepslateOre("deepslate_ceramite_ore");
    public static final Item DEEPSLATE_CERAMITE_ORE_ITEM =
            blockItem("deepslate_ceramite_ore", DEEPSLATE_CERAMITE_ORE);
    public static final Block CERAMITE_BLOCK = metalBlock("ceramite_block", SoundType.METAL, 3.0f);
    public static final Item CERAMITE_BLOCK_ITEM = blockItem("ceramite_block", CERAMITE_BLOCK);
    public static final Block BLUE_CERAMITE_BLOCK = metalBlock("blue_ceramite_block", SoundType.METAL, 3.0f);
    public static final Item BLUE_CERAMITE_BLOCK_ITEM = blockItem("blue_ceramite_block", BLUE_CERAMITE_BLOCK);
    public static final Block RED_CERAMITE_BLOCK = metalBlock("red_ceramite_block", SoundType.METAL, 3.0f);
    public static final Item RED_CERAMITE_BLOCK_ITEM = blockItem("red_ceramite_block", RED_CERAMITE_BLOCK);
    public static final Block BLACK_CERAMITE_BLOCK = metalBlock("black_ceramite_block", SoundType.METAL, 3.0f);
    public static final Item BLACK_CERAMITE_BLOCK_ITEM = blockItem("black_ceramite_block", BLACK_CERAMITE_BLOCK);
    public static final Block GREEN_CERAMITE_BLOCK = metalBlock("green_ceramite_block", SoundType.METAL, 3.0f);
    public static final Item GREEN_CERAMITE_BLOCK_ITEM = blockItem("green_ceramite_block", GREEN_CERAMITE_BLOCK);
    public static final Block DARK_GREEN_CERAMITE_BLOCK = metalBlock("dark_green_ceramite_block", SoundType.METAL, 3.0f);
    public static final Item DARK_GREEN_CERAMITE_BLOCK_ITEM = blockItem("dark_green_ceramite_block", DARK_GREEN_CERAMITE_BLOCK);
    public static final Block PINK_CERAMITE_BLOCK = metalBlock("pink_ceramite_block", SoundType.METAL, 3.0f);
    public static final Item PINK_CERAMITE_BLOCK_ITEM = blockItem("pink_ceramite_block", PINK_CERAMITE_BLOCK);
    public static final Block WHITE_CERAMITE_BLOCK = metalBlock("white_ceramite_block", SoundType.METAL, 3.0f);
    public static final Item WHITE_CERAMITE_BLOCK_ITEM = blockItem("white_ceramite_block", WHITE_CERAMITE_BLOCK);
    public static final Block YELLOW_CERAMITE_BLOCK = metalBlock("yellow_ceramite_block", SoundType.METAL, 3.0f);
    public static final Item YELLOW_CERAMITE_BLOCK_ITEM = blockItem("yellow_ceramite_block", YELLOW_CERAMITE_BLOCK);
    public static final Block LIGHT_BLUE_CERAMITE_BLOCK = metalBlock("light_blue_ceramite_block", SoundType.METAL, 3.0f);
    public static final Item LIGHT_BLUE_CERAMITE_BLOCK_ITEM = blockItem("light_blue_ceramite_block", LIGHT_BLUE_CERAMITE_BLOCK);

    // Machine : Extracteur Minier (récupération automatique de minerai)
    public static final Block MINING_RIG = machine("mining_rig");
    public static final Item MINING_RIG_ITEM = blockItem("mining_rig", MINING_RIG);

    // Machines de raffinage Prométhium (fours + alambic)
    public static final Block PROMETHIUM_FURNACE = promethiumFurnace("promethium_furnace", PromethiumFurnaceBlock::new);
    public static final Item PROMETHIUM_FURNACE_ITEM = blockItem("promethium_furnace", PROMETHIUM_FURNACE);
    public static final Block PROMETHIUM_BLAST_FURNACE = promethiumFurnace("promethium_blast_furnace", PromethiumBlastFurnaceBlock::new);
    public static final Item PROMETHIUM_BLAST_FURNACE_ITEM = blockItem("promethium_blast_furnace", PROMETHIUM_BLAST_FURNACE);
    public static final Block PROMETHIUM_SMOKER = promethiumFurnace("promethium_smoker", PromethiumSmokerBlock::new);
    public static final Item PROMETHIUM_SMOKER_ITEM = blockItem("promethium_smoker", PROMETHIUM_SMOKER);
    public static final Block PROMETHIUM_BREWING_STAND = promethiumBrewing("promethium_brewing_stand");
    public static final Item PROMETHIUM_BREWING_STAND_ITEM = blockItem("promethium_brewing_stand", PROMETHIUM_BREWING_STAND);

    // Super conteneurs (coffre 2x2 + conteneur 6x2)
    public static final Block SUPER_CHEST = superContainer("super_chest", SuperChestBlock::new);
    public static final Item SUPER_CHEST_ITEM = blockItem("super_chest", SUPER_CHEST);
    public static final Block SUPER_CONTAINER = superContainer("super_container", SuperContainerBlock::new);
    public static final Item SUPER_CONTAINER_ITEM = blockItem("super_container", SUPER_CONTAINER);

    /** Super conteneur (coffre 2x2 / conteneur 6x2) : grand inventaire + fusion. */
    private static Block superContainer(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                factory.apply(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .strength(2.5f)
                        .sound(SoundType.METAL))
        );
    }

    /** Machine (BlockEntity) : extracteur minier. */
    private static Block machine(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                new MiningRigBlock(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .strength(4.0f)
                        .sound(SoundType.METAL))
        );
    }

    /** Machine de raffinage Prométhium (four / haut fourneau / fumoir) : carburant prométhium + vitesse x8. */
    private static Block promethiumFurnace(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                factory.apply(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .requiresCorrectToolForDrops()
                        .strength(3.5f)
                        .lightLevel(state -> state.getValue(FurnaceBlock.LIT) ? 13 : 0)
                        .sound(SoundType.STONE))
        );
    }

    /** Alambic Prométhium : infusion x8, carburant prométhium. */
    private static Block promethiumBrewing(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                new PromethiumBrewingStandBlock(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .requiresCorrectToolForDrops()
                        .strength(0.5f)
                        .sound(SoundType.STONE))
        );
    }

    private ModBlocks() {
    }

    private static Block block(String name, SoundType sound) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                new Block(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .strength(1.5f)
                        .sound(sound))
        );
    }

    /** Bloc « métallique/minéral » (céramite) : dureté paramétrable + nécessite une pioche. */
    private static Block metalBlock(String name, SoundType sound, float strength) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                new Block(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .strength(strength)
                        .requiresCorrectToolForDrops()
                        .sound(sound))
        );
    }

    /** Minerai en ardoise : durete 4,5 et resistance 3,0, comme les minerais d'ardoise du jeu. */
    private static Block deepslateOre(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                new Block(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .strength(4.5f, 3.0f)          // durete, resistance (vanilla ardoise)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.DEEPSLATE))
        );
    }

    /** Objet de bloc avec comportement de munition (utilise par l'obus du char). */
    private static Item shellItem(String name, Block block) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new com.donopot.voiddynasty.CannonShellItem(block, new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, id)))
        );
    }

    private static Item blockItem(String name, Block block) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
        );
    }

    /** Le simple chargement de cette classe enregistre les blocs + items. */
    public static void init() {
    }
}
