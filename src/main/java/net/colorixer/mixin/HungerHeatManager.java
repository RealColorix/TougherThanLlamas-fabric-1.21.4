package net.colorixer.mixin;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerHeatManager {

    /* ========================= */
    /* SHADOWED MEMBERS        */
    /* ========================= */

    @Shadow public abstract int getFoodLevel();
    @Shadow public abstract void addExhaustion(float exhaustion);
    @Shadow private float saturationLevel; // Added for the change

    /* ========================= */
    /* INITIALIZATION          */
    /* ========================= */

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ttll$resetStartingSaturation(CallbackInfo ci) {
        this.saturationLevel = 0.0f;
    }

    /* ========================= */
    /* REGEN TIMING            */
    /* ========================= */

    @ModifyConstant(
            method   = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            constant = @Constant(intValue = 10)
    )
    private int hardmod$slowSaturated(int original) {
        return original * 20; // 10 sec healing
    }

    @ModifyConstant(
            method   = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            constant = @Constant(intValue = 80)
    )
    private int hardmod$slowHungry(int original) {
        return original * 5; // 20 sec healing
    }

    /* ========================= */
    /* REGEN THRESHOLD         */
    /* ========================= */

    @ModifyConstant(
            method   = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            constant = @Constant(intValue = 18)
    )
    private int ttll$lowerRegenThreshold(int original) {
        return 4;
    }

    /* ========================= */
    /* REGEN COST / AMOUNT     */
    /* ========================= */

    @ModifyConstant(
            method   = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            constant = @Constant(floatValue = 6.0F)
    )
    private float ttll$foodRegenExhaustion(float original) {
        return 0.5F;
    }

    @ModifyConstant(
            method   = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            constant = @Constant(floatValue = 1.0F)
    )
    private float ttll$foodRegenHeal(float original) {
        return 1.0F;
    }

    /* ========================= */
    /* CONDITIONAL HEALING     */
    /* ========================= */

    /**
     * Hunger healing only happens if:
     * (health * 0.75) < hunger
     */
    @Redirect(
            method = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;heal(F)V"
            )
    )
    private void ttll$conditionalHeal(ServerPlayerEntity player, float amount) {
        if ((player.getHealth() * 0.8f) < this.getFoodLevel()) {
            player.heal(amount);
        }
    }

    /* ========================= */
    /* PASSIVE BIOME DRAIN     */
    /* ========================= */

    private static final float MIN_EXHAUSTION = 0.0013f;
    private static final float MAX_EXHAUSTION = 0.0017f;
    private static final float IDEAL_TEMP = 0.8f;
    private static final float EXTREME_TEMP_DISTANCE = 0.8f;

    @Inject(
            method = "update(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At("TAIL")
    )
    private void hardmod$biomeBasedHunger(ServerPlayerEntity player, CallbackInfo ci) {
        if (player.isCreative() || player.isSpectator()) return;

        Biome biome = player.getWorld()
                .getBiome(player.getBlockPos())
                .value();

        float temperature = biome.getTemperature();
        float distanceFromIdeal = Math.abs(temperature - IDEAL_TEMP);

        float normalized = MathHelper.clamp(
                distanceFromIdeal / EXTREME_TEMP_DISTANCE,
                0.0F,
                1.0F
        );

        float exhaustionPerTick = MathHelper.lerp(
                normalized,
                MIN_EXHAUSTION,
                MAX_EXHAUSTION
        );

        addExhaustion(exhaustionPerTick);
    }
}