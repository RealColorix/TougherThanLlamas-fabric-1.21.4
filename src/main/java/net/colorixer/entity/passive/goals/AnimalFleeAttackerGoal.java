package net.colorixer.entity.passive.goals;

import net.colorixer.entity.passive.goals.AnimalDataAccessor;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;

public class AnimalFleeAttackerGoal extends Goal {
    private final AnimalEntity animal;
    private final double speed;
    private int remainingTicks = 0;
    private final int TOTAL_PANIC_TIME = 600; // 30 Seconds

    public AnimalFleeAttackerGoal(AnimalEntity animal, double speed) {
        this.animal = animal;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        // Pointed at the generic animal accessor
        if (this.animal instanceof AnimalDataAccessor data) {
            return data.ttll$isPanicking();
        }
        return false;
    }

    @Override
    public void start() {
        this.remainingTicks = TOTAL_PANIC_TIME;
        this.findAndMove();
    }

    @Override
    public boolean shouldContinue() {
        return this.remainingTicks > 0;
    }

    @Override
    public void tick() {
        this.remainingTicks--;

        if (this.animal.getNavigation().isIdle() && this.remainingTicks > 0) {
            this.findAndMove();
        }
    }

    private void findAndMove() {
        Vec3d fleePos = null;
        // If there is a direct attacker, run AWAY from them
        if (this.animal.getAttacker() != null) {
            fleePos = NoPenaltyTargeting.findFrom(this.animal, 16, 7, this.animal.getAttacker().getPos());
        }

        // If no attacker or can't find path away, just bolt in a random safe direction
        if (fleePos == null) {
            fleePos = NoPenaltyTargeting.find(this.animal, 16, 7);
        }

        if (fleePos != null) {
            this.animal.getNavigation().startMovingTo(fleePos.x, fleePos.y, fleePos.z, this.speed);
        }
    }

    @Override
    public void stop() {
        if (this.animal instanceof AnimalDataAccessor data) {
            data.ttll$setPanicking(false);
        }
        this.remainingTicks = 0;
        this.animal.getNavigation().stop();
    }
}