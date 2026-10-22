package com.benbenlaw.dimenergy.util;

import com.benbenlaw.dimenergy.storage.RiftDimEnergyStorage;
import edivad.dimstorage.api.AbstractDimStorage;
import edivad.dimstorage.api.DimStoragePlugin;
import edivad.dimstorage.api.Frequency;
import edivad.dimstorage.manager.DimStorageManager;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class RiftDimEnergyPlugin implements DimStoragePlugin {
    @Override
    public AbstractDimStorage createDimStorage(DimStorageManager dimStorageManager, Frequency frequency) {
        return new RiftDimEnergyStorage(dimStorageManager, frequency);
    }

    @Override
    public String identifier() {
        return "rift_energy";
    }

    @Override
    public void sendClientInfo(Player player, List<AbstractDimStorage> list) {

    }
}
