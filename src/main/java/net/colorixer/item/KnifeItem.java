package net.colorixer.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class KnifeItem extends Item {
    public KnifeItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(material.applySwordSettings(settings, attackDamage, attackSpeed));
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {

        return !miner.isCreative();
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        return true;
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        stack.damage(3, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {

        if (!world.isClient && miner instanceof PlayerEntity player && !player.isCreative()) {
            // Take 1 durability damage for each block broken
            stack.damage(1, player, EquipmentSlot.MAINHAND);
        }
        return super.postMine(stack, world, state, pos, miner);
    }
}
