package com.example.weathering.client;

import com.example.weathering.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class WeatheringChamberModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.WEATHERING_CHAMBER, WeatheringChamberScreen::new);
    }
}
