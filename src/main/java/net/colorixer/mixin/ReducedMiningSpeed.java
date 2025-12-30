package net.colorixer.mixin;

import net.colorixer.block.ModBlocks;
import net.colorixer.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class ReducedMiningSpeed {

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyMiningSpeed(BlockState blockState, CallbackInfoReturnable<Float> cir) {
        Float originalSpeed = cir.getReturnValue();
        if (originalSpeed == null || originalSpeed <= 0.0f) return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack heldItem = player.getMainHandStack();
        Item tool = heldItem.getItem();

        /* ============================= */
        /*   SPECIAL TOOL RULES          */
        /* ============================= */

        if (tool == ModItems.FLINT_KNIFE &&
                (blockState.isOf(Blocks.COBWEB) || blockState.isOf(ModBlocks.COBWEB_FUll))) {
            cir.setReturnValue(originalSpeed * 0.04f);
            return;
        }
        if (tool == ModItems.WOODEN_CLUB || tool == ModItems.ZOMBIE_ARM &&
                (blockState.isOf(Blocks.COBWEB) || blockState.isOf(ModBlocks.COBWEB_FUll))) {
            cir.setReturnValue(originalSpeed * -1f);
            return;
        }

        if (tool == ModItems.SHARP_ROCK && blockState.isIn(BlockTags.LOGS)) {
            cir.setReturnValue(originalSpeed * 0.5f);
            return;
        }

        if (tool == ModItems.SHARP_ROCK && blockState.isOf(Blocks.IRON_ORE)) {
            cir.setReturnValue(originalSpeed * 0.1F);
            return;
        }
        /* ============================= */
        /*   TIER ENFORCEMENT (FIX)      */
        /* ============================= */

        if (blockState.isToolRequired()) {

            boolean correctToolType = heldItem.isSuitableFor(blockState);

            // Wrong tool entirely
            if (!correctToolType && !EXCEPTION_BLOCKS.contains(blockState.getBlock())) {
                cir.setReturnValue(originalSpeed * 0.01f);
                return;
            }

            // Correct tool type but tier too low
            if (correctToolType && !toolMeetsTierRequirement(tool, blockState)) {
                cir.setReturnValue(originalSpeed * 0.04f);
                return;
            }
        }

        /* ============================= */
        /*   GLOBAL SLOWDOWN             */
        /* ============================= */

        cir.setReturnValue(originalSpeed * 0.25f);
    }

    /* ============================= */
    /*   TIER CHECK (1.21.4 SAFE)    */
    /* ============================= */

    private boolean toolMeetsTierRequirement(Item tool, BlockState state) {

        // Diamond-required blocks
        if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return tool == Items.DIAMOND_PICKAXE || tool == Items.NETHERITE_PICKAXE;
        }

        // Iron-required blocks
        if (state.isIn(BlockTags.NEEDS_IRON_TOOL)) {
            return tool == Items.IRON_PICKAXE
                    || tool == Items.DIAMOND_PICKAXE
                    || tool == Items.NETHERITE_PICKAXE;
        }

        // Stone-required blocks
        if (state.isIn(BlockTags.NEEDS_STONE_TOOL)) {
            return tool == Items.STONE_PICKAXE
                    || tool == Items.IRON_PICKAXE
                    || tool == Items.DIAMOND_PICKAXE
                    || tool == Items.NETHERITE_PICKAXE;
        }

        return true;
    }

    private static final List<Block> EXCEPTION_BLOCKS = List.of(
            Blocks.SNOW,
            Blocks.SNOW_BLOCK,
            Blocks.POWDER_SNOW
    );
}
