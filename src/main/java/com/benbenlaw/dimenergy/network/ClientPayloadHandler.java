package com.benbenlaw.dimenergy.network;

import com.benbenlaw.dimenergy.block.entity.BlockEntityDimEnergy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPayloadHandler {

    public static void handleSyncDimEnergy(SyncDimEnergy msg) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        if (level.getBlockEntity(msg.pos()) instanceof BlockEntityDimEnergy be) {
            be.energyState.serverEnergy = msg.energy();
            be.energyState.clientEnergy = msg.energy();
        }
    }
}