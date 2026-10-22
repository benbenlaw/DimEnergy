package com.benbenlaw.dimenergy.block;

import com.benbenlaw.dimenergy.RiftCompat;
import com.benbenlaw.dimenergy.block.entity.BlockEntityRiftDimEnergy;
import edivad.dimstorage.blocks.DimBlockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class RiftDimEnergyBlock extends DimBlockBase {
    public RiftDimEnergyBlock(BlockBehaviour.Properties properties) {
        super(properties.mapColor(MapColor.METAL).sound(SoundType.METAL).requiresCorrectToolForDrops().strength(3.5F).noOcclusion());
    }

    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityRiftDimEnergy(pos, state);
    }

    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createDimBlockTicker(level, blockEntityType, RiftCompat.DIMRIFTENERGY_TILE.get());
    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityRiftDimEnergy cell) {
                if (!player.isCrouching()) {
                    return cell.useItemOn(serverPlayer, level, pos, hand);
                }
            }
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int eventID, int eventParam) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        return blockentity != null && blockentity.triggerEvent(eventID, eventParam);
    }
}
