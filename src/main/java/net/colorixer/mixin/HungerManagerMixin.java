package net.colorixer.mixin;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * Slow natural healing:
 *   – saturated ½❤  every 200 ticks  (20 × slower than vanilla 10)
 *   – food‑only ½❤  every 400 ticks   (5 × slower than vanilla 80)
 */
@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    /* Saturated case: 10  → 200 */
    @ModifyConstant(
            method      = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            constant    = @Constant(intValue = 10)
    )
    private int hardmod$slowSaturated(int original) {
        return original * 30;   // 10 × 30  = 300
    }

    /* Food‑only case: 80 → 400 */
    @ModifyConstant(
            method      = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            constant    = @Constant(intValue = 80)
    )
    private int hardmod$slowHungry(int original) {
        return original * 8;    // 80 × 5 = 400
    }
}
