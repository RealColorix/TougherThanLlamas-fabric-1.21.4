package net.colorixer.entity.passive.cow;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;
import java.util.List;

public class CowFleeZombieGoal extends Goal {
    private final CowEntity cow;
    private final double speed;
    private ZombieEntity targetZombie;
    private double targetX;
    private double targetY;
    private double targetZ;

    public CowFleeZombieGoal(CowEntity cow, double speed) {
        this.cow = cow;
        this.speed = speed;
        // MOVE control ensures it doesn't try to wander while fleeing
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        // Find zombies within 10 blocks
        List<ZombieEntity> list = this.cow.getWorld().getEntitiesByClass(
                ZombieEntity.class,
                this.cow.getBoundingBox().expand(10.0, 3.0, 10.0),
                zombie -> true
        );

        if (list.isEmpty()) {
            return false;
        }

        this.targetZombie = list.get(0);
        return this.findTarget();
    }

    protected boolean findTarget() {
        // This finds a spot roughly in the opposite direction of the zombie
        Vec3d vec3d = NoPenaltyTargeting.findFrom(this.cow, 16, 7, this.targetZombie.getPos());

        if (vec3d == null) {
            return false;
        } else {
            this.targetX = vec3d.x;
            this.targetY = vec3d.y;
            this.targetZ = vec3d.z;
            return true;
        }
    }

    @Override
    public boolean shouldContinue() {
        // Continue moving until the cow arrives at the safe spot
        return !this.cow.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.cow.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
    }

    @Override
    public void stop() {
        this.targetZombie = null;
    }
}