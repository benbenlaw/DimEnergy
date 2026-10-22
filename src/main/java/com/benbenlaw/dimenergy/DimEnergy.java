package com.benbenlaw.dimenergy;

import com.benbenlaw.dimenergy.block.DimEnergyBlock;
import edivad.dimstorage.blockentity.BlockEntityFrequencyOwner;
import com.benbenlaw.dimenergy.block.DimEnergyCapabilities;
import com.benbenlaw.dimenergy.block.entity.BlockEntityDimEnergy;
import com.benbenlaw.dimenergy.network.DimEnergyNetworking;
import com.benbenlaw.dimenergy.screen.DimEnergyMenu;
import com.benbenlaw.dimenergy.screen.ScreenDimEnergy;
import com.benbenlaw.dimenergy.util.DimEnergyPlugin;
import edivad.dimstorage.DimStorage;
import edivad.dimstorage.blockentity.BlockEntityDimChest;
import edivad.dimstorage.items.ItemDimBase;
import edivad.dimstorage.manager.DimStorageManager;
import edivad.dimstorage.menu.DimChestMenu;
import edivad.dimstorage.setup.DimStorageCreativeModeTabs;
import edivad.dimstorage.setup.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DimEnergy.MOD_ID)
public class DimEnergy {
    public static final String MOD_ID = "dimenergy";
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);

    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY;
    static final DeferredRegister<MenuType<?>> MENU;

    public static final DeferredBlock<DimEnergyBlock> DIMENERGY;
    public static final DeferredItem<BlockItem> DIMENERGY_ITEM;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityDimEnergy>> DIMENERGY_TILE;
    public static final DeferredHolder DIMENERGY_MENU;

    public DimEnergy(final IEventBus eventBus, final ModContainer modContainer) {

        if (RiftCompat.isLoaded()) {
            RiftCompat.init(eventBus);
        }

        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);;
        BLOCK_ENTITY.register(eventBus);
        MENU.register(eventBus);

        eventBus.addListener(this::registerCapabilities);
        eventBus.addListener(this::handleCommonSetup);
        eventBus.addListener(this::registerNetworking);
        eventBus.addListener(this::addItemToCreativeTab);

    }

    static {
        MENU = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);
        BLOCK_ENTITY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);

        DIMENERGY = BLOCKS.registerBlock("dimensional_energy", DimEnergyBlock::new);
        DIMENERGY_ITEM = ITEMS.registerItem("dimensional_energy", (properties) -> new ItemDimBase(DIMENERGY.get(), properties.useBlockDescriptionPrefix()));
        DIMENERGY_TILE = BLOCK_ENTITY.register("dimensional_energy", () -> new BlockEntityType(BlockEntityDimEnergy::new, DIMENERGY.get()));


        DIMENERGY_MENU = MENU.register("dimensional_energy", () -> new MenuType((IContainerFactory)(id, inventory, buf) -> {
            BlockPos pos = buf.readBlockPos();
            BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
            boolean isOpen = buf.readBoolean();
            if (blockEntity instanceof BlockEntityDimEnergy chest) {
                return new DimEnergyMenu(id, inventory.player.getInventory(), chest, isOpen);
            } else {
                DimStorage.LOGGER.error("Wrong type of block entity (expected BlockEntityDimEnergy)!");
                return null;
            }
        }, FeatureFlags.DEFAULT_FLAGS));
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register((MenuType) DIMENERGY_MENU.get(), ScreenDimEnergy::new);
            if (RiftCompat.isLoaded()) {
                RiftCompat.registerScreens(event);
            }


        }
    }
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        DimEnergyCapabilities.registerCapabilities(event);
    }

    private void handleCommonSetup(FMLCommonSetupEvent event) {
        // DimStorage's plugin registry is a plain HashMap, so register on the main thread
        event.enqueueWork(() -> DimStorageManager.registerPlugin(new DimEnergyPlugin()));
    }

    public void registerNetworking(RegisterPayloadHandlersEvent event) {
        DimEnergyNetworking.registerNetworking(event);
    }


    private void addItemToCreativeTab(BuildCreativeModeTabContentsEvent event) {

        if (event.getTab() == DimStorageCreativeModeTabs.DIMSTORAGE_TAB.get()) {

            event.accept(DimEnergy.DIMENERGY.get());
        }
    }

    /** True for the energy cells (and the Rift cell when Rifts is installed), which export instead of "lock". */
    public static boolean isEnergyCell(BlockEntityFrequencyOwner owner) {
        return owner instanceof BlockEntityDimEnergy || (RiftCompat.isLoaded() && RiftCompat.isRiftCell(owner));
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

}
