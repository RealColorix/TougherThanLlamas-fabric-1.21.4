package net.colorixer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

    /**
     * We inject at the RETURN of getGamma.
     * This allows us to take the real option and modify its value
     * based on the current world's cheat status.
     */
    @Inject(method = "getGamma", at = @At("RETURN"), cancellable = true)
    private void ttll$conditionalGammaLock(CallbackInfoReturnable<SimpleOption<Double>> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Safety check: Ensure we are actually in-game and the server exists
        if (client != null) {
            IntegratedServer server = client.getServer();

            // Check if cheats are OFF
            if (server != null && !server.getSaveProperties().areCommandsAllowed()) {
                SimpleOption<Double> gammaOption = cir.getReturnValue();

                // Force the value to 0.0 (The "Moody" setting)
                // This makes the internal logic treat brightness as 0%
                gammaOption.setValue(0.0);

                // Return the modified option
                cir.setReturnValue(gammaOption);
            }
        }
    }
}