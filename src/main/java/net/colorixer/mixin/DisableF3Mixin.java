package net.colorixer.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public class DisableF3Mixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 1. Block Perspective Toggle (Checks custom 'V' bind)
        if (client.options.togglePerspectiveKey.matchesKey(key, scancode)) {
            if (!client.player.hasPermissionLevel(2)) {
                client.options.setPerspective(Perspective.FIRST_PERSON);
                ci.cancel();
            }
        }

        // 2. Block the raw F3 key (Keycode 292)
        if (key == GLFW.GLFW_KEY_F3) {
            if (!client.player.hasPermissionLevel(2)) {
                ci.cancel();
            }
        }
    }

    // 3. Blocks standard F3 combos (F3+G, F3+H, F3+B, etc.)
    @Inject(method = "processF3", at = @At("HEAD"), cancellable = true)
    private void onProcessF3(int key, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && !client.player.hasPermissionLevel(2)) {
            cir.setReturnValue(true); // Tells the game "I handled this"
            cir.cancel();
        }
    }

    // 4. Blocks render debug combos (F3+E, F3+W, F3+F, etc.)
    @Inject(method = "processDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onProcessDebugKeys(int key, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && !client.player.hasPermissionLevel(2)) {
            cir.setReturnValue(true); // Tells the game "I handled this"
            cir.cancel();
        }
    }
}