package net.colorixer.mixin;

import net.colorixer.entity.passive.cow.*;
import net.colorixer.entity.passive.goals.AnimalFleeAttackerGoal;
import net.colorixer.entity.passive.goals.AnimalFleeBlockBreakPlaceGoal;
import net.colorixer.entity.passive.goals.AnimalFleeZombieGoal;
import net.colorixer.entity.passive.goals.AnimalStampedeAttackGoal;
import net.colorixer.util.Kickable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CowEntity.class)
public abstract class CowModifier extends AnimalEntity implements Kickable {
    @Unique private CowBackwardsKickGoal kickGoal;

    protected CowModifier(EntityType<? extends AnimalEntity> type, net.minecraft.world.World world) {
        super(type, world);
    }

    @Override
    public void ttll$requestKick() {
        if (this.kickGoal != null) {
            this.kickGoal.executeKick();
        }
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void ttll$addGoals(CallbackInfo ci) {
        // Only add these goals if the entity is actually a Cow
        if (!((Object) this instanceof CowEntity cow)) return;

        this.goalSelector.getGoals().clear();
        this.kickGoal = new CowBackwardsKickGoal(cow);
        this.goalSelector.add(0, new SwimGoal(cow));
        this.goalSelector.add(1, this.kickGoal);
        this.goalSelector.add(2, new AnimalStampedeAttackGoal(cow, 1.8));
        this.goalSelector.add(3, new AnimalFleeAttackerGoal(cow, 2.0));
        this.goalSelector.add(4, new AnimalFleeBlockBreakPlaceGoal(this, 1.8));
        this.goalSelector.add(5, new AnimalMateGoal(cow, 1.0));
        this.goalSelector.add(6, new AnimalFleeZombieGoal(cow, 1.5));
        this.goalSelector.add(7, new TemptGoal(cow, 1.25, stack -> stack.isIn(ItemTags.COW_FOOD), false));
        this.goalSelector.add(8, new CowEatGrassGoal(cow));
        this.goalSelector.add(9, new FollowParentGoal(cow, 1.25));
        this.goalSelector.add(11, new WanderAroundFarGoal(cow, 1.0));
        this.goalSelector.add(13, new LookAtEntityGoal(cow, PlayerEntity.class, 6.0F));
        this.goalSelector.add(15, new LookAroundGoal(cow));
    }

    @Inject(method = "createChild", at = @At("RETURN"))
    private void ttll$onBirth(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<CowEntity> cir) {
        ((CowHungerAccessor) this).ttll$setLastBirthTime(this.getWorld().getTime());
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void ttll$handleMilking(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (itemStack.isOf(Items.BUCKET) && !this.isBaby()) {
            CowHungerAccessor data = (CowHungerAccessor) this;
            long currentTime = this.getWorld().getTime();

            boolean withinBirthWindow = (currentTime - data.ttll$getLastBirthTime()) <= 12000;
            boolean offCooldown = (currentTime - data.ttll$getLastMilkTime()) >= 3600;

            if (withinBirthWindow && offCooldown) {
                data.ttll$setLastMilkTime(currentTime);
                data.ttll$triggerPanic();
                this.setAttacker(player);

                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sound.SoundEvents.ENTITY_COW_MILK,
                        net.minecraft.sound.SoundCategory.NEUTRAL, 1.0F, 1.0F);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sound.SoundEvents.ENTITY_COW_HURT,
                        net.minecraft.sound.SoundCategory.NEUTRAL, 0.8F, 1.5F);
            } else {
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sound.SoundEvents.ENTITY_COW_AMBIENT,
                        net.minecraft.sound.SoundCategory.NEUTRAL, 0.5F, 0.7F);
                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }
}