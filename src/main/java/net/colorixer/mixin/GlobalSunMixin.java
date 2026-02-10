package net.colorixer.mixin;

import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class GlobalSunMixin {

    @Inject(method = "isAffectedByDaylight()Z", at = @At("HEAD"), cancellable = true)
    private void ttll$noSkeletonBurn(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof AbstractSkeletonEntity) {
            cir.setReturnValue(false);
        }
    }
}