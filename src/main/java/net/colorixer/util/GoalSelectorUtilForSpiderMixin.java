package net.colorixer.util;

import net.colorixer.mixin.MobEntityAccessor;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class GoalSelectorUtilForSpiderMixin {

    /* -------- NORMAL GOALS -------- */
    public static void addGoal(MobEntity mob, int priority, Goal goal) {
        ((MobEntityAccessor) mob)
                .ttll$getGoalSelector()
                .add(priority, goal);
    }

    /* -------- TARGET GOALS -------- */
    public static void addTargetGoal(MobEntity mob, int priority, Goal goal) {
        ((MobEntityAccessor) mob)
                .ttll$getTargetSelector()
                .add(priority, goal);
    }
}
