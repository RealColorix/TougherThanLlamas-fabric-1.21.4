package net.colorixer.mixin;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementObtainedStatus.class)
public class AdvancementObtainedStatusMixin {

    @Inject(method = "getFrameTexture", at = @At("HEAD"), cancellable = true)
    private void swapToCustomFrames(AdvancementFrame frame, CallbackInfoReturnable<Identifier> cir) {
        // We need to know the distance, but this Enum doesn't know about the Widget.
        // However, we only care about "UNOBTAINED" status for your gray/locked tiers.
        AdvancementObtainedStatus status = (AdvancementObtainedStatus)(Object)this;

        if (status == AdvancementObtainedStatus.UNOBTAINED) {
            // Since we can't easily get 'distance' here without complex thread locals,
            // we will use a small trick: check the current widget being rendered.
            // For now, let's keep it simple: if you want distance-based textures,
            // we stick to the Widget Mixin but use the CORRECT target found in your code.
        }
    }
}