package net.colorixer.mixin;

import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(AdvancementTab.class)
public abstract class AdvancementBackground {

    private static final int BASE = 64;
    private static final int TILE_SIZE = BASE * 16;
    private static final int Y_PADDING = 113;
    private static final int X_PADDING = 229;

    @Shadow private double originX;
    @Shadow private double originY;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0), index = 2)
    private int modifyX(int original) {
        // We calculate exactly which 'tile index' this is without using vanilla's rounded offsets.
        // This is the only way to ensure the background doesn't trail the widgets by one frame.
        int m = Math.round((float)original / (float)BASE);

        double slide = this.originX % (double)TILE_SIZE;
        if (slide < 0) slide += TILE_SIZE;

        // Final position uses the raw double originX to stay frame-perfect.
        return (int) (m * TILE_SIZE + slide) - X_PADDING -16 - ((BASE/16)* TILE_SIZE);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0), index = 3)
    private int modifyY(int original) {
        int n = Math.round((float)original / (float)BASE);

        double slideY = this.originY % (double)TILE_SIZE;
        if (slideY < 0) slideY += TILE_SIZE;

        return (int) (n * TILE_SIZE + slideY) - Y_PADDING-16 - ((BASE/16)* TILE_SIZE);
    }

    // --- Over-scrolling Logic ---
    @Shadow private int maxPanX;
    @Shadow private int maxPanY;
    @Shadow private int minPanX;
    @Shadow private int minPanY;
    // X-Axis (Ordinal 0)
    @ModifyArg(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D", ordinal = 0), index = 1)
    private double expandScrollMinX(double min) {
        // We ignore the 'min' argument (which is broken by the 1000 constant)
        // We calculate the stop point: -(TreeWidth - ScreenWidth) - Padding
        // ScreenWidth is 486
        return (double)(-(this.maxPanX - 486)) - X_PADDING;
    }

    @ModifyArg(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D", ordinal = 0), index = 2)
    private double expandScrollMaxX(double max) {
        // Stops precisely X_PADDING away from the leftmost advancement
        return (double)(-this.minPanX) + X_PADDING;
    }

    // Y-Axis (Ordinal 1)
    @ModifyArg(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D", ordinal = 1), index = 1)
    private double expandScrollMinY(double min) {
        // ScreenHeight is 253
        return (double)(-(this.maxPanY - 253)) - Y_PADDING;
    }

    @ModifyArg(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D", ordinal = 1), index = 2)
    private double expandScrollMaxY(double max) {
        return (double)(-this.minPanY) + Y_PADDING;
    }

    // --- HD Texture Scaling (Stays the same) ---
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0), index = 6)
    private int modifyWidth(int original) { return TILE_SIZE; }
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0), index = 7)
    private int modifyHeight(int original) { return TILE_SIZE; }
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0), index = 8)
    private int modifyTexWidth(int original) { return TILE_SIZE; }
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0), index = 9)
    private int modifyTexHeight(int original) { return TILE_SIZE; }

    // --- Window Bounds (486x253) ---
    // --- Fix: Force the 'if' checks to pass by faking the window size ---

    // --- Window Bounds Expansion ---
    // This expands the window for rendering and tooltips to 486x253
    @ModifyConstant(method = {"render", "drawWidgetTooltip"}, constant = @Constant(intValue = 234))
    private int expandWidth(int original) { return 486; }

    @ModifyConstant(method = {"render", "drawWidgetTooltip"}, constant = @Constant(intValue = 113))
    private int expandHeight(int original) { return 253; }

    // Target ONLY the move method constants
    @ModifyConstant(method = "move", constant = @Constant(intValue = 234))
    private int windowWidthForClamp(int original) {
        return 0; // This stops the 'Right' scroll at the tree edge
    }

    @ModifyConstant(method = "move", constant = @Constant(intValue = 113))
    private int windowHeightForClamp(int original) {
        return 0; // This stops the 'Down' scroll at the tree edge
    }



    @ModifyConstant(method = "render", constant = @Constant(intValue = 15))
    private int expandLoopWidth(int original) { return 31; }
    @ModifyConstant(method = "render", constant = @Constant(intValue = 8))
    private int expandLoopHeight(int original) { return 17; }
    @ModifyConstant(method = "render", constant = @Constant(intValue = 117))
    private int centerTreeX(int original) { return 243; }
    @ModifyConstant(method = "render", constant = @Constant(intValue = 56))
    private int centerTreeY(int original) { return 126; }
}