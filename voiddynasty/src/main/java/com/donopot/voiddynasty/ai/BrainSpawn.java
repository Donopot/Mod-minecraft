package com.donopot.voiddynasty.ai;

import com.donopot.voiddynasty.entity.Servitor1Entity;
import com.donopot.voiddynasty.entity.Servitor2Entity;
import com.donopot.voiddynasty.entity.Servitor3Entity;
import com.donopot.voiddynasty.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;

/**
 * Le servitor persistant : un servitor qui Existe.
 *
 * <p>Le servitor de chantier est créé et détruit par le contrôleur de chantier ; celui-ci est
 * différent : il est posé une fois, il reste, et il attend les ordres. On lui retire tous ses
 * buts vanilla pour qu'il n'aille pas se promener de lui-même : sans ordre, il ne fait rien.
 * C'est le cerveau (ou l'opérateur) qui décide, jamais son IA interne.</p>
 */
public final class BrainSpawn {

    /** Tag qui marque un servitor piloté par le cerveau (persisté en NBT, survit au redémarrage). */
    public static final String TAG = "voiddynasty_brain";
    public static final String NAME = "Servitor";

    private BrainSpawn() {}

    public enum Variant {
        TIR(1), LOURD(2), RAPIDE(3);

        final int id;

        Variant(int id) {
            this.id = id;
        }

        static Variant of(int id) {
            return switch (id) {
                case 1 -> TIR;
                case 3 -> RAPIDE;
                default -> LOURD;
            };
        }
    }

    public static LivingEntity spawn(ServerLevel level, BlockPos pos, Variant variant) {
        Entity entity = switch (variant) {
            case TIR -> ModEntities.SERVITOR_1.spawn(level, pos, EntitySpawnReason.COMMAND);
            case LOURD -> ModEntities.SERVITOR_2.spawn(level, pos, EntitySpawnReason.COMMAND);
            case RAPIDE -> ModEntities.SERVITOR_3.spawn(level, pos, EntitySpawnReason.COMMAND);
        };
        if (!(entity instanceof LivingEntity bot)) return null;
        configure(bot);
        // EntityType#spawn a DEJA ajoute l'entite au monde : ne pas rappeler addFreshEntity,
        // sinon le log serveur crache « UUID of added entity already exists ».
        return bot;
    }

    /** Applique l'état « piloté par le cerveau » : immobile, sans buts, présent pour de bon. */
    public static void configure(LivingEntity bot) {
        bot.addTag(TAG);
        bot.setCustomName(Component.literal(NAME));
        bot.setCustomNameVisible(true);
        if (bot instanceof com.donopot.voiddynasty.entity.ChaosCultistEntity cultist) {
            cultist.passerEnModePilote();
        }
    }

    private static final java.util.Set<java.util.UUID> CONFIGURED = new java.util.HashSet<>();

    /**
     * Le servitor persistant du niveau, s'il existe.
     *
     * <p>La configuration n'est appliquée qu'une fois par entité : la rejouer à chaque cycle
     * appellerait {@code navigation.stop()} toutes les deux secondes et couperait les
     * déplacements en cours.</p>
     */
    public static LivingEntity find(ServerLevel level) {
        for (Entity e : level.getAllEntities()) {
            if (e instanceof LivingEntity le && le.isAlive() && le.entityTags().contains(TAG)) {
                if (CONFIGURED.add(le.getUUID())) configure(le);
                return le;
            }
        }
        return null;
    }

    public static boolean despawn(ServerLevel level) {
        boolean any = false;
        for (Entity e : level.getAllEntities()) {
            if (e instanceof LivingEntity le && le.entityTags().contains(TAG)) {
                BrainInv.dropAll(level, le);
                CONFIGURED.remove(le.getUUID());
                le.discard();
                any = true;
            }
        }
        return any;
    }

    public static boolean isBrainServitor(Entity e) {
        return e instanceof LivingEntity le && le.entityTags().contains(TAG);
    }

    static boolean isServitorClass(Entity e) {
        return e instanceof Servitor1Entity || e instanceof Servitor2Entity || e instanceof Servitor3Entity;
    }
}
