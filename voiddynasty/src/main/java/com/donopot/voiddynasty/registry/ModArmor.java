package com.donopot.voiddynasty.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

/**
 * Armures du mod : Space Marine (Mark X) et Terminator (élite).
 * Défense/durabilité/robustesse viennent de l'{@link ArmorMaterial} ;
 * bonus de stats, taille et bouclier sont appliqués par {@code ArmorEffects}.
 */
public final class ModArmor {

    private static final TagKey<Item> REPAIR = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath("voiddynasty", "repairs_space_marine_armor"));

    // ---- Space Marine (Mark X) ----
    public static final ArmorMaterial SPACE_MARINE = material("space_marine", 40,
            Map.of(ArmorType.HELMET, 4, ArmorType.CHESTPLATE, 10, ArmorType.LEGGINGS, 8, ArmorType.BOOTS, 4),
            4.0f, 0.5f);

    // ---- Terminator (élite, plus massive) ----
    public static final ArmorMaterial TERMINATOR = material("terminator", 60,
            Map.of(ArmorType.HELMET, 6, ArmorType.CHESTPLATE, 14, ArmorType.LEGGINGS, 11, ArmorType.BOOTS, 6),
            5.0f, 0.7f);

    // Pièces Space Marine
    public static final Item HELMET = armor("space_marine_helmet", ArmorType.HELMET, SPACE_MARINE);
    public static final Item CHESTPLATE = armor("space_marine_chestplate", ArmorType.CHESTPLATE, SPACE_MARINE);
    public static final Item LEGGINGS = armor("space_marine_leggings", ArmorType.LEGGINGS, SPACE_MARINE);
    public static final Item BOOTS = armor("space_marine_boots", ArmorType.BOOTS, SPACE_MARINE);

    // Pièces Terminator
    public static final Item TERMINATOR_HELMET = armor("terminator_helmet", ArmorType.HELMET, TERMINATOR);
    public static final Item TERMINATOR_CHESTPLATE = armor("terminator_chestplate", ArmorType.CHESTPLATE, TERMINATOR);
    public static final Item TERMINATOR_LEGGINGS = armor("terminator_leggings", ArmorType.LEGGINGS, TERMINATOR);
    public static final Item TERMINATOR_BOOTS = armor("terminator_boots", ArmorType.BOOTS, TERMINATOR);

    private static ArmorMaterial material(String name, int durability, Map<ArmorType, Integer> defense,
                                          float toughness, float knockbackResistance) {
        Identifier id = Identifier.fromNamespaceAndPath("voiddynasty", name);
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, id);
        return new ArmorMaterial(durability, new EnumMap<>(defense), 15,
                SoundEvents.ARMOR_EQUIP_NETHERITE, toughness, knockbackResistance, REPAIR, asset);
    }

    private static Item armor(String name, ArmorType type, ArmorMaterial material) {
        Identifier id = Identifier.fromNamespaceAndPath("voiddynasty", name);
        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new Item(new Item.Properties()
                        .stacksTo(1)
                        .setId(ResourceKey.create(Registries.ITEM, id))
                        .durability(type.getDurability(material.durability()))
                        .attributes(material.createAttributes(type))
                        .component(DataComponents.EQUIPPABLE,
                                Equippable.builder(type.getSlot())
                                        .setAsset(material.assetId())
                                        .setEquipSound(material.equipSound())
                                        .build()))
        );
    }

    /** Le simple chargement de cette classe enregistre les items. */
    public static void init() {
    }

    private ModArmor() {}
}
