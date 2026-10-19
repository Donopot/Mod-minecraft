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
import java.util.List;
import java.util.Optional;

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
