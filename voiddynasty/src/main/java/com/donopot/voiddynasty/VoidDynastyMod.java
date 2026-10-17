package com.donopot.voiddynasty;

import com.donopot.voiddynasty.component.ModDataComponents;
import com.donopot.voiddynasty.entity.TyranidWarriorEntity;
import com.donopot.voiddynasty.entity.CarnifexEntity;
import com.donopot.voiddynasty.entity.CarnifexLeviathanEntity;
import com.donopot.voiddynasty.entity.LemanRussEntity;
import com.donopot.voiddynasty.entity.LictorEntity;
import com.donopot.voiddynasty.entity.NornEmissaryEntity;
import com.donopot.voiddynasty.entity.GreatUncleanOneEntity;
import com.donopot.voiddynasty.entity.LordOfChangeEntity;
import com.donopot.voiddynasty.entity.KeeperOfSecretsEntity;
import com.donopot.voiddynasty.entity.KhorneJuggernautEntity;
import com.donopot.voiddynasty.entity.LargeKhorneDaemonEntity;
import com.donopot.voiddynasty.entity.PlaguetoadEntity;
import com.donopot.voiddynasty.entity.GhazghkullEntity;
import com.donopot.voiddynasty.entity.ServoSkullEntity;
import com.donopot.voiddynasty.network.ModNetworking;
import com.donopot.voiddynasty.armor.ArmorEffects;
import com.donopot.voiddynasty.registry.ModArmor;
import com.donopot.voiddynasty.registry.ModBlockEntities;
import com.donopot.voiddynasty.registry.ModBlocks;
import com.donopot.voiddynasty.entity.Cultist1Entity;
import com.donopot.voiddynasty.entity.ChaosMarine1Entity;
import com.donopot.voiddynasty.entity.Servitor1Entity;
import com.donopot.voiddynasty.entity.ChaosDreadnoughtEntity;
import com.donopot.voiddynasty.entity.DaemonPrinceEntity;
import com.donopot.voiddynasty.entity.ChaosHellbruteEntity;
import com.donopot.voiddynasty.entity.ChaosForgefiendEntity;
import com.donopot.voiddynasty.entity.Servitor2Entity;
import com.donopot.voiddynasty.entity.Servitor3Entity;
import com.donopot.voiddynasty.entity.ChaosMarine2Entity;
import com.donopot.voiddynasty.entity.ChaosMarine3Entity;
import com.donopot.voiddynasty.entity.ChaosCaptainEntity;
import com.donopot.voiddynasty.entity.ChaosHellDrakeEntity;
import com.donopot.voiddynasty.entity.ChaosRaptorEntity;
import com.donopot.voiddynasty.entity.Cultist2Entity;
import com.donopot.voiddynasty.entity.Cultist3Entity;
import com.donopot.voiddynasty.entity.Cultist4Entity;
import com.donopot.voiddynasty.entity.CultistBossEntity;import com.donopot.voiddynasty.registry.ModEntities;
import com.donopot.voiddynasty.registry.ModItemGroups;
import com.donopot.voiddynasty.registry.ModItems;
import com.donopot.voiddynasty.registry.ModMenuTypes;
import com.donopot.voiddynasty.registry.ModSounds;
import com.donopot.voiddynasty.terrain.TerrainDamageManager;
import com.donopot.voiddynasty.weapon.WeaponService;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoidDynastyMod implements ModInitializer {
    public static final String MOD_ID = "voiddynasty";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ------------------------------------------------------------------
    // Items historiques (pipeline vertical-slice)
    // ------------------------------------------------------------------

    // Static test item — /give @s voiddynasty:voiddynasty_item
    public static final Identifier VOID_DYNASTY_ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "voiddynasty_item");
    public static final Item VOID_DYNASTY_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            VOID_DYNASTY_ITEM_ID,
            new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, VOID_DYNASTY_ITEM_ID)))
    );

    // Static test block (+ item) — /give @s voiddynasty:voiddynasty_block
    public static final Identifier VOID_DYNASTY_BLOCK_ID = Identifier.fromNamespaceAndPath(MOD_ID, "voiddynasty_block");
    public static final Block VOID_DYNASTY_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            VOID_DYNASTY_BLOCK_ID,
            new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, VOID_DYNASTY_BLOCK_ID)))
    );
    public static final Item VOID_DYNASTY_BLOCK_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            VOID_DYNASTY_BLOCK_ID,
            new BlockItem(VOID_DYNASTY_BLOCK, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, VOID_DYNASTY_BLOCK_ID)))
    );

    // Functional item: energy wave (DBZ-style beam + block destruction) — /give @s voiddynasty:energy_wave
    public static final Identifier ENERGY_WAVE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "energy_wave");
    public static final Item ENERGY_WAVE = Registry.register(
            BuiltInRegistries.ITEM,
            ENERGY_WAVE_ID,
            new EnergyWaveItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ENERGY_WAVE_ID)).stacksTo(1))
    );

    // Functional item: plasma cannon (fires an explosive LargeFireball) — /give @s voiddynasty:plasma_cannon
    public static final Identifier CANON_ID = Identifier.fromNamespaceAndPath(MOD_ID, "plasma_cannon");
    public static final Item CANON = Registry.register(
            BuiltInRegistries.ITEM,
            CANON_ID,
            new CanonItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CANON_ID)).stacksTo(1))
    );

    // Control pad: spawn / save / teleport the ship — /give @s voiddynasty:control_pad
    public static final Identifier CONTROL_PAD_ID = Identifier.fromNamespaceAndPath(MOD_ID, "control_pad");
    public static final Item CONTROL_PAD = Registry.register(
            BuiltInRegistries.ITEM,
            CONTROL_PAD_ID,
            new ControlPadItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CONTROL_PAD_ID)).stacksTo(1))
    );

    // Relique : fragment de champ de Gellar (mission Monde Mortuaire) — /give @s voiddynasty:gellar_shard
    public static final Identifier GELLAR_SHARD_ID = Identifier.fromNamespaceAndPath(MOD_ID, "gellar_shard");
    public static final Item GELLAR_SHARD = Registry.register(
            BuiltInRegistries.ITEM,
            GELLAR_SHARD_ID,
            new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, GELLAR_SHARD_ID)).stacksTo(1))
    );

    @Override
    public void onInitialize() {
        LOGGER.info("[voiddynasty] Mod initializing");
        com.donopot.voiddynasty.worldgen.ModWorldGen.enregistrer();

        // Registres du bolter (data components, items, sons, entités, réseau) + blocs décoratifs
        ModDataComponents.init();
        ModItems.init();
        ModSounds.init();
        ModBlocks.init();
        ModArmor.init();
        ModEntities.init();
        ModBlockEntities.init();
        ModMenuTypes.init();
        FabricDefaultAttributeRegistry.register(ModEntities.CARNIFEX, CarnifexEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CARNIFEX_LEVIATHAN, CarnifexLeviathanEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.LICTOR, LictorEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.LEMAN_RUSS, LemanRussEntity.createAttributes());
        // Jumeau de test du Leman Russ (identifiant neuf, meme entite, meme maillage).
        FabricDefaultAttributeRegistry.register(ModEntities.LEMAN_RUSS_V2, LemanRussEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CHAOS_HELLDRAKE, ChaosHellDrakeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.NORN_EMISSARY, NornEmissaryEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.GREAT_UNCLEAN_ONE, GreatUncleanOneEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.LORD_OF_CHANGE, LordOfChangeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.KEEPER_OF_SECRETS, KeeperOfSecretsEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.KHORNE_JUGGERNAUT, KhorneJuggernautEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.LARGE_KHORNE_DAEMON, LargeKhorneDaemonEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.PLAGUETOAD, PlaguetoadEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.GHAZGHKULL, GhazghkullEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.TYRANID_WARRIOR, TyranidWarriorEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CULTIST_1, Cultist1Entity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CULTIST_2, Cultist2Entity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CULTIST_3, Cultist3Entity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CULTIST_4, Cultist4Entity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CULTIST_BOSS, CultistBossEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CHAOS_MARINE_1, ChaosMarine1Entity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CHAOS_MARINE_2, ChaosMarine2Entity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CHAOS_MARINE_3, ChaosMarine3Entity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CHAOS_CAPTAIN, ChaosCaptainEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CHAOS_RAPTOR, ChaosRaptorEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.SERVITOR_1, Servitor1Entity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.SERVITOR_2, Servitor2Entity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.SERVITOR_3, Servitor3Entity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CHAOS_DREADNOUGHT, ChaosDreadnoughtEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CHAOS_HELLBRUTE, ChaosHellbruteEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CHAOS_FORGEFIEND, ChaosForgefiendEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.DAEMON_PRINCE, DaemonPrinceEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.SERVO_SKULL, ServoSkullEntity.createAttributes());
        ModItemGroups.init();
        ModNetworking.init();

        // Config des dégâts de terrain (config/voiddynasty.json)
        TerrainDamageManager.loadConfig(FabricLoader.getInstance().getConfigDir());

        // Tick serveur : progression du rechargement + tir automatique
        ServerTickEvents.END_SERVER_TICK.register(WeaponService::tickReloads);
        ServerTickEvents.END_SERVER_TICK.register(WeaponService::tickAutoFire);
        ServerTickEvents.END_SERVER_TICK.register(WeaponService::tickWeaponState);
        ServerTickEvents.END_SERVER_TICK.register(TerrainDamageManager::tickDecay);
        ServerTickEvents.END_SERVER_TICK.register(ArmorEffects::tick);
        ServerTickEvents.END_SERVER_TICK.register(com.donopot.voiddynasty.campagne.CampagneService::tick);

        ServerLifecycleEvents.SERVER_STARTED.register(com.donopot.voiddynasty.campagne.CampagneService::charger);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("[voiddynasty] SERVER_STARTED (version={})", server.getServerVersion());
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            dispatcher.register(
                    Commands.literal("voiddynasty")
                            .executes(VoidDynastyMod::runVoidDynasty)
            );
            CruiserCommand.register(dispatcher);
            FrigateCommand.register(dispatcher);
            FleetCommand.register(dispatcher);
            MortuaryCommand.register(dispatcher);
            MissionCommand.register(dispatcher);
            com.donopot.voiddynasty.campagne.CampagneCommand.register(dispatcher);
            // commande de test headless : /vdyntest <registry|terrain|spawn|all>
            VoidDynastyTestCommand.register(dispatcher);
        });
    }

    private static int runVoidDynasty(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(
                () -> Component.literal("[voiddynasty] pipeline OK — mod loaded and responding"),
                false
        );
        return 1;
    }
}
