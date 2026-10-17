package com.donopot.voiddynasty.registry;

import com.donopot.voiddynasty.weapon.BoltProjectile;
import com.donopot.voiddynasty.weapon.ProjectileChaos;
import com.donopot.voiddynasty.weapon.LaserBeamEntity;
import com.donopot.voiddynasty.entity.Cultist1Entity;
import com.donopot.voiddynasty.entity.Cultist2Entity;
import com.donopot.voiddynasty.entity.Cultist3Entity;
import com.donopot.voiddynasty.entity.Cultist4Entity;
import com.donopot.voiddynasty.entity.CultistBossEntity;
import com.donopot.voiddynasty.entity.ChaosMarine1Entity;
import com.donopot.voiddynasty.entity.ChaosMarine2Entity;
import com.donopot.voiddynasty.entity.ChaosMarine3Entity;
import com.donopot.voiddynasty.entity.ChaosCaptainEntity;
import com.donopot.voiddynasty.entity.ChaosHellDrakeEntity;
import com.donopot.voiddynasty.entity.ChaosRaptorEntity;
import com.donopot.voiddynasty.entity.DaemonPrinceEntity;
import com.donopot.voiddynasty.entity.ChaosDreadnoughtEntity;
import com.donopot.voiddynasty.entity.ChaosHellbruteEntity;
import com.donopot.voiddynasty.entity.ChaosForgefiendEntity;
import com.donopot.voiddynasty.entity.Servitor1Entity;
import com.donopot.voiddynasty.entity.Servitor2Entity;
import com.donopot.voiddynasty.entity.Servitor3Entity;
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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Registre des entités du mod (projectiles).
 */
public final class ModEntities {

