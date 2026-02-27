package net.colorixer.entity.hostile.skeleton;

import net.colorixer.util.SkeletonConversionTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import java.util.EnumSet;

public class BoneThrowGoal extends Goal {
    private final SkeletonEntity skeleton;
    private int cooldown = -1;
    private int targetInvisibleTicks;
    private boolean movingBackwards;

    public BoneThrowGoal(SkeletonEntity skeleton) {
        this.skeleton = skeleton;
        // MOVE and LOOK controls are required for strafing
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = skeleton.getTarget();
        if (target == null || !target.isAlive()) return false;

        int arrows = ((SkeletonConversionTracker) skeleton).getArrowCount();
        boolean hasNoBow = skeleton.getMainHandStack().getItem() != Items.BOW;

        return arrows <= 0 || hasNoBow;
    }

    @Override
    public void start() {
        // UNEQUIP BOW: If they have a bow but 0 arrows, drop/clear it
        if (skeleton.getMainHandStack().isOf(Items.BOW)) {
            // Option A: Just clear it (cleaner for AI)
            skeleton.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

            // Play a sound to indicate the bow "broke" or was dropped
            skeleton.playSound(net.minecraft.sound.SoundEvents.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
        }
    }

    @Override
    public void tick() {
        LivingEntity target = skeleton.getTarget();
        if (target == null) return;

        double distSq = skeleton.squaredDistanceTo(target);
        boolean canSee = skeleton.getVisibilityCache().canSee(target);

        // Update visibility
        this.targetInvisibleTicks = canSee ? 0 : targetInvisibleTicks + 1;

        // --- MOVEMENT LOGIC ---
        if (distSq <= 225.0 && this.targetInvisibleTicks < 20) {
            this.skeleton.getNavigation().stop();

            if (distSq < 36.0) { // too close
                // Compute vector from target to skeleton
                double dx = skeleton.getX() - target.getX();
                double dz = skeleton.getZ() - target.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0.001) {
                    dx /= len;
                    dz /= len;
                }

                // Move backward along that vector at moderate speed
                skeleton.getMoveControl().strafeTo((float)dx, (float)dz);
            } else {
                // Otherwise strafe side-to-side
                this.skeleton.getLookControl().lookAt(target, 30.0F, 30.0F);
                skeleton.getMoveControl().strafeTo(0.0F, 1.0F);
            }
        } else {
            // Player is far or hidden → navigate toward them
            skeleton.getNavigation().startMovingTo(target, 1.0);
        }

        // --- ATTACK LOGIC ---
        if (--cooldown <= 0 && canSee && distSq < 225.0) {
            this.throwBone(target);
            cooldown = 30 + skeleton.getRandom().nextInt(20);
        }
    }

    private void throwBone(LivingEntity target) {
        if (!(this.skeleton.getWorld() instanceof net.minecraft.server.world.ServerWorld world)) return;

        // Ensure we are facing the target before spawning
        this.skeleton.getLookControl().lookAt(target, 30.0F, 30.0F);

        net.colorixer.entity.projectile.BoneProjectile bone = new net.colorixer.entity.projectile.BoneProjectile(world, this.skeleton);
        bone.setPosition(this.skeleton.getX(), this.skeleton.getEyeY() - 0.1, this.skeleton.getZ());

        double dX = target.getX() - this.skeleton.getX();
        double dY = (target.getY() + (double)target.getStandingEyeHeight() * 0.5) - bone.getY();
        double dZ = target.getZ() - this.skeleton.getZ();
        double horizontalDist = Math.sqrt(dX * dX + dZ * dZ);

        float velocityModifier = 1.5F;
        double gravityCompensation = horizontalDist * 0.115;

        bone.setVelocity(dX, dY + gravityCompensation, dZ, velocityModifier, 0.0F);

        this.skeleton.playSound(net.minecraft.sound.SoundEvents.ENTITY_SKELETON_HURT, 1.0F, 1.5F);
        world.spawnEntity(bone);
    }
}