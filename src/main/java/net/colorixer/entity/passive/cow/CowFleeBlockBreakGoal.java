package net.colorixer.entity.passive.cow;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;

public class CowFleeBlockBreakGoal extends Goal {
    private final CowEntity cow;
    private final double speed;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int panicTicks = 0;
    private final int MAX_PANIC_TIME = 100; // 5 seconds

    public CowFleeBlockBreakGoal(CowEntity cow, double speed) {
        this.cow = cow;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        CowHungerAccessor data = (CowHungerAccessor) this.cow;
        // Check the boolean flag from the Mixin
        if (data.ttll$isBlockScared() && this.cow.getAttacker() == null) {
            return this.findTarget();
        }
        return false;
    }

    @Override
    public void start() {
        this.panicTicks = MAX_PANIC_TIME;
        this.cow.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
    }

    @Override
    public boolean shouldContinue() {
        // Keep going as long as the 10-second timer hasn't run out
        return this.panicTicks > 0;
    }

    @Override
    public void tick() {
        if (this.panicTicks > 0) {
            this.panicTicks--;
        }

        // If the cow reaches its target but still has time left, find a NEW spot
        if (this.cow.getNavigation().isIdle() && this.panicTicks > 0) {
            if (this.findTarget()) {
                this.cow.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
            }
        }
    }

    @Override
    public void stop() {
        CowHungerAccessor data = (CowHungerAccessor) this.cow;
        // IMPORTANT: Reset the flag and timer
        data.ttll$setBlockScared(false);
        this.panicTicks = 0;
        this.cow.getNavigation().stop();
    }

    protected boolean findTarget() {
        // Increased range to 20 to make them really bolt
        Vec3d vec3d = NoPenaltyTargeting.find(this.cow, 20, 7);
        if (vec3d == null) return false;

        this.targetX = vec3d.x;
        this.targetY = vec3d.y;
        this.targetZ = vec3d.z;
        return true;
    }
}