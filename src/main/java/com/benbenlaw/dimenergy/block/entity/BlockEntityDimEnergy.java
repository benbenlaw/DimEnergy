package com.benbenlaw.dimenergy.block.entity;

import com.benbenlaw.dimenergy.DimEnergy;
import com.benbenlaw.dimenergy.network.SyncDimEnergy;
import com.benbenlaw.dimenergy.screen.DimEnergyMenu;
import com.benbenlaw.dimenergy.storage.DimEnergyStorage;
import edivad.dimstorage.api.Frequency;
import edivad.dimstorage.blockentity.BlockEntityFrequencyOwner;
import edivad.dimstorage.manager.DimStorageManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BlockEntityDimEnergy extends BlockEntityFrequencyOwner {

    public long clientEnergy = 0;

    public BlockEntityDimEnergy(BlockPos pos, BlockState state) {
        super(DimEnergy.DIMENERGY_TILE.get(), pos, state);
    }

    public EnergyState energyState = new EnergyState(this.getFrequency()) {
        @Override
        public void sendSyncPacket() {
            PacketDistributor.sendToAllPlayers(
                    new SyncDimEnergy(BlockEntityDimEnergy.this.getBlockPos(),
                            getStorage().getAmountAsLong())
            );
        }
    };

    public void syncToClient(ServerPlayer player) {
        PacketDistributor.sendToPlayer(
                player,
                new SyncDimEnergy(this.getBlockPos(), getStorage().getAmountAsLong())
        );
    }

    public void onServerTick(Level level, BlockPos pos, BlockState state) {
        this.ejectEnergy();
        this.energyState.update(level);
    }

    public void onClientTick(Level level, BlockPos pos, BlockState state) {
        this.energyState.update(level);
    }

    private void ejectEnergy() {
        DimEnergyStorage storage = this.getStorage();

        if (!isLocked()) return;

        if (storage.getAmountAsLong() <= 0) return;

        for (Direction side : Direction.values()) {

            BlockPos targetPos = this.worldPosition.relative(side);
            BlockEntity target = this.level.getBlockEntity(targetPos);

            if (target == null) continue;
            if (this.checkSameFrequency(target)) continue;

            EnergyHandler targetHandler = this.level.getCapability(
                    Capabilities.Energy.BLOCK,
                    targetPos,
                    side.getOpposite()
            );

            if (targetHandler == null) continue;

            try (Transaction tx = Transaction.open(null)) {

                long before = targetHandler.getAmountAsLong();

                long available = storage.getAmountAsLong();
                if (available <= 0) continue;

                long space = targetHandler.getCapacityAsLong() - before;
                if (space <= 0) continue;

                int maxMove = (int) Math.min(available, space);

                int simulated = targetHandler.insert(maxMove, tx);
                if (simulated <= 0) continue;

                long after = targetHandler.getAmountAsLong();

                if (after == before) continue;

                int actualAccepted = (int) Math.min(simulated, after - before);
                if (actualAccepted <= 0) continue;

                int extracted = storage.extract(actualAccepted, tx);
                if (extracted != actualAccepted) continue;

                tx.commit();
            }
        }
    }

    private boolean checkSameFrequency(BlockEntity blockentity) {
        if (blockentity instanceof BlockEntityDimEnergy other) {
            return this.getFrequency().equals(other.getFrequency());
        }
        return false;
    }

    @Override
    public void setFrequency(Frequency frequency) {
        super.setFrequency(frequency);
        if (!this.level.isClientSide()) {
            this.energyState.setFrequency(frequency);
        }
    }

    public @NonNull DimEnergyStorage getStorage() {
        assert this.level != null;
        return (DimEnergyStorage) DimStorageManager.instance(this.level)
                .getStorage(this.getFrequency(), "energy");
    }

    public InteractionResult useItemOn(ServerPlayer player, Level level, BlockPos pos, InteractionHand hand) {
        if (this.canAccess(player)) {
            syncToClient(player);
            player.openMenu(this, buf -> buf.writeBlockPos(this.getBlockPos()).writeBoolean(false));
        } else {
            player.sendSystemMessage(Component.literal("Access Denied!").withStyle(ChatFormatting.RED));
        }
        return InteractionResult.SUCCESS;
    }

    public @Nullable EnergyHandler getEnergyHandler(@Nullable Direction direction) {
        return new EnergyHandler() {

            @Override
            public long getAmountAsLong() {
                return getStorage().getAmountAsLong();
            }

            @Override
            public long getCapacityAsLong() {
                return getStorage().getCapacityAsLong();
            }

            @Override
            public int insert(int amount, TransactionContext ctx) {
                return getStorage().insert(amount, ctx);
            }

            @Override
            public int extract(int amount, TransactionContext ctx) {
                if (!isLocked()) return 0;
                return getStorage().extract(amount, ctx);
            }
        };
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag tag = new CompoundTag();
        tag.store("frequency", Frequency.CODEC, this.getFrequency());
        tag.putBoolean("locked", this.isLocked());
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDataPacket(Connection net, ValueInput input) {
        super.onDataPacket(net, input);
        this.setFrequency(input.read("frequency", Frequency.CODEC).orElseThrow());
        this.setLocked(input.getBooleanOr("locked", false));
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return super.getUpdateTag(registries);
    }

    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
    }

    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DimEnergyMenu(id, inventory, this, false);
    }
}