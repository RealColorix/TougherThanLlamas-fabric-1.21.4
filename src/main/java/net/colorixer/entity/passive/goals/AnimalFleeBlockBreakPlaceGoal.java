package net.colorixer.entity.passive.goals;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;

public class AnimalFleeBlockBreakPlaceGoal extends Goal {
    private final AnimalEntity animal;
    private final double speed;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int panicTicks = 0;
    private final int MAX_PANIC_TIME = 100;

    public AnimalFleeBlockBreakPlaceGoal(AnimalEntity animal, double speed) {
        this.animal = animal;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        AnimalDataAccessor data = (AnimalDataAccessor) this.animal;
        // Now checks the generic AnimalDataAccessor
        if (data.ttll$isBlockScared() && this.animal.getAttacker() == null) {
            return this.findTarget();
        }
        return false;
    }

    @Override
    public void start() {
        this.panicTicks = MAX_PANIC_TIME;
        this.animal.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
    }

    @Override
    public boolean shouldContinue() {
        return this.panicTicks > 0;
    }

    @Override
    public void tick() {
        if (this.panicTicks > 0) {
            this.panicTicks--;
        }

        if (this.animal.getNavigation().isIdle() && this.panicTicks > 0) {
            if (this.findTarget()) {
                this.animal.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
            }
        }
    }

    @Override
    public void stop() {
        AnimalDataAccessor data = (AnimalDataAccessor) this.animal;
        data.ttll$setBlockScared(false);
        this.panicTicks = 0;
        this.animal.getNavigation().stop();
    }

    protected boolean findTarget() {
        Vec3d vec3d = NoPenaltyTargeting.find(this.animal, 20, 7);
        if (vec3d == null) return false;

        this.targetX = vec3d.x;
        this.targetY = vec3d.y;
        this.targetZ = vec3d.z;
        return true;
    }
}