package net.colorixer.item;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class ItemsThatCanHitAndBreak {

    private static final Random random = new Random();

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            return onBlockRightClick(player, world, hand, hitResult);
        });
    }

    private static ActionResult onBlockRightClick(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (world.isClient) {
            return ActionResult.PASS; // Handle only on the server
        }

        BlockPos pos = hitResult.getBlockPos();
        Block targetBlock = world.getBlockState(pos).getBlock();
        ItemStack heldItemStack = player.getStackInHand(hand);

        // Check if the player is already in cooldown
        if (player.getItemCooldownManager().isCoolingDown(heldItemStack)) {
            return ActionResult.FAIL;
        }

        // Check if the block is stone or similar
        if (isValidBlock(targetBlock)) {
            if (heldItemStack.getItem() == Items.FLINT) {
                if (random.nextDouble() <= 0.5){
                    // Play the flint and steel use sound
                    world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS,
                            1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    heldItemStack.decrement(1);
                    ItemStack gravelStack = new ItemStack(ModItems.FLINT_FRAGMENT, 2);
                    dropItem(world, player, gravelStack);
                    player.swingHand(hand, true);
                    player.getItemCooldownManager().set(heldItemStack, 20);
                    return ActionResult.SUCCESS;
                }  else {
                    world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
                            1F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    player.getItemCooldownManager().set(heldItemStack, 20);
                    player.swingHand(hand, true);
                    return ActionResult.PASS;
                }
            } else if (heldItemStack.getItem() == Items.STICK) {
                if (random.nextDouble() <= 0.33){
                    // Play the flint and steel use sound
                    world.playSound(null, pos, SoundEvents.BLOCK_BAMBOO_WOOD_BREAK, SoundCategory.PLAYERS,
                            1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    heldItemStack.decrement(1);
                    ItemStack gravelStack = new ItemStack(ModItems.POINTY_STICK, 1);
                    dropItem(world, player, gravelStack);
                    player.swingHand(hand, true);
                    player.getItemCooldownManager().set(heldItemStack, 20);
                    return ActionResult.SUCCESS;
                }  else {
                    world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
                            1F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    player.getItemCooldownManager().set(heldItemStack, 20);
                    player.swingHand(hand, true);
                    return ActionResult.PASS;
                }
            }else if (heldItemStack.getItem() == ModItems.BRANCH) {
                if (random.nextDouble() <= 0.25){
                    // Play the flint and steel use sound
                    world.playSound(null, pos, SoundEvents.BLOCK_BAMBOO_WOOD_BREAK, SoundCategory.PLAYERS,
                            1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    heldItemStack.decrement(1);
                    ItemStack gravelStack = new ItemStack(Items.STICK, 1);
                    dropItem(world, player, gravelStack);
                    player.swingHand(hand, true);
                    player.getItemCooldownManager().set(heldItemStack, 20);
                    return ActionResult.SUCCESS;
                }  else {
                    world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
                            1F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    player.getItemCooldownManager().set(heldItemStack, 20);
                    player.swingHand(hand, true);
                    return ActionResult.PASS;
                }
            }else if (heldItemStack.getItem() == Items.BONE) {
                if (random.nextDouble() <= 0.1) {
                    // Play the flint and steel use sound
                    world.playSound(null, pos, SoundEvents.BLOCK_BONE_BLOCK_BREAK, SoundCategory.PLAYERS,
                            1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    heldItemStack.decrement(1);
                    ItemStack gravelStack = new ItemStack(ModItems.SHARPEND_BONE, 1);
                    dropItem(world, player, gravelStack);
                    player.swingHand(hand, true);
                    player.getItemCooldownManager().set(heldItemStack, 20);
                    return ActionResult.SUCCESS;
                } else {
                    world.playSound(null, pos, SoundEvents.BLOCK_BONE_BLOCK_HIT, SoundCategory.BLOCKS,
                            1F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    player.getItemCooldownManager().set(heldItemStack, 20);
                    player.swingHand(hand, true);
                    return ActionResult.PASS;
                }
            }
        }

        // Default to passing if the block is not stone or similar
        return ActionResult.PASS;
    }

    private static void dropItem(World world, PlayerEntity player, ItemStack itemStack) {
        // Validate the item stack
        if (itemStack.isEmpty()) {
            return;
        }

        // Get the player's position and facing direction
        Vec3d playerPos = player.getPos();
        Vec3d lookDirection = player.getRotationVec(1.0F).normalize();

        // Set the spawn position: slightly above and in front of the player
        Vec3d dropPos = playerPos.add(lookDirection.multiply(0.5)).add(0, 1.0, 0);

        // Create the item entity
        ItemEntity itemEntity = new ItemEntity(world, dropPos.x, dropPos.y, dropPos.z, itemStack);

        // Apply a slight forward and upward velocity to the item
        double velocityMultiplier = 0.2; // Adjust to control forward speed
        Vec3d dropVelocity = lookDirection.multiply(velocityMultiplier).add(0, 0.2, 0); // Add upward push
        itemEntity.setVelocity(dropVelocity);

        // Set a pickup delay to prevent instant pickup
        itemEntity.setPickupDelay(5);

        // Spawn the item entity in the world
        boolean success = world.spawnEntity(itemEntity);

        if (!success) {
            System.err.println("Failed to spawn ItemEntity for: " + itemStack.getItem());
        }
    }

    private static boolean isValidBlock(Block block) {
        return block == Blocks.STONE || block == Blocks.GRANITE || block == Blocks.DIORITE ||
                block == Blocks.ANDESITE || block == Blocks.DEEPSLATE || block == Blocks.TUFF ||
                block == Blocks.CALCITE || block == Blocks.SMOOTH_BASALT || block == Blocks.BASALT ||
                block == Blocks.BLACKSTONE || block == Blocks.COBBLESTONE || block == Blocks.COBBLED_DEEPSLATE;
    }
}
