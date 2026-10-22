package com.benbenlaw.dimenergy.screen;

import com.benbenlaw.dimenergy.DimEnergy;
import edivad.dimstorage.client.screen.pattern.FrequencyScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ScreenRiftDimEnergy extends FrequencyScreen<RiftDimEnergyMenu> {

    private static final Identifier DIMENERGY_GUI =
            DimEnergy.identifier("textures/gui/dimenergy.png");

    private static final Identifier ENERGY_BAR =
            DimEnergy.identifier("energy_bar");

    public ScreenRiftDimEnergy(RiftDimEnergyMenu container, Inventory inventory, Component text) {
        super(container, container.owner, inventory, text, DIMENERGY_GUI, container.isOpen);
    }

    @Override
    protected void init() {
        super.init();
        this.drawSettings(this.drawSettings);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractBackground(guiGraphics, mouseX, mouseY, a);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, DIMENERGY_GUI,
                x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        int currentEnergy = Math.toIntExact(menu.owner.energyState.clientEnergy);
        int maxEnergy = menu.owner.getStorage().getCapacityAsInt();

        int barHeight = 60;
        int filled = (int)((currentEnergy / (float) maxEnergy) * barHeight);
        int topOffset = barHeight - filled;

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                ENERGY_BAR,
                16, barHeight,
                0, topOffset,
                x + 11,
                y + 21 + topOffset,
                16,
                filled
        );

        boolean locked = menu.owner.isLocked();
        guiGraphics.text(this.font, Component.literal("Exporting: " + locked), x + 50, y + 35, -13421773, false);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int xm, int ym) {
        super.extractLabels(guiGraphics, xm, ym);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int barX = x + 11;
        int barY = y + 21;

        int currentEnergy = Math.toIntExact(menu.owner.energyState.clientEnergy);
        int maxEnergy = menu.owner.getStorage().getCapacityAsInt();

        if (mouseX >= barX && mouseX <= barX + 16 &&
                mouseY >= barY && mouseY <= barY + 60) {

            Component text = Component.literal(
                    "Rift Energy: " + currentEnergy + " / " + maxEnergy
            );

            List<ClientTooltipComponent> components =
                    List.of(ClientTooltipComponent.create(text.getVisualOrderText()));

            graphics.tooltip(this.font, components, mouseX, mouseY,
                    DefaultTooltipPositioner.INSTANCE, null);
        }
    }
}
