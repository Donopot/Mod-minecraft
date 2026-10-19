package com.donopot.voiddynasty.work;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Crafts : on n'apprend pas les recettes une par une au compagnon.
 *
 * <p>Le serveur connait deja tout le livre de recettes ({@code RecipeManager}). Ce service le lit,
 * puis execute une recette a partir du contenu d'un coffre en verifiant la station requise :
 * grille 2x2 pour les recettes de poche, table de craft a portee pour la grille 3x3.</p>
 *
 * <p>Les recettes speciales (teintures, bannieres, feux d'artifice) ne sont pas executables ici :
 * elles sont ignorees a la lecture, pas inventees a l'execution.</p>
 */
public final class CraftService {

    private static final int STATION_RADIUS = 5;
    public static final int MAX_BATCH = 4096;

    private CraftService() {}

    /** Une recette resolue : ce qu'elle donne, sur quelle grille, et ce qu'elle consomme par lot. */
    private record Plan(String id, ItemStack result, int width, int height, List<Ingredient> cells) {
        boolean needsTable() { return width > 2 || height > 2; }
        String station() { return needsTable() ? "table" : "bag"; }
        int perBatch() { return cells.size(); }
    }

    // ------------------------------------------------------------------ lecture

    public static JsonObject recipes(ServerLevel level, String filter, int limit) {
        String needle = filter == null ? "" : filter.trim().toLowerCase();
        JsonArray out = new JsonArray();
        int total = 0;
        for (RecipeHolder<?> holder : level.getServer().getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof CraftingRecipe recipe)) continue;
            Plan plan = plan(holder.id().identifier().toString(), recipe);
            if (plan == null) continue;
            String resultId = BuiltInRegistries.ITEM.getKey(plan.result().getItem()).toString();
            if (!needle.isEmpty() && !resultId.contains(needle) && !plan.id().contains(needle)) continue;
            total++;
            if (out.size() >= limit) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("recipe", plan.id());
            entry.addProperty("result", resultId);
            entry.addProperty("result_count", plan.result().getCount());
            entry.addProperty("station", plan.station());
            entry.addProperty("grid", plan.width() + "x" + plan.height());
            entry.add("ingredients", ingredientsJson(plan));
            out.add(entry);
        }
        JsonObject json = new JsonObject();
        json.addProperty("ok", true);
        json.addProperty("matched", total);
        json.addProperty("returned", out.size());
        json.addProperty("scope", "Livre de recettes du serveur ; station bag = grille 2x2, table = grille 3x3");
        json.add("recipes", out);
        return json;
    }

    // ------------------------------------------------------------------ execution

    public static JsonObject craft(ServerLevel level, LivingEntity bot, BlockPos chestPos, String item, int count) {
        if (bot == null) return error("NO_SERVITOR", "Aucun servitor dans cette dimension");
        if (count < 1 || count > MAX_BATCH) return error("INVALID_ARGUMENT", "count must be 1.." + MAX_BATCH);
        Container chest = container(level, chestPos);
        if (chest == null) return error("INVALID_CHEST", "Loaded, unprotected chest or barrel required at " + chestPos.toShortString());

        String wanted = item.contains(":") ? item : "minecraft:" + item;
        Plan best = null;
        for (RecipeHolder<?> holder : level.getServer().getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof CraftingRecipe recipe)) continue;
            Plan plan = plan(holder.id().identifier().toString(), recipe);
            if (plan == null) continue;
            if (!BuiltInRegistries.ITEM.getKey(plan.result().getItem()).toString().equals(wanted)) continue;
            // Preference : la recette la moins gourmande, puis la plus petite grille.
            if (best == null || plan.perBatch() < best.perBatch()
                    || (plan.perBatch() == best.perBatch() && plan.width() * plan.height() < best.width() * best.height())) {
                best = plan;
            }
        }
        if (best == null) return error("NO_RECIPE", "Aucune recette de craft ne produit " + wanted
                + " (recette speciale ou item non craftable : four ? pierre taillee ?)");
        Plan plan = best;

        if (plan.needsTable() && !craftingTableNear(level, bot.blockPosition())) {
            return error("NEEDS_CRAFTING_TABLE", "La recette " + plan.id() + " utilise une grille "
                    + plan.width() + "x" + plan.height() + " : une table de craft doit etre a moins de "
                    + STATION_RADIUS + " blocs du compagnon");
        }

        int batches = (count + plan.result().getCount() - 1) / plan.result().getCount();
        List<ItemStack> work = new ArrayList<>();
        for (int i = 0; i < chest.getContainerSize(); i++) work.add(chest.getItem(i).copy());

        // Ce qu'il faudrait, en signalant precisement ce qui manque.
        JsonArray missing = new JsonArray();
        List<ItemStack> trial = WorkInventory.copy(work);
        for (int batch = 0; batch < batches; batch++) {
            for (Ingredient cell : plan.cells()) {
                int need = 1;
                int have = 0;
                for (ItemStack slot : trial) {
                    if (!slot.isEmpty() && cell.test(slot)) { have += slot.getCount(); }
                }
                int taken = 0;
                for (ItemStack slot : trial) {
                    if (taken >= need) break;
                    if (slot.isEmpty() || !cell.test(slot)) continue;
                    int take = Math.min(need - taken, slot.getCount());
                    slot.shrink(take);
                    taken += take;
                }
                if (taken < need) {
                    JsonObject miss = new JsonObject();
                    miss.addProperty("ingredient", describe(cell));
                    miss.addProperty("missing", need - taken);
                    miss.addProperty("available_before", have);
                    missing.add(miss);
                }
            }
            if (missing.size() > 0) break;
        }
        if (missing.size() > 0) {
            JsonObject json = error("MISSING_INGREDIENTS", "Il manque des ingredients dans le coffre "
                    + chestPos.toShortString() + " : fabrique d'abord les ingredients intermediaires");
            json.add("missing", missing);
            json.addProperty("recipe", plan.id());
            return json;
        }

        List<ItemStack> debit = WorkInventory.copy(work);
        for (int batch = 0; batch < batches; batch++) {
            for (Ingredient cell : plan.cells()) {
                if (!consume(debit, cell, 1)) {
                    return error("MISSING_INGREDIENTS", "Ingredient introuvable au debit : " + describe(cell));
                }
            }
        }

        int produced = batches * plan.result().getCount();
        ItemStack result = plan.result().copyWithCount(produced);
        List<ItemStack> after = WorkInventory.insert(debit, chest.getContainerSize(), List.of(result));
        if (after == null) return error("CHEST_FULL", "Pas assez de place dans le coffre pour " + produced
                + " " + BuiltInRegistries.ITEM.getKey(result.getItem()));

        for (int i = 0; i < chest.getContainerSize(); i++) chest.setItem(i, i < after.size() ? after.get(i) : ItemStack.EMPTY);
        chest.setChanged();

        JsonObject json = new JsonObject();
        json.addProperty("ok", true);
        json.addProperty("recipe", plan.id());
        json.addProperty("station", plan.station());
        json.addProperty("crafted", produced);
        json.addProperty("item", BuiltInRegistries.ITEM.getKey(result.getItem()).toString());
        json.addProperty("batches", batches);
        json.add("chest", MissionService.positionJson(chestPos));
        JsonArray consumed = new JsonArray();
        for (int batch = 0; batch < batches; batch++) {
            for (Ingredient cell : plan.cells()) {
                consumed.add(describe(cell));
            }
        }
        json.add("consumed", consumed);
        return json;
    }


    // ------------------------------------------------------------------ plan d'approvisionnement

    private static final int MAX_DEPTH = 4;
    private static final int MAX_NODES = 24;

    /**
     * Arbre de dependances pour fabriquer {@code count} de {@code item} : ce qui est deja dans le
     * coffre, ce qu'il faut fabriquer et dans quel ordre, et pour chaque matiere premiere manquante
     * les blocs sources presents autour du compagnon. Aucun bloc n'est casse ici.
     */
    public static JsonObject materials(ServerLevel level, LivingEntity bot, BlockPos chestPos,
                                       String item, int count, int radius) {
        if (bot == null) return error("NO_SERVITOR", "Aucun servitor dans cette dimension");
        if (count < 1 || count > MAX_BATCH) return error("INVALID_ARGUMENT", "count must be 1.." + MAX_BATCH);
        int r = Math.max(4, Math.min(32, radius));
        Container chest = container(level, chestPos);
        if (chest == null) return error("INVALID_CHEST", "Loaded, unprotected chest or barrel required at " + chestPos.toShortString());

        String wanted = item.contains(":") ? item : "minecraft:" + item;
        Map<String, Integer> have = new LinkedHashMap<>();
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.isEmpty()) continue;
            have.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
        }
        Map<String, Integer> left = new LinkedHashMap<>(have);
        Map<String, Integer> raw = new LinkedHashMap<>();
        List<JsonObject> steps = new ArrayList<>();
        Set<String> nodes = new LinkedHashSet<>();
        Map<String, List<String>> alternatives = new LinkedHashMap<>();
        String failure = expand(level, wanted, count, 0, left, raw, steps, nodes, alternatives);
        if (failure != null) return error("NO_RECIPE", failure);

        // Les fontes identiques sont regroupees : une seule mission de four pour N lingots.
        Map<String, JsonObject> merged = new LinkedHashMap<>();
        List<JsonObject> ordered = new ArrayList<>();
        for (JsonObject step : steps) {
            boolean smelt = step.has("kind") && "smelt".equals(step.get("kind").getAsString());
            String key = step.get("craft").getAsString();
            if (smelt && merged.containsKey(key)) {
                JsonObject first = merged.get(key);
                int times = first.get("times").getAsInt() + step.get("times").getAsInt();
                first.addProperty("times", times);
                first.addProperty("produces", first.get("produces").getAsInt() + step.get("produces").getAsInt());
                long ticks = first.get("cooking_ticks").getAsLong() + step.get("cooking_ticks").getAsLong();
                first.addProperty("cooking_ticks", ticks);
                continue;
            }
            if (smelt) merged.put(key, step);
            ordered.add(step);
        }

        JsonArray stepArray = new JsonArray();
        // L'ordre d'insertion est deja celui de l'execution : ingredients avant l'item final.
        for (JsonObject step : ordered) stepArray.add(step);

        BlockPos center = bot.blockPosition();
        // La boite de recherche doit rester sous la limite d'observation du mod : on la reduit si besoin.
        BlockPos min;
        BlockPos max;
        while (true) {
            min = new BlockPos(center.getX() - r, Math.max(level.getMinY(), center.getY() - 16), center.getZ() - r);
            max = new BlockPos(center.getX() + r, Math.min(level.getMaxY(), center.getY() + 8), center.getZ() + r);
            if (boxVolume(min, max) <= 32000 || r <= 4) break;
            r--;
        }
        JsonArray missing = new JsonArray();
        // Pret = plus une seule etape a executer (les etapes disent deja ce qui manque a fabriquer
        // ou a recolter : un ingredient disponible ne cree pas d'etape).
        boolean ready = steps.isEmpty();
        for (Map.Entry<String, Integer> entry : raw.entrySet()) {
            int shortfall = Math.max(0, entry.getValue());
            if (shortfall == 0) continue;
            JsonObject miss = new JsonObject();
            miss.addProperty("item", entry.getKey());
            miss.addProperty("missing", shortfall);
            miss.addProperty("in_chest", have.getOrDefault(entry.getKey(), 0));
            Set<String> sources = MissionService.SOURCES.get(entry.getKey());
            if (sources == null) {
                miss.addProperty("gatherable", false);
                miss.addProperty("note", "Aucune mission de recolte pour cet item : il doit venir du joueur (coffre, echange, butin)");
            } else {
                miss.addProperty("gatherable", true);
                JsonArray blocks = new JsonArray();
                sources.forEach(blocks::add);
                miss.add("source_blocks", blocks);
                miss.add("search_box", searchBox(level, min, max));
                JsonObject found = WorkObservation.inspect(level, findRequest(entry.getKey(), min, max));
                if (found.get("ok").getAsBoolean()) {
                    miss.addProperty("found_in_box", found.get("matches_total").getAsInt());
                    miss.addProperty("absence_proven", found.has("conclusive_absence") && found.get("conclusive_absence").getAsBoolean());
                    // Rien trouve pour cette variante : on essaie les autres items acceptes par la recette
                    // (n'importe quelle buche convient pour faire du charbon de bois).
                    if (found.get("matches_total").getAsInt() == 0 && alternatives.containsKey(entry.getKey())) {
                        for (String alt : alternatives.get(entry.getKey())) {
                            if (alt.equals(entry.getKey()) || !MissionService.SOURCES.containsKey(alt)) continue;
                            JsonObject other = WorkObservation.inspect(level, findRequest(alt, min, max));
                            if (!other.get("ok").getAsBoolean() || other.get("matches_total").getAsInt() == 0) continue;
                            miss.addProperty("alternative_input", alt);
                            JsonArray altBlocks = new JsonArray();
                            MissionService.SOURCES.get(alt).forEach(altBlocks::add);
                            miss.add("source_blocks", altBlocks);
                            miss.addProperty("found_in_box", other.get("matches_total").getAsInt());
                            miss.addProperty("absence_proven", false);
                            found = other;
                            break;
                        }
                    }
                    // Zone de recolte prete a l'emploi : la boite des premiers blocs trouves, sans plus.
                    if (found.has("matches") && found.getAsJsonArray("matches").size() > 0) {
                        JsonArray zone = suggestedZone(found.getAsJsonArray("matches"));
                        if (zone != null) miss.add("suggested_zone", zone);
                    }
                } else {
                    miss.addProperty("find_error", String.valueOf(found.get("detail")));
                }
            }
            missing.add(miss);
        }

        JsonObject json = new JsonObject();
        json.addProperty("ok", true);
        json.addProperty("item", wanted);
        json.addProperty("want", count);
        json.addProperty("ready", ready);
        json.addProperty("station", steps.isEmpty() ? "aucune" : "voir etapes");
        json.add("steps", stepArray);
        json.add("in_chest", mapJson(have));
        json.add("to_gather", missing);
        json.add("fuels_in_chest", SmeltService.fuelEntries(level, chest));
        json.addProperty("rule", "Une etape = un appel servitor_craft ou servitor_gather, puis verification du coffre. Aucun craft ne devine ses ingredients.");
        return json;
    }

    private static String expand(ServerLevel level, String item, int qty, int depth, Map<String, Integer> left,
                                 Map<String, Integer> raw, List<JsonObject> steps, Set<String> nodes,
                                 Map<String, List<String>> alternatives) {
        if (qty <= 0) return null;
        int available = left.getOrDefault(item, 0);
        int taken = Math.min(available, qty);
        if (taken > 0) left.put(item, available - taken);
        int remaining = qty - taken;
        if (remaining == 0) return null;

        // Ce que le compagnon sait recolter est une matiere premiere : on ne le fabrique pas.
        if (MissionService.SOURCES.containsKey(item)) {
            raw.merge(item, remaining, Integer::sum);
            return null;
        }

        // La fonte d'abord quand elle existe (minerai -> lingot), sinon un craft, sinon une matiere premiere.
        if (depth < MAX_DEPTH && nodes.size() < MAX_NODES && nodes.add(item)) {
            SmeltService.Plan smelt = SmeltService.plan(level, item);
            if (smelt != null) {
                int batches = (remaining + smelt.outputCount() - 1) / smelt.outputCount();
                JsonObject step = new JsonObject();
                step.addProperty("kind", "smelt");
                step.addProperty("craft", item);
                step.addProperty("times", batches);
                step.addProperty("produces", batches * smelt.outputCount());
                step.addProperty("station", "four");
                step.addProperty("cooking_ticks", (long) smelt.cookingTime() * batches);
                JsonArray needs = new JsonArray();
                needs.add(smelt.input());
                step.add("needs", needs);
                step.addProperty("input", smelt.input());
                JsonArray accepts = new JsonArray();
                smelt.accepts().forEach(accepts::add);
                step.add("accepts", accepts);
                step.addProperty("fuel", "carburant du meme coffre (voir fuels_in_chest)");
                alternatives.put(smelt.input(), smelt.accepts());
                // Le coffre peut contenir n'importe quel item accepte par la recette (toutes les
                // essences de buches pour du charbon de bois) : on consomme d'abord ceux-la.
                int covered = 0;
                for (String alt : smelt.accepts()) {
                    if (covered >= remaining) break;
                    int available2 = left.getOrDefault(alt, 0);
                    if (available2 <= 0) continue;
                    int take = Math.min(available2, remaining - covered);
                    left.put(alt, available2 - take);
                    covered += take;
                }
                int needMore = remaining - covered;
                if (needMore <= 0) {
                    // Toute l'entree est deja dans le coffre : l'etape reste a executer.
                    steps.add(step);
                    nodes.remove(item);
                    return null;
                }
                String failure = expand(level, smelt.input(), needMore, depth + 1, left, raw, steps, nodes, alternatives);
                // Post-ordre : les etapes des ingredients sont ajoutees avant celle de l'item.
                steps.add(step);
                nodes.remove(item);
                if (failure != null) return failure;
                return null;
            }
            nodes.remove(item);
        }

        Plan plan = bestRecipe(level, item);
        if (plan == null) {
            raw.merge(item, remaining, Integer::sum);
            return null;
        }
        // Garde-fous : profondeur, taille de l'arbre, et cycle (un item ne se produit pas lui-meme).
        if (depth >= MAX_DEPTH || nodes.size() >= MAX_NODES || !nodes.add(item)) {
            raw.merge(item, remaining, Integer::sum);
            return null;
        }
        int batches = (remaining + plan.result().getCount() - 1) / plan.result().getCount();
        JsonObject step = new JsonObject();
        step.addProperty("kind", "craft");
        step.addProperty("craft", item);
        step.addProperty("times", batches);
        step.addProperty("produces", batches * plan.result().getCount());
        step.addProperty("station", plan.station());
        JsonArray needs = new JsonArray();
        for (Ingredient cell : plan.cells()) {
            String id = describe(cell);
            needs.add(id);
            // Un ingredient a tag accepte n'importe quel item du tag (toutes les planches pour
            // des batons) : on consomme d'abord ceux qui sont deja dans le coffre.
            int covered = 0;
            for (var holder : cell.items().toList()) {
                if (covered >= batches) break;
                String alt = BuiltInRegistries.ITEM.getKey(holder.value()).toString();
                int available3 = left.getOrDefault(alt, 0);
                if (available3 <= 0) continue;
                int take = Math.min(available3, batches - covered);
                left.put(alt, available3 - take);
                covered += take;
            }
            int needMore = batches - covered;
            if (needMore <= 0) continue;
            String failure = expand(level, id, needMore, depth + 1, left, raw, steps, nodes, alternatives);
            if (failure != null) {
                nodes.remove(item);
                return failure;
            }
        }
        step.add("needs", needs);
        steps.add(step);
        nodes.remove(item);
        return null;
    }

    private static Plan bestRecipe(ServerLevel level, String item) {
        Plan best = null;
        for (RecipeHolder<?> holder : level.getServer().getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof CraftingRecipe recipe)) continue;
            Plan plan = plan(holder.id().identifier().toString(), recipe);
            if (plan == null) continue;
            if (!BuiltInRegistries.ITEM.getKey(plan.result().getItem()).toString().equals(item)) continue;
            if (best == null || plan.perBatch() < best.perBatch()) best = plan;
        }
        return best;
    }

    private static JsonObject findRequest(String item, BlockPos min, BlockPos max) {
        JsonObject request = new JsonObject();
        request.addProperty("schema", 1);
        request.addProperty("type", "FIND");
        request.addProperty("item", item);
        request.add("min", MissionService.positionJson(min));
        request.add("max", MissionService.positionJson(max));
        request.addProperty("limit", 8);
        return request;
    }

    /** Boite serree autour des premiers blocs trouves (3 maximum), volume plafonne a 64 blocs. */
    private static JsonArray suggestedZone(JsonArray matches) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int used = 0;
        // On prend les blocs les PLUS BAS : un tronc flottant en haut d'arbre est inatteignable
        // pour un compagnon qui marche, la base est ce qui compte.
        List<JsonObject> sorted = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            JsonObject match = matches.get(i).getAsJsonObject();
            if (match.has("position")) sorted.add(match);
        }
        sorted.sort((left2, right2) -> Integer.compare(
                left2.getAsJsonArray("position").get(1).getAsInt(),
                right2.getAsJsonArray("position").get(1).getAsInt()));
        for (JsonObject match : sorted) {
            if (used >= 3) break;
            JsonArray pos = match.getAsJsonArray("position");
            int x = pos.get(0).getAsInt(), y = pos.get(1).getAsInt(), z = pos.get(2).getAsInt();
            minX = Math.min(minX, x); maxX = Math.max(maxX, x);
            minY = Math.min(minY, y); maxY = Math.max(maxY, y);
            minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
            used++;
        }
        if (used == 0) return null;
        // Une marge d'un bloc, puis on resserre pour ne pas depasser la limite de volume d'une mission.
        minX--; minY--; minZ--;
        maxX++; maxY++; maxZ++;
        while ((long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1) > 64) {
            if (maxX - minX >= maxZ - minZ) { maxX--; minX++; } else { maxZ--; minZ++; }
            if (maxX < minX || maxZ < minZ) break;
        }
        JsonArray zone = new JsonArray();
        zone.add(MissionService.positionJson(new BlockPos(minX, minY, minZ)));
        zone.add(MissionService.positionJson(new BlockPos(maxX, maxY, maxZ)));
        return zone;
    }

    /** Zone large autour du gisement le plus proche : utile quand la zone serree est inatteignable. */
    public static JsonArray wideZone(ServerLevel level, String item, JsonArray box, BlockPos chest, BlockPos near) {
        if (box == null || box.size() < 2) return null;
        BlockPos min = toPos(box.get(0).getAsJsonArray());
        BlockPos max = toPos(box.get(1).getAsJsonArray());
        JsonObject found = WorkObservation.inspect(level, findRequest(item, min, max));
        if (!found.get("ok").getAsBoolean() || found.get("matches_total").getAsInt() == 0) return null;
        JsonArray matches = found.getAsJsonArray("matches");
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < matches.size(); i++) {
            JsonObject match = matches.get(i).getAsJsonObject();
            if (!match.has("position")) continue;
            BlockPos pos = toPos(match.getAsJsonArray("position"));
            double dist = near == null ? 0 : pos.distSqr(near);
            if (best == null || dist < bestDist) { bestDist = dist; best = pos; }
        }
        if (best == null) return null;
        // Cube autour du gisement, en descendant sous le bloc trouve pour inclure la base / l'acces.
        JsonArray zone = new JsonArray();
        JsonArray low = new JsonArray();
        low.add(best.getX() - 8); low.add(Math.max(level.getMinY(), best.getY() - 10)); low.add(best.getZ() - 8);
        JsonArray high = new JsonArray();
        high.add(best.getX() + 8); high.add(Math.min(level.getMaxY(), best.getY() + 4)); high.add(best.getZ() + 8);
        zone.add(low);
        zone.add(high);
        return excludeChest(zone, chest);
    }

    /** Zone serree recalculee a la demande autour des premiers blocs reellement presents. */
    public static JsonArray tightZone(ServerLevel level, String item, JsonArray box, BlockPos exclude) {
        if (box == null || box.size() < 2) return null;
        BlockPos min = toPos(box.get(0).getAsJsonArray());
        BlockPos max = toPos(box.get(1).getAsJsonArray());
        JsonObject found = WorkObservation.inspect(level, findRequest(item, min, max));
        if (!found.get("ok").getAsBoolean() || found.get("matches_total").getAsInt() == 0) return null;
        JsonArray zone = suggestedZone(found.getAsJsonArray("matches"));
        if (zone == null) return null;
        return excludeChest(zone, exclude);
    }

    /** Retire le coffre de destination (et son support) de la zone de recolte. */
    public static JsonArray excludeChest(JsonArray zone, BlockPos chest) {
        if (chest == null || zone == null || zone.size() < 2) return zone;
        int minX = zone.get(0).getAsJsonArray().get(0).getAsInt();
        int minY = zone.get(0).getAsJsonArray().get(1).getAsInt();
        int minZ = zone.get(0).getAsJsonArray().get(2).getAsInt();
        int maxX = zone.get(1).getAsJsonArray().get(0).getAsInt();
        int maxY = zone.get(1).getAsJsonArray().get(1).getAsInt();
        int maxZ = zone.get(1).getAsJsonArray().get(2).getAsInt();
        int x = chest.getX(), y = chest.getY(), z = chest.getZ();
        boolean inside = x >= minX && x <= maxX && z >= minZ && z <= maxZ && y >= minY - 1 && y <= maxY;
        if (!inside) return zone;
        int left = x - minX, right = maxX - x, front = z - minZ, back = maxZ - z;
        int smallest = Math.min(Math.min(left, right), Math.min(front, back));
        if (smallest == left) minX = x + 1;
        else if (smallest == right) maxX = x - 1;
        else if (smallest == front) minZ = z + 1;
        else maxZ = z - 1;
        if (minX > maxX || minZ > maxZ) return null;
        JsonArray out = new JsonArray();
        JsonArray low = new JsonArray();
        low.add(minX); low.add(minY); low.add(minZ);
        JsonArray high = new JsonArray();
        high.add(maxX); high.add(maxY); high.add(maxZ);
        out.add(low);
        out.add(high);
        return out;
    }

    private static BlockPos toPos(JsonArray array) {
        return new BlockPos(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
    }

    private static long boxVolume(BlockPos min, BlockPos max) {
        return (long) (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
    }

    private static JsonArray searchBox(ServerLevel level, BlockPos min, BlockPos max) {
        JsonArray box = new JsonArray();
        box.add(MissionService.positionJson(min));
        box.add(MissionService.positionJson(max));
        return box;
    }

    private static JsonObject mapJson(Map<String, Integer> counts) {
        JsonObject json = new JsonObject();
        counts.forEach(json::addProperty);
        return json;
    }

    // ------------------------------------------------------------------ outils

    private static Plan plan(String id, CraftingRecipe recipe) {
        int width;
        int height;
        List<Ingredient> cells = new ArrayList<>();
        if (recipe instanceof ShapedRecipe shaped) {
            width = shaped.getWidth();
            height = shaped.getHeight();
            for (Optional<Ingredient> cell : shaped.getIngredients()) {
                if (cell.isPresent() && !cell.get().isEmpty()) cells.add(cell.get());
            }
            // Les cellules vides ne consomment rien : la liste ne garde que les ingredients reels.
        } else if (recipe instanceof ShapelessRecipe) {
            // Pas d'acces direct aux ingredients : PlacementInfo expose la liste commune aux recettes.
            List<Ingredient> all = recipe.placementInfo().ingredients();
            if (all.isEmpty()) return null;
            width = Math.min(3, all.size());
            height = (all.size() + width - 1) / width;
            for (Ingredient ing : all) if (!ing.isEmpty()) cells.add(ing);
        } else {
            return null;
        }
        if (cells.isEmpty()) return null;

        List<ItemStack> grid = new ArrayList<>();
        int slots = Math.max(1, width * height);
        for (int i = 0; i < slots; i++) grid.add(ItemStack.EMPTY);
        int cursor = 0;
        for (Ingredient ing : cells) {
            if (cursor >= slots) break;
            grid.set(cursor++, firstStack(ing));
        }
        ItemStack result;
        try {
            result = recipe.assemble(CraftingInput.of(width, height, grid));
        } catch (RuntimeException e) {
            return null;
        }
        if (result == null || result.isEmpty()) return null;
        return new Plan(id, result, width, height, cells);
    }

    private static JsonArray ingredientsJson(Plan plan) {
        JsonArray arr = new JsonArray();
        for (Ingredient ing : plan.cells()) arr.add(describe(ing));
        return arr;
    }

    private static String describe(Ingredient ing) {
        return ing.items().findFirst()
                .map(holder -> BuiltInRegistries.ITEM.getKey(holder.value()).toString())
                .orElse("?");
    }

    private static ItemStack firstStack(Ingredient ing) {
        return ing.items().findFirst()
                .map(holder -> new ItemStack(holder.value()))
                .orElse(ItemStack.EMPTY);
    }

    private static boolean consume(List<ItemStack> inventory, Ingredient ingredient, int count) {
        int left = count;
        for (ItemStack slot : inventory) {
            if (left <= 0) break;
            if (slot.isEmpty() || !ingredient.test(slot)) continue;
            int take = Math.min(left, slot.getCount());
            slot.shrink(take);
            left -= take;
        }
        return left == 0;
    }

    /** Table de craft la plus proche d'une position de reference (rayon 8), ou null. */
    public static BlockPos nearbyTable(ServerLevel level, BlockPos center) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.hasChunkAt(pos)) continue;
                    if (!level.getBlockState(pos).is(Blocks.CRAFTING_TABLE)) continue;
                    double dist = pos.distSqr(center);
                    if (dist < bestDist) { bestDist = dist; best = pos; }
                }
            }
        }
        return best;
    }

    private static boolean craftingTableNear(ServerLevel level, BlockPos center) {
        for (int dx = -STATION_RADIUS; dx <= STATION_RADIUS; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -STATION_RADIUS; dz <= STATION_RADIUS; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.hasChunkAt(pos)) continue;
                    if (level.getBlockState(pos).is(Blocks.CRAFTING_TABLE)) return true;
                }
            }
        }
        return false;
    }

    static Container container(ServerLevel level, BlockPos pos) {
        if (pos == null || !level.hasChunkAt(pos)) return null;
        if (com.donopot.voiddynasty.compat.ClaimIntegration.isClaimProtected(level, pos)) return null;
        var entity = level.getBlockEntity(pos);
        if (entity instanceof Container container
                && (entity instanceof net.minecraft.world.level.block.entity.ChestBlockEntity
                    || entity instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity)) {
            return container;
        }
        return null;
    }

    private static JsonObject error(String code, String detail) {
        JsonObject json = new JsonObject();
        json.addProperty("ok", false);
        json.addProperty("error", code);
        json.addProperty("detail", detail);
        return json;
    }
}
