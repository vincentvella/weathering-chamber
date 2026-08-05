package com.example.weathering.menu;

import com.example.weathering.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WeatheringChamberMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private static final int MACHINE_SLOT_COUNT = 2;

    private final Container container;
    private final ContainerData data;

    // Client constructor (the MenuType supplier). A throwaway container + data is
    // created; the server syncs the real contents/progress over the slots + data slots.
    public WeatheringChamberMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(2));
    }

    // Server constructor (from the block entity's createMenu).
    public WeatheringChamberMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.WEATHERING_CHAMBER, containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        // Positions line up with the vanilla furnace GUI texture we reuse.
        this.addSlot(new Slot(container, INPUT_SLOT, 56, 17)); // input
        this.addSlot(new Slot(container, OUTPUT_SLOT, 116, 35) { // output — no manual insert
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // AbstractContainerMenu.addStandardInventorySlots was added in 1.21.2; on 1.21.1
        // we add the 27 inventory + 9 hotbar slots by hand at the same anchor (8, 84).
        //? if >=1.21.2 {
        /*this.addStandardInventorySlots(playerInventory, 8, 84);
        *///?} else {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        //?}
        this.addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    /** Returns 0..24 — the pixel width of the filled progress arrow. */
    public int getScaledProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1);
        int arrowWidth = 24;
        return (maxProgress != 0 && progress != 0) ? progress * arrowWidth / maxProgress : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            newStack = original.copy();
            if (index < MACHINE_SLOT_COUNT) {
                // From the machine into the player inventory.
                if (!this.moveItemStackTo(original, MACHINE_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(original, 0, MACHINE_SLOT_COUNT, false)) {
                // From the player inventory into the machine.
                return ItemStack.EMPTY;
            }

            if (original.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }
}
