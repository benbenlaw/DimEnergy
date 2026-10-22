package com.benbenlaw.dimenergy;

import com.benbenlaw.dimenergy.block.RiftDimEnergyBlock;
import com.benbenlaw.dimenergy.block.entity.BlockEntityRiftDimEnergy;
import com.benbenlaw.dimenergy.network.SyncRiftDimEnergy;
import com.benbenlaw.dimenergy.network.UpdateRiftDimEnergy;
import com.benbenlaw.dimenergy.screen.RiftDimEnergyMenu;
import com.benbenlaw.dimenergy.screen.ScreenRiftDimEnergy;
import com.benbenlaw.dimenergy.util.RiftDimEnergyPlugin;
import com.benbenlaw.rifts.block.capability.RiftsCapabilities;
import edivad.dimstorage.DimStorage;
import edivad.dimstorage.blockentity.BlockEntityFrequencyOwner;
import edivad.dimstorage.items.ItemDimBase;
import edivad.dimstorage.manager.DimStorageManager;
import edivad.dimstorage.setup.DimStorageCreativeModeTabs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Everything that touches the Rifts mod. This class (and the Rift classes it references) must only be
 * loaded after {@link #isLoaded()} has returned true.
 */
public class RiftCompat {

    public static final String RIFTS_MOD_ID = "rifts";

    public static DeferredBlock<RiftDimEnergyBlock> DIMRIFTENERGY;
    public static DeferredItem<BlockItem> DIMRIFTENERGY_ITEM;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityRiftDimEnergy>> DIMRIFTENERGY_TILE;
    public static DeferredHolder DIMRIFTENERGY_MENU;

    public static boolean isLoaded() {
        return ModList.get().isLoaded(RIFTS_MOD_ID);
    }

    public static void init(IEventBus eventBus) {
        DIMRIFTENERGY = DimEnergy.BLOCKS.registerBlock("dimensional_rift_energy", RiftDimEnergyBlock::new);
        DIMRIFTENERGY_ITEM = DimEnergy.ITEMS.registerItem("dimensional_rift_energy", (properties) -> new ItemDimBase(DIMRIFTENERGY.get(), properties.useBlockDescriptionPrefix()));
        DIMRIFTENERGY_TILE = DimEnergy.BLOCK_ENTITY.register("dimensional_rift_energy", () -> new BlockEntityType(BlockEntityRiftDimEnergy::new, DIMRIFTENERGY.get()));

        DIMRIFTENERGY_MENU = DimEnergy.MENU.register("dimensional_rift_energy", () -> new MenuType((IContainerFactory) (id, inventory, buf) -> {
            BlockPos pos = buf.readBlockPos();
            BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
            boolean isOpen = buf.readBoolean();
            if (blockEntity instanceof BlockEntityRiftDimEnergy cell) {
                return new RiftDimEnergyMenu(id, inventory.player.getInventory(), cell, isOpen);
            } else {
                DimStorage.LOGGER.error("Wrong type of block entity (expected BlockEntityRiftDimEnergy)!");
                return null;
            }
        }, FeatureFlags.DEFAULT_FLAGS));

        eventBus.addListener(RiftCompat::registerCapabilities);
        eventBus.addListener(RiftCompat::handleCommonSetup);
        eventBus.addListener(RiftCompat::addItemToCreativeTab);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(RiftsCapabilities.RIFT_ENERGY, DIMRIFTENERGY_TILE.get(),
                BlockEntityRiftDimEnergy::getEnergyHandler);
    }

    private static void handleCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DimStorageManager.registerPlugin(new RiftDimEnergyPlugin()));
    }

    private static void addItemToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == DimStorageCreativeModeTabs.DIMSTORAGE_TAB.get()) {
            event.accept(DIMRIFTENERGY.get());
        }
    }

    public static void registerNetworking(PayloadRegistrar registrar) {
        registrar.playToServer(UpdateRiftDimEnergy.TYPE, UpdateRiftDimEnergy.STREAM_CODEC, UpdateRiftDimEnergy::handle);
        registrar.playToClient(SyncRiftDimEnergy.TYPE, SyncRiftDimEnergy.STREAM_CODEC, SyncRiftDimEnergy::handle);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register((MenuType) DIMRIFTENERGY_MENU.get(), ScreenRiftDimEnergy::new);
    }

    public static boolean isRiftCell(BlockEntityFrequencyOwner owner) {
        return owner instanceof BlockEntityRiftDimEnergy;
    }

    /** Client side: called from the shared GUI mixins; returns true if the owner was a Rift cell. */
    public static boolean sendUpdateIfRift(BlockEntityFrequencyOwner owner) {
        if (owner instanceof BlockEntityRiftDimEnergy rift) {
            ClientPacketDistributor.sendToServer(new UpdateRiftDimEnergy(rift));
            return true;
        }
        return false;
    }
}
