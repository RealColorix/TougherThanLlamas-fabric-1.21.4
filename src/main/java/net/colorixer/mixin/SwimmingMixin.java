package net.colorixer.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class SwimmingMixin extends LivingEntity {

    protected SwimmingMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private boolean ttll$isFlowingWaterNearby(World world, BlockPos center) {
        // Scans a 3x3 area around the player
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                FluidState state = world.getFluidState(center.add(x, 0, z));
                if (!state.isEmpty() && !state.isStill()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void ttll$applySwimmingPenalty(Vec3d movementInput, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getAbilities().creativeMode) return;

        World world = this.getWorld();
        BlockPos pos = this.getBlockPos();

        // 1. HEALTH & HUNGER RATIOS
        float healthRatio = player.getHealth() / player.getMaxHealth();
        float healthMultiplier = healthRatio > 0.25f
                ? 0.8f + (healthRatio - 0.25f) * (0.2f / 0.75f)
                : 0.1f + 0.7f * (healthRatio / 0.25f) * (healthRatio / 0.25f);

        float hungerRatio = player.getHungerManager().getFoodLevel() / 20.0f;
        float hungerMultiplier = hungerRatio > 0.25f
                ? 0.8f + (hungerRatio - 0.25f) * (0.2f / 0.75f)
                : 0.1f + 0.7f * (hungerRatio / 0.25f) * (hungerRatio / 0.25f);

        float totalMultiplier = healthMultiplier * hungerMultiplier;

        // 2. DETECTION
        boolean touchingAnyWater = this.isTouchingWater();
        boolean nearbyFlowing = ttll$isFlowingWaterNearby(world, pos);

        // SUPPORT CHECK (2 blocks down)
        boolean hasSupport = false;
        for (int i = 1; i <= 2; i++) {
            BlockPos checkPos = pos.down(i);
            if (world.getBlockState(checkPos).isSolid() || world.getFluidState(checkPos).isStill()) {
                hasSupport = true;
                break;
            }
        }

        // 3. VELOCITY LOGIC
        Vec3d velocity = this.getVelocity();
        double nextX = velocity.x;
        double nextZ = velocity.z;
        double nextY = velocity.y;

        // Only apply horizontal slowness if actually touching water
        if (touchingAnyWater) {
            nextX *= totalMultiplier;
            nextZ *= totalMultiplier;
        }

        // WATERFALL LOGIC (The "Anti-Bypass" check)
        // If there's flowing water anywhere in the 3x3 and no ground support...
        if (nearbyFlowing && !hasSupport) {
            if (nextY > -0.2) {
                // Smoothly pull them down, even if they are only "grazing" the waterfall
                nextY -= 0.05;
            }
            if (nextY < -0.2) nextY = -0.2;
        } else if (touchingAnyWater && nextY > 0) {
            // Normal penalty for upward swimming in still water
            nextY *= totalMultiplier;
        }

        this.setVelocity(nextX, nextY, nextZ);
    }
}