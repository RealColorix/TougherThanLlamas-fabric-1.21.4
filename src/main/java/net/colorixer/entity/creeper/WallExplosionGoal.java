package net.colorixer.entity.creeper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.EnumSet;

public class WallExplosionGoal extends Goal {

    private final CreeperEntity creeper;
    private LivingEntity target;

    public WallExplosionGoal(CreeperEntity creeper) {
        this.creeper = creeper;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        this.target = this.creeper.getTarget();
        return this.isValidTarget();
    }

    @Override
    public boolean shouldContinue() {
        return this.isValidTarget();
    }

    private boolean isValidTarget() {
        if (this.target == null || !this.target.isAlive()) return false;

        double distSq = this.creeper.squaredDistanceTo(this.target);
        return distSq > 4.0 && distSq <= 64.0;
    }

    @Override
    public void tick() {
        if (this.target == null) return;

        // Check pathing
        Path path = this.creeper.getNavigation().findPathTo(this.target, 0);
        boolean pathBlocked = path == null || !path.reachesTarget();

        // Raycast visibility check
        Vec3d start = this.creeper.getEyePos();
        Vec3d end = this.target.getEyePos();

        BlockHitResult hit = this.creeper.getWorld().raycast(
                new RaycastContext(
                        start,
                        end,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        this.creeper
                )
        );

        boolean wallInWay = hit.getType() == HitResult.Type.BLOCK;

        if (pathBlocked && wallInWay) {
            ignite();
        }
    }

    private void ignite() {
        this.creeper.getNavigation().stop();

        // Force ignition state directly
        this.creeper.setFuseSpeed(1);
    }

    @Override
    public void stop() {
        if (this.creeper.isAlive()) {
            this.creeper.setFuseSpeed(-1);
        }
        this.target = null;
    }
}
