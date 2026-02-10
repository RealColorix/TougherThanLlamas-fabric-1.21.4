package net.colorixer.mixin;

import net.colorixer.util.ExhaustionHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Function;

@Mixin(InGameHud.class)
public abstract class HungerJitterHudMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$updateJitterTimer(CallbackInfo ci) {
        ExhaustionHelper.tick();
    }

    @Redirect(
            method = "renderFood",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V")
    )
    private void ttll$jitterDraw(DrawContext instance, Function<Identifier, net.minecraft.client.render.RenderLayer> renderLayers, Identifier texture, int x, int y, int width, int height) {
        int animatedY = y;

        if (ExhaustionHelper.getJitterTimer() > 0) {
            // SPEED: Changed 0.01 to 0.03 for a much faster vibration
            double speed = System.currentTimeMillis() * 0.03;

            // UNSYNC: Using a larger prime-ish multiplier for 'x'
            // ensures the icons don't look like they are moving together
            double offset = x * 12.345;

            // INTENSITY: The '1.2' is the height of the bounce
            animatedY += (int) (Math.sin(speed + offset) * 1.2);
        }

        instance.drawGuiTexture(renderLayers, texture, x, animatedY, width, height);
    }
}