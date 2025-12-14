package net.colorixer.util;

import net.colorixer.mixin.MobEntityAccessor;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class GoalSelectorUtilForSpiderMixin {

    public static void addGoal(MobEntity mob, int priority, Goal goal) {
        ((MobEntityAccessor) mob)
                .ttll$getGoalSelector()
                .add(priority, goal);
    }
}
