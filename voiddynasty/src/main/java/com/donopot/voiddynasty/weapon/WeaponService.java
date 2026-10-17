package com.donopot.voiddynasty.weapon;

import com.donopot.voiddynasty.component.ModDataComponents;
import com.donopot.voiddynasty.registry.ModSounds;
import com.donopot.voiddynasty.terrain.TerrainDamageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Logique centrale des armes : modes de tir, projectiles physiques, rechargement
 * (mag complet ou cartouche par cartouche), recul et tir automatique.
 */
public final class WeaponService {

    private static final Logger LOGGER = LoggerFactory.getLogger("voiddynasty.weapon");

    /** Rechargements « mag complet » en cours, clé = UUID joueur -> ticks restants. */
    private static final Map<UUID, Integer> ACTIVE_RELOADS = new HashMap<>();

    /** Rechargements « cartouche par cartouche » (fusil à pompe), UUID -> ticks avant prochaine cartouche. */
    private static final Map<UUID, Integer> SHELL_RELOADS = new HashMap<>();

    /** Joueurs en tir automatique/continu (maintien du clic). */
    private static final Map<UUID, Integer> AUTO_FIRE = new HashMap<>();

    /** Arme (Item) en cours de rechargement par joueur (annule le reload si l'arme change). */
    private static final Map<UUID, Item> RELOAD_WEAPON = new HashMap<>();

    /** Bloom de dispersion par joueur (augmente en tirant, se dissipe au repos). */
    private static final Map<UUID, Float> SPREAD_BLOOM = new HashMap<>();

    /** Chaleur de surchauffe par joueur (armes plasma). */
    private static final Map<UUID, Float> WEAPON_HEAT = new HashMap<>();

    private WeaponService() {
    }

    // ------------------------------------------------------------------
    // Tir
    // ------------------------------------------------------------------

