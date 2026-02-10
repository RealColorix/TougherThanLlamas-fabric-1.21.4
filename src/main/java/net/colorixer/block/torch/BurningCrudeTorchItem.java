package net.colorixer.block.torch;

import net.colorixer.component.ModDataComponentTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
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
        if (!world.isClient && entity instanceof PlayerEntity player && !player.getAbilities().creativeMode) {

            // 1. Get current ticks (Default to 24000 for new items)
            int currentTicks = stack.getOrDefault(ModDataComponentTypes.FUEL_TIME, (long)MAX_FUEL).intValue();

            if (currentTicks <= 0) {
                stack.setCount(0);
                return;
            }

            // 2. Burn Logic
            float burnRate = 1.0f;
            if (world.isRaining() && world.isSkyVisible(player.getBlockPos())) {
                burnRate = 10.0f; // Simplified rain drain for items
            }

            currentTicks -= (int)burnRate;

            // 3. Save ticks back to component
            stack.set(ModDataComponentTypes.FUEL_TIME, (long)currentTicks);

            // 4. Update the visual damage bar (Bar needs 0 to 24000)
            // We set damage to (MAX - CURRENT) so the bar shrinks correctly
            stack.setDamage(MAX_FUEL - currentTicks);
        }
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