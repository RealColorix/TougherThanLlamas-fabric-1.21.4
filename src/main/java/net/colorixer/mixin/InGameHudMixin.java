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

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    private void removeVignette(DrawContext context, Entity entity, CallbackInfo ci) {
        // This stops the method before it can draw anything
        ci.cancel();
    }
}