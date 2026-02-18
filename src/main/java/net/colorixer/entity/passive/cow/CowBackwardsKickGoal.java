package net.colorixer.entity.passive.cow;

import net.colorixer.util.Kickable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;
import java.util.List;

public class CowBackwardsKickGoal extends Goal {
    private final CowEntity cow;

    public CowBackwardsKickGoal(CowEntity cow) {
        this.cow = cow;
        this.setControls(EnumSet.of(Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        // ADD THIS: If the cow is currently being tempted by wheat, don't try to kick!
        if (this.cow.getBrain().hasMemoryModule(net.minecraft.entity.ai.brain.MemoryModuleType.TEMPTING_PLAYER)) return false;

        if (cow.isBaby()) return false;
        if (!(cow instanceof Kickable k) || k.ttll$getKickCooldown() > 0) return false;
        if (!k.ttll$isEnraged() && cow.getRandom().nextFloat() > 0.05f) return false;

        return !getValidTargets().isEmpty();
    }

    private List<LivingEntity> getValidTargets() {
        double range = (cow instanceof Kickable k && k.ttll$isEnraged()) ? 3.5 : 2.5;

        return cow.getWorld().getEntitiesByClass(LivingEntity.class,
                cow.getBoundingBox().expand(range, 1.0, range),
                entity -> entity != cow
                        && !(entity instanceof CowEntity)
                        && !(entity instanceof net.minecraft.entity.passive.HorseEntity)
                        && !(entity instanceof net.minecraft.entity.passive.MuleEntity)
                        && !(entity instanceof net.minecraft.entity.passive.DonkeyEntity)
                        && !(entity instanceof net.minecraft.entity.passive.PigEntity)
                        && isBehind(entity));
    }


    private boolean isBehind(Entity entity) {
        Vec3d vecToEntity = entity.getPos().subtract(cow.getPos()).normalize();
        Vec3d cowForward = cow.getRotationVec(1.0F);
        return vecToEntity.dotProduct(cowForward) < -0.4;
    }

    @Override
    public void start() {
        executeKick();
    }

    public void executeKick() {
        if (!(cow instanceof Kickable k)) return;
        if (!(cow.getWorld() instanceof ServerWorld world)) return;

        List<LivingEntity> targets = getValidTargets();

        if (!targets.isEmpty()) {
            for (LivingEntity target : targets) {
                target.damage(world, cow.getDamageSources().mobAttack(cow), 7.0f);
                target.takeKnockback(1.0, cow.getX() - target.getX(), cow.getZ() - target.getZ());
                world.playSound(null, cow.getBlockPos(), SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, cow.getSoundCategory(), 1.0f, 0.5f);
            }

            k.ttll$setKickCooldown(100);
            k.ttll$setKickTicks(10);
            k.ttll$setEnraged(false); // Reset rage after successful kick
        }
    }

    @Override
    public void tick() {
        if (this.cow instanceof Kickable k && k.ttll$getKickTicks() > 0) {
            LivingEntity target = cow.getTarget();
            if (target != null) {
                // Calculate direction away from target
                double diffX = cow.getX() - target.getX();
                double diffZ = cow.getZ() - target.getZ();

                // 1. Tell the AI to look at the "opposite" point
                cow.getLookControl().lookAt(cow.getX() + diffX, cow.getEyeY(), cow.getZ() + diffZ);

                // 2. Force the body to follow the head immediately
                // This prevents that slow "drifting" turn
                cow.bodyYaw = cow.headYaw;
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        return cow instanceof Kickable k && k.ttll$getKickTicks() > 0;
    }
}