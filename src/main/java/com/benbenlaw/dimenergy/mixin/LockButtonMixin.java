package com.benbenlaw.dimenergy.mixin;

import com.benbenlaw.dimenergy.DimEnergy;
import com.benbenlaw.dimenergy.block.entity.BlockEntityDimEnergy;
import com.benbenlaw.dimenergy.RiftCompat;
import com.benbenlaw.dimenergy.network.UpdateDimEnergy;
import edivad.dimstorage.blockentity.BlockEntityFrequencyOwner;
import edivad.dimstorage.client.screen.element.button.LockButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LockButton.class)
public abstract class LockButtonMixin {

    @Shadow
    private BlockEntityFrequencyOwner owner;

    @Inject(method = "extractContents", at = @At("HEAD"))
    private void dimenergy$exportText(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (DimEnergy.isEnergyCell(this.owner)) {
            ((AbstractWidget) (Object) this).setMessage(Component.translatable(
                    this.owner.isLocked() ? "gui.dimenergy.true" : "gui.dimenergy.false"));
        }
    }

    @Inject(method = "onPress", at = @At("TAIL"))
    private void dimenergy$onPress(InputWithModifiers input, CallbackInfo ci) {

        if (this.owner instanceof BlockEntityDimEnergy energy) {
            ClientPacketDistributor.sendToServer(new UpdateDimEnergy(energy));
        } else if (RiftCompat.isLoaded()) {
            RiftCompat.sendUpdateIfRift(this.owner);
        }
    }
}