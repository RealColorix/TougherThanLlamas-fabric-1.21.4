package net.colorixer.block.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext; // Required for checking placement
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager; // Required for registering block states
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class GrassSeedsBlock extends PlantBlock {

    public static final BooleanProperty FERTILIZED = BooleanProperty.of("fertilized");

    // 1. Define your new block state property
    public static final BooleanProperty ON_FARMLAND = BooleanProperty.of("on_farmland");

    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

    public GrassSeedsBlock(Settings settings) {
        super(settings);
        // 2. Set the default state so the game doesn't crash on load
        this.setDefaultState(this.stateManager.getDefaultState().with(ON_FARMLAND, false));
    }

    // 3. Register the property to this block
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ON_FARMLAND);
    }

    // 4. Check the block below when the player places the seeds
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockBelow = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
        boolean isFarmland = blockBelow.isOf(Blocks.FARMLAND);

        return this.getDefaultState().with(ON_FARMLAND, isFarmland);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return null;
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int growthChance = 25;

        BlockState blockBelow = world.getBlockState(pos.down());

        if (blockBelow.contains(FERTILIZED) && blockBelow.get(FERTILIZED)) {
            growthChance = 18; // Change chance to 1/18
        }

        if (random.nextInt(growthChance) == 0) {
            world.setBlockState(pos, Blocks.SHORT_GRASS.getDefaultState());
        }
    }
}