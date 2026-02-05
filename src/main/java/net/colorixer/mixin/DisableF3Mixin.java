package net.colorixer.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class DisableF3Mixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        // GLFW_KEY_F3 is the constant for the F3 key
        if (key == GLFW.GLFW_KEY_F3) {
            MinecraftClient client = MinecraftClient.getInstance();

            // If player doesn't have permission level 2 (Cheats), cancel the key press
            if (client.player != null && !client.player.hasPermissionLevel(2)) {
                ci.cancel();
            }
        }
    }
}