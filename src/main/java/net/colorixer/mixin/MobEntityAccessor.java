package net.colorixer.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobEntity.class)
public interface MobEntityAccessor {

    @Accessor("goalSelector")
    GoalSelector ttll$getGoalSelector();

    @Accessor("targetSelector")
    GoalSelector ttll$getTargetSelector();

    @Invoker("isAffectedByDaylight")
    boolean callIsAffectedByDaylight();

    @Invoker("getDropChance")
    float callGetDropChance(EquipmentSlot slot);

    @Invoker("updateDropChances")
    void callUpdateDropChances(EquipmentSlot slot);

}
