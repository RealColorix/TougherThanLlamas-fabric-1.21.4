package net.colorixer.mixin;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BiConsumer;

@Mixin(StraightTrunkPlacer.class)
public class StraightTrunkPlacerMixin {

    @Inject(method = "generate", at = @At("RETURN"))
    private void replaceStraightBase(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config, CallbackInfoReturnable<List<FoliagePlacer.TreeNode>> cir) {
        // Only trigger for 1 out of 7 trees
        if (random.nextInt(7) != 0) return;

        BlockState trunkState = config.trunkProvider.get(random, startPos);

        if (trunkState.isOf(Blocks.OAK_LOG)) {
            replacer.accept(startPos, ModBlocks.OAK_BOTTOM_LOG.getDefaultState());
        } else if (trunkState.isOf(Blocks.BIRCH_LOG)) {
            replacer.accept(startPos, ModBlocks.BIRCH_BOTTOM_LOG.getDefaultState());
        }
    }
}