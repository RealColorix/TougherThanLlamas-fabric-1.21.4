package net.colorixer.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin {

    @Unique private static final Identifier LARGE_WINDOW_TEXTURE = Identifier.of("ttll", "textures/gui/advancements/large_window.png");

    @Unique private static final int NEW_WIDTH = 512;
    @Unique private static final int NEW_HEIGHT = 300;

    // These are the "Inner" dimensions of the window in your large_window.png
    @Unique private static final int NEW_PAGE_WIDTH = 494;
    @Unique private static final int NEW_PAGE_HEIGHT = 264;

    @Inject(method = "drawWindow", at = @At("HEAD"), cancellable = true)
    private void ttll$drawLargeWindow(DrawContext context, int x, int y, CallbackInfo ci) {
        context.drawTexture(
                RenderLayer::getGuiTextured,
                LARGE_WINDOW_TEXTURE,
                x, y,
                0.0F, 0.0F,
                NEW_WIDTH, NEW_HEIGHT,
                512, 512
        );
        // Note: We don't cancel here because the original method still handles
        // drawing the "Stone Age" title and the tabs.
    }

    // FIX 1: The Window "Hitbox" and centering
    @ModifyConstant(method = {"render", "mouseClicked", "drawWindow"}, constant = @Constant(intValue = 252))
    private int changeWidthConstant(int original) { return NEW_WIDTH; }

    @ModifyConstant(method = {"render", "mouseClicked", "drawWindow"}, constant = @Constant(intValue = 140))
    private int changeHeightConstant(int original) { return NEW_HEIGHT; }

    // FIX 2: The "Scissoring" and Background Area
    // This is the most important part. By changing 234 and 113,
    // we tell the game the "Dirt" area is much bigger.
    // This "un-crops" the drawing area so the background can actually show up
    @ModifyConstant(method = {"drawAdvancementTree", "drawWidgetTooltip", "render"}, constant = @Constant(intValue = 234))
    private int changeScissorWidth(int original) { return 494; }

    @ModifyConstant(method = {"drawAdvancementTree", "drawWidgetTooltip", "render"}, constant = @Constant(intValue = 113))
    private int changeScissorHeight(int original) { return 273; }
    // FIX 3: Global Texture Size
    // This stops the game from thinking the image file is only 256x256
    @ModifyConstant(method = "drawWindow", constant = @Constant(intValue = 256))
    private int changeAtlasSize(int original) { return 512; }

    // FIX 4: The Title Text position
    // Since the window is bigger, we nudge the title "Stone Age" so it's not hugging the edge
    @ModifyConstant(method = "drawWindow", constant = @Constant(intValue = 8))
    private int changeTitleX(int original) { return 15; }

    @ModifyConstant(method = "drawWindow", constant = @Constant(intValue = 6))
    private int changeTitleY(int original) { return 8; }
}