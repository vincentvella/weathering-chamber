package com.example.weathering.block.entity;

import com.example.weathering.ModBlockEntities;
import com.example.weathering.menu.WeatheringChamberMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WeatheringChamberBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    private int progress = 0;
    private int maxProgress = 200; // 200 ticks = 10 seconds per grind

    /** The erosion chain: one grind turns the key into the value. */
    private static final Map<Item, ItemStack> EROSION = new HashMap<>();
    static {
        EROSION.put(Items.COBBLESTONE, new ItemStack(Items.GRAVEL));
        EROSION.put(Items.GRAVEL, new ItemStack(Items.SAND));
    }

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : maxProgress;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            } else {
                maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public WeatheringChamberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WEATHERING_CHAMBER_BE, pos, state);
    }

    // ---------------------------------------------------------------- ticking
    public static void tick(Level level, BlockPos pos, BlockState state, WeatheringChamberBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        boolean canGrind = be.isTouchingWater(level, pos) && be.hasRecipe() && be.hasRoomForOutput();
        if (canGrind) {
            be.progress++;
            be.setChanged();
            if (be.progress >= be.maxProgress) {
                be.craft();
                be.progress = 0;
            }
        } else if (be.progress != 0) {
            be.progress = 0;
            be.setChanged();
        }
    }

    private boolean isTouchingWater(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (level.getFluidState(pos.relative(dir)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private ItemStack currentResult() {
        ItemStack input = items.get(INPUT_SLOT);
        if (input.isEmpty()) {
            return null;
        }
        return EROSION.get(input.getItem());
    }

    private boolean hasRecipe() {
        return currentResult() != null;
    }

    private boolean hasRoomForOutput() {
        ItemStack result = currentResult();
        if (result == null) {
            return false;
        }
        ItemStack output = items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return output.getItem() == result.getItem()
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void craft() {
        ItemStack result = currentResult();
        if (result == null) {
            return;
        }
        items.get(INPUT_SLOT).shrink(1);
        ItemStack output = items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
        setChanged();
    }

    // ---------------------------------------------------------------- MenuProvider
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.weathering.weathering_chamber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new WeatheringChamberMenu(containerId, playerInventory, this, this.dataAccess);
    }

    // ---------------------------------------------------------------- WorldlyContainer
    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // Hoppers: insert only into the input slot, extract only from the output slot.
    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{INPUT_SLOT, OUTPUT_SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == INPUT_SLOT;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT_SLOT;
    }

    // ---------------------------------------------------------------- NBT (signature diverges)
    // 26.x moved block-entity serialization to the ValueOutput/ValueInput API;
    // 1.21.1 still uses CompoundTag + HolderLookup.Provider.
    //? if >=26.1 {
    /*@Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("weathering.progress", this.progress);
        output.putInt("weathering.max_progress", this.maxProgress);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        this.progress = input.getIntOr("weathering.progress", 0);
        this.maxProgress = input.getIntOr("weathering.max_progress", 200);
    }
    *///?} else {
    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putInt("weathering.progress", this.progress);
        tag.putInt("weathering.max_progress", this.maxProgress);
    }

    @Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.progress = tag.getInt("weathering.progress");
        this.maxProgress = tag.contains("weathering.max_progress") ? tag.getInt("weathering.max_progress") : 200;
    }
    //?}
}
