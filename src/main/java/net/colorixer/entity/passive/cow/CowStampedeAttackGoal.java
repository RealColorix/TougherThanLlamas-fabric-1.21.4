package net.colorixer.entity.passive.cow;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.server.world.ServerWorld;

public class CowStampedeAttackGoal extends MeleeAttackGoal {
    private final CowEntity cow;

    public CowStampedeAttackGoal(CowEntity cow) {
        super(cow, 1.75, true);
        this.cow = cow;
    }

    @Override
    public boolean canStart() {
        // Must be an adult AND have at least 50% health to start a stampede
        return !this.cow.isBaby() && isHealthyEnough() && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        // If they drop below half health during the fight, they stop the charge
        return !this.cow.isBaby() && isHealthyEnough() && super.shouldContinue();
    }

    @Override
    public void stop() {
        super.stop();
        // If the goal stopped because they are hurt, clear the target
        // This prevents them from "roaming" near the player while still being 'angry'
        if (!isHealthyEnough()) {
            this.cow.setTarget(null);
        }
    }

    private boolean isHealthyEnough() {
        // Checks if current health is >= 50% of max health
        return this.cow.getHealth() >= (this.cow.getMaxHealth() / 2.0f);
    }

    @Override
    protected void attack(LivingEntity target) {
        // 1.3x multiplier for a tight reach
        double maxReachSq = (this.cow.getWidth() * 2F) * (this.cow.getWidth() * 2F) + target.getWidth();
        double distanceSq = this.cow.squaredDistanceTo(target);

        if (distanceSq <= maxReachSq && this.canAttack(target)) {
            this.resetCooldown();

            // Damage: 1.0 to 3.0
            float damage = 1.0f + this.cow.getRandom().nextFloat() * 2.0f;

            if (this.cow.getWorld() instanceof ServerWorld serverWorld) {
                target.damage(serverWorld, this.cow.getDamageSources().mobAttack(this.cow), damage);
                // Note: swingHand usually triggers the front-leg animation in vanilla
                this.cow.swingHand(this.cow.getActiveHand());
            }
        }
    }
}