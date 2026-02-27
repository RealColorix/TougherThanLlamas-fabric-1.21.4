package net.colorixer.entity.hostile.creeper;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.EnumSet;

public class ProximityExplosionGoal extends Goal {
    private final CreeperEntity creeper;

    public ProximityExplosionGoal(CreeperEntity creeper) {
        this.creeper = creeper;
        this.setControls(EnumSet.noneOf(Control.class)); // No movement control
    }

    @Override
    public boolean canStart() {
        // Always active while there is a valid target
        return creeper.getTarget() != null && creeper.getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = creeper.getTarget();
        if (target == null || !target.isAlive()) return;

        double distanceSq = creeper.squaredDistanceTo(target);
        if (distanceSq <= 9.0) { // within 3 blocks
            Vec3d creeperEyes = creeper.getCameraPosVec(1.0F);
            Vec3d targetEyes = target.getCameraPosVec(1.0F);

            BlockHitResult hit = creeper.getWorld().raycast(new RaycastContext(
                    creeperEyes,
                    targetEyes,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    creeper
            ));

            if (hit.getType() == BlockHitResult.Type.BLOCK) {
                // Properly trigger explosion countdown
                if (!creeper.isIgnited()) {
                    creeper.ignite(); // sets fuse and starts hissing
                }
            }
        }
    }
}