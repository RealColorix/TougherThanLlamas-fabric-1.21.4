package net.colorixer.mixin;

import net.colorixer.entity.zombie.ZombieBreakTorchesGoal;
import net.colorixer.entity.zombie.ZombieEatsAnimalsGoal;
import net.colorixer.entity.zombie.ZombieKillsAnimalsGoal;
import net.colorixer.item.ModItems;
import net.colorixer.util.GoalSelectorUtilForZombie;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ZombieEntity.class)
public abstract class ZombieModifier {

    private static final UUID LUNGE_SPEED_ID = UUID.fromString("6a17b680-4564-4632-9721-39659f136611");
    private static final EntityAttributeModifier LUNGE_BOOST
            = new EntityAttributeModifier(Identifier.of("ttll", "lunge_speed"),
            0.2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$handleZombieAggression(CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;
        if (zombie.getWorld().isClient) return;

        var reach = zombie.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE);
        var speed = zombie.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        LivingEntity target = zombie.getTarget();
        ItemStack stack = zombie.getMainHandStack();

        // --- PART 1: REACH LOGIC (Iron/Better) ---
        if (reach != null) {
            if (isIronOrBetter(stack) && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof ShovelItem)) {
                if (reach.getBaseValue() != 3.0) reach.setBaseValue(3.0);
            }
        }

        // --- PART 2: LUNGE LOGIC (Adults Only) ---
        // We add !zombie.isBaby() here
        if (speed != null && target != null && zombie.isAlive() && !zombie.isBaby()) {
            double distSq = zombie.squaredDistanceTo(target);

            // 3.5 blocks squared = 12.25
            if (distSq < 25) {
                if (!speed.hasModifier(LUNGE_BOOST.id())) {
                    speed.addTemporaryModifier(LUNGE_BOOST);
                }
            } else {
                speed.removeModifier(LUNGE_BOOST.id());
            }
        } else if (speed != null && speed.hasModifier(LUNGE_BOOST.id())) {
            speed.removeModifier(LUNGE_BOOST.id());
        }
    }

    @Inject(method = "initCustomGoals", at = @At("TAIL"))
    private void ttll$addBlockBreakingGoal(CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;

        // Using your Util to add the breaking goal
        GoalSelectorUtilForZombie.addGoal(zombie, 3, new ZombieBreakTorchesGoal(zombie));
        GoalSelectorUtilForZombie.addGoal(zombie, 4, new ZombieKillsAnimalsGoal(zombie, 1.0));
        GoalSelectorUtilForZombie.addGoal(zombie, 5, new ZombieEatsAnimalsGoal(zombie, 1.0));
    }


    @Inject(method = "initEquipment", at = @At("TAIL"))
    private void ttll$extraZombieGear(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;

        if (random.nextFloat() < 0.10F) {
            float gearTypeRoll = random.nextFloat();
            // 1. Initialize as EMPTY instead of null
            ItemStack stack = ItemStack.EMPTY;

            if (gearTypeRoll < 0.90F) {
                int stoneType = random.nextInt(8);
                stack = switch (stoneType) {
                    case 0 -> new ItemStack(Items.STONE_SWORD);
                    case 1 -> new ItemStack(Items.STONE_SHOVEL);
                    case 2 -> new ItemStack(Items.WOODEN_SWORD);
                    case 3 -> new ItemStack(Items.WOODEN_SHOVEL);
                    case 4 -> new ItemStack(Items.WOODEN_HOE);
                    case 5 -> new ItemStack(ModItems.WOODEN_CLUB);
                    case 6 -> new ItemStack(ModItems.BONE_CLUB);
                    default -> new ItemStack(Items.STONE_HOE);
                };
            }
            else if (random.nextFloat() < 0.1f) {
                stack = random.nextBoolean() ? new ItemStack(Items.IRON_SWORD) : new ItemStack(Items.IRON_SHOVEL);
            }

            // 2. Only equip if the stack actually contains something
            if (!stack.isEmpty()) {
                zombie.equipStack(EquipmentSlot.MAINHAND, stack);
            }
        }
    }
    // 3. DROP LOGIC (Only Iron or better drops)
    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void ttll$forceQualityDrops(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;

        // Check if the death was caused by fire or magic (daylight/fire/lava)
        boolean burntToDeath = source.isIn(DamageTypeTags.IS_FIRE);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = zombie.getEquippedStack(slot);
            if (!stack.isEmpty()) {

                // Only drop animal loot if NOT burnt to death
                if (isIronOrBetter(stack) || (!burntToDeath && isAnimalDrop(stack))) {
                    if (stack.isDamageable() && isIronOrBetter(stack)) {
                        applyExponentialDamage(stack, zombie);
                    }
                    zombie.dropStack(world, stack);
                }
                zombie.equipStack(slot, ItemStack.EMPTY);
            }
        }
        ci.cancel();
    }

    // New helper to identify the "Animal Loot"
    private boolean isAnimalDrop(ItemStack stack) {
        Item item = stack.getItem();
        // Check for common animal drops so they don't get deleted on death
        return item == Items.BEEF || item == Items.COOKED_BEEF ||
                item == Items.PORKCHOP || item == Items.COOKED_PORKCHOP ||
                item == Items.MUTTON || item == Items.COOKED_MUTTON ||
                item == Items.LEATHER || item == Items.WHITE_WOOL;
    }

    private boolean isIronOrBetter(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof MiningToolItem || item instanceof SwordItem) {
            String name = item.toString();
            return name.contains("iron_shovel") ||
                    name.contains("iron_sword");
        }
        return false;
    }

    private void applyExponentialDamage(ItemStack stack, ZombieEntity zombie) {
        int maxDamage = stack.getMaxDamage();
        float remPerc = 0.1f + ((float) Math.pow(zombie.getRandom().nextFloat(), 2.5) * 0.8f);
        stack.setDamage(MathHelper.clamp(Math.round(maxDamage * (1.0f - remPerc)), 0, maxDamage - 1));
    }

}