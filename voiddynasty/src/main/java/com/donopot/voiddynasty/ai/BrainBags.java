package com.donopot.voiddynasty.ai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.donopot.voiddynasty.work.WorkInventory;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Récupération des sacs d'un compagnon mort ou passé dans une autre dimension.
 *
 * <p>Le sac personnel vit dans les données du <b>niveau</b> qui le contient : un servitor qui meurt,
 * ou qui change de dimension, laisse ses objets dans le monde qu'il vient de quitter. Sans registre
 * extérieur, ces objets sont inaccessibles à jamais — c'est le trou que comble cette classe.</p>
 *
 * <p>Un <b>registre</b> unique vit dans l'overworld, donc indépendant de la dimension et des
 * redémarrages. Il contient des sacs « en attente » (stacks complets, composants compris).</p>
 *
 * <p>Transfert en deux temps, pour ne jamais perdre ni dupliquer :</p>
 * <ol>
 *   <li>l'entrée est écrite dans le registre en état {@code PENDING_CLEAR} ;</li>
 *   <li>le sac d'origine est vidé ;</li>
 *   <li>l'entrée passe en {@code ACTIVE}.</li>
 * </ol>
 * <p>Si le serveur tombe entre 1 et 2, le sac d'origine est encore plein : à la reprise, l'entrée
 * {@code PENDING_CLEAR} est supprimée (elle doublonnerait). Si le serveur tombe entre 2 et 3, le sac
 * d'origine est vide : l'entrée devient {@code ACTIVE} (rien n'est perdu).</p>
 */
public final class BrainBags {

    /** Emplacements de l'inventaire principal d'un joueur pris en compte pour une restitution. */
    public static final int PLAYER_SLOTS = 36;
    static final String ACTIVE = "ACTIVE";
    static final String PENDING = "PENDING_CLEAR";

    private BrainBags() {}

    /** Un sac en attente de restitution. */
    public record Entry(String id, UUID owner, String dimension, double x, double y, double z,
                        String reason, long epochMillis, String state, List<ItemStack> stacks) {
        public boolean active() {
            return ACTIVE.equals(state);
        }

        public int count() {
            return stacks.stream().mapToInt(ItemStack::getCount).sum();
        }
    }

    private static final Codec<List<ItemStack>> STACKS = ItemStack.CODEC.listOf();
    private static final Codec<Entry> ENTRY = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(Entry::id),
            Codec.STRING.fieldOf("owner").forGetter(e -> e.owner().toString()),
            Codec.STRING.fieldOf("dimension").forGetter(Entry::dimension),
            Codec.DOUBLE.fieldOf("x").forGetter(Entry::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Entry::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Entry::z),
            Codec.STRING.fieldOf("reason").forGetter(Entry::reason),
            Codec.LONG.fieldOf("time").forGetter(Entry::epochMillis),
            Codec.STRING.fieldOf("state").forGetter(Entry::state),
            STACKS.fieldOf("stacks").forGetter(Entry::stacks)
    ).apply(i, (id, owner, dim, x, y, z, reason, time, state, stacks) -> new Entry(
            id, UUID.fromString(owner), dim, x, y, z, reason, time, state, List.copyOf(stacks))));

    /** Registre persistant, attaché à l'overworld : une seule copie pour tout le serveur. */
    public static final class Ledger extends SavedData {
        final Map<String, Entry> entries = new LinkedHashMap<>();
        long nextId = 1;

        static Ledger restore(List<Entry> saved, long next) {
            Ledger l = new Ledger();
            for (Entry e : saved) l.entries.put(e.id(), e);
            l.nextId = Math.max(next, saved.size() + 1L);
            return l;
        }

        String next() {
            return "bag-" + (nextId++);
        }

        public List<Entry> all() {
            return List.copyOf(entries.values());
        }

        public Entry get(String id) {
            return id == null ? null : entries.get(id.trim().toLowerCase(Locale.ROOT));
        }
    }

    private static final Codec<Ledger> CODEC = RecordCodecBuilder.create(i -> i.group(
            ENTRY.listOf().fieldOf("bags").forGetter(l -> List.copyOf(l.entries.values())),
            Codec.LONG.optionalFieldOf("next_id", 1L).forGetter(l -> l.nextId)
    ).apply(i, Ledger::restore));

    public static final SavedDataType<Ledger> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("voiddynasty", "brain_bag_ledger"),
            Ledger::new, CODEC, DataFixTypes.SAVED_DATA_FORCED_CHUNKS);

    public static Ledger ledger(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static void init() {
        // Mort du compagnon : son sac quitte le niveau et rejoint le registre, intact.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!BrainSpawn.isBrainServitor(entity)) return;
            if (!(entity.level() instanceof ServerLevel deathLevel)) return;
            if (BrainInv.of(entity).isEmpty()) return;
            stash(deathLevel, entity, "MORT");
        });
        // Changement de dimension : le sac suit le compagnon au lieu de rester derrière lui.
        ServerEntityLevelChangeEvents.AFTER_ENTITY_CHANGE_LEVEL.register(
                (original, replacement, origin, destination) -> {
                    if (!BrainSpawn.isBrainServitor(original)) return;
                    migrate(origin, destination, original.getUUID());
                });
        // Après un arrêt, on réconcilie les transferts interrompus avant de servir la moindre lecture.
        ServerLifecycleEvents.SERVER_STARTED.register(BrainBags::reconcile);
    }

    // ------------------------------------------------------------------ transferts

    /**
     * Retire le sac du compagnon de son niveau et le dépose dans le registre.
     *
     * @return l'identifiant du sac en attente, ou {@code null} si le sac était vide.
     */
    public static String stash(ServerLevel level, LivingEntity bot, String reason) {
        if (level == null || bot == null) return null;
        List<ItemStack> stacks = BrainInv.of(bot);
        if (stacks.isEmpty()) return null;
        MinecraftServer server = level.getServer();
        if (server == null) return null;

        Ledger ledger = ledger(server);
        String id = ledger.next();
        Vec3 pos = bot.position();
        Entry pending = new Entry(id, bot.getUUID(), level.dimension().identifier().toString(),
                pos.x, pos.y, pos.z, reason, System.currentTimeMillis(), PENDING,
                WorkInventory.copy(stacks));
        ledger.entries.put(id, pending);
        ledger.setDirty();                       // 1. l'entrée existe avant que le sac disparaisse

        BrainInventoryState.get(level).replace(bot.getUUID(), List.of());   // 2. le sac d'origine est vidé

        ledger.entries.put(id, new Entry(id, pending.owner(), pending.dimension(), pending.x(),
                pending.y(), pending.z(), pending.reason(), pending.epochMillis(), ACTIVE,
                pending.stacks()));
        ledger.setDirty();                       // 3. transfert complet
        return id;
    }

    /** Déplace le sac d'une dimension à l'autre ; en cas de place insuffisante, il va au registre. */
    static void migrate(ServerLevel origin, ServerLevel destination, UUID owner) {
        if (origin == null || destination == null || origin == destination) return;
        List<ItemStack> stacks = BrainInventoryState.get(origin).bag(owner);
        if (stacks.isEmpty()) return;

        List<ItemStack> merged = WorkInventory.insert(
                BrainInventoryState.get(destination).bag(owner), BrainInv.SLOTS, stacks);
        if (merged == null) {
            // Le sac de destination est déjà chargé : on conserve tout dans le registre plutôt que
            // d'écraser quoi que ce soit.
            MinecraftServer server = destination.getServer();
            if (server == null) return;
            Ledger ledger = ledger(server);
            String id = ledger.next();
            ledger.entries.put(id, new Entry(id, owner, origin.dimension().identifier().toString(),
                    0, 0, 0, "DIMENSION_SAC_PLEIN", System.currentTimeMillis(), PENDING,
                    WorkInventory.copy(stacks)));
            ledger.setDirty();
            BrainInventoryState.get(origin).replace(owner, List.of());
            Entry e = ledger.entries.get(id);
            ledger.entries.put(id, new Entry(id, e.owner(), e.dimension(), e.x(), e.y(), e.z(),
                    e.reason(), e.epochMillis(), ACTIVE, e.stacks()));
            ledger.setDirty();
            return;
        }
        BrainInventoryState.get(destination).replace(owner, merged);
        BrainInventoryState.get(origin).replace(owner, List.of());
    }

    /**
     * Réconcilie les transferts interrompus par un arrêt brutal.
     * Une entrée {@code PENDING_CLEAR} dont le sac d'origine est encore plein n'a jamais eu lieu.
     */
    public static String reconcile(MinecraftServer server) {
        Ledger ledger = ledger(server);
        List<String> dropped = new ArrayList<>();
        for (Entry e : new ArrayList<>(ledger.entries.values())) {
            if (e.active()) continue;
            ServerLevel level = levelOf(server, e.dimension());
            boolean stillThere = level != null && !BrainInventoryState.get(level).bag(e.owner()).isEmpty();
            if (stillThere) {
                ledger.entries.remove(e.id());     // le vidage n'a pas eu lieu : l'entrée doublonnerait
                dropped.add(e.id());
            } else {
                ledger.entries.put(e.id(), new Entry(e.id(), e.owner(), e.dimension(), e.x(), e.y(),
                        e.z(), e.reason(), e.epochMillis(), ACTIVE, e.stacks()));
                dropped.add(e.id() + "(active)");
            }
        }
        if (!dropped.isEmpty()) ledger.setDirty();
        return dropped.isEmpty() ? "aucun transfert interrompu" : "reconcilie : " + String.join(", ", dropped);
    }

    // ------------------------------------------------------------------ lecture

    public static List<Entry> list(MinecraftServer server) {
        return ledger(server).all().stream().filter(Entry::active).toList();
    }

    public static String describe(MinecraftServer server) {
        List<Entry> all = list(server);
        if (all.isEmpty()) return "aucun sac en attente";
        StringBuilder sb = new StringBuilder(all.size() + " sac(s) en attente :");
        for (Entry e : all) {
            sb.append("\n[").append(e.id()).append("] ").append(e.reason())
                    .append(" — ").append(e.count()).append(" objet(s), ")
                    .append(e.stacks().size()).append(" pile(s) — ")
                    .append(e.dimension()).append(" en (").append((int) e.x()).append(", ")
                    .append((int) e.y()).append(", ").append((int) e.z()).append(")");
        }
        return sb.toString();
    }

    /** Tout ce qui existe pour un propriétaire donné : sacs vivants de chaque dimension + registre. */
    public static String audit(MinecraftServer server, UUID owner) {
        StringBuilder sb = new StringBuilder("audit sacs");
        if (owner != null) sb.append(" pour ").append(owner);
        for (ServerLevel level : server.getAllLevels()) {
            List<ItemStack> stacks = owner == null ? List.of() : BrainInventoryState.get(level).bag(owner);
            if (!stacks.isEmpty()) {
                sb.append("\n  vivant ").append(level.dimension().identifier())
                        .append(" : ").append(count(stacks)).append(" objet(s)");
            }
        }
        for (Entry e : list(server)) {
            if (owner == null || e.owner().equals(owner)) {
                sb.append("\n  registre ").append(e.id()).append(" (").append(e.reason()).append(") : ")
                        .append(count(e.stacks())).append(" objet(s) depuis ").append(e.dimension());
            }
        }
        if (sb.indexOf("\n") < 0) sb.append(" : rien");
        return sb.toString();
    }

    // ------------------------------------------------------------------ restitution

    /** Restitue un sac dans le sac personnel du compagnon vivant. Tout ou rien. */
    public static String restoreToBot(MinecraftServer server, String id, LivingEntity bot) {
        Ledger ledger = ledger(server);
        Entry e = ledger.get(id);
        if (e == null || !e.active()) return "sac introuvable ou incomplet : " + id;
        if (bot == null) return "aucun servitor en jeu";

        List<ItemStack> merged = WorkInventory.insert(BrainInv.of(bot), BrainInv.SLOTS, e.stacks());
        if (merged == null) {
            return "refus : le sac du servitor n'a pas la place pour " + e.count()
                    + " objet(s) — rien n'a été déplacé";
        }
        BrainInventoryState.get((ServerLevel) bot.level()).replace(bot.getUUID(), merged);
        ledger.entries.remove(e.id());
        ledger.setDirty();
        return "sac " + e.id() + " restitue au servitor (" + e.count() + " objet(s))";
    }

    /** Restitue un sac dans l'inventaire du joueur. Tout ou rien : ni perte ni doublon. */
    public static String giveToPlayer(MinecraftServer server, String id, ServerPlayer player) {
        Ledger ledger = ledger(server);
        Entry e = ledger.get(id);
        if (e == null || !e.active()) return "sac introuvable ou incomplet : " + id;
        if (player == null) return "joueur introuvable";

        List<ItemStack> current = new ArrayList<>();
        for (int i = 0; i < PLAYER_SLOTS; i++) current.add(player.getInventory().getItem(i).copy());
        List<ItemStack> merged = WorkInventory.insert(current, PLAYER_SLOTS, e.stacks());
        if (merged == null) {
            return "refus : inventaire du joueur plein pour " + e.count()
                    + " objet(s) — rien n'a été déplacé";
        }
        for (int i = 0; i < PLAYER_SLOTS; i++) player.getInventory().setItem(i, ItemStack.EMPTY);
        int slot = 0;
        for (ItemStack stack : merged) {
            if (slot >= PLAYER_SLOTS) break;
            player.getInventory().setItem(slot++, stack);
        }
        ledger.entries.remove(e.id());
        ledger.setDirty();
        return "sac " + e.id() + " donne a " + player.getName().getString()
                + " (" + e.count() + " objet(s))";
    }

    /**
     * Fait tomber le sac au sol, au point indiqué. Seules les piles réellement créées sont
     * débitées du registre : une pile dont l'apparition échoue reste disponible.
     */
    public static String dropAt(MinecraftServer server, String id, ServerLevel level, Vec3 pos) {
        Ledger ledger = ledger(server);
        Entry e = ledger.get(id);
        if (e == null || !e.active()) return "sac introuvable ou incomplet : " + id;
        if (level == null || pos == null) return "position de depot invalide";

        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : e.stacks()) {
            ItemEntity entity = new ItemEntity(level, pos.x, pos.y, pos.z, stack.copy());
            if (level.addFreshEntity(entity)) continue;
            remaining.add(stack.copy());
        }
        int dropped = count(e.stacks()) - count(remaining);
        if (remaining.isEmpty()) {
            ledger.entries.remove(e.id());
        } else {
            ledger.entries.put(e.id(), new Entry(e.id(), e.owner(), e.dimension(), e.x(), e.y(), e.z(),
                    e.reason(), e.epochMillis(), ACTIVE, remaining));
        }
        ledger.setDirty();
        return "sac " + e.id() + " depose au sol (" + dropped + " objet(s))"
                + (remaining.isEmpty() ? "" : " — " + count(remaining) + " objet(s) restent au registre");
    }

    // ------------------------------------------------------------------ outils

    static ServerLevel levelOf(MinecraftServer server, String dimension) {
        if (server == null || dimension == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().identifier().toString().equals(dimension)) return level;
        }
        return null;
    }

    static int count(List<ItemStack> stacks) {
        return stacks.stream().mapToInt(ItemStack::getCount).sum();
    }
}
