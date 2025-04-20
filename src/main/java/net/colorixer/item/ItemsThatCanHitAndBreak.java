package net.colorixer.item;

import net.colorixer.block.ModBlocks;
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
        UseBlockCallback.EVENT.register(ItemsThatCanHitAndBreak::onBlockRightClick);
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
                    ItemStack stack = new ItemStack(ModItems.FLINT_FRAGMENT, 2);
                    dropItem(world, player, stack);
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
                    ItemStack stack = new ItemStack(ModItems.POINTY_STICK, 1);
                    dropItem(world, player, stack);
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
                    ItemStack stack = new ItemStack(Items.STICK, 1);
                    dropItem(world, player, stack);
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
                    ItemStack stack = new ItemStack(ModItems.SHARPEND_BONE, 1);
                    dropItem(world, player, stack);
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
            }else if (heldItemStack.getItem() == ModItems.POINTY_STICK) {
                if (random.nextDouble() <= 0.33) {
                    // Play the flint and steel use sound
                    world.playSound(null, pos, SoundEvents.BLOCK_BAMBOO_WOOD_BREAK, SoundCategory.PLAYERS,
                            1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    heldItemStack.decrement(1);
                    ItemStack stack = new ItemStack(ModItems.SMALL_POINTY_STICKS, 1);
                    dropItem(world, player, stack);
                    player.swingHand(hand, true);
                    player.getItemCooldownManager().set(heldItemStack, 20);
                    return ActionResult.SUCCESS;
                } else {
                    world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
                            1F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    player.getItemCooldownManager().set(heldItemStack, 20);
                    player.swingHand(hand, true);
                    return ActionResult.PASS;
                }
            }else if (heldItemStack.getItem() == ModItems.ROCK) {
                if (random.nextDouble() <= 0.3) {
                    // Play the flint and steel use sound
                    world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS,
                            0.5F, 0.2F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    heldItemStack.decrement(1);
                    ItemStack stack = new ItemStack(ModItems.SHARP_ROCK, 1);
                    dropItem(world, player, stack);
                    player.swingHand(hand, true);
                    player.getItemCooldownManager().set(heldItemStack, 50);
                    return ActionResult.SUCCESS;
                } else {
                    world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
                            1F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    player.getItemCooldownManager().set(heldItemStack, 50);
                    player.swingHand(hand, true);
                    return ActionResult.PASS;
                }
            }else if (heldItemStack.getItem() == ModItems.IRON_NUGGET_CAST) {
                    world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_BREAK, SoundCategory.PLAYERS,
                            1.5F, 0.2F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    heldItemStack.decrement(1);
                    ItemStack stack = new ItemStack(Items.IRON_NUGGET, 1);
                    dropItem(world, player, stack);
                    player.swingHand(hand, true);
                    return ActionResult.SUCCESS;
            }else if (heldItemStack.getItem() == ModItems.IRON_INGOT_CAST) {
                world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_BREAK, SoundCategory.PLAYERS,
                        1.5F, 0.2F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                heldItemStack.decrement(1);
                ItemStack stack = new ItemStack(Items.IRON_INGOT, 1);
                dropItem(world, player, stack);
                player.swingHand(hand, true);
                return ActionResult.SUCCESS;
            }else if (heldItemStack.getItem() == ModItems.GOLD_NUGGET_CAST) {
                world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_BREAK, SoundCategory.PLAYERS,
                        1.5F, 0.2F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                heldItemStack.decrement(1);
                ItemStack stack = new ItemStack(Items.GOLD_NUGGET, 1);
                dropItem(world, player, stack);
                player.swingHand(hand, true);
                return ActionResult.SUCCESS;
            }else if (heldItemStack.getItem() == ModItems.GOLD_INGOT_CAST) {
                world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_BREAK, SoundCategory.PLAYERS,
                        1.5F, 0.2F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                heldItemStack.decrement(1);
                ItemStack stack = new ItemStack(Items.GOLD_INGOT, 1);
                dropItem(world, player, stack);
                player.swingHand(hand, true);
                return ActionResult.SUCCESS;
            }else if (heldItemStack.getItem() == ModItems.COPPER_INGOT_CAST) {
                world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_BREAK, SoundCategory.PLAYERS,
                        1.5F, 0.2F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                heldItemStack.decrement(1);
                ItemStack stack = new ItemStack(Items.COPPER_INGOT, 1);
                dropItem(world, player, stack);
                player.swingHand(hand, true);
                return ActionResult.SUCCESS;
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


    }

    private static boolean isValidBlock(Block block) {
        return block == Blocks.STONE || block == Blocks.GRANITE || block == Blocks.DIORITE ||
                block == Blocks.ANDESITE || block == Blocks.DEEPSLATE || block == Blocks.TUFF ||
                block == Blocks.CALCITE || block == Blocks.SMOOTH_BASALT || block == Blocks.BASALT ||
                block == Blocks.BLACKSTONE || block == Blocks.COBBLESTONE || block == Blocks.COBBLED_DEEPSLATE||
                block == ModBlocks.WEATHERED_STONE || block == ModBlocks.COBBLESTONE || block == ModBlocks.CRACKED_STONE ||
                block == ModBlocks.SHATTERED_STONE|| block == ModBlocks.EXCAVATED_STONE||block == Blocks.BEDROCK
                ||block == Blocks.COAL_ORE||block == Blocks.DEEPSLATE_COAL_ORE
                ||block == Blocks.IRON_ORE||block == Blocks.DEEPSLATE_IRON_ORE
                ||block == Blocks.COPPER_ORE||block == Blocks.DEEPSLATE_COPPER_ORE
                ||block == Blocks.GOLD_ORE||block == Blocks.DEEPSLATE_GOLD_ORE
                ||block == Blocks.LAPIS_ORE||block == Blocks.DEEPSLATE_LAPIS_ORE
                ||block == Blocks.REDSTONE_ORE||block == Blocks.DEEPSLATE_REDSTONE_ORE
                ||block == Blocks.EMERALD_ORE||block == Blocks.DEEPSLATE_EMERALD_ORE
                ||block == Blocks.DIAMOND_ORE||block == Blocks.DEEPSLATE_DIAMOND_ORE
                ||block == Blocks.RAW_IRON_BLOCK||block == Blocks.RAW_COPPER_BLOCK
                ||block == Blocks.RAW_GOLD_BLOCK||block == Blocks.IRON_BLOCK
                ||block == Blocks.COPPER_BLOCK||block == Blocks.GOLD_BLOCK
                ||block == Blocks.ANCIENT_DEBRIS||block == Blocks.DIAMOND_BLOCK
                ||block == Blocks.EMERALD_BLOCK||block == Blocks.NETHERITE_BLOCK
                ||block == Blocks.LAPIS_BLOCK||block == Blocks.REDSTONE_BLOCK;
    }
}
