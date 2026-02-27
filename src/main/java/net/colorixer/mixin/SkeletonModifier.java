package net.colorixer.mixin;

import net.colorixer.entity.hostile.skeleton.BoneThrowGoal;
import net.colorixer.entity.hostile.skeleton.ClubAttackGoal;
import net.colorixer.util.GoalSelectorUtilForMob;
import net.colorixer.util.SkeletonConversionTracker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeletonEntity.class)
public class SkeletonModifier {

    @Inject(method = "initGoals", at = @At("HEAD"), cancellable = true)
    private void ttll$replaceAllGoals(CallbackInfo ci) {
        if (!((Object) this instanceof SkeletonEntity skeleton)) return;


        GoalSelectorUtilForMob.addGoal(skeleton, 1, new ClubAttackGoal(skeleton));
        GoalSelectorUtilForMob.addGoal(skeleton, 2, new BoneThrowGoal(skeleton));
        // ===== VANILLA MOVEMENT GOALS =====
        GoalSelectorUtilForMob.addGoal(skeleton, 2, new AvoidSunlightGoal(skeleton));
        GoalSelectorUtilForMob.addGoal(skeleton, 3, new EscapeSunlightGoal(skeleton, 1.0D));
        GoalSelectorUtilForMob.addGoal(skeleton, 3, new FleeEntityGoal<>(skeleton, WolfEntity.class, 6.0F, 1.0D, 1.2D));
        GoalSelectorUtilForMob.addGoal(skeleton, 5, new WanderAroundFarGoal(skeleton, 1.0D));
        GoalSelectorUtilForMob.addGoal(skeleton, 6, new LookAtEntityGoal(skeleton, PlayerEntity.class, 8.0F));
        GoalSelectorUtilForMob.addGoal(skeleton, 6, new LookAroundGoal(skeleton));

        // ===== VANILLA TARGET GOALS =====
        GoalSelectorUtilForMob.addTargetGoal(skeleton, 1, new RevengeGoal(skeleton));
        GoalSelectorUtilForMob.addTargetGoal(skeleton, 2, new ActiveTargetGoal<>(skeleton, PlayerEntity.class, true));
        GoalSelectorUtilForMob.addTargetGoal(skeleton, 3, new ActiveTargetGoal<>(skeleton, IronGolemEntity.class, true));
        GoalSelectorUtilForMob.addTargetGoal(
                skeleton,
                3,
                new ActiveTargetGoal<>(
                        skeleton,
                        TurtleEntity.class,
                        10,
                        true,
                        false,
                        TurtleEntity.BABY_TURTLE_ON_LAND_FILTER
                )
        );

        // ===== YOUR CUSTOM GOALS =====

        ci.cancel();
    }

    @Inject(method = "shootAt", at = @At("TAIL"))
    private void ttll$reduceArrowCount(LivingEntity target, float pullProgress, CallbackInfo ci) {
        SkeletonConversionTracker tracker = (SkeletonConversionTracker) this;
        int currentArrows = tracker.getArrowCount();
        if (currentArrows > 0) {
            tracker.setArrowCount(currentArrows - 1);
        }
    }
}