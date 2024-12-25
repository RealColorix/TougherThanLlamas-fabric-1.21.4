package net.colorixer.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class KnittingSticksItem extends Item {

    private static final int MAX_PROGRESS = 90; // Constant value for max progress
    private static final long KNITTING_COOLDOWN_MS = 333; // 2 actions per second (500ms interval)
    private long lastKnittingTime = 0; // Tracks the last time knitting occurred

    public KnittingSticksItem(Settings settings) {
        super(settings.maxDamage(MAX_PROGRESS)); // Pass the constant value here
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            // Start the knitting process
            player.setCurrentHand(hand);
        }
        return ActionResult.SUCCESS;
    }



    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastKnittingTime < KNITTING_COOLDOWN_MS) {
                return; // Prevent knitting if within cooldown
            }

            lastKnittingTime = currentTime; // Update the last knitting time

            int currentProgress = stack.getDamage(); // Get current durability (progress)
            currentProgress++;

            // Play the "walk on wool" sound every knitting tick
            world.playSound(
                    null, // Source
                    player.getBlockPos(), // Position of the sound
                    SoundEvents.BLOCK_WOOL_STEP, // Walk on wool sound
                    SoundCategory.PLAYERS, // Player sound category
                    0.5F, // Volume
                    0.4F / (player.getWorld().getRandom().nextFloat() * 0.4F + 0.8F) // Random pitch
            );

            if (currentProgress >= MAX_PROGRESS) {
                // Knitting complete
                completeKnitting(player, stack);
                player.stopUsingItem();
            } else {
                stack.setDamage(currentProgress); // Increment progress
            }
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true; // Always show the durability bar
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int progress = stack.getDamage();
        return (int) (13.0F * progress / MAX_PROGRESS); // Progress bar increases as progress grows
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        int progress = stack.getDamage();
        float ratio = (float) progress / MAX_PROGRESS;
        return ratio < 0.5F
                ? 0xFF0000 | (int) (0x00FF00 * ratio * 2) // From red to yellow
                : 0x00FF00 | (int) (0xFF0000 * (1 - ratio) * 2); // From yellow to green
    }

    private void completeKnitting(PlayerEntity player, ItemStack stack) {
        // Play pickup sound upon completion
        player.getWorld().playSound(
                player, // Source
                player.getBlockPos(), // Position of the sound
                SoundEvents.ENTITY_ITEM_PICKUP, // Pickup sound
                SoundCategory.PLAYERS, // Player sound category
                2.0F, // Volume
                0.4F / (player.getWorld().getRandom().nextFloat() * 0.4F + 0.8F) // Random pitch
        );

        // If the item is knitting grass fiber, give twine
        if (stack.getItem() == ModItems.KNITTING_GRASS_FIBER) {
            ItemStack twine = new ItemStack(ModItems.TWINE);
            if (!player.getInventory().insertStack(twine)) {
                player.dropItem(twine, true);
            }
        }

        stack.decrement(1); // Consume the current knitting stick

        // Give the default knitting stick
        ItemStack knittingStick = new ItemStack(ModItems.KNITTING_STICKS);
        if (!player.getInventory().insertStack(knittingStick)) {
            player.dropItem(knittingStick, true);
        }
    }
}
