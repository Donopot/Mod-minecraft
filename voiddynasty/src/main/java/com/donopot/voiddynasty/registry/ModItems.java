package com.donopot.voiddynasty.registry;

import com.donopot.voiddynasty.LaserDrillItem;
import com.donopot.voiddynasty.ServoSkullItem;
import com.donopot.voiddynasty.component.ModDataComponents;
import com.donopot.voiddynasty.weapon.WeaponDefinition;
import com.donopot.voiddynasty.weapon.WeaponDefinitions;
import com.donopot.voiddynasty.weapon.WeaponItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.BrewingFuel;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;

/**
 * Registre des armes, munitions et matériaux du mod (aligné sur le schéma void_dynasty:weapon/v1).
 */
public final class ModItems {

    public static final String NAMESPACE = "voiddynasty";

    // Munitions (déclarées AVANT les armes : WeaponDefinitions y fait référence)
    public static final Item BOLT_SHELL = ammo("bolt_shell");
    public static final Item HEAVY_BOLT_SHELL = ammo("heavy_bolt_shell");
    public static final Item STALKER_BOLT_ROUND = ammo("stalker_bolt_round");
    public static final Item SHOTGUN_SHELL = ammo("shotgun_shell");
    public static final Item LAS_CELL = ammo("las_cell");
    public static final Item PLASMA_CELL = ammo("plasma_cell");
    public static final Item MELTA_CELL = ammo("melta_cell");
    public static final Item PROMETHIUM = promethiumFuel("promethium");
    public static final Item MISSILE_AMMO = ammo("missile_ammo");

    // Matériaux (composants de craft)
    public static final Item ADAMANTIUM_PLATE = ammo("adamantium_plate");
    public static final Item WIRES = ammo("wires");
    public static final Item WAX_SEAL = ammo("wax_seal");
    public static final Item PLASMA_COIL = ammo("plasma_coil");
    public static final Item WARHEAD = ammo("warhead");
    public static final Item FOCUS_LENS = ammo("focus_lens");

    // Céramite (blindage 40k) + titane + carbure de silicium
    public static final Item TITANIUM_INGOT = ammo("titanium_ingot");
    public static final Item SILICON_CARBIDE = ammo("silicon_carbide");
    public static final Item CERAMITE_INGOT = ammo("ceramite_ingot");
    public static final Item BLUE_CERAMITE_INGOT = ammo("blue_ceramite_ingot");
    public static final Item RED_CERAMITE_INGOT = ammo("red_ceramite_ingot");
    public static final Item BLACK_CERAMITE_INGOT = ammo("black_ceramite_ingot");
    public static final Item GREEN_CERAMITE_INGOT = ammo("green_ceramite_ingot");
    public static final Item DARK_GREEN_CERAMITE_INGOT = ammo("dark_green_ceramite_ingot");
    public static final Item PINK_CERAMITE_INGOT = ammo("pink_ceramite_ingot");
    public static final Item WHITE_CERAMITE_INGOT = ammo("white_ceramite_ingot");
    public static final Item YELLOW_CERAMITE_INGOT = ammo("yellow_ceramite_ingot");
    public static final Item LIGHT_BLUE_CERAMITE_INGOT = ammo("light_blue_ceramite_ingot");

    // Armes à distance
    public static final Item BOLTGUN = weapon("boltgun", WeaponDefinitions.BOLTGUN);
    public static final Item BOLT_PISTOL = weapon("bolt_pistol", WeaponDefinitions.BOLT_PISTOL);
    public static final Item HEAVY_BOLTER = weapon("heavy_bolter", WeaponDefinitions.HEAVY_BOLTER);
    public static final Item STALKER_BOLT_RIFLE = weapon("stalker_bolt_rifle", WeaponDefinitions.STALKER_BOLT_RIFLE);
    public static final Item LAS_GUN = weapon("las_gun", WeaponDefinitions.LAS_GUN);
    public static final Item LAZ_PISTOL = weapon("laz_pistol", WeaponDefinitions.LAZ_PISTOL);
    public static final Item PLASMA_PISTOL = weapon("plasma_pistol", WeaponDefinitions.PLASMA_PISTOL);
    public static final Item PLASMA_INCINERATOR = weapon("plasma_incinerator", WeaponDefinitions.PLASMA_INCINERATOR);
    public static final Item SHOTGUN = weapon("shotgun", WeaponDefinitions.SHOTGUN);
    public static final Item FLAMETHROWER = weapon("flamethrower", WeaponDefinitions.FLAMETHROWER);
    public static final Item MELTA_GUN = weapon("melta_gun", WeaponDefinitions.MELTA_GUN);
    public static final Item HUNTER_KILLER_MISSILE = weapon("hunter_killer_missile", WeaponDefinitions.HUNTER_KILLER_MISSILE);

