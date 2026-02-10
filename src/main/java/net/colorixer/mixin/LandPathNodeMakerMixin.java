package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LandPathNodeMaker.class)
public class LandPathNodeMakerMixin {
    @Inject(method = "getCommonNodeType", at = @At("HEAD"), cancellable = true)
    private static void ttll$treatLeavesAsAir(BlockView world, BlockPos pos, CallbackInfoReturnable<PathNodeType> cir) {
        BlockState state = world.getBlockState(pos);

        // Check if the block is any type of leaf
        if (state.isOf(Blocks.OAK_LEAVES) || state.isOf(Blocks.JUNGLE_LEAVES) ||
                state.isOf(Blocks.DARK_OAK_LEAVES) || state.isOf(Blocks.ACACIA_LEAVES) ||
                state.isOf(Blocks.BIRCH_LEAVES) || state.isOf(Blocks.SPRUCE_LEAVES) ||
                state.isOf(Blocks.AZALEA_LEAVES) || state.isOf(Blocks.MANGROVE_LEAVES) ||
                state.isOf(Blocks.CHERRY_LEAVES) || state.isOf(Blocks.PALE_OAK_LEAVES)) {

            // Return OPEN, which is the same NodeType as Air
            cir.setReturnValue(PathNodeType.OPEN);
        }
    }
}