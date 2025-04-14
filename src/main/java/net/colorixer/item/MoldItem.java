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

public class MoldItem extends Item {

    public MoldItem(Settings settings) {
        super(settings);
    }

    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    // Begin the use action on right-click.
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return ActionResult.SUCCESS;
    }

    // Called every tick while the item is being used.
    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            int ticksUsed = getMaxUseTime(stack) - remainingUseTicks;
            // Every 20 ticks swap to the next mold item.
            if (ticksUsed > 0 && ticksUsed % 20 == 0) {
                Item newItem = null;
                if (stack.getItem() == ModItems.NUGGET_MOLD) {
                    newItem = ModItems.INGOT_MOLD;
                } else if (stack.getItem() == ModItems.INGOT_MOLD) {
                    newItem = ModItems.SWORD_MOLD;
                } else if (stack.getItem() == ModItems.SWORD_MOLD) {
                    newItem = ModItems.PICKAXE_MOLD;
                }else if (stack.getItem() == ModItems.PICKAXE_MOLD) {
                    newItem = ModItems.AXE_MOLD;
                }else if (stack.getItem() == ModItems.AXE_MOLD) {
                    newItem = ModItems.SHOVEL_MOLD;
                }else if (stack.getItem() == ModItems.SHOVEL_MOLD) {
                    newItem = ModItems.HOE_MOLD;
                }else if (stack.getItem() == ModItems.HOE_MOLD) {
                    newItem = ModItems.CHISEL_MOLD;
                }else if (stack.getItem() == ModItems.CHISEL_MOLD) {
                    newItem = ModItems.BUCKET_MOLD;
                }else if (stack.getItem() == ModItems.BUCKET_MOLD) {
                    newItem = ModItems.PLATE_MOLD;
                }else if (stack.getItem() == ModItems.PLATE_MOLD) {
                    newItem = ModItems.NUGGET_MOLD;
                }

                if (newItem != null) {
                    int slot = player.getInventory().selectedSlot;
                    ItemStack newStack = new ItemStack(newItem, stack.getCount());
                    player.getInventory().setStack(slot, newStack);
                    player.getItemCooldownManager().set(newStack, 40);
                    world.playSound(null,
                            player.getBlockPos(),
                            SoundEvents.BLOCK_COMPOSTER_FILL,
                            SoundCategory.PLAYERS,
                            1.5F,
                            1.0F);
                }
            }
        }
        super.usageTick(world, user, stack, remainingUseTicks);
    }
}
