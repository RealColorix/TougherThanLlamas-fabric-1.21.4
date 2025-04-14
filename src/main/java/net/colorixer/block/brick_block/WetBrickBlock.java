package net.colorixer.block.brick_block;

import com.mojang.serialization.MapCodec;
import net.colorixer.block.ModBlockEntities;
import net.colorixer.util.IdentifierUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
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

public class WetBrickBlock extends BlockWithEntity {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public WetBrickBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    private static final VoxelShape SHAPE_EAST  = Block.createCuboidShape(6, 0, 2, 10, 2, 14);
    private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(2, 0, 6, 14, 2, 10);
    private static final VoxelShape SHAPE_WEST  = Block.createCuboidShape(6, 0, 2, 10, 2, 14);
    private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(2, 0, 6, 14, 2, 10);

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing());
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
        if (worldView instanceof World world) {
            // Break from water source or flowing water touching the block
            if (world.getFluidState(pos).getFluid() == Fluids.WATER) {
                ItemStack drop = new ItemStack(Registries.ITEM.get(IdentifierUtil.createIdentifier("ttll", "wet_brick")));
                dropStack(world, pos, drop);
                world.removeBlock(pos, false);
                return Blocks.AIR.getDefaultState();
            }

            // Break from block below being removed
            if (direction == Direction.DOWN) {
                BlockPos below = pos.down();
                BlockState blockBelow = world.getBlockState(below);
                if (!blockBelow.isSideSolidFullSquare(world, below, Direction.UP)) {
                    ItemStack drop = new ItemStack(Registries.ITEM.get(IdentifierUtil.createIdentifier("ttll", "crude_clay")));
                    dropStack(world, pos, drop);
                    world.removeBlock(pos, false);
                    return Blocks.AIR.getDefaultState();
                }
            }
        }

        return super.getStateForNeighborUpdate(state, worldView, tickView, pos, direction, neighborPos, neighborState, random);
    }


    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WetBrickBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : (type == ModBlockEntities.WET_BRICK_BLOCK_ENTITY
                ? (BlockEntityTicker<T>) (BlockEntityTicker<WetBrickBlockEntity>) WetBrickBlockEntity::tick
                : null);
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

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!world.isClient) return;

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof WetBrickBlockEntity entity)) return;

        boolean canDry = world.isSkyVisible(pos.up()) &&
                !world.isRaining() &&
                !world.isThundering() &&
                world.isDay();

        // If the block is drying, spawn water evaporation particles.
        if (canDry && random.nextFloat() < 0.2f) {
            double px = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double py = pos.getY() + 0.2;
            double pz = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;

            // Use the built-in cloud particle to simulate water evaporation.
            world.addParticle(ParticleTypes.CLOUD, px, py, pz, 0, 0.02, 0);
        }
    }

}
