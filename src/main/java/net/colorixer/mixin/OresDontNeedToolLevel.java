package net.colorixer.mixin;

import net.colorixer.block.ModBlocks;
import net.colorixer.item.ModItems;
import net.colorixer.util.IdentifierUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class OresDontNeedToolLevel {

    private static final TagKey<Item> BEGINNER_MINING_TOOLS = TagKey.of(
            RegistryKeys.ITEM,
            IdentifierUtil.createIdentifier("ttll", "beginner_stone_mining_tools")
    );

    @Inject(method = "isSuitableFor", at = @At("HEAD"), cancellable = true)
    private void allowBeginnerToolsToMine(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (stack.isIn(BEGINNER_MINING_TOOLS)) {
            Block block = state.getBlock();

            // 1. ALLOW LIST (The only things wood-tier can "cheat" on)
            if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
                    block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
                cir.setReturnValue(true);

            }
        } else if (stack.isOf(ModItems.IRON_CHISEL)) {
            Block block = state.getBlock();

            // 1. ALLOW LIST (The only things wood-tier can "cheat" on)
            if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
                    block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE||
                    block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE ||
                    block == ModBlocks.BIRCH_BOTTOM_LOG_CHISELED || block == ModBlocks.OAK_BOTTOM_LOG_CHISELED||
                    block == ModBlocks.BIRCH_BOTTOM_LOG || block == ModBlocks.OAK_BOTTOM_LOG) {
                cir.setReturnValue(true);

            }
        }
    }
}
