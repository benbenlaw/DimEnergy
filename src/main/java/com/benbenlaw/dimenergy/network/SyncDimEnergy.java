package com.benbenlaw.dimenergy.network;

import com.benbenlaw.dimenergy.DimEnergy;
import com.benbenlaw.dimenergy.block.entity.BlockEntityDimEnergy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDimEnergy(BlockPos pos, long energy) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncDimEnergy> TYPE =
            new CustomPacketPayload.Type<>(DimEnergy.identifier("sync_dim_energy"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDimEnergy> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SyncDimEnergy::pos,
                    ByteBufCodecs.VAR_LONG, SyncDimEnergy::energy,
                    SyncDimEnergy::new
            );

    public SyncDimEnergy(BlockEntityDimEnergy be) {
        this(be.getBlockPos(), be.getStorage().getAmountAsLong());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDimEnergy msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = ctx.player().level();

            if (level.getBlockEntity(msg.pos) instanceof BlockEntityDimEnergy be) {

                be.energyState.serverEnergy = msg.energy();
                be.energyState.clientEnergy = msg.energy();
            }
        });
    }
}