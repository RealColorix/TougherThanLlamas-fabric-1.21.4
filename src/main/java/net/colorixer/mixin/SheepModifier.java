package net.colorixer.mixin;

import net.colorixer.entity.passive.goals.AnimalFleeAttackerGoal;
import net.colorixer.entity.passive.goals.AnimalFleeBlockBreakPlaceGoal;
import net.colorixer.entity.passive.goals.AnimalFleeZombieGoal;
import net.colorixer.entity.passive.goals.AnimalStampedeAttackGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SheepEntity.class)
public abstract class SheepModifier extends AnimalEntity { // ADD THIS EXTENSION

    @Shadow private EatGrassGoal eatGrassGoal;

    // Standard Mixin constructor to satisfy the compiler
    protected SheepModifier(EntityType<? extends AnimalEntity> type, net.minecraft.world.World world) {
        super(type, world);
    }

    @Inject(method = "initGoals", at = @At("HEAD"), cancellable = true)
    private void ttll$overrideAllSheepGoals(CallbackInfo ci) {
        // Cast to SheepEntity for the goals
        SheepEntity sheep = (SheepEntity) (Object) this;

        this.goalSelector.getGoals().clear();
        this.eatGrassGoal = new EatGrassGoal(sheep);

        this.goalSelector.add(0, new SwimGoal(sheep));
        this.goalSelector.add(1, new AnimalFleeAttackerGoal(sheep, 1.7));
        this.goalSelector.add(2, new AnimalFleeBlockBreakPlaceGoal(sheep, 1.5));
        this.goalSelector.add(3, new AnimalFleeZombieGoal(sheep, 1.5));
        this.goalSelector.add(5, new AnimalMateGoal(sheep, 1.0));
        this.goalSelector.add(6, new TemptGoal(sheep, 1.1, (stack) -> stack.isIn(ItemTags.SHEEP_FOOD), true));
        this.goalSelector.add(7, new FollowParentGoal(sheep, 1.1));
        this.goalSelector.add(8, this.eatGrassGoal);
        this.goalSelector.add(9, new WanderAroundFarGoal(sheep, 1.0));
        this.goalSelector.add(10, new LookAtEntityGoal(sheep, PlayerEntity.class, 10.0F));
        this.goalSelector.add(11, new LookAroundGoal(sheep));
    }
}