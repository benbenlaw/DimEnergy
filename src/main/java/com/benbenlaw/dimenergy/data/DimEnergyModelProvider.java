package com.benbenlaw.dimenergy.data;

import java.util.stream.Stream;

import com.benbenlaw.dimenergy.DimEnergy;
import com.benbenlaw.dimenergy.RiftCompat;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;

public class DimEnergyModelProvider extends ModelProvider {

  public DimEnergyModelProvider(PackOutput output) {
    super(output, DimEnergy.MOD_ID);
  }

  @Override
  protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    createDimEnergy(blockModels, DimEnergy.DIMENERGY.get());
    createDimEnergy(blockModels, RiftCompat.DIMRIFTENERGY.get());
  }

  private void createDimEnergy(BlockModelGenerators blockModels, Block block) {
    var tm = new TextureMapping()
        .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_bottom"))
        .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top"))
        .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_sides"))
        .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_sides"))
        .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_sides"))
        .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_sides"))
        .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_top"));

    var model = ModelTemplates.CUBE
        .extend().transform(ItemDisplayContext.GUI, transformVecBuilder -> {
          transformVecBuilder.rotation(25, 45, 0F)
              .translation(0F, 0F, 0F)
              .scale(0.625F);
        }).build()
        .create(block, tm, blockModels.modelOutput);

    blockModels.blockStateOutput.accept(BlockModelGenerators
        .createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
    blockModels.registerSimpleItemModel(block, model);
  }

  @Override
  protected Stream<? extends Holder<Block>> getKnownBlocks() {
    return Stream.of();
  }

  @Override
  protected Stream<? extends Holder<Item>> getKnownItems() {
    return Stream.of();
  }
}