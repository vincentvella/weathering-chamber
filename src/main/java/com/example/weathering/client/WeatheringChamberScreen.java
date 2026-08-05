package com.example.weathering.client;

import com.example.weathering.menu.WeatheringChamberMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WeatheringChamberScreen extends AbstractContainerScreen<WeatheringChamberMenu> {
    // Custom container background: water-blue title wash, aligned slots, and a
    // water->sand progress arrow. Lives at assets/weathering/textures/gui/.
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("weathering", "textures/gui/weathering_chamber.png");

    public WeatheringChamberScreen(WeatheringChamberMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    // The GUI rendering API was reworked for the Blaze3D/Vulkan backend in 26.x:
    // GuiGraphics -> GuiGraphicsExtractor, renderBg -> extractBackground, and blit now
    // takes a RenderPipeline as its first argument.
    //? if >=26.1 {
    /*@Override
    public void extractBackground(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE,
            x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.isCrafting()) {
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE,
                x + 79, y + 34, 176.0F, 14.0F, this.menu.getScaledProgress(), 16, 256, 256);
        }
    }
    *///?} else {
    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.isCrafting()) {
            graphics.blit(TEXTURE, x + 79, y + 34, 176.0F, 14.0F, this.menu.getScaledProgress(), 16, 256, 256);
        }
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    //?}
}
