package com.benbenlaw.dimenergy.mixin;

import com.benbenlaw.dimenergy.DimEnergy;
import com.benbenlaw.dimenergy.block.entity.BlockEntityDimEnergy;
import com.benbenlaw.dimenergy.RiftCompat;
import com.benbenlaw.dimenergy.network.UpdateDimEnergy;
import edivad.dimstorage.blockentity.BlockEntityFrequencyOwner;
import edivad.dimstorage.client.screen.pattern.FrequencyScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FrequencyScreen.class)
public abstract class FrequencyScreenMixin {

    @Shadow
    protected BlockEntityFrequencyOwner blockEntityFrequencyOwner;

    @Shadow
    @Final
    private static Component LOCKED;

    // Our cells export energy rather than being "locked", so relabel the panel entry for them only
    @Redirect(method = "extractLabels", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
            target = "Ledivad/dimstorage/client/screen/pattern/FrequencyScreen;LOCKED:Lnet/minecraft/network/chat/Component;"))
    private Component dimenergy$lockedLabel() {
        if (DimEnergy.isEnergyCell(this.blockEntityFrequencyOwner)) {
            return Component.translatable("gui.dimenergy.exporting");
        }
        return LOCKED;
    }

    @Inject(method = "changeFrequency", at = @At("TAIL"))
    private void dimenergy$sendEnergyPacket(CallbackInfo ci) {
        if (this.blockEntityFrequencyOwner instanceof BlockEntityDimEnergy energy) {
            ClientPacketDistributor.sendToServer(new UpdateDimEnergy(energy));
        } else if (RiftCompat.isLoaded()) {
            RiftCompat.sendUpdateIfRift(this.blockEntityFrequencyOwner);
        }
    }
}