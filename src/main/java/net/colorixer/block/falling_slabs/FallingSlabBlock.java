package net.colorixer.block.falling_slabs;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.state.property.Properties.WATERLOGGED;

public class FallingSlabBlock extends FallingBlock implements Waterloggable {
    public static final EnumProperty<SlabType> TYPE = Properties.SLAB_TYPE;

    protected static final VoxelShape BOTTOM_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);

    public static final MapCodec<FallingSlabBlock> CODEC = createCodec(FallingSlabBlock::new);

    public FallingSlabBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(TYPE, SlabType.BOTTOM)
                .with(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends FallingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED);  // Add WATERLOGGED here
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(TYPE)) {
            case DOUBLE -> VoxelShapes.fullCube();
            case TOP -> TOP_SHAPE;
            case BOTTOM -> BOTTOM_SHAPE;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(TYPE)) {
            case DOUBLE -> VoxelShapes.fullCube();
            case TOP -> TOP_SHAPE;
            case BOTTOM -> BOTTOM_SHAPE;
        };
    }


    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        BlockState existingState = ctx.getWorld().getBlockState(pos);
        FluidState fluidState = ctx.getWorld().getFluidState(pos);

        if (existingState.isOf(this)) {
            if (existingState.get(TYPE) != SlabType.DOUBLE &&
                    (!existingState.get(WATERLOGGED) || fluidState.getFluid() == Fluids.WATER)) {
                return existingState.with(TYPE, SlabType.DOUBLE)
                        .with(WATERLOGGED, false); // Double slabs can't be waterlogged
            }
        }

        SlabType intendedType = ctx.getSide() == Direction.DOWN || (ctx.getHitPos().y - pos.getY() > 0.5D) ?
                SlabType.TOP : SlabType.BOTTOM;

        return getDefaultState()
                .with(TYPE, intendedType)
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            boolean shouldFall = false;

            if (state.get(TYPE) == SlabType.TOP) {
                shouldFall = true;
            } else if (state.get(TYPE) == SlabType.BOTTOM) {
                BlockPos below = pos.down();
                BlockState belowState = world.getBlockState(below);
                VoxelShape shape = belowState.getCollisionShape(world, below);

                if (shape.isEmpty() || shape.getMax(Direction.Axis.Y) < 1.0) {
                    shouldFall = true;
                }
            }

            if (shouldFall) {
                // Remove the block first to prevent visual glitch
                world.removeBlock(pos, false);

                // Create falling entity with correct initial position
                double yOffset = (state.get(TYPE) == SlabType.TOP) ? -0.5 : 0;
                FallingBlockEntity entity = FallingBlockEntity.spawnFromBlock(
                        (ServerWorld)world,
                        pos,
                        state.with(TYPE, SlabType.BOTTOM) // Always fall as bottom
                );
                entity.setPosition(
                        pos.getX() + 0.5,
                        pos.getY() + (state.get(TYPE) == SlabType.TOP ? 0.5 : 0),
                        pos.getZ() + 0.5
                );
            }
        }
    }




    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, this.getFallDelay());
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean shouldFall;

        // Make TOP slabs fall no matter what
        if (state.get(TYPE) == SlabType.TOP) {
            shouldFall = true;
        } else {
            // Keep regular behavior for bottom/double slabs
            shouldFall = canFallThrough(world.getBlockState(pos.down()));
        }

        if (shouldFall) {
            FallingBlockEntity.spawnFromBlock(world, pos, state);
        }
    }



    @Override
    public void onLanding(World world, BlockPos pos, BlockState fallingState, BlockState currentState, FallingBlockEntity entity) {
        BlockPos belowPos = pos.down();
        BlockState belowState = world.getBlockState(belowPos);

        boolean isSameBlock = fallingState.getBlock() == belowState.getBlock();
        boolean isFallingDouble = fallingState.get(TYPE) == SlabType.DOUBLE;
        boolean isFallingSingle = fallingState.get(TYPE) != SlabType.DOUBLE;
        boolean isBelowBottomSlab = belowState.contains(TYPE) && belowState.get(TYPE) == SlabType.BOTTOM;

        if (isSameBlock && isBelowBottomSlab) {
            if (isFallingSingle) {
                // ✅ Single slab + bottom slab = double slab
                world.setBlockState(belowPos, fallingState.with(TYPE, SlabType.DOUBLE), Block.NOTIFY_ALL);
                world.removeBlock(pos, false);
                entity.dropItem = false;
                return;
            }

            if (isFallingDouble) {
                // ✅ Double slab + bottom slab = double slab below + bottom slab above
                world.setBlockState(belowPos, fallingState.with(TYPE, SlabType.DOUBLE), Block.NOTIFY_ALL);
                world.setBlockState(pos, fallingState.with(TYPE, SlabType.BOTTOM), Block.NOTIFY_ALL);
                entity.dropItem = false;
                return;
            }
        }

        // ✅ If it was a DOUBLE slab, place it as is (don’t downgrade)
        if (isFallingDouble) {
            world.setBlockState(pos, fallingState.with(TYPE, SlabType.DOUBLE), Block.NOTIFY_ALL);
            return;
        }

        // Default fallback: place as BOTTOM slab
        world.setBlockState(pos, fallingState.with(TYPE, SlabType.BOTTOM), Block.NOTIFY_ALL);
    }



    @Override
    public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        BlockState fallingState = fallingBlockEntity.getBlockState();

        // Check if the falling block is a double slab
        if (fallingState.get(TYPE) == SlabType.DOUBLE) {
            // Drop 2 slabs instead of 1
            Block.dropStack(world, pos, new ItemStack(this, 1));
        } else {
            // Default behavior for single slabs
            super.onDestroyedOnLanding(world, pos, fallingBlockEntity);
        }
    }



    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        SlabType slabType = state.get(TYPE);
        if (slabType == SlabType.DOUBLE || !context.getStack().isOf(this.asItem())) {
            return false;
        }

        if (context.canReplaceExisting()) {
            boolean clickedUpper = context.getHitPos().y - context.getBlockPos().getY() > 0.5D;
            Direction side = context.getSide();

            if (slabType == SlabType.BOTTOM) {
                return side == Direction.UP || clickedUpper && side.getAxis().isHorizontal();
            }
            return side == Direction.DOWN || !clickedUpper && side.getAxis().isHorizontal();
        }
        return true;
    }


    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        if (state.get(TYPE) != SlabType.DOUBLE && state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        // Get the default neighbor update behavior
        BlockState updatedState = super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);

        // Ensure double slabs can't be waterlogged
        if (updatedState.get(TYPE) == SlabType.DOUBLE && updatedState.get(WATERLOGGED)) {
            updatedState = updatedState.with(WATERLOGGED, false);
        }

        return updatedState;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        switch (type) {
            case LAND:
                return false;
            case WATER:
                return state.getFluidState().isIn(FluidTags.WATER);
            case AIR:
                return false;
            default:
                return false;
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        // Only return water if not a double slab
        return state.get(TYPE) != SlabType.DOUBLE && state.get(WATERLOGGED)
                ? Fluids.WATER.getStill(false)
                : super.getFluidState(state);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {

        return state.get(TYPE) != SlabType.DOUBLE
                && Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
    }

    @Override
    public boolean canFillWithFluid(@Nullable PlayerEntity player, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.get(TYPE) != SlabType.DOUBLE ? Waterloggable.super.canFillWithFluid(player, world, pos, state, fluid) : false;
    }

}
