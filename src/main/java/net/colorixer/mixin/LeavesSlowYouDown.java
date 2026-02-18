package net.colorixer.mixin;

import net.colorixer.access.LeavesFallAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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

        /* ===================================================== */
        /* PLAYER - EXACTLY YOUR ORIGINAL PHYSICS                */
        /* ===================================================== */
        if (entity instanceof PlayerEntity player) {
            double x = v.x * 0.99;
            double z = v.z * 0.99;
            double y = v.y;

            LeavesFallAccess access = (LeavesFallAccess) player;

            if (y < 0.0 && !access.ttll$hasProcessedLeaves()) {
                float fall = player.fallDistance;
                if (fall > 2.0F && !world.isClient) {
                    float damage = computeLogScaledFall(fall);
                    player.damage((ServerWorld) world, player.getDamageSources().generic(), damage);
                }
                access.ttll$setProcessedLeaves(true);
            }

            if (y < 0.0) {
                player.fallDistance = 0.0F;
            }

            if (y > 0.0) {
                y *= 0.83;
            } else if (y < 0.0) {
                y *= 0.9;
            }

            if (player.isOnGround() || !world.getBlockState(pos).isOf(this)) {
                access.ttll$setProcessedLeaves(false);
            }

            player.setVelocity(x, y, z);
            player.velocityModified = true;
        }

    }

    private static float computeLogScaledFall(float fallDistance) {
        if (fallDistance <= 0.0F) {
            return 0.0F;
        }
        return (float)(fallDistance / (Math.log(fallDistance) + 1.0));
    }
}