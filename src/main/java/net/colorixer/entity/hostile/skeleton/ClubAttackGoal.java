package net.colorixer.entity.hostile.skeleton;

import net.colorixer.item.ModItems;
import net.colorixer.util.SkeletonConversionTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.EnumSet;

public class ClubAttackGoal extends Goal {
    private final SkeletonEntity skeleton;
    private int attackCooldown;
    private ItemStack previousStack = ItemStack.EMPTY;

    public ClubAttackGoal(SkeletonEntity skeleton) {
        this.skeleton = skeleton;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = skeleton.getTarget();
        if (target == null || !target.isAlive()) return false;

        if (skeleton.isConverting()) return false;

        ItemStack currentHand = skeleton.getMainHandStack();
        // Loot protection
        if (!currentHand.isEmpty() && !currentHand.isOf(Items.BOW)) return false;

        return skeleton.squaredDistanceTo(target) <= 10.0;
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    private static final Identifier BONE_CLUB_DAMAGE_ID = Identifier.of("colorixer", "bone_club_damage");

    @Override
    public void start() {
        this.previousStack = skeleton.getMainHandStack().copy();

        if (((SkeletonConversionTracker) skeleton).isConvertingToSkeleton()) {
            this.skeleton.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            this.skeleton.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.BONE_CLUB));
        }

        // Apply the modifier using the new Identifier system
        var instance = skeleton.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (instance != null) {
            // New constructor: Identifier, value, operation
            instance.addTemporaryModifier(new EntityAttributeModifier(
                    BONE_CLUB_DAMAGE_ID, -2.4, EntityAttributeModifier.Operation.ADD_VALUE));
        }

        this.skeleton.calculateDimensions();
    }

    @Override
    public void tick() {
        LivingEntity target = skeleton.getTarget();
        if (target == null) return;

        skeleton.getLookControl().lookAt(target, 30.0F, 30.0F);
        this.skeleton.getNavigation().startMovingTo(target, 1.0);

        if (--attackCooldown <= 0 && skeleton.squaredDistanceTo(target) <= 5) { // Reduced distance to match melee range
            if (this.skeleton.getWorld() instanceof ServerWorld serverWorld) {

                // USE THIS INSTEAD OF target.damage(...)
                // This will respect your leather armor reduction!
                this.skeleton.tryAttack(serverWorld, target);

                this.skeleton.swingHand(Hand.MAIN_HAND);
                this.attackCooldown = 20;
            }
        }
    }

    @Override
    public void stop() {
        this.skeleton.setStackInHand(Hand.MAIN_HAND, this.previousStack);

        // Remove using the Identifier
        var instance = skeleton.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (instance != null) {
            instance.removeModifier(BONE_CLUB_DAMAGE_ID);
        }

        this.previousStack = ItemStack.EMPTY;
        this.skeleton.calculateDimensions();
    }
}