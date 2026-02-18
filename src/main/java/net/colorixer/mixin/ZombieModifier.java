package net.colorixer.mixin;

import net.colorixer.entity.zombie.ZombieBreakTorchesGoal;
import net.colorixer.entity.zombie.ZombieEatsAnimalsGoal;
import net.colorixer.entity.zombie.ZombieKillsAnimalsGoal;
import net.colorixer.util.GoalSelectorUtilForZombie;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ZombieEntity.class)
public abstract class ZombieModifier {

    private static final UUID LUNGE_SPEED_ID = UUID.fromString("6a17b680-4564-4632-9721-39659f136611");
    private static final EntityAttributeModifier LUNGE_BOOST = new EntityAttributeModifier(
            Identifier.of("ttll", "lunge_speed"),
            0.2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$handleZombieTick(CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;
        if (zombie.getWorld().isClient) return;

        if (!zombie.canPickUpLoot()) zombie.setCanPickUpLoot(true);

        // --- AUTO-EQUIP ARMOR FROM HAND ---
        // --- AUTO-EQUIP ARMOR FROM HAND ---
        // --- AUTO-EQUIP ARMOR FROM HAND ---
        ItemStack handStack = zombie.getMainHandStack();
        if (handStack.getItem() instanceof ArmorItem) {
            // Call it directly on the 'zombie' instance
            EquipmentSlot slot = zombie.getPreferredEquipmentSlot(handStack);
            ItemStack currentArmor = zombie.getEquippedStack(slot);

            // If the body part is naked, put the armor on
            if (currentArmor.isEmpty()) {
                zombie.equipStack(slot, handStack.copy());
                zombie.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

                // This marks it as "player dropped" so it doesn't take damage later
                ((MobEntityAccessor)zombie).callUpdateDropChances(slot);
            }
        }

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

    @Inject(method = "canPickupItem", at = @At("HEAD"), cancellable = true)
    private void ttll$allowPickup(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void ttll$forceQualityDrops(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;
        boolean burntToDeath = source.isIn(DamageTypeTags.IS_FIRE);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = zombie.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            float dropChance = ((MobEntityAccessor)zombie).callGetDropChance(slot);
            // wasPickedUp means dropChance is 2.0F (set by updateDropChances)
            boolean wasPickedUp = dropChance >= 1.0F;
            boolean shouldDrop = wasPickedUp || isArmor(stack) || isIronOrBetter(stack);

            if ((stack.contains(DataComponentTypes.FOOD) || isAnimalProduct(stack)) && burntToDeath) {
                shouldDrop = false;
            }

            if (shouldDrop) {
                // FIXED: If it wasn't picked up, apply damage.
                // If it WAS picked up, skip this block and drop it "as is".
                if (!wasPickedUp && stack.isDamageable()) {
                    applyExponentialDamage(stack, zombie);
                }
                zombie.dropStack(world, stack);
            }
            zombie.equipStack(slot, ItemStack.EMPTY);
        }
        ci.cancel();
    }

    @Inject(method = "initCustomGoals", at = @At("TAIL"))
    private void ttll$setupZombieGoals(CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;
        GoalSelectorUtilForZombie.addGoal(zombie, 3, new ZombieBreakTorchesGoal(zombie));
        GoalSelectorUtilForZombie.addGoal(zombie, 4, new ZombieKillsAnimalsGoal(zombie, 1.0));
        GoalSelectorUtilForZombie.addGoal(zombie, 5, new ZombieEatsAnimalsGoal(zombie, 1.0));
    }

    private boolean isAnimalProduct(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.LEATHER || item == Items.WHITE_WOOL || item == Items.FEATHER ||
                item == Items.BONE || item == Items.STRING || item == Items.EGG;
    }

    private boolean isArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    private boolean isIronOrBetter(ItemStack stack) {
        String name = stack.getItem().toString();
        return name.contains("iron_") || name.contains("diamond_") || name.contains("netherite_");
    }

    private void applyExponentialDamage(ItemStack stack, ZombieEntity zombie) {
        int maxDamage = stack.getMaxDamage();
        float remPerc = 0.1f + ((float) Math.pow(zombie.getRandom().nextFloat(), 2.5) * 0.8f);
        stack.setDamage(MathHelper.clamp(Math.round(maxDamage * (1.0f - remPerc)), 0, maxDamage - 1));
    }
}