    public static final EntityType<BoltProjectile> BOLT_PROJECTILE = register(
            "bolt_projectile",
            EntityType.Builder.<BoltProjectile>of(BoltProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .noSummon()
                    .noSave()
                    .clientTrackingRange(64)
    );

    public static final EntityType<LaserBeamEntity> LASER_BEAM = register(
            "laser_beam",
            EntityType.Builder.<LaserBeamEntity>of(LaserBeamEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .noSummon()
                    .noSave()
                    .clientTrackingRange(256)
    );

    public static final EntityType<TyranidWarriorEntity> TYRANID_WARRIOR = register(
            "tyranid_warrior",
            EntityType.Builder.of(TyranidWarriorEntity::new, MobCategory.MONSTER)
                    .sized(1.2f, 2.4f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<CarnifexEntity> CARNIFEX = register(
            "carnifex",
            EntityType.Builder.of(CarnifexEntity::new, MobCategory.MONSTER)
                    .sized(2.8f, 5.0f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<CarnifexLeviathanEntity> CARNIFEX_LEVIATHAN = register(
            "carnifex_leviathan",
            EntityType.Builder.of(CarnifexLeviathanEntity::new, MobCategory.MONSTER)
                    .sized(2.4f, 3.6f)
                    .clientTrackingRange(64)
    );

    // Char Leman Russ (decision de Donopot ④, « du pilotable »). La source le classe elle-meme en
    // « vehicule » : il est donc une entite, pas un decor.
    // Gabarit du modele LIVRE : 1,98 (largeur) x 1,83 (hauteur) x 3,90 (longueur). La source
    // modelisait 3,00 de long (rapport 1,64) alors que la figurine de reference de Donopot en fait
    // environ 2,3 : la longueur est etiree de 30 % pour s'en rapprocher, et le modele est recentre.
    // C'est une retouche ASSUMEE, reglable par un seul nombre : --allonger dans
    // tools/convert_modele_java.py. La hitbox suit le modele, elle n'est pas choisie a part.
    public static final EntityType<LemanRussEntity> LEMAN_RUSS = register(
            "lemunruss",
            EntityType.Builder.of(LemanRussEntity::new, MobCategory.MISC)
                    .sized(4.77f, 4.42f)
                    .clientTrackingRange(10)
    );

    // Char Leman Russ V2 — VEHICULE NEUF, jumeau de test (decision Donopot 2026-10-17).
    // Meme classe d'entite et meme maillage que ci-dessus, mais un identifiant NEUF.
    // Il repond a une seule question : ce maillage s'affiche-t-il dans l'installation de
    // Donopot ? Un identifiant neuf ne peut exister que si le bon jar est charge, donc
    // reussir a l'invoquer prouve AUSSI que le jar installe est bien le notre.
    public static final EntityType<LemanRussEntity> LEMAN_RUSS_V2 = register(
            "lemunruss_v2",
            EntityType.Builder.of(LemanRussEntity::new, MobCategory.MISC)
                    .sized(4.77f, 4.42f)
                    .clientTrackingRange(10)
    );

    public static final EntityType<LictorEntity> LICTOR = register(
            "lictor",
            EntityType.Builder.of(LictorEntity::new, MobCategory.MONSTER)
                    .sized(1.2f, 2.4f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<NornEmissaryEntity> NORN_EMISSARY = register(
            "norn_emissary",
            EntityType.Builder.of(NornEmissaryEntity::new, MobCategory.MONSTER)
                    .sized(3.2f, 7.0f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<GreatUncleanOneEntity> GREAT_UNCLEAN_ONE = register(
            "great_unclean_one",
            EntityType.Builder.of(GreatUncleanOneEntity::new, MobCategory.MONSTER)
                    .sized(3.0f, 6.0f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<LordOfChangeEntity> LORD_OF_CHANGE = register(
            "lord_of_change",
            EntityType.Builder.of(LordOfChangeEntity::new, MobCategory.MONSTER)
                    .sized(2.8f, 8.0f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<KeeperOfSecretsEntity> KEEPER_OF_SECRETS = register(
            "keeper_of_secrets",
            EntityType.Builder.of(KeeperOfSecretsEntity::new, MobCategory.MONSTER)
                    .sized(1.6f, 5.0f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<KhorneJuggernautEntity> KHORNE_JUGGERNAUT = register(
            "khorne_juggernaut",
            EntityType.Builder.of(KhorneJuggernautEntity::new, MobCategory.MONSTER)
                    .sized(1.8f, 1.8f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<LargeKhorneDaemonEntity> LARGE_KHORNE_DAEMON = register(
            "large_khorne_daemon",
            EntityType.Builder.of(LargeKhorneDaemonEntity::new, MobCategory.MONSTER)
                    .sized(4.0f, 9.0f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<PlaguetoadEntity> PLAGUETOAD = register(
            "plaguetoad",
            EntityType.Builder.of(PlaguetoadEntity::new, MobCategory.MONSTER)
                    .sized(1.8f, 1.6f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<GhazghkullEntity> GHAZGHKULL = register(
            "ghazghkull",
            EntityType.Builder.of(GhazghkullEntity::new, MobCategory.MONSTER)
                    .sized(1.4f, 3.0f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<ServoSkullEntity> SERVO_SKULL = register(
            "servo_skull",
            EntityType.Builder.of(ServoSkullEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<Cultist1Entity> CULTIST_1 = register(
            "cultist_1",
            EntityType.Builder.of(Cultist1Entity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.75f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<Cultist2Entity> CULTIST_2 = register(
            "cultist_2",
            EntityType.Builder.of(Cultist2Entity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.75f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<Cultist3Entity> CULTIST_3 = register(
            "cultist_3",
            EntityType.Builder.of(Cultist3Entity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.75f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<Cultist4Entity> CULTIST_4 = register(
            "cultist_4",
            EntityType.Builder.of(Cultist4Entity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.75f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<CultistBossEntity> CULTIST_BOSS = register(
            "cultist_boss",
            EntityType.Builder.of(CultistBossEntity::new, MobCategory.MONSTER)
                    .sized(0.8f, 2.7f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<ChaosMarine1Entity> CHAOS_MARINE_1 = register(
            "chaos_marine_1",
            EntityType.Builder.of(ChaosMarine1Entity::new, MobCategory.MONSTER)
                    .sized(0.9f, 2.74f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<ChaosMarine2Entity> CHAOS_MARINE_2 = register(
            "chaos_marine_2",
            EntityType.Builder.of(ChaosMarine2Entity::new, MobCategory.MONSTER)
                    .sized(0.9f, 2.36f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<ChaosMarine3Entity> CHAOS_MARINE_3 = register(
            "chaos_marine_3",
            EntityType.Builder.of(ChaosMarine3Entity::new, MobCategory.MONSTER)
                    .sized(0.9f, 2.36f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<ChaosCaptainEntity> CHAOS_CAPTAIN = register(
            "chaos_captain",
            EntityType.Builder.of(ChaosCaptainEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 2.73f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<ChaosRaptorEntity> CHAOS_RAPTOR = register(
            "chaos_raptor",
            EntityType.Builder.of(ChaosRaptorEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 2.36f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<ChaosHellDrakeEntity> CHAOS_HELLDRAKE = register(
            "chaos_helldrake",
            EntityType.Builder.of(ChaosHellDrakeEntity::new, MobCategory.MONSTER)
                    // hitbox du phantom (0.9 x 0.5) : c'est LUI qui porte la créature dans la source.
                    // L'attribut SCALE=2 de la source double ensuite cette taille (1.8 x 1.0).
                    .sized(0.9f, 0.5f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<Servitor1Entity> SERVITOR_1 = register(
            "servitor_1",
            EntityType.Builder.of(Servitor1Entity::new, MobCategory.MONSTER)
                    .sized(1.09f, 1.91f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<Servitor2Entity> SERVITOR_2 = register(
            "servitor_2",
            EntityType.Builder.of(Servitor2Entity::new, MobCategory.MONSTER)
                    .sized(1.09f, 1.91f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<Servitor3Entity> SERVITOR_3 = register(
            "servitor_3",
            EntityType.Builder.of(Servitor3Entity::new, MobCategory.MONSTER)
                    .sized(1.64f, 2.43f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<ProjectileChaos> PROJECTILE_CHAOS = register(
            "projectile_chaos",
            EntityType.Builder.<ProjectileChaos>of(ProjectileChaos::new, MobCategory.MISC)
                    .sized(0.15f, 0.15f)
                    .noSummon()
                    .clientTrackingRange(64)
    );

    public static final EntityType<ChaosDreadnoughtEntity> CHAOS_DREADNOUGHT = register(
            "chaos_dreadnought",
            EntityType.Builder.of(ChaosDreadnoughtEntity::new, MobCategory.MONSTER)
                    .sized(2.85f, 2.83f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<ChaosHellbruteEntity> CHAOS_HELLBRUTE = register(
            "chaos_hellbrute",
            EntityType.Builder.of(ChaosHellbruteEntity::new, MobCategory.MONSTER)
                    .sized(2.85f, 2.83f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<ChaosForgefiendEntity> CHAOS_FORGEFIEND = register(
            "chaos_forgefiend",
            EntityType.Builder.of(ChaosForgefiendEntity::new, MobCategory.MONSTER)
                    .sized(3.0f, 4.5f)
                    .clientTrackingRange(64)
    );

    public static final EntityType<DaemonPrinceEntity> DAEMON_PRINCE = register(
            "daemon_prince",
            EntityType.Builder.of(DaemonPrinceEntity::new, MobCategory.MONSTER)
                    .sized(2.4f, 4.0f)
                    .clientTrackingRange(96)
    );

    private ModEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        Identifier id = Identifier.fromNamespaceAndPath("voiddynasty", name);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                id,
                builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id))
        );
    }

    public static void init() {
    }
}
