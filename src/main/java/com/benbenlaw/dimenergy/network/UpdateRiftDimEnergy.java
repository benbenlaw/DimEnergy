package com.benbenlaw.dimenergy.network;

import com.benbenlaw.dimenergy.DimEnergy;
import com.benbenlaw.dimenergy.RiftCompat;
import com.benbenlaw.dimenergy.block.entity.BlockEntityRiftDimEnergy;
import edivad.dimstorage.api.Frequency;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateRiftDimEnergy(BlockPos pos, Frequency freq, boolean locked) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateRiftDimEnergy> TYPE = new CustomPacketPayload.Type<>(DimEnergy.identifier("update_rift_dim_energy"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateRiftDimEnergy> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, UpdateRiftDimEnergy::pos,
                    Frequency.STREAM_CODEC, UpdateRiftDimEnergy::freq,
                    ByteBufCodecs.BOOL, UpdateRiftDimEnergy::locked,
                    UpdateRiftDimEnergy::new);

    public UpdateRiftDimEnergy(BlockEntityRiftDimEnergy tank) {
        this(tank.getBlockPos(), tank.getFrequency(), tank.isLocked());
    }

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateRiftDimEnergy message, IPayloadContext ctx) {
        Player player = ctx.player();
        Level level = player.level();
        level.getBlockEntity(message.pos, RiftCompat.DIMRIFTENERGY_TILE.get()).ifPresent((tank) -> {
            tank.setFrequency(message.freq);
            tank.setLocked(message.locked);
            level.sendBlockUpdated(message.pos, tank.getBlockState(), tank.getBlockState(), 3);
            player.openMenu(tank, (buf) -> buf.writeBlockPos(message.pos).writeBoolean(true));
        });
    }
}
