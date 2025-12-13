package net.colorixer.item.items;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class KnifeItem extends SwordItem {

    public KnifeItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient) {
            boolean isInstantBreakFoliage = state.getHardness(world, pos) == 0.0F;

            if (isInstantBreakFoliage && stack.isDamageable()) {
                stack.damage(1, (PlayerEntity) miner); // simple damage call


            }

            if (stack.getDamage() >= stack.getMaxDamage() -1) {
                if (miner instanceof PlayerEntity player) {
                    PlayerInventory inv = player.getInventory();
                    int selected = inv.selectedSlot;
                    inv.setStack(selected, ItemStack.EMPTY);

                    world.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_ITEM_BREAK,
                            SoundCategory.PLAYERS,
                            1.0F, 1.0F
                    );

                    world.syncWorldEvent(200, player.getBlockPos(), 0);
                }
            }
        }

        return super.postMine(stack, world, state, pos, miner);
    }
}
