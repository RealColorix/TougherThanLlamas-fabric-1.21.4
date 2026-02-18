package net.colorixer.mixin;

import net.colorixer.block.AshLayerBlock;
import net.colorixer.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireBlock.class)
public class FireBlockMixin {

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void ttll$removeFireBlockHitbox(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        cir.setReturnValue(VoxelShapes.empty());
    }

    @Inject(method = "getShapeForState", at = @At("HEAD"), cancellable = true)
    private static void ttll$forceEmptyFireBuilder(BlockState state, CallbackInfoReturnable<VoxelShape> cir) {
        cir.setReturnValue(VoxelShapes.empty());
    }

    @Redirect(
            method = "scheduledTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z")
    )
    private boolean ttll$replaceScheduledTickRemoval(net.minecraft.server.world.ServerWorld world, BlockPos pos, boolean move) {
        return ttll$performAshLogic(world, pos);
    }

    // Target 2: trySpreadingFire (This is where blocks actually turn into fire)
    @Redirect(
            method = "trySpreadingFire",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z")
    )
    private boolean ttll$replaceSpreadRemoval(World world, BlockPos pos, boolean move) {
        return ttll$performAshLogic(world, pos);
    }

    // Shared logic helper
    private boolean ttll$performAshLogic(World world, BlockPos pos) {
        BlockState oldState = world.getBlockState(pos);
        Random random = world.getRandom();

        // ADD THIS CHECK: oldState.isAir() is false and it's not a Fire block itself
        if (!oldState.isAir() && !oldState.isOf(net.minecraft.block.Blocks.FIRE) &&
                !oldState.isIn(BlockTags.LOGS) && random.nextInt(100) < 66) {

            int layers = 1;
            return world.setBlockState(pos, ModBlocks.ASH.getDefaultState().with(AshLayerBlock.LAYERS, layers), 3);
        }

        return world.removeBlock(pos, false);
    }

}