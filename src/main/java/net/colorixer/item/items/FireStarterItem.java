package net.colorixer.item.items;

import net.colorixer.block.ModBlocks;
import net.colorixer.block.campfire.CampfireBlock;
import net.colorixer.block.campfire.CampfireBlockEntity;
import net.colorixer.block.furnace.FurnaceBlock;
import net.colorixer.block.furnace.FurnaceBlockEntity;
import net.colorixer.block.torch.CrudeTorchBlock;
import net.colorixer.block.torch.BurningCrudeTorchBlock;
import net.colorixer.util.ExhaustionHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
        PlayerEntity player = context.getPlayer();

        if (player == null) return ActionResult.PASS;

        // --- EXHAUSTION MECHANIC ---
        // Since useOnBlock is called when interacting, we apply drain here.
        // We use a smaller amount (0.1F) because this method can fire rapidly.


        // 1. FURNACE LOGIC
        if (state.getBlock() instanceof FurnaceBlock && !state.get(FurnaceBlock.LIT)) {
            if (world.getBlockEntity(pos) instanceof FurnaceBlockEntity furnace && furnace.getFuel() > 0) {
                if (!world.isClient) {
                    player.getHungerManager().addExhaustion(0.05F);
                    ExhaustionHelper.triggerJitter(5);
                    if (world.random.nextDouble() < this.chance) {
                        furnace.ignite();
                        world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 1.0f);
                    } else {
                        world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 0.2f, 1.5f);
                    }
                    damageItem(context, player);
                }
                return ActionResult.SUCCESS;
            }
        }

        // Check if the block is your Campfire and it's currently unlit
        if (state.getBlock() instanceof CampfireBlock && !state.get(CampfireBlock.LIT)) {
            // Get the BlockEntity and make sure it's the right type
            if (world.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {

                // Fix: Use the getter method for fuel (or the variable if it's public)
                if (campfire.getFuel() > 0) {
                    if (!world.isClient) {
                        player.getHungerManager().addExhaustion(0.05F);
                        ExhaustionHelper.triggerJitter(5);
                        // Use the chance variable from your item class
                        if (world.random.nextDouble() < this.chance) {
                            // This calls the method we wrote earlier to flip the SIT/STAGE properties
                            campfire.ignite();
                            world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 1.0f);
                        } else {
                            // Failed to light - play a "scraping" or "breaking" sound
                            world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 0.2f, 1.5f);
                        }

                        // Handle durability/stack decrement
                        damageItem(context, player);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }

        // 2. CRUDE TORCH LOGIC
        if (state.getBlock() instanceof CrudeTorchBlock) {
            if (!world.isClient) {
                player.getHungerManager().addExhaustion(0.05F);
                ExhaustionHelper.triggerJitter(5);
                if (world.random.nextDouble() < this.chance) {
                    Direction currentFacing = state.get(CrudeTorchBlock.FACING);
                    world.setBlockState(pos, ModBlocks.BURNING_CRUDE_TORCH.getDefaultState()
                            .with(Properties.FACING, currentFacing), 3);
                    world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                } else {
                    world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 0.5f, 1.5f);
                }
                damageItem(context, player);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private void damageItem(ItemUsageContext context, PlayerEntity player) {
        if (player != null && !player.getAbilities().creativeMode) {
            context.getStack().damage(1, player,
                    context.getHand() == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
    }
}