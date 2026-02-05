package net.colorixer.block.torch;

import net.colorixer.component.ModDataComponentTypes;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BurningCrudeTorchItem extends BlockItem {

    public static final int MAX_MINUTES = 20;
    public static final int TICKS_PER_MINUTE = 1200;

    public BurningCrudeTorchItem(Block block, Settings settings) {
        // We set maxDamage to MAX_MINUTES (20).
        super(block, settings.maxDamage(MAX_MINUTES).maxCount(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity player && !player.getAbilities().creativeMode) {

            // 1. Check if the item is already dead (Safety check)
            if (stack.getDamage() >= MAX_MINUTES) {
                stack.setCount(0);
                return;
            }

            long currentTime = world.getTime();
            long nextTarget = stack.getOrDefault(ModDataComponentTypes.FUEL_TIME, 0L);

            // Initialize timestamp if this is a fresh item
            if (nextTarget == 0) {
                stack.set(ModDataComponentTypes.FUEL_TIME, currentTime + TICKS_PER_MINUTE);
                return;
            }

            // 2. The Minute-Tick Logic
            if (currentTime >= nextTarget) {
                int newDamage = stack.getDamage() + 1;
                stack.setDamage(newDamage);

                if (newDamage >= MAX_MINUTES) {
                    // Delete the item immediately
                    stack.setCount(0);
                } else {
                    // Schedule next update for 60 seconds from now
                    stack.set(ModDataComponentTypes.FUEL_TIME, currentTime + TICKS_PER_MINUTE);
                }

                // Force the inventory to sync so the bar moves immediately
                player.getInventory().markDirty();
            }
        }
    }

    // --- BAR VISUALS ---

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        // Keeps the bar visible even when the torch is brand new (0 damage)
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        // Calculate percentage of fuel left
        float fuelPercentage = (float)(MAX_MINUTES - stack.getDamage()) / (float)MAX_MINUTES;
        // Map 100%->0% to 13->0 pixels
        return Math.round(13.0f * fuelPercentage);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        // Calculate fuel percentage for color (1.0 is full green, 0.0 is red)
        float f = Math.max(0.0f, (float)(MAX_MINUTES - stack.getDamage()) / (float)MAX_MINUTES);
        // HSV: 0.33 is Green, 0.0 is Red. Smooth transition.
        return MathHelper.hsvToRgb(f * 0.33f, 1.0f, 1.0f);
    }
}