package net.colorixer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public abstract class DoorWaterloggableMixin extends Block implements Waterloggable {

    public DoorWaterloggableMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "appendProperties", at = @At("TAIL"))
    private void ttll$addWaterloggedProperty(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(Properties.WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState ttll$setLowerWaterlogged(BlockState original, ItemPlacementContext ctx) {
        if (original == null) return null;
        // Check if placed in water
        boolean isWater = ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER);
        return original.with(Properties.WATERLOGGED, isWater);
    }

    @Inject(method = "onPlaced", at = @At("HEAD"), cancellable = true)
    private void ttll$onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        BlockPos upperPos = pos.up();
        boolean isUpperWaterlogged = world.getFluidState(upperPos).isOf(Fluids.WATER);

        // If top is water, we already handle bottom in getPlacementState,
        // but this ensures the top state is correctly set on placement.
        world.setBlockState(upperPos, state.with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(Properties.WATERLOGGED, isUpperWaterlogged), 3);
        ci.cancel();
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("RETURN"), cancellable = true)
    private void ttll$updateWaterloggedState(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random, CallbackInfoReturnable<BlockState> cir) {
        BlockState result = cir.getReturnValue();
        if (result == null || result.isAir()) return;

        // 1. Check if CURRENT block is in water
        boolean isDirectlyInWater = world.getFluidState(pos).isOf(Fluids.WATER);

        // 2. Add your specific logic: If I am the bottom and the top is water, I am waterlogged.
        boolean isWaterloggedByNeighbor = false;
        DoubleBlockHalf half = state.get(DoorBlock.HALF);

        BlockPos otherHalfPos = (half == DoubleBlockHalf.LOWER) ? pos.up() : pos.down();
        if (world.getFluidState(otherHalfPos).isOf(Fluids.WATER)) {
            isWaterloggedByNeighbor = true;
        }

        // Apply waterlogging if either condition is true
        boolean shouldBeWaterlogged = isDirectlyInWater || isWaterloggedByNeighbor;

        result = result.with(Properties.WATERLOGGED, shouldBeWaterlogged);
        cir.setReturnValue(result);

        if (shouldBeWaterlogged) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
    }
}