package net.colorixer.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public abstract class SwordBlockingMixin extends Item {

    public SwordBlockingMixin(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (stack.isOf(Items.IRON_SWORD) || stack.isOf(Items.GOLDEN_SWORD) ||
                stack.isOf(Items.DIAMOND_SWORD) || stack.isOf(Items.NETHERITE_SWORD)) {

            // If holding a shield in the offhand, let the shield take over
            if (hand == Hand.MAIN_HAND && user.getOffHandStack().isOf(Items.SHIELD)) {
                // Strip the consumable component if it was added previously
                if (stack.contains(DataComponentTypes.CONSUMABLE)) {
                    stack.remove(DataComponentTypes.CONSUMABLE);
                }
                // PASS tells the game engine: "I did nothing, try the off-hand!"
                return ActionResult.PASS;
            }

            // Patch the item stack dynamically so the game engine knows it can be blocked with
            // Patch the item stack dynamically so the game engine knows it can be blocked with
            if (!stack.contains(DataComponentTypes.CONSUMABLE)) {
                stack.set(DataComponentTypes.CONSUMABLE, ConsumableComponent.builder()
                        .consumeSeconds(3600f) // Represents how long you can hold the block
                        .useAction(UseAction.BLOCK) // Triggers the old vanilla blocking animation
                        .build());
            }

            // --- ADD THIS SOUND HERE ---
            // Plays a nice metallic rustle when you raise the sword
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    net.minecraft.sound.SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                    net.minecraft.sound.SoundCategory.PLAYERS,
                    0.8F, 1.2F + (world.random.nextFloat() * 0.2F));

            user.setCurrentHand(hand);
            return ActionResult.CONSUME;

        }

        return super.use(world, user, hand);
    }
}