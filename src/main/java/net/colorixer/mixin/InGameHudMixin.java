package net.colorixer.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    // Target "renderVignetteOverlay" to match your base code Line 193
    // Descriptor must match: (Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/Entity;)V
    @Inject(
            method = "renderVignetteOverlay",
            at = @At("HEAD"),
            cancellable = true
    )
    private void removeVignette(DrawContext context, Entity entity, CallbackInfo ci) {
        // Cancel the method immediately
        ci.cancel();
    }
}