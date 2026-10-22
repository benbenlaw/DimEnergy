package com.benbenlaw.dimenergy.data;

import java.util.concurrent.CompletableFuture;

import com.benbenlaw.dimenergy.DimEnergy;
import com.benbenlaw.dimenergy.RiftCompat;
import edivad.dimstorage.setup.ModRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public class DimEnergyRecipeProvider extends RecipeProvider {

  private final HolderLookup.RegistryLookup<Item> items;

  public DimEnergyRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
    super(registries, output);
    this.items = registries.lookupOrThrow(Registries.ITEM);
  }

  public static class Runner extends RecipeProvider.Runner {

    public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
      super(output, registries);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
      return new DimEnergyRecipeProvider(registries, output);
    }

    @Override
    public String getName() {
      return "DimEnergyRecipeProvider";
    }
  }

  @Override
  protected void buildRecipes() {

    ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, DimEnergy.DIMENERGY.get())
            .pattern("aba")
            .pattern("bcb")
            .pattern("ada")
            .define('a', ModRegistration.DIMWALL)
            .define('b', Items.REDSTONE)
            .define('c', Items.REDSTONE_BLOCK)
            .define('d', ModRegistration.SOLIDDIMCORE)
            .unlockedBy(getHasName(Items.REDSTONE_BLOCK), has(Items.REDSTONE_BLOCK))
            .save(output);

    // Placeholder recipe: upgrade an energy cell into a rift energy cell
    ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, RiftCompat.DIMRIFTENERGY.get())
            .requires(DimEnergy.DIMENERGY.get())
            .requires(Items.ENDER_EYE)
            .unlockedBy(getHasName(DimEnergy.DIMENERGY.get()), has(DimEnergy.DIMENERGY.get()))
            .save(output.withConditions(new ModLoadedCondition(RiftCompat.RIFTS_MOD_ID)));
  }
}