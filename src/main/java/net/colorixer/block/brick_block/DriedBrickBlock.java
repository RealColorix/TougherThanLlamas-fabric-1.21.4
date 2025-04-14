package net.colorixer.block.brick_block;

import net.colorixer.util.IdentifierUtil;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.tick.ScheduledTickView;
import net.minecraft.registry.Registries;
import net.minecraft.item.ItemStack;

public class DriedBrickBlock extends Block implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public DriedBrickBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(WATERLOGGED, false)
                .with(FACING, Direction.NORTH));
    }

    private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(6, 0, 2, 10, 2, 14);
    private static final VoxelShape SHAPE_SOUTH  = Block.createCuboidShape(2, 0, 6, 14, 2, 10);
    private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(6, 0, 2, 10, 2, 14);
    private static final VoxelShape SHAPE_NORTH  = Block.createCuboidShape(2, 0, 6, 14, 2, 10);

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing())
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos below = pos.down();
        BlockState blockBelow = world.getBlockState(below);
        return blockBelow.isSideSolidFullSquare(world, below, Direction.UP);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView worldView,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        if (state.get(WATERLOGGED) && worldView instanceof World world) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        // Destroy block if no support from below
        if (direction == Direction.DOWN && worldView instanceof World world) {
            BlockPos below = pos.down();
            BlockState blockBelow = world.getBlockState(below);
            if (!blockBelow.isSideSolidFullSquare(worldView, below, Direction.UP)) {
                // Drop 1 dried brick item (make sure it's registered)
                ItemStack drop = new ItemStack(Registries.ITEM.get(IdentifierUtil.createIdentifier("ttll", "dried_brick")));
                dropStack(world, pos, drop);
                world.removeBlock(pos, false);
                return Blocks.AIR.getDefaultState();
            }
        }

        return super.getStateForNeighborUpdate(state, worldView, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST  -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }
}
