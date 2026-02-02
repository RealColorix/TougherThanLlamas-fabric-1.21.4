package net.colorixer.item.items;

import net.colorixer.block.furnace.FurnaceBlock;
import net.colorixer.block.furnace.FurnaceBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
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
        PlayerEntity player = context.getPlayer();

        // Check if it's the furnace and it's not lit
        if (state.getBlock() instanceof FurnaceBlock && !state.get(FurnaceBlock.LIT)) {
            if (world.getBlockEntity(pos) instanceof FurnaceBlockEntity furnace) {

                // Only try if there's fuel
                if (furnace.getFuel() > 0) {
                    if (!world.isClient) {
                        // Ignition logic (Chance based)
                        if (world.random.nextDouble() < this.chance) {
                            furnace.ignite();
                            world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 1.0f);
                        } else {
                            world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 0.2f, 1.5f);
                        }

                        // Damage the item
                        if (player != null) {
                            context.getStack().damage(1, player,
                                    context.getHand() == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                        }
                    }
                    // Return SUCCESS to ensure the swing animation plays
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }
}