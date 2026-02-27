package net.colorixer.entity.passive.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.world.ServerWorld;

public class AnimalStampedeAttackGoal extends MeleeAttackGoal {
    private final AnimalEntity animal;

    // Constructor now takes the speed directly as the second argument
    public AnimalStampedeAttackGoal(AnimalEntity animal, double speed) {
        super(animal, speed, true);
        this.animal = animal;
    }

    @Override
    public boolean canStart() {
        // Check if the animal is enraged via your interface
        boolean isEnraged = ((AnimalDataAccessor)this.animal).ttll$isEnraged();

        // Start instantly if enraged, even if super.canStart() hasn't caught up yet
        return !this.animal.isBaby() && isHealthyEnough() && (isEnraged || super.canStart());
    }

    private boolean isHealthyEnough() {
        return this.animal.getHealth() >= (this.animal.getMaxHealth() / 2.0f);
    }

    @Override
    protected void attack(LivingEntity target) {
        // Scaling reach based on animal width
        double maxReachSq = (this.animal.getWidth() * 2.0F) * (this.animal.getWidth() * 2.0F) + target.getWidth();
        double distanceSq = this.animal.squaredDistanceTo(target);

        if (distanceSq <= maxReachSq && this.canAttack(target)) {
            this.resetCooldown();

            // Damage scales based on the animal's Max Health (e.g., Cow > Sheep)
            float baseDamage = this.animal.getMaxHealth() * 0.1f;
            float variance = this.animal.getRandom().nextFloat() * 2.0f;
            float finalDamage = baseDamage * variance;

            if (this.animal.getWorld() instanceof ServerWorld serverWorld) {
                target.damage(serverWorld, this.animal.getDamageSources().mobAttack(this.animal), finalDamage);
                this.animal.swingHand(this.animal.getActiveHand());
            }
        }
    }
}