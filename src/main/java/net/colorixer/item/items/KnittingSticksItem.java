package net.colorixer.item.items;

import net.colorixer.item.ModItems;
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

import java.util.Objects;

public class KnittingSticksItem extends Item {

    private final int maxProgress;
    private final Item dropItem;
    private final int dropQuantity;
    private final Item dropToolItem;
    private final SoundEvent knittingSound;
    private static final long KNITTING_COOLDOWN_MS = 333;
    private long lastKnittingTime = 0;

    /**
     * @param settings      Item settings.
     * @param maxProgress   Ticks required to finish one piece of knitting.
     * @param dropItem      Yarn or other result item. Must be non‑null.
     * @param dropQuantity  How many of dropItem to give.
     * @param dropToolItem  The knitting sticks (or broken bits) returned. Must be non‑null.
     * @param knittingSound Sound to play each knitting tick.
     */
    public KnittingSticksItem(Settings settings,
                              int maxProgress,
                              Item dropItem,
                              int dropQuantity,
                              Item dropToolItem,
                              SoundEvent knittingSound) {
        super(settings.maxDamage(maxProgress));
        this.maxProgress   = maxProgress;
        this.dropItem      = Objects.requireNonNull(dropItem,     "dropItem must not be null");
        this.dropQuantity  = dropQuantity;
        this.dropToolItem  = Objects.requireNonNull(dropToolItem, "dropToolItem must not be null");
        this.knittingSound = Objects.requireNonNull(knittingSound, "knittingSound must not be null");
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            player.setCurrentHand(hand);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient || !(user instanceof PlayerEntity player)) return;

        long now = System.currentTimeMillis();
        if (now - lastKnittingTime < KNITTING_COOLDOWN_MS) return;
        lastKnittingTime = now;

        int progress = stack.getDamage() + 1;

        world.playSound(null, player.getBlockPos(),
                knittingSound, SoundCategory.PLAYERS,
                0.3F,
                0.4F / (player.getWorld().getRandom().nextFloat() * 0.4F + 0.8F));

        if (progress >= maxProgress) {
            completeKnitting(player, stack);
            player.stopUsingItem();
        } else {
            stack.setDamage(progress);
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F * stack.getDamage() / maxProgress);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        float ratio = (float) stack.getDamage() / maxProgress;
        int red, green, blue = 0;
        if (ratio < 0.5F) {
            red   = 255;
            green = Math.round(510 * ratio);
        } else {
            red   = Math.round(255 * (1.0F - ratio) * 2);
            green = 255;
        }
        red   = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        return (red << 16) | (green << 8) | blue;
    }

    private void completeKnitting(PlayerEntity player, ItemStack stack) {
        player.getWorld().playSound(player, player.getBlockPos(),
                SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL,
                2.0F,
                0.4F / (player.getWorld().getRandom().nextFloat() * 0.4F + 0.8F));

        // consume one knitting‑stick‑use
        stack.decrement(1);

        // drop the knitted piece(s)
        ItemStack result = new ItemStack(dropItem, dropQuantity);
        if (!player.getInventory().insertStack(result)) {
            player.dropItem(result, true);
        }

        // return a tool stick (or broken sticks) back to the player
        ItemStack toolBack = new ItemStack(dropToolItem, 1);
        if (!player.getInventory().insertStack(toolBack)) {
            player.dropItem(toolBack, true);
        }
    }
}
