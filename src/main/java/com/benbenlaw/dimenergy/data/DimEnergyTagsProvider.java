package com.benbenlaw.dimenergy.data;

import com.benbenlaw.dimenergy.DimEnergy;
import com.benbenlaw.dimenergy.RiftCompat;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class DimEnergyTagsProvider extends BlockTagsProvider {

  public DimEnergyTagsProvider(PackOutput packOutput,
                               CompletableFuture<HolderLookup.Provider> lookupProvider) {
    super(packOutput, lookupProvider, DimEnergy.MOD_ID);
  }

  @Override
  protected void addTags(HolderLookup.Provider provider) {
    // The rift cell only exists when Rifts is installed, so its tag entries are optional
    tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .add(DimEnergy.DIMENERGY.get())
        .addOptional(RiftCompat.DIMRIFTENERGY.get());
    tag(BlockTags.NEEDS_IRON_TOOL)
        .add(DimEnergy.DIMENERGY.get())
        .addOptional(RiftCompat.DIMRIFTENERGY.get());
  }
}