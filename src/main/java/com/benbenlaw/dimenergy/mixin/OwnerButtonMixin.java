package com.benbenlaw.dimenergy.mixin;

import com.benbenlaw.dimenergy.block.entity.BlockEntityDimEnergy;
import com.benbenlaw.dimenergy.RiftCompat;
import com.benbenlaw.dimenergy.network.UpdateDimEnergy;
import edivad.dimstorage.blockentity.BlockEntityFrequencyOwner;
import edivad.dimstorage.client.screen.element.button.OwnerButton;
import net.minecraft.client.input.InputWithModifiers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OwnerButton.class)
public abstract class OwnerButtonMixin {

    @Shadow
    private BlockEntityFrequencyOwner owner;

    @Inject(method = "onPress", at = @At("TAIL"))
    private void dimenergy$onPress(InputWithModifiers input, CallbackInfo ci) {

        if (this.owner instanceof BlockEntityDimEnergy energy) {
            ClientPacketDistributor.sendToServer(new UpdateDimEnergy(energy));
        } else if (RiftCompat.isLoaded()) {
            RiftCompat.sendUpdateIfRift(this.owner);
        }
    }
}