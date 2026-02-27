package net.colorixer.mixin;

import net.colorixer.entity.passive.goals.AnimalFleeAttackerGoal;
import net.colorixer.entity.passive.goals.AnimalFleeBlockBreakPlaceGoal;
import net.colorixer.entity.passive.goals.AnimalFleeZombieGoal;
import net.colorixer.entity.passive.goals.AnimalStampedeAttackGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PigEntity.class)
public abstract class PigModifier extends AnimalEntity { // ADD THIS EXTENSION


    // Standard Mixin constructor to satisfy the compiler
    protected PigModifier(EntityType<? extends AnimalEntity> type, net.minecraft.world.World world) {
        super(type, world);
    }

    @Inject(method = "initGoals", at = @At("HEAD"), cancellable = true)
    private void ttll$overrideAllSheepGoals(CallbackInfo ci) {
        // Cast to SheepEntity for the goals
        PigEntity pig = (PigEntity) (Object) this;

        this.goalSelector.getGoals().clear();

        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new AnimalStampedeAttackGoal(this, 1.2));
        this.goalSelector.add(2, new AnimalFleeAttackerGoal(this, 1.3));
        this.goalSelector.add(3, new AnimalFleeBlockBreakPlaceGoal(this, 1.4));
        this.goalSelector.add(4, new AnimalFleeZombieGoal(this, 1.3));
        this.goalSelector.add(5, new AnimalMateGoal(this, (double)1.0F));
        this.goalSelector.add(6, new TemptGoal(this, 1.2, (stack) -> stack.isOf(Items.CARROT_ON_A_STICK), false));
        this.goalSelector.add(7, new TemptGoal(this, 1.2, (stack) -> stack.isIn(ItemTags.PIG_FOOD), false));
        this.goalSelector.add(8, new FollowParentGoal(this, 1.1));
        this.goalSelector.add(9, new WanderAroundFarGoal(this, (double)1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(11, new LookAroundGoal(this));

    }
}