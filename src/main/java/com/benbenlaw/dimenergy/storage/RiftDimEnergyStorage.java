package com.benbenlaw.dimenergy.storage;

import com.benbenlaw.rifts.block.capability.RiftEnergyHandler;
import edivad.dimstorage.api.AbstractDimStorage;
import edivad.dimstorage.api.Frequency;
import edivad.dimstorage.manager.DimStorageManager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class RiftDimEnergyStorage extends AbstractDimStorage implements RiftEnergyHandler {

    private int energy;

    public RiftDimEnergyStorage(DimStorageManager manager, Frequency freq) {
        super(manager, freq);
        this.energy = 0;
    }

    @Override
    public void clearStorage() {
        energy = 0;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("energy", this.energy);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.energy = input.getIntOr("energy", 0);
    }

    @Override
    public String type() {
        return "rift_energy";
    }

    @Override
    public long getAmountAsLong() {
        return this.energy;
    }

    @Override
    public long getCapacityAsLong() {
        return 10000000;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        int space = (int) (getCapacityAsLong() - this.energy);
        int accepted = Math.min(amount, space);

        if (accepted > 0) {
            this.energy += accepted;
            this.setDirty();
        }

        return accepted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        int extracted = Math.min(amount, this.energy);

        if (extracted <= 0) return 0;

        this.energy -= extracted;
        this.setDirty();

        return extracted;
    }

    public int getEnergy() {
        return this.energy;
    }
}
