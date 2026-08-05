package com.example.weathering;

import com.example.weathering.block.WeatheringChamberBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ModBlocks {
    public static final Block WEATHERING_CHAMBER = register(
        "weathering_chamber",
        WeatheringChamberBlock::new,
        BlockBehaviour.Properties.of()
            .strength(3.5f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE)
    );

    // Registration diverges: 26.x requires a ResourceKey + Properties.setId(...); 1.21.1 does not.
    //? if >=1.21.2 {
    /*private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(WeatheringChamberMod.MOD_ID, name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        Block block = factory.apply(properties.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        BlockItem blockItem = new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        return block;
    }
    *///?} else {
    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(WeatheringChamberMod.MOD_ID, name);
        Block block = factory.apply(properties);
        Registry.register(BuiltInRegistries.BLOCK, id, block);

        BlockItem blockItem = new BlockItem(block, new Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, id, blockItem);
        return block;
    }
    //?}

    public static void register() {
        WeatheringChamberMod.LOGGER.info("Registering blocks for {}", WeatheringChamberMod.MOD_ID);
        // Fabric renamed the creative-tab modification API: ItemGroupEvents (1.21.1) ->
        // CreativeModeTabEvents (26.x). Both accept the FUNCTIONAL_BLOCKS ResourceKey and
        // an ItemLike via the output/entries callback.
        //? if >=26.1 {
        /*net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .register(output -> output.accept(WEATHERING_CHAMBER));
        *///?} else {
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .register(entries -> entries.accept(WEATHERING_CHAMBER));
        //?}
    }
}