    /** Tir unique (modes semi / rafale / mêlée). */
    public static void fire(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof WeaponItem weapon)) return;
        WeaponDefinition def = weapon.definition();

        if (isReloading(player)) return;
        if (player.getCooldowns().isOnCooldown(stack)) return;

        int shots = (def.mode() == FireMode.BURST_3) ? 3 : 1;
        int tires = 0;
        for (int i = 0; i < shots; i++) {
            if (!consumeAmmo(player, stack, def)) {
                playDryFire(player);
                break;
            }
            fireShot(player, def);
            tires++;
        }

        // Recul VISIBLE en main (phase D) : uniquement si un coup est vraiment parti (pas à vide).
        if (tires > 0) marquerRecul(stack);

        // Bloom de dispersion + surchauffe plasma (après le tir).
        addSpreadBloom(player, def);
        if (def.family().equals("plasma") && overheat(player, stack)) {
            return;
        }

        player.getCooldowns().addCooldown(stack, def.fireIntervalTicks());
        playFireSound(player, def);
        applyRecoil(player, def);
    }

    /** Un projectile (ou un coup de mêlée) partant de l'œil du joueur. */
    private static void fireShot(ServerPlayer player, WeaponDefinition def) {
        ServerLevel level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 aim = player.getLookAngle();

        // flash de bouche
        spawnMuzzleFlash(level, eye, aim, def);

        if (def.mode() == FireMode.MELEE) {
            // mêlée : hitscan court
            meleeHit(player, level, def, eye, aim);
            return;
        }

        // hitscan (laser / melta, speed = 0)
        if (def.projectile().speed() <= 0f) {
            hitscanShot(player, level, def, eye, aim);
            return;
        }

        // projectiles physiques
        int pellets = Math.max(1, def.projectilesPerShot());
        float spread = computeSpread(player, def);
        for (int i = 0; i < pellets; i++) {
            BoltProjectile proj = new BoltProjectile(level, player, def);
            proj.setPos(eye.x, eye.y, eye.z);
            proj.shoot(aim.x, aim.y, aim.z, def.projectile().speed() / 20f, spread);
            level.addFreshEntity(proj);
        }
    }

    private static void hitscanShot(ServerPlayer player, ServerLevel level, WeaponDefinition def,
                                    Vec3 eye, Vec3 aim) {
        // Raycast complet (entités + blocs) depuis la vue du joueur :
        // player.pick() ne détecte QUE les blocs, donc aucun dégât sur les mobs.
        Predicate<Entity> filter = e -> e != player && e instanceof LivingEntity;
        HitResult hit = ProjectileUtil.getHitResultOnViewVector(player, filter, def.maxRange());
        Vec3 end = hit.getLocation();
        // Vrai rayon : ligne continue rendue côté client (LaserBeamEntity).
        level.addFreshEntity(new LaserBeamEntity(level, eye, end, ARGB.color(255, 60, 60)));
        if (hit.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hit).getEntity();
            if (target instanceof LivingEntity le) {
                DamageCalculator.applyDirect(player, le, def, end);
            }
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            // Dégâts de terrain (laser / melta).
            TerrainDamageManager.onImpact(level, ((BlockHitResult) hit).getBlockPos(),
                    player.getEyePosition(), aim, def.terrain());
        }
        level.sendParticles(def.impact(), end.x, end.y, end.z, 10, 0.15, 0.15, 0.15, 0.05);
        level.sendParticles(ParticleTypes.SMOKE, end.x, end.y, end.z, 4, 0.1, 0.1, 0.1, 0.02);
    }


    private static void meleeHit(ServerPlayer player, ServerLevel level, WeaponDefinition def,
                                 Vec3 eye, Vec3 aim) {
        double reach = def.maxRange();
        Predicate<LivingEntity> inReach = e ->
                e != player && e.isAlive() && e.distanceToSqr(eye) <= reach * reach;
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity t : level.getEntities(EntityTypeTest.forClass(LivingEntity.class), inReach)) {
            Vec3 to = t.position().subtract(eye);
            if (to.lengthSqr() < 1.0E-7) continue;
            double dist = to.length();
            if (aim.dot(to.normalize()) > 0.6 && dist < bestDist) {
                best = t;
                bestDist = dist;
            }
        }
        if (best != null) {
            DamageCalculator.applyDirect(player, best, def, best.position());
        }
        // Dégâts de terrain (mêlée : coupe / choc / onde de choc).
        HitResult look = player.pick(reach, 0f, false);
        if (look.getType() == HitResult.Type.BLOCK) {
            TerrainDamageManager.onImpact(level, ((BlockHitResult) look).getBlockPos(),
                    player.getEyePosition(), aim, def.terrain());
        }
    }

    /** Tir en continu (automatique / continu) — appelé chaque tick serveur. */
    public static void tickAutoFire(MinecraftServer server) {
        if (AUTO_FIRE.isEmpty()) return;
        Iterator<Map.Entry<UUID, Integer>> it = AUTO_FIRE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                it.remove();
                continue;
            }
            ItemStack held = player.getMainHandItem();
            if (!(held.getItem() instanceof WeaponItem weapon)) {
                it.remove();
                continue;
            }
            WeaponDefinition def = weapon.definition();
            // Sécurité : si le joueur ne maintient plus l'item (relâchement, mort,
            // changement d'arme), on arrête le tir même si releaseUsing() n'a pas tourné.
            if (!player.isUsingItem()) {
                it.remove();
                continue;
            }
            if (player.getCooldowns().isOnCooldown(held)) continue;
            if (!consumeAmmo(player, held, def)) {
                playDryFire(player);
                it.remove(); // arrête le tir auto (une seule notification « chargeur vide »)
                continue;
            }
            fireShot(player, def);
            player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
            addSpreadBloom(player, def);
            player.getCooldowns().addCooldown(held, def.fireIntervalTicks());
            playFireSound(player, def);
            applyRecoil(player, def);
        }
    }

    public static void startAutoFire(ServerPlayer player) {
        AUTO_FIRE.put(player.getUUID(), 0);
    }

    public static void stopAutoFire(ServerPlayer player) {
        AUTO_FIRE.remove(player.getUUID());
    }

    private static boolean consumeAmmo(ServerPlayer player, ItemStack stack, WeaponDefinition def) {
        if (def.ammo() == null) return true; // mêlée : infinie
        int ammo = getAmmo(stack);
        if (ammo <= 0) return false;
        setAmmo(stack, ammo - 1);
        return true;
    }

    private static void spawnMuzzleFlash(ServerLevel level, Vec3 eye, Vec3 aim, WeaponDefinition def) {
        double mx = eye.x + aim.x * 0.6;
        double my = eye.y + aim.y * 0.6;
        double mz = eye.z + aim.z * 0.6;
        switch (def.family()) {
            case "flame" -> {
                // cône de feu (lance-flammes)
                for (int i = 0; i < 10; i++) {
                    level.sendParticles(ParticleTypes.FLAME, mx, my, mz, 1, 0.14, 0.14, 0.14, 0.04);
                }
                level.sendParticles(ParticleTypes.SMOKE, mx, my, mz, 2, 0.1, 0.1, 0.1, 0.02);
            }
            case "melta" -> {
                // chaleur de fusion : flammes + fumée dense
                for (int i = 0; i < 8; i++) {
                    level.sendParticles(ParticleTypes.FLAME, mx, my, mz, 1, 0.12, 0.12, 0.12, 0.03);
                }
                level.sendParticles(ParticleTypes.SMOKE, mx, my, mz, 4, 0.1, 0.1, 0.1, 0.02);
            }
            case "plasma" -> {
                // plasma : flash vert + fumée
                level.sendParticles(def.tracer(), mx, my, mz, 12, 0.08, 0.08, 0.08, 0.02);
                level.sendParticles(ParticleTypes.SMOKE, mx, my, mz, 3, 0.08, 0.08, 0.08, 0.01);
            }
            case "las" -> {
                // laser : flash rouge léger
                level.sendParticles(def.tracer(), mx, my, mz, 5, 0.05, 0.05, 0.05, 0.01);
            }
            case "explosive" -> {
                // missile : flash + fumée massive
                for (int i = 0; i < 6; i++) {
                    level.sendParticles(ParticleTypes.FLAME, mx, my, mz, 1, 0.1, 0.1, 0.1, 0.03);
                }
                level.sendParticles(ParticleTypes.LARGE_SMOKE, mx, my, mz, 4, 0.12, 0.12, 0.12, 0.02);
            }
            case "solid" -> {
                // fusil à pompe : étincelles + fumée dense
                level.sendParticles(def.tracer(), mx, my, mz, 8, 0.08, 0.08, 0.08, 0.02);
                level.sendParticles(ParticleTypes.SMOKE, mx, my, mz, 4, 0.08, 0.08, 0.08, 0.02);
            }
            default -> {
                // bolt : étincelles électriques
                level.sendParticles(def.tracer(), mx, my, mz, 8, 0.06, 0.06, 0.06, 0.02);
            }
        }
    }

    // ------------------------------------------------------------------
    // Phase D — marqueurs d'animation (état transitoire porté par l'objet)
    // ------------------------------------------------------------------

    /**
     * Durée du recul visible, en ticks. **Fixe**, volontairement : la cadence est dérivée des coups
     * par seconde (20 / rps), donc un recul proportionnel au cooldown durerait moins d'un tick sur un
     * bolter — invisible. Le cooldown vanilla ne peut pas exprimer une durée absolue.
     */
    public static final int RECUL_TICKS = 3;

    /** Pose de recul : le client bascule sur la variante de modèle `_recoil`. */
    public static void marquerRecul(ItemStack stack) {
        stack.set(ModDataComponents.RECOIL, RECUL_TICKS);
    }

    /** Pose de rechargement : le client bascule sur la variante de modèle `_reloading`. */
    public static void marquerRechargement(ItemStack stack, int ticks) {
        stack.set(ModDataComponents.RELOADING, Math.max(1, ticks));
    }

    /**
     * Décrémente les marqueurs d'animation d'un objet. Appelé par {@code WeaponItem.inventoryTick},
     * donc côté serveur seul et **où que soit l'objet** (main, sacoche, coffre) : un marqueur finit
     * toujours par disparaître, même si le joueur range l'arme ou se déconnecte en plein recul.
     * C'est ce qui évite qu'une arme reste figée en pose de recul dans un coffre.
     *
     * @return le nombre de marqueurs arrivés à zéro et retirés
     */
    public static int tickMarqueurs(ItemStack stack) {
        return tickMarqueur(stack, ModDataComponents.RECOIL)
                + tickMarqueur(stack, ModDataComponents.RELOADING);
    }

    private static int tickMarqueur(ItemStack stack, DataComponentType<Integer> type) {
        Integer restant = stack.get(type);
        if (restant == null) return 0;
        if (restant <= 1) {
            stack.remove(type);
            return 1;
        }
        stack.set(type, restant - 1);
        return 0;
    }

    private static void applyRecoil(ServerPlayer player, WeaponDefinition def) {
        if (def.recoilVertical() == 0f && def.recoilHorizontal() == 0f) return;
        float kick = def.recoilVertical() * 0.75f;
        float yaw = (player.getRandom().nextFloat() - 0.5f) * def.recoilHorizontal();
        player.setXRot(player.getXRot() - kick);
        player.setYRot(player.getYRot() + yaw);
    }

    private static void playFireSound(ServerPlayer player, WeaponDefinition def) {
        ServerLevel level = player.level();
        level.playSound(null, player.blockPosition(), def.fireSound(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void playDryFire(ServerPlayer player) {
        ServerLevel level = player.level();
        level.playSound(null, player.blockPosition(), ModSounds.DRY_FIRE,
                SoundSource.PLAYERS, 0.6f, 1.0f);
    }

    // ------------------------------------------------------------------
    // Dispersion dynamique + surchauffe
    // ------------------------------------------------------------------

    /** Dispersion réelle : aim (accroupi) / hip, pénalité de mouvement, puis bloom. */
    private static float computeSpread(ServerPlayer player, WeaponDefinition def) {
        boolean aiming = player.isShiftKeyDown();
        float base = aiming ? def.aimSpread() : def.hipSpread();
        float moveMult = 1.0f;
        if (player.isSprinting()) {
            moveMult = 1.6f;
        } else if (player.getDeltaMovement().horizontalDistance() < 0.03f) {
            moveMult = aiming ? 0.7f : 0.85f;
        }
        float bloom = SPREAD_BLOOM.getOrDefault(player.getUUID(), 0f);
        return Math.max(0f, base * moveMult + bloom);
    }

    /** Chaque tir ajoute un peu de dispersion (bloom), jusqu'à un plafond. */
    private static void addSpreadBloom(ServerPlayer player, WeaponDefinition def) {
        float bloom = SPREAD_BLOOM.getOrDefault(player.getUUID(), 0f) + def.hipSpread() * 0.35f;
        SPREAD_BLOOM.put(player.getUUID(), Math.min(bloom, 8f));
    }

    /** Accumule la chaleur du plasma ; retourne true si l'arme vient de surchauffer. */
    private static boolean overheat(ServerPlayer player, ItemStack stack) {
        float heat = WEAPON_HEAT.getOrDefault(player.getUUID(), 0f) + 40f;
        if (heat >= 100f) {
            WEAPON_HEAT.put(player.getUUID(), 0f);
            player.getCooldowns().addCooldown(stack, 40);
            ServerLevel level = player.level();
            Vec3 eye = player.getEyePosition();
            level.sendParticles(ParticleTypes.LARGE_SMOKE, eye.x, eye.y, eye.z, 12, 0.25, 0.25, 0.25, 0.05);
            level.sendParticles(ParticleTypes.FLAME, eye.x, eye.y, eye.z, 8, 0.15, 0.15, 0.15, 0.04);
            level.playSound(null, player.blockPosition(), ModSounds.DRY_FIRE, SoundSource.PLAYERS, 0.9f, 0.5f);
            return true;
        }
        WEAPON_HEAT.put(player.getUUID(), heat);
        return false;
    }

    /** Dissipation du bloom et de la chaleur (appelé chaque tick serveur). */
    public static void tickWeaponState(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Float>> bit = SPREAD_BLOOM.entrySet().iterator();
        while (bit.hasNext()) {
            Map.Entry<UUID, Float> e = bit.next();
            float v = e.getValue() - 0.2f;
            if (v <= 0f) bit.remove(); else e.setValue(v);
        }
        Iterator<Map.Entry<UUID, Float>> hit = WEAPON_HEAT.entrySet().iterator();
        while (hit.hasNext()) {
            Map.Entry<UUID, Float> e = hit.next();
            float v = e.getValue() - 2f;
            if (v <= 0f) hit.remove(); else e.setValue(v);
        }
    }

    // ------------------------------------------------------------------
    // Explosion de zone (sans casser de blocs — terrain_griefing = false)
    // ------------------------------------------------------------------

    /** Poussière verte des explosions plasma. */
    private static final DustParticleOptions PLASMA_EXPLOSION =
            new DustParticleOptions(ARGB.color(80, 255, 160), 1.8f);

    public static void explode(ServerLevel level, Vec3 pos, float radius, float damage, Entity owner, String family) {
        switch (family) {
            case "plasma" -> {
                // Explosion plasma : boule d'énergie verte + flammes.
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 2, 0.2, 0.2, 0.2, 0.05);
                level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, (int) (radius * 6),
                        radius * 0.3, radius * 0.3, radius * 0.3, 0.05);
                level.sendParticles(PLASMA_EXPLOSION, pos.x, pos.y, pos.z, (int) (radius * 14),
                        radius * 0.4, radius * 0.4, radius * 0.4, 0.05);
            }
            case "explosive" -> {
                // Missile : explosion massive orange + fumée.
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 6, 0.4, 0.4, 0.4, 0.08);
                level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, (int) (radius * 10),
                        radius * 0.4, radius * 0.4, radius * 0.4, 0.06);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, (int) (radius * 4),
                        radius * 0.5, radius * 0.5, radius * 0.5, 0.04);
            }
            default -> {
                // Bolt (détonation différée) : étincelles + fumée légère.
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 2, 0.2, 0.2, 0.2, 0.05);
                level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, (int) (radius * 4),
                        radius * 0.2, radius * 0.2, radius * 0.2, 0.04);
            }
        }

        DamageSource source = level.damageSources().generic();
        if (owner instanceof ServerPlayer sp) source = level.damageSources().playerAttack(sp);
        else if (owner instanceof LivingEntity le) source = level.damageSources().mobAttack(le);

        double r2 = radius * radius;
        for (LivingEntity t : level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
                e -> e != owner && e.isAlive() && e.distanceToSqr(pos) <= r2)) {
            double d = Math.max(1.0, t.position().distanceTo(pos));
            float falloff = (float) Math.max(0.3, 1.0 - d / (radius + 1.0));
            t.hurtServer(level, source, damage * falloff);
        }
    }

    // ------------------------------------------------------------------
    // Rechargement
    // ------------------------------------------------------------------

    public static boolean isReloading(ServerPlayer player) {
        return ACTIVE_RELOADS.containsKey(player.getUUID()) || SHELL_RELOADS.containsKey(player.getUUID());
    }

    public static void startReload(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof WeaponItem weapon)) return;
        WeaponDefinition def = weapon.definition();
        if (def.ammo() == null) return;                     // mêlée
        if (getAmmo(stack) >= def.magazineSize()) return;    // déjà plein
        if (isReloading(player)) return;

        if (def.perShellReload()) {
            if (countRounds(player, def.ammo()) <= 0) return;
            SHELL_RELOADS.put(player.getUUID(), Math.max(1, Math.round(def.secondsPerShell() * 20f)));
            marquerRechargement(stack, Math.max(1, Math.round(def.secondsPerShell() * 20f)));
        } else {
            if (countRounds(player, def.ammo()) <= 0) return;
            ACTIVE_RELOADS.put(player.getUUID(), def.reloadTicks());
            marquerRechargement(stack, def.reloadTicks());
        }
        RELOAD_WEAPON.put(player.getUUID(), weapon);
    }

    public static void tickReloads(MinecraftServer server) {
        // Mag complet
        Iterator<Map.Entry<UUID, Integer>> it = ACTIVE_RELOADS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null || !(player.getMainHandItem().getItem() instanceof WeaponItem)) {
                it.remove();
                RELOAD_WEAPON.remove(entry.getKey());
                continue;
            }
            ItemStack held = player.getMainHandItem();
            // annule le rechargement si le joueur a changé d'arme
            if (RELOAD_WEAPON.get(entry.getKey()) != held.getItem()) {
                it.remove();
                RELOAD_WEAPON.remove(entry.getKey());
                continue;
            }
            int ticks = entry.getValue() - 1;
            if (ticks <= 0) {
                finishFullReload(player, held);
                it.remove();
                RELOAD_WEAPON.remove(entry.getKey());
            } else {
                entry.setValue(ticks);
            }
        }

        // Cartouche par cartouche (fusil à pompe)
        Iterator<Map.Entry<UUID, Integer>> sit = SHELL_RELOADS.entrySet().iterator();
        while (sit.hasNext()) {
            Map.Entry<UUID, Integer> entry = sit.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null || !(player.getMainHandItem().getItem() instanceof WeaponItem)) {
                sit.remove();
                RELOAD_WEAPON.remove(entry.getKey());
                continue;
            }
            ItemStack held = player.getMainHandItem();
            // annule le rechargement si le joueur a changé d'arme
            if (RELOAD_WEAPON.get(entry.getKey()) != held.getItem()) {
                sit.remove();
                RELOAD_WEAPON.remove(entry.getKey());
                continue;
            }
            WeaponDefinition def = ((WeaponItem) held.getItem()).definition();
            int ticks = entry.getValue() - 1;
            if (ticks <= 0) {
                int ammo = getAmmo(held);
                int needed = def.magazineSize() - ammo;
                if (needed > 0 && countRounds(player, def.ammo()) > 0) {
                    removeRounds(player, 1, def.ammo());
                    setAmmo(held, ammo + 1);
                    player.level().playSound(null, player.blockPosition(), ModSounds.RELOAD,
                            SoundSource.PLAYERS, 0.8f, 1.0f);
                    if (getAmmo(held) < def.magazineSize() && countRounds(player, def.ammo()) > 0) {
                        entry.setValue(Math.max(1, Math.round(def.secondsPerShell() * 20f)));
                    } else {
                        sit.remove();
                        RELOAD_WEAPON.remove(entry.getKey());
                    }
                } else {
                    sit.remove();
                    RELOAD_WEAPON.remove(entry.getKey());
                }
            } else {
                entry.setValue(ticks);
            }
        }
    }

    private static void finishFullReload(ServerPlayer player, ItemStack stack) {
        WeaponDefinition def = ((WeaponItem) stack.getItem()).definition();
        int ammo = getAmmo(stack);
        int needed = def.magazineSize() - ammo;
        int take = Math.min(needed, countRounds(player, def.ammo()));
        removeRounds(player, take, def.ammo());
        setAmmo(stack, ammo + take);
        player.level().playSound(null, player.blockPosition(), ModSounds.RELOAD,
                SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    // ------------------------------------------------------------------
    // Munitions (Data Component + inventaire)
    // ------------------------------------------------------------------

    private static int getAmmo(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.AMMO, 0);
    }

    private static void setAmmo(ItemStack stack, int ammo) {
        stack.set(ModDataComponents.AMMO, ammo);
    }

    private static int countRounds(ServerPlayer player, Item ammo) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() == ammo) total += stack.getCount();
        }
        return total;
    }

    private static void removeRounds(ServerPlayer player, int amount, Item ammo) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() == ammo) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
    }
}
