package net.colorixer.entity.hostile.zombie;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

import java.util.EnumSet;
import java.util.List;

public class ZombieEatsAnimalsGoal extends Goal {
    private final ZombieEntity zombie;
    private final double speed;
    private int eatTick = 0;
    private boolean isGluttonousEating = false;

    public ZombieEatsAnimalsGoal(ZombieEntity zombie, double speed) {
        this.zombie = zombie;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (zombie.getTarget() != null) return false;
        if (!(zombie.getWorld() instanceof ServerWorld serverWorld)) return false;

        ItemStack handStack = zombie.getStackInHand(Hand.MAIN_HAND);
        boolean holdingFood = isFood(handStack);

        if (holdingFood) {
            if (zombie.getHealth() < zombie.getMaxHealth()) return true;
            if (zombie.getRandom().nextInt(200) == 0) {
                this.isGluttonousEating = true;
                return true;
            }
        }

        return canSeeDrops(serverWorld);
    }

    private boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && stack.getComponents().contains(DataComponentTypes.FOOD);
    }

    // Helper to check if the zombie is holding something it SHOULD NOT drop
    private boolean isHoldingWorkGear(ItemStack stack) {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof MiningToolItem;
    }

    private boolean canSeeDrops(ServerWorld world) {
        ItemStack handStack = zombie.getStackInHand(Hand.MAIN_HAND);

        // If holding a sword or tool, we NEVER want to swap for food.
        if (isHoldingWorkGear(handStack)) return false;

        // If holding food, we only care about drops if we are hungry (handled in canStart)
        // If holding non-food junk (leather/wool), we care about drops only if they are food.
        return !world.getEntitiesByClass(ItemEntity.class,
                zombie.getBoundingBox().expand(8.0, 3.0, 8.0),
                item -> !item.isRemoved() && (handStack.isEmpty() || (!isFood(handStack) && isFood(item.getStack())))
        ).isEmpty();
    }

    @Override
    public void tick() {
        if (!(zombie.getWorld() instanceof ServerWorld serverWorld)) return;

        ItemStack handStack = zombie.getStackInHand(Hand.MAIN_HAND);

        // 1. EAT LOGIC
        if (isFood(handStack) && (zombie.getHealth() < zombie.getMaxHealth() || isGluttonousEating)) {
            zombie.getNavigation().stop();
            eatTick++;

            if (eatTick % 10 == 0) {
                zombie.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 0.5f, 1.0f);
                zombie.swingHand(Hand.MAIN_HAND);
            }

            if (eatTick >= 40) {
                zombie.heal(6.0f);
                handStack.decrement(1);
                zombie.playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.5f);
                eatTick = 0;
                isGluttonousEating = false;
            }
            return;
        }

        // 2. SMART PICKUP / SWAP LOGIC
        // We only proceed if NOT holding gear.
        if (!isHoldingWorkGear(handStack)) {
            List<ItemEntity> nearbyItems = serverWorld.getEntitiesByClass(ItemEntity.class,
                    zombie.getBoundingBox().expand(1.2, 1.0, 1.2), item -> !item.isRemoved());

            for (ItemEntity itemEntity : nearbyItems) {
                ItemStack groundStack = itemEntity.getStack();

                // Swap if Hand is empty OR (Hand is junk AND Ground is food)
                if (handStack.isEmpty() || (!isFood(handStack) && isFood(groundStack))) {
                    if (!handStack.isEmpty()) {
                        zombie.dropStack(serverWorld, handStack.copy());
                    }
                    zombie.setStackInHand(Hand.MAIN_HAND, groundStack.copy());
                    itemEntity.discard();
                    break;
                }
            }
        }

        // 3. NAVIGATION TO ITEMS
        if (handStack.isEmpty() || (!isFood(handStack) && !isHoldingWorkGear(handStack))) {
            List<ItemEntity> distantItems = serverWorld.getEntitiesByClass(ItemEntity.class,
                    zombie.getBoundingBox().expand(10.0, 4.0, 10.0),
                    item -> !item.isRemoved() && (handStack.isEmpty() || isFood(item.getStack())));

            if (!distantItems.isEmpty()) {
                zombie.getNavigation().startMovingTo(distantItems.get(0), speed);
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        if (zombie.getTarget() != null) return false;
        return (isFood(zombie.getStackInHand(Hand.MAIN_HAND)) && (zombie.getHealth() < zombie.getMaxHealth() || isGluttonousEating))
                || canSeeDrops((ServerWorld)zombie.getWorld());
    }

    @Override
    public void stop() {
        this.eatTick = 0;
        this.isGluttonousEating = false;
    }
}