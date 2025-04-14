package net.colorixer.mixin;

import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AdvancementTab.class)
public abstract class AdvancementBackground {

    private static final int SCALE = 4; // 4x scale = 64x64 tiles
    private static final int BASE = 16; // Vanilla tile size

    // Modify screen X position (k + 64 * m)
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V",
                    ordinal = 0
            ),
            index = 2
    )
    private int modifyX(int x) {
        return x * SCALE;
    }

    // Modify screen Y position (l + 64 * n)
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V",
                    ordinal = 0
            ),
            index = 3
    )
    private int modifyY(int y) {
        return y * SCALE;
    }

    // Width of draw region
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
        return BASE * SCALE; // 16 * 4 = 64
    }

    // Height of draw region
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

    // Texture width
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

    // Texture height
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
