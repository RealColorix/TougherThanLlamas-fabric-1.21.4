package net.colorixer.entity.passive.cow;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;

public class CowFleeAttackerGoal extends Goal {
    private final CowEntity cow;
    private final double speed;
    private int remainingTicks = 0;
    private final int TOTAL_PANIC_TIME = 600; // 30 Seconds

    public CowFleeAttackerGoal(CowEntity cow, double speed) {
        this.cow = cow;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        CowHungerAccessor data = (CowHungerAccessor) this.cow;
        // ONLY starts if the boolean was flipped by an attack or milking
        return data.ttll$isPanicking();
    }

    @Override
    public void start() {
        this.remainingTicks = TOTAL_PANIC_TIME;
        this.findAndMove();
    }

    @Override
    public boolean shouldContinue() {
        // Run exactly until the 30-second timer hits zero
        return this.remainingTicks > 0;
    }

    @Override
    public void tick() {
        this.remainingTicks--;

        // If cow reaches its spot but time remains, pick a new spot
        if (this.cow.getNavigation().isIdle() && this.remainingTicks > 0) {
            this.findAndMove();
        }
    }

    private void findAndMove() {
        Vec3d fleePos = null;
        if (this.cow.getAttacker() != null) {
            fleePos = NoPenaltyTargeting.findFrom(this.cow, 16, 7, this.cow.getAttacker().getPos());
        }

        if (fleePos == null) {
            fleePos = NoPenaltyTargeting.find(this.cow, 16, 7);
        }

        if (fleePos != null) {
            this.cow.getNavigation().startMovingTo(fleePos.x, fleePos.y, fleePos.z, this.speed);
        }
    }

    @Override
    public void stop() {
        CowHungerAccessor data = (CowHungerAccessor) this.cow;
        data.ttll$setPanicking(false); // Reset the trigger
        this.remainingTicks = 0;
        this.cow.getNavigation().stop();
    }
}