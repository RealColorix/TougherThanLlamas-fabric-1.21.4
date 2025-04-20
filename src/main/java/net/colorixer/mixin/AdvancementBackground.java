package net.colorixer.mixin;

import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AdvancementTab.class)
public abstract class AdvancementBackground {

    private static final int SCALE = 16; // your scale factor
    private static final int BASE = 16;  // vanilla tile size

    // Shadow the origin fields so we can access them.
    @Shadow
    private double originX;
    @Shadow
    private double originY;

    /**
     * Modify the X coordinate for drawing the background.
     *
     * Vanilla calculates:
     *   x = (floor(originX) mod 16) + 16 * m
     *
     * We instead want to use the continuous (non-floored) value of originX/SCALE:
     *   x = ( ( (originX / SCALE) mod 16 ) + 16 * m ) * SCALE
     */
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V",
                    ordinal = 0
            ),
            index = 2
    )
    private int modifyX(int original) {
        // Determine the discrete grid index (m) from vanilla’s original calculation.
        int vanillaOrigin = MathHelper.floor(this.originX);
        int vanillaOffset = vanillaOrigin % BASE;
        if (vanillaOffset < 0) {
            vanillaOffset += BASE;
        }
        int m = (original - vanillaOffset) / BASE;

        // Use a continuous offset instead of flooring the scaled origin.
        double continuousOffset = (this.originX / SCALE) % BASE;
        if (continuousOffset < 0) {
            continuousOffset += BASE;
        }
        // Multiply the continuous offset and discrete grid index by SCALE.
        return (int) ((continuousOffset + BASE * m) * SCALE);
    }

    /**
     * Modify the Y coordinate for drawing the background.
     *
     * Vanilla calculates:
     *   y = (floor(originY) mod 16) + 16 * n
     *
     * We use the continuous value of originY/SCALE:
     *   y = ( ( (originY / SCALE) mod 16 ) + 16 * n ) * SCALE
     */
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V",
                    ordinal = 0
            ),
            index = 3
    )
    private int modifyY(int original) {
        // Determine the discrete grid index (n) from vanilla’s original calculation.
        int vanillaOriginY = MathHelper.floor(this.originY);
        int vanillaOffsetY = vanillaOriginY % BASE;
        if (vanillaOffsetY < 0) {
            vanillaOffsetY += BASE;
        }
        int n = (original - vanillaOffsetY) / BASE;

        // Use the continuous (fractional) offset.
        double continuousOffset = (this.originY / SCALE) % BASE;
        if (continuousOffset < 0) {
            continuousOffset += BASE;
        }
        return (int) ((continuousOffset + BASE * n) * SCALE);
    }

    // The following remain unchanged so that the texture is drawn at the correct dimensions.
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V",
                    ordinal = 0
            ),
            index = 6
    )
    private int modifyWidth(int original) {
        return BASE * SCALE;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V",
                    ordinal = 0
            ),
            index = 7
    )
    private int modifyHeight(int original) {
        return BASE * SCALE;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V",
                    ordinal = 0
            ),
            index = 8
    )
    private int modifyTexWidth(int original) {
        return BASE * SCALE;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V",
                    ordinal = 0
            ),
            index = 9
    )
    private int modifyTexHeight(int original) {
        return BASE * SCALE;
    }
}
