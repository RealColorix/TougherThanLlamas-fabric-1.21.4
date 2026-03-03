package net.colorixer.mixin.entities.hostile;

import net.colorixer.entity.hostile.zombie.ZombieBreakTorchesGoal;
import net.colorixer.entity.hostile.zombie.ZombieEatsAnimalsGoal;
import net.colorixer.entity.hostile.zombie.ZombieKillsAnimalsGoal;
import net.colorixer.util.EquipmentDropUtil;
import net.colorixer.util.GoalSelectorUtilForMob;
import net.colorixer.util.SkeletonConversionTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ZombieEntity.class)
public abstract class ZombieModifier implements SkeletonConversionTracker {

    private static final UUID LUNGE_SPEED_ID = UUID.fromString("6a17b680-4564-4632-9721-39659f136611");
    private static final EntityAttributeModifier LUNGE_BOOST = new EntityAttributeModifier(
            Identifier.of("ttll", "lunge_speed"),
            0.2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$handleZombieTick(CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;
        if (zombie.getWorld().isClient) return;


        var reach = zombie.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE);
        var speed = zombie.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        LivingEntity target = zombie.getTarget();

        if (reach != null) {
            if (isIronOrBetter(zombie.getMainHandStack())) {
                if (reach.getBaseValue() != 3.0) reach.setBaseValue(3.0);
            } else if (reach.getBaseValue() == 3.0) {
                reach.setBaseValue(2.5);
            }
        }

        if (speed != null && target != null && zombie.isAlive() && !zombie.isBaby()) {
            if (zombie.squaredDistanceTo(target) < 25) {
                if (!speed.hasModifier(LUNGE_BOOST.id())) speed.addTemporaryModifier(LUNGE_BOOST);
            } else {
                speed.removeModifier(LUNGE_BOOST.id());
            }
        } else if (speed != null) {
            speed.removeModifier(LUNGE_BOOST.id());
        }
    }



    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void ttll$forceQualityDrops(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;
        if (this.isConvertingToSkeleton()) return;

        EquipmentDropUtil.dropAllMobEquipment(zombie, world, source);
        ci.cancel();
    }

    @Inject(method = "initCustomGoals", at = @At("TAIL"))
    private void ttll$setupZombieGoals(CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;
        GoalSelectorUtilForMob.addGoal(zombie, 3, new ZombieBreakTorchesGoal(zombie));
        GoalSelectorUtilForMob.addGoal(zombie, 4, new ZombieKillsAnimalsGoal(zombie, 1.0));
        GoalSelectorUtilForMob.addGoal(zombie, 5, new ZombieEatsAnimalsGoal(zombie, 1.0));
    }

    private boolean isIronOrBetter(ItemStack stack) {
        String name = stack.getItem().toString();
        return name.contains("iron_") || name.contains("diamond_") || name.contains("netherite_");
    }
}