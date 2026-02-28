package net.colorixer.block.crops;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class HempBlock extends CropBlock {

    public static final IntProperty AGE = Properties.AGE_7;

    private static final VoxelShape STAGE_0_SHAPE =
            Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
    private static final VoxelShape STAGE_1_SHAPE =
            Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 12.0, 10.0);
    private static final VoxelShape FULL_SHAPE =
            Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    public HempBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int age = getAge(state);
        if (age == 0) return STAGE_0_SHAPE;
        if (age == 1) return STAGE_1_SHAPE;
        return FULL_SHAPE;
    }

    @Override
    public IntProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 5; // cap at 5 even though AGE_7 exists
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.down());

        if (below.isOf(Blocks.FARMLAND)) return true;

        if (below.isOf(this)) {
            BlockState floorBelow = world.getBlockState(pos.down(2));
            return floorBelow.isOf(Blocks.FARMLAND);
        }

        return false;
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
        // --- INSTANT SYNC LOGIC ---
        // This runs every time a neighbor changes (no random tick required)
        if (direction == Direction.DOWN && neighborState.isOf(this)) {
            Property<?> statusProp = state.getBlock().getStateManager().getProperty("crop_status");
            if (statusProp instanceof IntProperty intProp) {
                int currentStatus = state.get(intProp);
                int belowStatus = neighborState.get(intProp);

                // If the bottom block status changed, force this top block to match immediately
                if (currentStatus != belowStatus) {
                    return state.with(intProp, belowStatus);
                }
            }
        }

        // Standard Minecraft behavior (checking for broken support, etc.)
        return super.getStateForNeighborUpdate(state, worldView, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int age = getAge(state);
        BlockState belowState = world.getBlockState(pos.down());
        boolean isTop = belowState.isOf(this);
        boolean isBase = belowState.isOf(Blocks.FARMLAND);


        if (!(random.nextInt(10) == 0)) return;

        // --- 1. TOP BLOCK LOGIC ---
        // We handle the top block entirely on our own to bypass the vanilla Farmland penalty.
        if (isTop) {
            // Check if there is enough light to grow
            if (world.getBaseLightLevel(pos, 0) >= 9) {
                // Roll a 1-in-5 chance to grow (adjust this number to make it faster/slower)
                if (random.nextInt(5) == 0) {
                    if (age == 2) {
                        world.setBlockState(pos, state.with(AGE, 3), 2);
                    } else if (age == 3) {
                        // Skip 4 and go straight to 5 at a normal, balanced speed
                        world.setBlockState(pos, state.with(AGE, 5), 2);
                    }
                }
            }
            // IMPORTANT: Return here so the top block NEVER calls super.randomTick!
            return;
        }

        // --- 2. BASE BLOCK LOGIC ---
        // The base block IS on farmland, so we can let vanilla Minecraft handle it.
        if (isBase) {
            super.randomTick(state, world, pos, random);

            // Re-fetch state after super call in case it grew
            BlockState currentState = world.getBlockState(pos);
            if (!currentState.isOf(this)) return;

            Property<?> statusProp = currentState.getBlock().getStateManager().getProperty("crop_status");
            if (!(statusProp instanceof IntProperty intProp)) return;
            int status = currentState.get(intProp);
            int currentAge = getAge(currentState);

            if (status == 0) {
                // If base hits age 2-4, try to spawn the top
                if (currentAge >= 2 && currentAge <= 4 && world.isAir(pos.up()) && random.nextInt(3) == 0) {
                    world.setBlockState(pos.up(), this.getDefaultState().with(AGE, 2).with(intProp, status), 3);
                }

                // Keep base capped at 4
                if (currentAge > 4) {
                    world.setBlockState(pos, currentState.with(AGE, 4), 2);
                }
            }
        }
    }
}