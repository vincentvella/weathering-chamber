package com.example.weathering;

import com.example.weathering.block.entity.WeatheringChamberBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static BlockEntityType<WeatheringChamberBlockEntity> WEATHERING_CHAMBER_BE;

    public static void register() {
        WEATHERING_CHAMBER_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(WeatheringChamberMod.MOD_ID, "weathering_chamber"),
            FabricBlockEntityTypeBuilder.create(WeatheringChamberBlockEntity::new, ModBlocks.WEATHERING_CHAMBER).build()
        );
    }
}
