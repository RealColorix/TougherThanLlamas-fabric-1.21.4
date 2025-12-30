package net.colorixer.item.items;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
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

    public KnittingSticksItem(Settings settings,
                              int maxProgress,
                              Item dropItem,
                              int dropQuantity,
                              Item dropToolItem,
                              SoundEvent knittingSound) {

        super(
                settings
                        .maxDamage(maxProgress)
                        // Required so the consumable path is used
                        .component(
                                DataComponentTypes.FOOD,
                                new FoodComponent(0, 0.0F, true)
                        )
                        // THIS controls animation + looping sound
                        .component(
                                DataComponentTypes.CONSUMABLE,
                                ConsumableComponent.builder()
                                        .consumeSeconds(999999.0F) // effectively infinite
                                        .useAction(UseAction.EAT)
                                        .sound(Registries.SOUND_EVENT.getEntry(knittingSound))
                                        .consumeParticles(false)
                                        .build()
                        )
        );

        this.maxProgress   = maxProgress;
        this.dropItem      = Objects.requireNonNull(dropItem);
        this.dropQuantity  = dropQuantity;
        this.dropToolItem  = Objects.requireNonNull(dropToolItem);
        this.knittingSound = Objects.requireNonNull(knittingSound);
    }

    /* ============================= */
    /*   USE — FOOD ONLY             */
    /* ============================= */



    /* ============================= */
    /*   KNITTING LOGIC              */
    /* ============================= */

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient || !(user instanceof PlayerEntity player)) return;

        long now = System.currentTimeMillis();
        if (now - lastKnittingTime < KNITTING_COOLDOWN_MS) return;
        lastKnittingTime = now;

        int progress = stack.getDamage() + 1;

        // Optional extra rhythm sound (safe to remove)
        world.playSound(
                null,
                player.getBlockPos(),
                knittingSound,
                SoundCategory.PLAYERS,
                0.25F,
                0.9F
        );

        if (progress >= maxProgress) {
            completeKnitting(player, stack);
            player.stopUsingItem(); // stops infinite eat animation
        } else {
            stack.setDamage(progress);
        }
    }

    /* ============================= */
    /*   ITEM BAR                    */
    /* ============================= */

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

        return (red << 16) | (green << 8) | blue;
    }

    /* ============================= */
    /*   COMPLETION                  */
    /* ============================= */

    private void completeKnitting(PlayerEntity player, ItemStack stack) {
        World world = player.getWorld();

        world.playSound(
                player,
                player.getBlockPos(),
                SoundEvents.ENTITY_ITEM_PICKUP,
                SoundCategory.NEUTRAL,
                2.0F,
                0.5F
        );

        stack.decrement(1);

        ItemStack result = new ItemStack(dropItem, dropQuantity);
        if (!player.getInventory().insertStack(result)) {
            player.dropItem(result, true);
        }

        ItemStack toolBack = new ItemStack(dropToolItem, 1);
        if (!player.getInventory().insertStack(toolBack)) {
            player.dropItem(toolBack, true);
        }
    }
}
