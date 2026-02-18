package net.colorixer.entity.creeper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.EnumSet;

public class HideGoal extends Goal {
    private final CreeperEntity creeper;
    private BlockPos targetHidePos;

    public HideGoal(CreeperEntity creeper) {
        this.creeper = creeper;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = creeper.getTarget();

        // Trigger if target exists and is NOT in melee range (approx 2 blocks)
        if (target != null && creeper.squaredDistanceTo(target) > 4.0D) {
            // If pathfinding to melee range (1 block) is blocked
            if (creeper.getNavigation().findPathTo(target, 1) == null) {
                this.targetHidePos = findHiddenDarkSpot(target);
                return targetHidePos != null;
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (targetHidePos != null) {
            // Move slightly faster when retreating to stay out of sight
            creeper.getNavigation().startMovingTo(targetHidePos.getX(), targetHidePos.getY(), targetHidePos.getZ(), 1.3D);
        }
    }

    @Override
    public void stop() {
        this.targetHidePos = null;
    }

    @Override
    public boolean shouldContinue() {
        // Keep hiding as long as the player is still unreachable or we haven't reached the spot
        LivingEntity target = creeper.getTarget();
        if (target == null || creeper.getNavigation().findPathTo(target, 1) != null) {
            return false;
        }
        return !creeper.getNavigation().isIdle();
    }

    private BlockPos findHiddenDarkSpot(LivingEntity target) {
        World world = creeper.getWorld();
        BlockPos currentPos = creeper.getBlockPos();
        Vec3d targetPos = target.getEyePos();

        // Search a wider area (15 blocks) to find a good ambush spot
        for (BlockPos checkPos : BlockPos.iterateOutwards(currentPos, 15, 4, 15)) {
            // 1. Must be dark (Light level < 7 is standard for mob spawning/hiding)
            if (world.getLightLevel(LightType.BLOCK, checkPos) < 7) {
                // 2. Must be air and have solid ground below
                if (world.getBlockState(checkPos).isAir() && world.getBlockState(checkPos.down()).isSolid()) {

                    // 3. KEY: The player MUST NOT be able to see this block
                    // We check if there is a line of sight between player's eyes and the spot
                    Vec3d checkVec = Vec3d.ofCenter(checkPos);
                    if (!world.raycast(new net.minecraft.world.RaycastContext(
                            targetPos,
                            checkVec,
                            net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                            net.minecraft.world.RaycastContext.FluidHandling.NONE,
                            creeper)).getType().equals(net.minecraft.util.hit.HitResult.Type.MISS)) {

                        // If the raycast hit a block (Type is NOT MISS), it means the spot is hidden!
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }
}