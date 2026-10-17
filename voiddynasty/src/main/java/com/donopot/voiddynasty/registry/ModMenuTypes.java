package com.donopot.voiddynasty.registry;

import com.donopot.voiddynasty.menu.SuperChestMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

/**
 * Registre des menus du mod. Le constructeur {@link MenuType} est rendu accessible
 * par l'access widener (il est private en vanilla 26.3).
 */
public final class ModMenuTypes {

    public static final MenuType<SuperChestMenu> SUPER_CHEST = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath("voiddynasty", "super_chest"),
            new MenuType<>((syncId, inv) -> new SuperChestMenu(syncId, inv), FeatureFlagSet.of())
    );

    private ModMenuTypes() {
    }

    public static void init() {
    }
}
