package com.example.weathering;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatheringChamberMod implements ModInitializer {
    public static final String MOD_ID = "weathering";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModMenuTypes.register();
        LOGGER.info("Weathering Chamber initialized — sand is now renewable.");
    }
}
