package net.colorixer.mixin;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.LargeOakTrunkPlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BiConsumer;

@Mixin(LargeOakTrunkPlacer.class)
public class LargeOakTrunkPlacerMixin {

    @Inject(method = "generate", at = @At("RETURN"))
    private void replaceLargeOakBase(
            TestableWorld world,
            BiConsumer<BlockPos, BlockState> replacer,
            Random random,
            int height,
            BlockPos startPos,
            TreeFeatureConfig config,
            CallbackInfoReturnable<List<FoliagePlacer.TreeNode>> cir
    ) {
        if (random.nextInt(7) != 0) return;

        replacer.accept(startPos, ModBlocks.OAK_BOTTOM_LOG.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Y));
    }
}