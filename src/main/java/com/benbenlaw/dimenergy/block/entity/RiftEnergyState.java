package com.benbenlaw.dimenergy.block.entity;

import com.benbenlaw.dimenergy.storage.RiftDimEnergyStorage;
import edivad.dimstorage.api.Frequency;
import edivad.dimstorage.manager.DimStorageManager;
import net.minecraft.world.level.Level;

public abstract class RiftEnergyState {
    public long clientEnergy;
    public long serverEnergy;
    private Frequency frequency;

    public RiftEnergyState(Frequency frequency) {
        this.clientEnergy = 0;
        this.serverEnergy = 0;
        this.frequency = frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public void update(Level level) {
        long prev;
        long next;

        if (level.isClientSide()) {
            prev = this.clientEnergy;
            this.clientEnergy = this.serverEnergy;
            next = this.clientEnergy;
        } else {
            prev = this.serverEnergy;

            RiftDimEnergyStorage storage =
                (RiftDimEnergyStorage) DimStorageManager.instance(level)
                    .getStorage(this.frequency, "rift_energy");

            this.serverEnergy = storage.getAmountAsLong();

            this.sendSyncPacket();
            this.clientEnergy = this.serverEnergy;

            next = this.serverEnergy;
        }

        if (prev != next) {
            this.onEnergyChanged();
        }
    }

    public void onEnergyChanged() {}

    public abstract void sendSyncPacket();
}
