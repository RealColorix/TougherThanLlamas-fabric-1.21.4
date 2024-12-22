package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class ReducedMiningSpeed {

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyMiningSpeed(BlockState blockState, CallbackInfoReturnable<Float> cir) {
        // Check if the return value exists
        if (cir.getReturnValue() != null) {
            float originalSpeed = cir.getReturnValue();
            float reducedSpeed = originalSpeed * 0.25f; // Reduce to 0.25x
            cir.setReturnValue(reducedSpeed);
        }
    }
}
