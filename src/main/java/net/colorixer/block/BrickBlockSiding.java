package net.colorixer.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.BlockState;
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

public class BrickBlockSiding extends FallingBlock implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0, 0, 0, 16, 16, 4);
    private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(0, 0, 12, 16, 16, 16);
    private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(0, 0, 0, 4, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(12, 0, 0, 16, 16, 16);

    public BrickBlockSiding(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false).with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends FallingBlock> getCodec() {
        return null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing()).with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, WorldView worldView, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED) && worldView instanceof World world) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
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
            case NORTH -> SHAPE_SOUTH;
            case SOUTH -> SHAPE_NORTH;
            case EAST -> SHAPE_WEST;
            case WEST -> SHAPE_EAST;
            default -> SHAPE_SOUTH;
        };
    }
}
