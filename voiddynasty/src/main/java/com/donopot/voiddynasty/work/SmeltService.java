package com.donopot.voiddynasty.work;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Four : la fonte, comme les crafts, vient du livre de recettes du serveur.
 *
 * <p>Aucune table de cuisson n'est codee en dur ici : la recette ({@code SmeltingRecipe}), le temps de
 * cuisson et le carburant ({@code DataComponents.COOKING_FUEL}) sont lus a l'execution. Le service
 * consomme l'entree et le carburant dans un coffre, et remet le resultat dans le meme coffre.</p>
 *
 * <p>Le four doit exister a portee du compagnon : on ne fond pas dans le sac. Le temps de cuisson
 * n'est pas attendu (mode operateur) mais il est calcule et rendu dans le recu : c'est le carburant
 * reel qui est consomme, pas une approximation.</p>
 */
public final class SmeltService {

    private static final int STATION_RADIUS = 5;
    public static final int MAX_BATCH = 4096;

    private SmeltService() {}

    /** Une recette de fonte resolue : entree, sortie, temps et experience par piece. */
    record Plan(String id, String output, int outputCount, String input, int cookingTime, float experience) {
        int perBatch() { return outputCount; }
    }

    // ------------------------------------------------------------------ lecture

    public static JsonObject recipes(ServerLevel level, String filter, int limit) {
        String needle = filter == null ? "" : filter.trim().toLowerCase();
        JsonArray out = new JsonArray();
        int total = 0;
        for (RecipeHolder<?> holder : level.getServer().getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof SmeltingRecipe recipe)) continue;
            Plan plan = plan(holder.id().identifier().toString(), recipe);
            if (plan == null) continue;
            if (!needle.isEmpty() && !plan.output().contains(needle) && !plan.input().contains(needle)
                    && !plan.id().contains(needle)) continue;
            total++;
            if (out.size() >= limit) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("recipe", plan.id());
            entry.addProperty("result", plan.output());
            entry.addProperty("result_count", plan.outputCount());
            entry.addProperty("input", plan.input());
            entry.addProperty("cooking_ticks", plan.cookingTime());
            entry.addProperty("experience", plan.experience());
            entry.addProperty("station", "four");
            out.add(entry);
        }
        JsonObject json = new JsonObject();
        json.addProperty("ok", true);
        json.addProperty("matched", total);
        json.addProperty("returned", out.size());
        json.addProperty("scope", "Recettes de fonte du serveur (four). Le temps de cuisson est en ticks : 200 = 10 s.");
        json.add("recipes", out);
        return json;
    }

    static Plan plan(ServerLevel level, String item) {
        Plan best = null;
        for (RecipeHolder<?> holder : level.getServer().getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof SmeltingRecipe recipe)) continue;
            Plan plan = plan(holder.id().identifier().toString(), recipe);
            if (plan == null || !plan.output().equals(item)) continue;
            // On privilegie une entree que le compagnon sait recolter (raw_iron plutot que le bloc de minerai).
            boolean better = best == null
                    || (gatherable(plan.input()) && !gatherable(best.input()))
                    || (gatherable(plan.input()) == gatherable(best.input()) && plan.cookingTime() < best.cookingTime());
            if (better) best = plan;
        }
        return best;
    }

    private static boolean gatherable(String item) {
        return MissionService.SOURCES.containsKey(item);
    }

    private static Plan plan(String id, SmeltingRecipe recipe) {
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        if (ingredients.isEmpty() || ingredients.get(0).isEmpty()) return null;
        String input = ingredients.get(0).items().findFirst()
                .map(holder -> BuiltInRegistries.ITEM.getKey(holder.value()).toString())
                .orElse(null);
        if (input == null) return null;
        ItemStack result;
        try {
            ItemStack probe = new ItemStack(ingredients.get(0).items().findFirst()
                    .map(holder -> holder.value()).orElse(null));
            result = recipe.assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(probe));
        } catch (RuntimeException e) {
            return null;
        }
        if (result == null || result.isEmpty()) return null;
        return new Plan(id, BuiltInRegistries.ITEM.getKey(result.getItem()).toString(), result.getCount(),
                input, Math.max(1, recipe.cookingTime()), recipe.experience());
    }

    // ------------------------------------------------------------------ fonte

    public static JsonObject smelt(ServerLevel level, LivingEntity bot, BlockPos chestPos, String item, int count) {
        if (bot == null) return error("NO_SERVITOR", "Aucun servitor dans cette dimension");
        if (count < 1 || count > MAX_BATCH) return error("INVALID_ARGUMENT", "count must be 1.." + MAX_BATCH);
        Container chest = CraftService.container(level, chestPos);
        if (chest == null) return error("INVALID_CHEST", "Loaded, unprotected chest or barrel required at " + chestPos.toShortString());

        String wanted = item.contains(":") ? item : "minecraft:" + item;
        Plan plan = plan(level, wanted);
        if (plan == null) {
            return error("NO_RECIPE", "Aucune recette de fonte ne produit " + wanted
                    + " (verifie servitor_smelt_recipes : certains items se fabriquent, ils ne se fondent pas)");
        }
        if (!furnaceNear(level, bot.blockPosition())) {
            return error("NEEDS_FURNACE", "Il faut un four (ou un haut fourneau / fumoir) a moins de "
                    + STATION_RADIUS + " blocs du compagnon pour fondre " + wanted);
        }

        List<ItemStack> work = new ArrayList<>();
        for (int i = 0; i < chest.getContainerSize(); i++) work.add(chest.getItem(i).copy());

        int available = 0;
        for (ItemStack stack : work) {
            if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(plan.input())) {
                available += stack.getCount();
            }
        }
        if (available < count) {
            JsonObject error = error("MISSING_INGREDIENTS", "Il faut " + count + " " + plan.input()
                    + " dans le coffre, il y en a " + available);
            error.addProperty("input", plan.input());
            error.addProperty("need", count);
            error.addProperty("have", available);
            error.addProperty("fuel_note", "Le carburant se choisit aussi dans ce coffre.");
            return error;
        }

        long cookingTicks = (long) plan.cookingTime() * count;
        Fuel fuel = pickFuel(level, work, plan.input());
        if (fuel == null) {
            JsonObject error = error("NO_FUEL", "Aucun carburant dans le coffre : il faut un item avec un pouvoir "
                    + "calorifique (charbon, bois, planches, lave...)");
            error.add("chest_items", chestItems(level, work));
            return error;
        }
        long fuelsNeeded = (cookingTicks + fuel.ticksPerUnit() - 1) / fuel.ticksPerUnit();
        int fuelAvailable = 0;
        for (ItemStack stack : work) {
            if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(fuel.item())) {
                fuelAvailable += stack.getCount();
            }
        }
        if (fuelAvailable < fuelsNeeded) {
            JsonObject error = error("NOT_ENOUGH_FUEL", "Il faut " + fuelsNeeded + " " + fuel.item()
                    + " (" + cookingTicks + " ticks de cuisson a " + fuel.ticksPerUnit() + " ticks par piece), il y en a "
                    + fuelAvailable);
            error.addProperty("fuel", fuel.item());
            error.addProperty("need", fuelsNeeded);
            error.addProperty("have", fuelAvailable);
            return error;
        }

        consume(work, plan.input(), count);
        consume(work, fuel.item(), (int) fuelsNeeded);
        int produced = count * plan.outputCount();
        var outputItem = BuiltInRegistries.ITEM.get(identifier(plan.output()))
                .map(holder -> holder.value()).orElse(null);
        if (outputItem == null) return error("NO_RECIPE", "Item de sortie inconnu : " + plan.output());
        ItemStack output = new ItemStack(outputItem, produced);
        if (!insert(work, output)) {
            return error("CHEST_FULL", "Le coffre n'a pas la place pour " + produced + " " + plan.output());
        }
        for (int i = 0; i < work.size() && i < chest.getContainerSize(); i++) chest.setItem(i, work.get(i));
        chest.setChanged();

        JsonObject json = new JsonObject();
        json.addProperty("ok", true);
        json.addProperty("item", plan.output());
        json.addProperty("requested", count);
        json.addProperty("produced", produced);
        json.addProperty("recipe", plan.id());
        json.addProperty("input", plan.input());
        json.addProperty("input_consumed", count);
        json.addProperty("fuel", fuel.item());
        json.addProperty("fuel_consumed", fuelsNeeded);
        json.addProperty("fuel_ticks_each", fuel.ticksPerUnit());
        json.addProperty("cooking_ticks", cookingTicks);
        json.addProperty("experience", plan.experience() * count);
        json.addProperty("station", "four");
        json.addProperty("note", "Fonte d'operateur : recette et carburant reels, temps de cuisson calcule mais non attendu.");
        return json;
    }

    // ------------------------------------------------------------------ carburant

    private record Fuel(String item, int ticksPerUnit) {}

    /** Carburant au meilleur rendement present dans le coffre, jamais l'item qu'on veut fondre. */
    private static Fuel pickFuel(ServerLevel level, List<ItemStack> work, String input) {
        Fuel best = null;
        for (ItemStack stack : work) {
            if (stack.isEmpty()) continue;
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if (id.equals(input)) continue;
            int ticks = burnTicks(level, stack);
            if (ticks <= 0) continue;
            if (best == null || ticks > best.ticksPerUnit()) best = new Fuel(id, ticks);
        }
        return best;
    }

    /** Pouvoir calorifique reel de l'item, lu depuis son composant de cuisson et resolu sur le serveur. */
    static int burnTicks(ServerLevel level, ItemStack stack) {
        CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
        if (fuel == null) return 0;
        ResolvableInt burnTime = fuel.burnTime();
        if (burnTime instanceof ResolvableInt.Constant constant) return Math.max(0, constant.value());
        try {
            // Les valeurs sont des cles de datapack (context_int_provider/cooking/...) : il faut un
            // contexte de butin adosse au niveau pour les resoudre. On ne devine jamais la duree.
            var params = new net.minecraft.world.level.storage.loot.LootParams(
                    level, net.minecraft.util.context.ContextMap.EMPTY, Map.of(), 0.0F);
            var context = new net.minecraft.world.level.storage.loot.LootContext.Builder(params)
                    .create(Optional.empty());
            return Math.max(0, burnTime.get(context, 0));
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public static JsonObject fuels(ServerLevel level, BlockPos chestPos) {
        Container chest = CraftService.container(level, chestPos);
        if (chest == null) return error("INVALID_CHEST", "Loaded, unprotected chest or barrel required at " + chestPos.toShortString());
        JsonArray out = fuelEntries(level, chest);
        JsonObject json = new JsonObject();
        json.addProperty("ok", true);
        json.addProperty("returned", out.size());
        json.add("fuels", out);
        return json;
    }

    /** Carburants d'un conteneur, avec leur pouvoir calorifique reel. */
    public static JsonArray fuelEntries(ServerLevel level, Container chest) {
        List<ItemStack> work = new ArrayList<>();
        for (int i = 0; i < chest.getContainerSize(); i++) work.add(chest.getItem(i).copy());
        JsonArray out = new JsonArray();
        for (ItemStack stack : work) {
            if (stack.isEmpty()) continue;
            int ticks = burnTicks(level, stack);
            if (ticks <= 0) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            entry.addProperty("count", stack.getCount());
            entry.addProperty("ticks_each", ticks);
            entry.addProperty("items_smeltable", ticks / 200);
            out.add(entry);
        }
        return out;
    }

    // ------------------------------------------------------------------ outils

    private static boolean furnaceNear(ServerLevel level, BlockPos center) {
        for (int dx = -STATION_RADIUS; dx <= STATION_RADIUS; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -STATION_RADIUS; dz <= STATION_RADIUS; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.hasChunkAt(pos)) continue;
                    if (level.getBlockState(pos).getBlock() instanceof AbstractFurnaceBlock) return true;
                }
            }
        }
        return false;
    }

    private static void consume(List<ItemStack> inventory, String item, int count) {
        int left = count;
        for (ItemStack slot : inventory) {
            if (left <= 0) break;
            if (slot.isEmpty() || !BuiltInRegistries.ITEM.getKey(slot.getItem()).toString().equals(item)) continue;
            int take = Math.min(left, slot.getCount());
            slot.shrink(take);
            left -= take;
        }
    }

    private static boolean insert(List<ItemStack> inventory, ItemStack stack) {
        int remaining = stack.getCount();
        for (ItemStack slot : inventory) {
            if (remaining <= 0) break;
            if (slot.isEmpty() || !ItemStack.isSameItemSameComponents(slot, stack)) continue;
            int room = Math.min(remaining, slot.getMaxStackSize() - slot.getCount());
            if (room <= 0) continue;
            slot.grow(room);
            remaining -= room;
        }
        for (int i = 0; i < inventory.size() && remaining > 0; i++) {
            if (!inventory.get(i).isEmpty()) continue;
            int place = Math.min(remaining, stack.getMaxStackSize());
            ItemStack copy = stack.copy();
            copy.setCount(place);
            inventory.set(i, copy);
            remaining -= place;
        }
        return remaining == 0;
    }

    private static JsonArray chestItems(ServerLevel level, List<ItemStack> work) {
        JsonArray out = new JsonArray();
        for (ItemStack stack : work) {
            if (stack.isEmpty()) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            entry.addProperty("count", stack.getCount());
            entry.addProperty("ticks_each", burnTicks(level, stack));
            out.add(entry);
        }
        return out;
    }

    private static net.minecraft.resources.Identifier identifier(String id) {
        return net.minecraft.resources.Identifier.parse(id);
    }

    private static JsonObject error(String code, String detail) {
        JsonObject json = new JsonObject();
        json.addProperty("ok", false);
        json.addProperty("error", code);
        json.addProperty("detail", detail);
        return json;
    }
}
