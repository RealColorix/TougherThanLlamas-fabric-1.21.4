package net.colorixer.entity.hostile.zombie;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

import java.util.EnumSet;
import java.util.List;

public class ZombieKillsAnimalsGoal extends Goal {
    private final ZombieEntity zombie;
    private LivingEntity targetAnimal;
    private final double speed;
    private int attackCooldown = 0;

    public ZombieKillsAnimalsGoal(ZombieEntity zombie, double speed) {
        this.zombie = zombie;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (zombie.getTarget() != null) return false;
        if (!(zombie.getWorld() instanceof ServerWorld serverWorld)) return false;

        List<AnimalEntity> animals = serverWorld.getEntitiesByClass(
                AnimalEntity.class,
                zombie.getBoundingBox().expand(15.0, 4.0, 15.0),
                entity -> entity instanceof CowEntity || entity instanceof SheepEntity || entity instanceof PigEntity
        );

        if (!animals.isEmpty()) {
            this.targetAnimal = animals.get(0);
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if (!(zombie.getWorld() instanceof ServerWorld serverWorld)) return;

        // --- NEW: ON-THE-GO ITEM SWAP ---
        handleItemSwap(serverWorld);

        if (targetAnimal == null || !targetAnimal.isAlive()) return;

        zombie.getLookControl().lookAt(targetAnimal, 30.0F, 30.0F);
        double distSq = zombie.squaredDistanceTo(targetAnimal);

        if (distSq <= 3.5) {
            if (attackCooldown <= 0) {
                zombie.swingHand(Hand.MAIN_HAND);
                zombie.tryAttack(serverWorld, targetAnimal);
                this.attackCooldown = 20;
            }
            zombie.getNavigation().stop();
        } else {
            zombie.getNavigation().startMovingTo(targetAnimal, speed);
        }

        if (attackCooldown > 0) attackCooldown--;
    }

    private void handleItemSwap(ServerWorld world) {
        List<ItemEntity> nearbyItems = world.getEntitiesByClass(ItemEntity.class,
                zombie.getBoundingBox().expand(1.5, 1.0, 1.5), item -> !item.isRemoved());

        ItemStack handStack = zombie.getStackInHand(Hand.MAIN_HAND);

        // 1. Check if the zombie is currently holding something "Valuable" (Tool/Weapon)
        boolean holdingTool = handStack.getItem() instanceof SwordItem ||
                handStack.getItem() instanceof ShovelItem ||
                handStack.getItem() instanceof MiningToolItem;

        boolean holdingFood = !handStack.isEmpty() && handStack.getComponents().contains(DataComponentTypes.FOOD);

        for (ItemEntity itemEntity : nearbyItems) {
            ItemStack groundStack = itemEntity.getStack();
            boolean groundIsFood = groundStack.getComponents().contains(DataComponentTypes.FOOD);

            // 2. ONLY SWAP IF:
            // - Hand is empty
            // - OR (Hand is NOT a tool AND Hand is NOT food AND Ground IS food)
            // This ensures they keep their Swords/Shovels but swap Leather for Beef.
            if (handStack.isEmpty() || (!holdingTool && !holdingFood && groundIsFood)) {

                if (!handStack.isEmpty()) {
                    zombie.dropStack(world, handStack.copy());
                }

                zombie.setStackInHand(Hand.MAIN_HAND, groundStack.copy());
                itemEntity.discard();
                break;
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        return targetAnimal != null && targetAnimal.isAlive() && zombie.getTarget() == null;
    }

    @Override
    public void stop() {
        this.targetAnimal = null;
        zombie.getNavigation().stop();
    }
}