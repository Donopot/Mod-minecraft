package com.donopot.voiddynasty.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Compiled at P0; intentionally fails until P3 registers every required identifier. */
@GameTestHolder("voiddynasty")
@PrefixGameTestTemplate(false)
public final class ContratIdsTest {
    private ContratIdsTest() {}

    @GameTest(template = "empty", templateNamespace = "neoforge")
    public static void requiredIdsResolve(GameTestHelper helper) {
        try (var stream = ContratIdsTest.class.getResourceAsStream("/migration/contrat-ids.json")) {
            if (stream == null) throw new IllegalStateException("Missing frozen contract /migration/contrat-ids.json");
            JsonObject contract = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            if (contract.get("schema").getAsInt() != 1) throw new IllegalStateException("Unsupported contract schema");
            int checked = 0;
            for (var entry : contract.getAsJsonObject("runtime_checks").entrySet()) {
                Registry<?> registry = switch (entry.getKey()) {
                    case "block" -> BuiltInRegistries.BLOCK;
                    case "item" -> BuiltInRegistries.ITEM;
                    case "entity_type" -> BuiltInRegistries.ENTITY_TYPE;
                    case "block_entity_type" -> BuiltInRegistries.BLOCK_ENTITY_TYPE;
                    case "menu" -> BuiltInRegistries.MENU;
                    case "sound_event" -> BuiltInRegistries.SOUND_EVENT;
                    case "creative_mode_tab" -> BuiltInRegistries.CREATIVE_MODE_TAB;
                    case "data_component_type" -> BuiltInRegistries.DATA_COMPONENT_TYPE;
                    default -> throw new IllegalStateException("Unknown registry " + entry.getKey());
                };
                for (var value : entry.getValue().getAsJsonArray()) {
                    var id = ResourceLocation.parse(value.getAsString());
                    // Registry#get may return a default air/pig. Existence must be checked explicitly.
                    helper.assertTrue(registry.containsKey(id), "Missing " + entry.getKey() + " " + id);
                    checked++;
                }
            }
            helper.assertTrue(checked > 9, "Empty or truncated migration contract");
            helper.succeed();
        } catch (java.io.IOException error) { throw new IllegalStateException("Cannot read migration contract", error); }
    }
}
