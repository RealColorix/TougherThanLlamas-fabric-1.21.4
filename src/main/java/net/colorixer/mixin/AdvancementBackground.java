package net.colorixer.mixin;

import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AdvancementTab.class)
public abstract class AdvancementBackground {

    private static final int BASE = 64;
    private static final int TILE_SIZE = BASE * 16;
    private static final int PADDING = 64;

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
        return (int) (m * TILE_SIZE + slide) - PADDING - ((BASE/16)* TILE_SIZE);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0), index = 3)
    private int modifyY(int original) {
        int n = Math.round((float)original / (float)BASE);

        double slideY = this.originY % (double)TILE_SIZE;
        if (slideY < 0) slideY += TILE_SIZE;

        return (int) (n * TILE_SIZE + slideY) - PADDING - ((BASE/16)* TILE_SIZE);
    }

    // --- Over-scrolling Logic ---
    @ModifyArg(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D"), index = 1)
    private double expandScrollMin(double min) { return min - PADDING; }

    @ModifyArg(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D"), index = 2)
    private double expandScrollMax(double max) { return max + PADDING; }

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
    @ModifyConstant(method = {"render", "drawWidgetTooltip", "move"}, constant = @Constant(intValue = 234))
    private int expandWidth(int original) { return 486; }
    @ModifyConstant(method = {"render", "drawWidgetTooltip", "move"}, constant = @Constant(intValue = 113))
    private int expandHeight(int original) { return 253; }
    @ModifyConstant(method = "render", constant = @Constant(intValue = 15))
    private int expandLoopWidth(int original) { return 31; }
    @ModifyConstant(method = "render", constant = @Constant(intValue = 8))
    private int expandLoopHeight(int original) { return 17; }
    @ModifyConstant(method = "render", constant = @Constant(intValue = 117))
    private int centerTreeX(int original) { return 243; }
    @ModifyConstant(method = "render", constant = @Constant(intValue = 56))
    private int centerTreeY(int original) { return 126; }
}