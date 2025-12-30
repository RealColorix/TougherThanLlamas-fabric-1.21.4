package net.colorixer.mixin;

import net.colorixer.access.LeavesFallAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public abstract class LeavesSlowYouDown extends Block {

    public LeavesSlowYouDown(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        Vec3d v = entity.getVelocity();

        double x = v.x * 0.99;
        double z = v.z * 0.99;
        double y = v.y;

        if (!(entity instanceof LivingEntity living)) {
            entity.setVelocity(x, y, z);
            entity.velocityModified = true;
            return;
        }

        LeavesFallAccess access = (LeavesFallAccess) living;

        /* ===================================================== */
        /* FIRST CONTACT DAMAGE                                  */
        /* ===================================================== */
        if (y < 0.0 && !access.ttll$hasProcessedLeaves()) {
            float fall = living.fallDistance;

            if (fall > 2.0F) {
                float damage = computeLogScaledFall(fall);

                entity.damage(
                        (ServerWorld) world,
                        entity.getDamageSources().generic(),
                        damage
                );
            }

            access.ttll$setProcessedLeaves(true);
        }

        /* ===================================================== */
        /* CRITICAL PART — FULL FALL RESET WHILE FALLING         */
        /* ===================================================== */
        if (y < 0.0) {
            living.fallDistance = 0.0F;
        }

        /* ===================================================== */
        /* VELOCITY MODIFIERS (UNCHANGED)                        */
        /* ===================================================== */
        if (y > 0.0) {
            y *= 0.83;   // jump damping only
        } else if (y < 0.0) {
            y *= 0.9;    // fall damping
        }

        /* ===================================================== */
        /* RESET STATE WHEN LEAVING LEAVES                       */
        /* ===================================================== */
        if (living.isOnGround() || !world.getBlockState(pos).isOf(this)) {
            access.ttll$setProcessedLeaves(false);
        }

        living.setVelocity(x, y, z);
        living.velocityModified = true;
    }

    /**
     * fallDistance / (log(fallDistance) + 1)
     */
    private static float computeLogScaledFall(float fallDistance) {
        if (fallDistance <= 0.0F) {
            return 0.0F;
        }
        return (float)(fallDistance / (Math.log(fallDistance) + 1.0));
    }
}
