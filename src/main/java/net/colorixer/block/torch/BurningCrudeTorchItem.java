package net.colorixer.block.torch;

import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import net.colorixer.component.ModDataComponentTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BurningCrudeTorchItem extends BlockItem {

    public static final int MAX_FUEL = 24000;



    public BurningCrudeTorchItem(Block block, Settings settings) {
        super(block, settings.maxCount(1));

    }



    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    // Inside BurningCrudeTorchItem.java
    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        // This runs on the server after the block is placed
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity be) {
                // Get the fuel from the ITEM
                int ticks = stack.getOrDefault(ModDataComponentTypes.FUEL_TIME, (long)MAX_FUEL).intValue();
                // Push it into the BLOCK ENTITY
                be.setBurnTime(ticks);
            }
        }
        return super.postPlacement(pos, world, player, stack, state);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && !((PlayerEntity)entity).getAbilities().creativeMode) {

            // --- WATER EXTINGUISH LOGIC (Player) ---
            // 0.6f is roughly waist/chest height. 1.0f is eye level.
            // If player is 1.5 blocks deep, their submerged height is high.
            if (entity.isSubmergedInWater() || entity.getFluidHeight(net.minecraft.registry.tag.FluidTags.WATER) > 1.5) {
                extinguish(stack, world, entity.getBlockPos());
                return;
            }

            // 1. Get current ticks
            int currentTicks = stack.getOrDefault(ModDataComponentTypes.FUEL_TIME, (long)MAX_FUEL).intValue();

            if (currentTicks <= 0) {
                stack.setCount(0);
                return;
            }

            // 2. Burn Logic
            float burnRate = 1.0f;
            if (world.isRaining() && world.isSkyVisible(entity.getBlockPos())) {
                burnRate = 10.0f;
            }

            currentTicks -= (int)burnRate;
            stack.set(ModDataComponentTypes.FUEL_TIME, (long)currentTicks);
            stack.setDamage(MAX_FUEL - currentTicks);
        }
    }

    // --- ITEM ENTITY LOGIC (Dropped Items) ---


    /**
     * Helper to play sound and remove the item
     */
    private void extinguish(ItemStack stack, World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        stack.setCount(0);
    }

    // --- BAR VISUALS ---
    @Override
    public boolean isItemBarVisible(ItemStack stack) { return true; }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int ticks = stack.getOrDefault(ModDataComponentTypes.FUEL_TIME, (long)MAX_FUEL).intValue();
        return Math.round(13.0f * (float)ticks / MAX_FUEL);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        int ticks = stack.getOrDefault(ModDataComponentTypes.FUEL_TIME, (long)MAX_FUEL).intValue();
        float f = Math.max(0.0f, (float)ticks / MAX_FUEL);
        return MathHelper.hsvToRgb(f * 0.33f, 1.0f, 1.0f);
    }
}