package net.colorixer.item.items;

import net.colorixer.block.brick_furnace.BrickFurnaceBlock;
import net.colorixer.block.brick_furnace.BrickFurnaceBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FireStarterItem extends Item {
    public final double chance;

    public FireStarterItem(Settings settings, double chance) {
        super(settings);
        this.chance = chance;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        // Check if the block is a Brick Furnace and not already lit.
        if (state.getBlock() instanceof BrickFurnaceBlock && !state.get(BrickFurnaceBlock.LIT)) {
            // Retrieve the furnace block entity.
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof BrickFurnaceBlockEntity)) {
                return ActionResult.PASS;
            }
            BrickFurnaceBlockEntity furnace = (BrickFurnaceBlockEntity) be;

            // Check if the furnace has fuel (at least one non-empty fuel slot).
            boolean hasFuel = false;
            for (ItemStack fuelStack : furnace.getFuelInventory()) {
                if (!fuelStack.isEmpty()) {
                    hasFuel = true;
                    break;
                }
            }
            // If no fuel is present, do not proceed with lighting.
            if (!hasFuel) {
                return ActionResult.PASS;
            }

            // Attempt to light the furnace based on the chance.
            if (world.random.nextDouble() < this.chance) {
                // Light the furnace.
                world.setBlockState(pos, state.with(BrickFurnaceBlock.LIT, true));
                // Play the fire starting sound.
                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }else {
                world.playSound(null, context.getBlockPos(), SoundEvents.BLOCK_WOOD_FALL, SoundCategory.PLAYERS, 1.0f, 1.0f);

            }

            // Damage the item.
            PlayerEntity player = context.getPlayer();
            if (player != null) {
                ItemStack toolStack = context.getStack();
                toolStack.damage(1, player);
                if (toolStack.getDamage() >= toolStack.getMaxDamage() - 1) {
                    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                    world.playSound(null, context.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            }
        }
        return ActionResult.PASS;
    }
}
