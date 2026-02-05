package net.colorixer.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

    @Inject(method = "getGamma", at = @At("HEAD"), cancellable = true)
    private void lockGammaStatic(CallbackInfoReturnable<SimpleOption<Double>> cir) {
        // We create the option with the standard slider callbacks
        SimpleOption<Double> gammaOption = new SimpleOption<>(
                "options.gamma",
                SimpleOption.emptyTooltip(),
                // This ensures "HARDCORE DARKNESS" is the only text
                (optionText, value) -> Text.translatable("options.gamma.normal").formatted(Formatting.GRAY),
                SimpleOption.DoubleSliderCallbacks.INSTANCE,
                0.0, // Initial value
                value -> {} // Empty consumer: This is the key to stopping the vanilla logic/sound
        );

        // To be 100% sure the value never changes (and sound never plays),
        // we force the value to 0.0 right here.
        gammaOption.setValue(0.0);

        cir.setReturnValue(gammaOption);
    }
}