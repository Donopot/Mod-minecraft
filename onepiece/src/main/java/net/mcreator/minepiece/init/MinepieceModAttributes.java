package net.mcreator.minepiece.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * Shim Fabric 26.3 de l'addon Mine Piece (attributs custom).
 * Fournit les deux attributs de haki attendus par opworldlike.
 */
public final class MinepieceModAttributes {
    public static Holder<Attribute> ARMAMENT_HAKI_LEVEL;
    public static Holder<Attribute> OBSERVATION_HAKI_LEVEL;
    public static Holder<Attribute> SIZE;

    private MinepieceModAttributes() {
    }

    public static void register() {
        ARMAMENT_HAKI_LEVEL = registerAttribute("armament_haki_level");
        OBSERVATION_HAKI_LEVEL = registerAttribute("observation_haki_level");
        SIZE = registerAttribute("size", 1.0);
    }

    private static Holder<Attribute> registerAttribute(String name) {
        return registerAttribute(name, 0.0);
    }

    private static Holder<Attribute> registerAttribute(String name, double defaultValue) {
        Attribute attribute = new Attribute("attribute.minepiece." + name, defaultValue) {};
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Identifier.fromNamespaceAndPath("minepiece", name), attribute);
    }
}
