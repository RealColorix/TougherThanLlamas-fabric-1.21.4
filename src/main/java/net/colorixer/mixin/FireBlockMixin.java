package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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


}