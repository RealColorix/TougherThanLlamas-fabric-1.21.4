package net.colorixer.entity.passive.goals;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;
import java.util.List;

public class AnimalFleeZombieGoal extends Goal {
    private final AnimalEntity animal; // Changed from CowEntity
    private final double speed;
    private ZombieEntity targetZombie;
    private double targetX;
    private double targetY;
    private double targetZ;

    public AnimalFleeZombieGoal(AnimalEntity animal, double speed) {
        this.animal = animal;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        // Find zombies within 10 blocks of this animal
        List<ZombieEntity> list = this.animal.getWorld().getEntitiesByClass(
                ZombieEntity.class,
                this.animal.getBoundingBox().expand(10.0, 3.0, 10.0),
                zombie -> true
        );

        if (list.isEmpty()) {
            return false;
        }

        this.targetZombie = list.get(0);
        return this.findTarget();
    }

    protected boolean findTarget() {
        // Find a pathing point away from the target zombie
        Vec3d vec3d = NoPenaltyTargeting.findFrom(this.animal, 16, 7, this.targetZombie.getPos());

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
        return !this.animal.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.animal.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
    }

    @Override
    public void stop() {
        this.targetZombie = null;
    }
}