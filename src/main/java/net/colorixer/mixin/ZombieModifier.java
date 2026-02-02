package net.colorixer.mixin;

import net.colorixer.entity.zombie.ZombieBreakTorchesGoal;
import net.colorixer.item.ModItems;
import net.colorixer.util.GoalSelectorUtilForZombie;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
public abstract class ZombieModifier {

    private static final java.util.UUID LUNGE_SPEED_ID = java.util.UUID.fromString("6a17b680-4564-4632-9721-39659f136611");
    private static final net.minecraft.entity.attribute.EntityAttributeModifier LUNGE_BOOST
            = new net.minecraft.entity.attribute.EntityAttributeModifier(net.minecraft.util.Identifier.of("ttll", "lunge_speed"),
            0.2, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

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
    }


    @Inject(method = "initEquipment", at = @At("TAIL"))
    private void ttll$extraZombieGear(net.minecraft.util.math.random.Random random, net.minecraft.world.LocalDifficulty localDifficulty, CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;

        // 25% of zombies get 'Special' gear logic
        if (random.nextFloat() < 0.20F) {
            float gearTypeRoll = random.nextFloat();
            ItemStack stack;

            // Roughly 80% of these 'special' zombies get Stone (Common)
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
            // Only 20% of the 25% (which is 5% total) get Iron (Vanilla Rare)
            else {
                stack = random.nextBoolean() ? new ItemStack(Items.IRON_SWORD) : new ItemStack(Items.IRON_SHOVEL);
            }

            zombie.equipStack(EquipmentSlot.MAINHAND, stack);
        }
    }

    // 3. DROP LOGIC (Only Iron or better drops)
    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void ttll$forceQualityDrops(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = zombie.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                // If it is Iron, Diamond, or Netherite, it drops
                if (isIronOrBetter(stack)) {
                    if (stack.isDamageable()) {
                        applyExponentialDamage(stack, zombie);
                    }
                    zombie.dropStack(world, stack);
                }
                // We clear the slot so the Stone gear "disappears" on death
                zombie.equipStack(slot, ItemStack.EMPTY);
            }
        }
        ci.cancel(); // Stop vanilla drop logic
    }

    private boolean isIronOrBetter(ItemStack stack) {
        Item item = stack.getItem();

        // Check if it's a Tool (Shovel/Hoe/Pick), a Sword, or Armor
        if (item instanceof MiningToolItem || item instanceof SwordItem) {
            String name = item.toString();
            // Return true only if it's NOT stone, wood, gold, or leather
            return !name.contains("stone") &&
                    !name.contains("wooden") &&
                    !name.contains("gold");
        }
        return false;
    }

    private void applyExponentialDamage(ItemStack stack, ZombieEntity zombie) {
        int maxDamage = stack.getMaxDamage();
        float remPerc = 0.1f + ((float) Math.pow(zombie.getRandom().nextFloat(), 2.5) * 0.8f);
        stack.setDamage(MathHelper.clamp(Math.round(maxDamage * (1.0f - remPerc)), 0, maxDamage - 1));
    }
}