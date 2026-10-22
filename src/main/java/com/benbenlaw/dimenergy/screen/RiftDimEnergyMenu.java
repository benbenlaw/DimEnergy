package com.benbenlaw.dimenergy.screen;

import com.benbenlaw.dimenergy.RiftCompat;
import com.benbenlaw.dimenergy.block.entity.BlockEntityRiftDimEnergy;
import edivad.dimstorage.menu.DimStorageMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class RiftDimEnergyMenu extends DimStorageMenu {
    public BlockEntityRiftDimEnergy owner;
    public boolean isOpen;

    public RiftDimEnergyMenu(int windowId, Inventory inventory, BlockEntityRiftDimEnergy owner, boolean isOpen) {
        super((MenuType) RiftCompat.DIMRIFTENERGY_MENU.get(), windowId);
        this.owner = owner;
        this.isOpen = isOpen;
        this.addInventorySlots(inventory);

        this.broadcastChanges();
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public void removed(Player player) {
        super.removed(player);
    }
}
