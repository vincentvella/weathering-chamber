package com.example.weathering;

import com.example.weathering.menu.WeatheringChamberMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {
    public static MenuType<WeatheringChamberMenu> WEATHERING_CHAMBER;

    public static void register() {
        WEATHERING_CHAMBER = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(WeatheringChamberMod.MOD_ID, "weathering_chamber"),
            new MenuType<>(WeatheringChamberMenu::new, FeatureFlagSet.of())
        );
    }
}
