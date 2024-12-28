package net.colorixer.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class KnittingSticksItem extends Item {

    private final int maxProgress; // Maximum progress required for knitting
    private final Item dropItem;   // Item to drop upon completion
    private final int dropQuantity; // Quantity of the item to drop
    private final SoundEvent knittingSound; // Sound to play during knitting
    private static final long KNITTING_COOLDOWN_MS = 333; // Cooldown in milliseconds
    private long lastKnittingTime = 0; // Tracks the last knitting action time

    /**
     * Constructor for KnittingSticksItem.
     *
     * @param settings      Item settings.
     * @param maxProgress   Maximum progress required for knitting.
     * @param dropItem      Item to drop upon knitting completion.
     * @param dropQuantity  Quantity of the drop item.
     * @param knittingSound Sound to play during knitting.
     */
    public KnittingSticksItem(Settings settings, int maxProgress, Item dropItem, int dropQuantity, SoundEvent knittingSound) {
        super(settings.maxDamage(maxProgress)); // Use maxProgress as max damage for progress tracking
        this.maxProgress = maxProgress;
        this.dropItem = dropItem;
        this.dropQuantity = dropQuantity;
        this.knittingSound = knittingSound;
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

            int currentProgress = stack.getDamage(); // Get current progress
            currentProgress++;

            // Play the knitting sound every knitting tick
            world.playSound(
                    null, // Source
                    player.getBlockPos(), // Position of the sound
                    this.knittingSound, // Custom knitting sound
                    SoundCategory.PLAYERS, // Player sound category
                    0.4F, // Volume
                    0.4F / (player.getWorld().getRandom().nextFloat() * 0.4F + 0.8F) // Random pitch
            );

            if (currentProgress >= maxProgress) {
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
        return true; // Always show the durability bar as progress bar
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int progress = stack.getDamage();
        return (int) (13.0F * progress / maxProgress); // Calculate progress bar step
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        int progress = stack.getDamage();
        float ratio = (float) progress / maxProgress;

        int red;
        int green;
        int blue = 0; // Blue remains constant for red-yellow-green transition

        if (ratio < 0.5F) {
            // Transition from Red to Yellow
            red = 255;
            green = (int) (510 * ratio); // 0 to 255
        } else {
            // Transition from Yellow to Green
            red = (int) (255 * (1.0F - ratio) * 2); // 255 to 0
            green = 255;
        }

        // Clamp RGB values to 0-255
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));

        // Combine RGB into a single integer
        return (red << 16) | (green << 8) | blue;
    }

    /**
     * Completes the knitting process by dropping the configured item and resetting the knitting stick.
     *
     * @param player The player performing the knitting.
     * @param stack  The knitting stick ItemStack.
     */
    public void completeKnitting(PlayerEntity player, ItemStack stack) {
        // Play pickup sound upon completion
        player.getWorld().playSound(
                player, // Source
                player.getBlockPos(), // Position of the sound
                SoundEvents.ENTITY_ITEM_PICKUP, // Sound event
                SoundCategory.PLAYERS, // Sound category
                2.0F, // Volume
                0.4F / (player.getWorld().getRandom().nextFloat() * 0.4F + 0.8F) // Random pitch
        );

        // Create the drop item stack
        ItemStack item = new ItemStack(this.dropItem, this.dropQuantity);
        if (!player.getInventory().insertStack(item)) {
            player.dropItem(item, true); // Drop in the world if inventory is full
        }

        stack.decrement(1); // Consume the current knitting stick

        // Give the default knitting stick back to the player
        ItemStack knittingStick = new ItemStack(ModItems.KNITTING_STICKS);
        if (!player.getInventory().insertStack(knittingStick)) {
            player.dropItem(knittingStick, true); // Drop in the world if inventory is full
        }
    }
}