    // Mêlée
    public static final Item CHAINSWORD = weapon("chainsword", WeaponDefinitions.CHAINSWORD);
    public static final Item POWERSWORD = weapon("powersword", WeaponDefinitions.POWERSWORD);
    public static final Item POWER_HAMMER = weapon("power_hammer", WeaponDefinitions.POWER_HAMMER);
    public static final Item LASER_DRILL = tool("laser_drill", LaserDrillItem::new);
    public static final Item SERVO_SKULL = tool("servo_skull", ServoSkullItem::new);

    /** Item outil custom non empilable (foreuse laser / servo-crâne). */
    private static Item tool(String name, java.util.function.Function<Item.Properties, Item> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(1))
        );
    }

    // Spawn egg (mobs)
    public static final Item CULTIST_1_SPAWN_EGG = spawnEgg("cultist_1_spawn_egg", ModEntities.CULTIST_1);
    public static final Item CULTIST_2_SPAWN_EGG = spawnEgg("cultist_2_spawn_egg", ModEntities.CULTIST_2);
    public static final Item CULTIST_3_SPAWN_EGG = spawnEgg("cultist_3_spawn_egg", ModEntities.CULTIST_3);
    public static final Item CULTIST_4_SPAWN_EGG = spawnEgg("cultist_4_spawn_egg", ModEntities.CULTIST_4);
    public static final Item CULTIST_BOSS_SPAWN_EGG = spawnEgg("cultist_boss_spawn_egg", ModEntities.CULTIST_BOSS);
    public static final Item CHAOS_MARINE_1_SPAWN_EGG = spawnEgg("chaos_marine_1_spawn_egg", ModEntities.CHAOS_MARINE_1);
    public static final Item CHAOS_MARINE_2_SPAWN_EGG = spawnEgg("chaos_marine_2_spawn_egg", ModEntities.CHAOS_MARINE_2);
    public static final Item CHAOS_MARINE_3_SPAWN_EGG = spawnEgg("chaos_marine_3_spawn_egg", ModEntities.CHAOS_MARINE_3);
    public static final Item CHAOS_CAPTAIN_SPAWN_EGG = spawnEgg("chaos_captain_spawn_egg", ModEntities.CHAOS_CAPTAIN);
    public static final Item CHAOS_RAPTOR_SPAWN_EGG = spawnEgg("chaos_raptor_spawn_egg", ModEntities.CHAOS_RAPTOR);
    public static final Item SERVITOR_1_SPAWN_EGG = spawnEgg("servitor_1_spawn_egg", ModEntities.SERVITOR_1);
    public static final Item SERVITOR_2_SPAWN_EGG = spawnEgg("servitor_2_spawn_egg", ModEntities.SERVITOR_2);
    public static final Item SERVITOR_3_SPAWN_EGG = spawnEgg("servitor_3_spawn_egg", ModEntities.SERVITOR_3);
    public static final Item CHAOS_DREADNOUGHT_SPAWN_EGG = spawnEgg("chaos_dreadnought_spawn_egg", ModEntities.CHAOS_DREADNOUGHT);
    public static final Item CHAOS_HELLBRUTE_SPAWN_EGG = spawnEgg("chaos_hellbrute_spawn_egg", ModEntities.CHAOS_HELLBRUTE);
    public static final Item CHAOS_FORGEFIEND_SPAWN_EGG = spawnEgg("chaos_forgefiend_spawn_egg", ModEntities.CHAOS_FORGEFIEND);
    public static final Item DAEMON_PRINCE_SPAWN_EGG = spawnEgg("daemon_prince_spawn_egg", ModEntities.DAEMON_PRINCE);
    public static final Item TYRANID_WARRIOR_SPAWN_EGG = spawnEgg("tyranid_warrior_spawn_egg", ModEntities.TYRANID_WARRIOR);

    public static final Item CARNIFEX_SPAWN_EGG = spawnEgg("carnifex_spawn_egg", ModEntities.CARNIFEX);
    public static final Item CARNIFEX_LEVIATHAN_SPAWN_EGG = spawnEgg("carnifex_leviathan_spawn_egg", ModEntities.CARNIFEX_LEVIATHAN);
    public static final Item LICTOR_SPAWN_EGG = spawnEgg("lictor_spawn_egg", ModEntities.LICTOR);
    public static final Item LEMAN_RUSS_SPAWN_EGG = spawnEgg("lemunruss_spawn_egg", ModEntities.LEMAN_RUSS);
    public static final Item NORN_EMISSARY_SPAWN_EGG = spawnEgg("norn_emissary_spawn_egg", ModEntities.NORN_EMISSARY);
    public static final Item GREAT_UNCLEAN_ONE_SPAWN_EGG = spawnEgg("great_unclean_one_spawn_egg", ModEntities.GREAT_UNCLEAN_ONE);
    public static final Item LORD_OF_CHANGE_SPAWN_EGG = spawnEgg("lord_of_change_spawn_egg", ModEntities.LORD_OF_CHANGE);
    public static final Item KEEPER_OF_SECRETS_SPAWN_EGG = spawnEgg("keeper_of_secrets_spawn_egg", ModEntities.KEEPER_OF_SECRETS);
    public static final Item KHORNE_JUGGERNAUT_SPAWN_EGG = spawnEgg("khorne_juggernaut_spawn_egg", ModEntities.KHORNE_JUGGERNAUT);
    public static final Item CHAOS_HELLDRAKE_SPAWN_EGG = spawnEgg("chaos_helldrake_spawn_egg", ModEntities.CHAOS_HELLDRAKE);
    public static final Item LARGE_KHORNE_DAEMON_SPAWN_EGG = spawnEgg("large_khorne_daemon_spawn_egg", ModEntities.LARGE_KHORNE_DAEMON);
    public static final Item PLAGUETOAD_SPAWN_EGG = spawnEgg("plaguetoad_spawn_egg", ModEntities.PLAGUETOAD);
    public static final Item GHAZGHKULL_SPAWN_EGG = spawnEgg("ghazghkull_spawn_egg", ModEntities.GHAZGHKULL);

    private ModItems() {
    }

    /** Item simple empilable (munition ou matériau). */
    private static Item ammo(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(64))
        );
    }

    /** Prométhium : carburant de raffinage (fuel d'alambic via le composant BREWING_FUEL). */
    private static Item promethiumFuel(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new Item(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, id))
                        .stacksTo(64)
                        .component(DataComponents.BREWING_FUEL, new BrewingFuel(
                                new ResolvableInt.Constant(32),
                                new ResolvableFloat.Constant(1.0f))))
        );
    }

    /** Item d'arme : porte sa {@link WeaponDefinition} + chargeur plein par défaut. */
    private static Item weapon(String name, WeaponDefinition def) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        Item.Properties props = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .stacksTo(1);
        // Armes à distance : chargeur plein au craft/créatif (sinon on démarre à vide = blocage).
        if (def.ammo() != null) {
            props = props.component(ModDataComponents.AMMO, def.magazineSize());
        }
        return Registry.register(BuiltInRegistries.ITEM, id, new WeaponItem(props, def));
    }

    /** Œuf d'apparition pour une entité du mod. */
    private static Item spawnEgg(String name, EntityType<?> type) {
        Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new SpawnEggItem(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, id))
                        .component(DataComponents.ENTITY_DATA, TypedEntityData.of(type, new CompoundTag())))
        );
    }

    /** Le simple chargement de cette classe enregistre les items. */
    public static void init() {
    }
}
