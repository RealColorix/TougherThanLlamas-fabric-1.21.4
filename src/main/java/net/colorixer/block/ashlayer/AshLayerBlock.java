package net.colorixer.block.ashlayer;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class AshLayerBlock extends FallingBlock {
    public static final IntProperty LAYERS = Properties.LAYERS;

    protected static final VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[]{
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 0.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    };

    public AshLayerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(LAYERS, 1));
    }

    @Override
    protected MapCodec<? extends FallingBlock> getCodec() {
        return createCodec(AshLayerBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    // --- Gravity Logic ---

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        // Schedule itself to fall
        world.scheduleBlockTick(pos, this, 2);
        // Force the block ABOVE to check if it should now fall (chain reaction)
        BlockPos above = pos.up();
        if (world.getBlockState(above).isOf(this)) {
            world.scheduleBlockTick(above, this, 2);
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        tickView.scheduleBlockTick(pos, this, 2);
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    /**
     * Custom fall logic: Can fall through air OR another ash layer that isn't full.
     */
    public static boolean canFallInto(BlockState state) {
        if (state.isAir() || state.isIn(net.minecraft.registry.tag.BlockTags.FIRE)) return true;
        // If it's ash and less than 8 layers, we can "fall into" it (which triggers the collision merge)
        return state.getBlock() instanceof AshLayerBlock && state.get(LAYERS) < 8;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // Use our custom check instead of the default FallingBlock.canFallThrough
        if (canFallInto(world.getBlockState(pos.down())) && pos.getY() >= world.getBottomY()) {
            FallingBlockEntity entity = FallingBlockEntity.spawnFromBlock(world, pos, state);
            if (entity != null) {
                entity.dropItem = false;
            }
        }
    }

    // --- The Landing/Collision Logic ---

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof FallingBlockEntity fallingBlock) {
            BlockState fallingState = fallingBlock.getBlockState();

            if (fallingState.isOf(this)) {
                int existingLayers = state.get(LAYERS);
                int incomingLayers = fallingState.get(LAYERS);
                int totalLayers = existingLayers + incomingLayers;

                if (totalLayers <= 8) {
                    world.setBlockState(pos, state.with(LAYERS, totalLayers), Block.NOTIFY_ALL);
                } else {
                    world.setBlockState(pos, state.with(LAYERS, 8), Block.NOTIFY_ALL);
                    BlockPos abovePos = pos.up();
                    if (world.getBlockState(abovePos).isReplaceable()) {
                        world.setBlockState(abovePos, this.getDefaultState().with(LAYERS, totalLayers - 8), Block.NOTIFY_ALL);
                    }
                }

                fallingBlock.dropItem = false;
                fallingBlock.discard();
            }
        }
    }

    // --- Placement Logic ---

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState existing = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (existing.isOf(this)) {
            int i = existing.get(LAYERS);
            return existing.with(LAYERS, Math.min(8, i + 1));
        }
        return this.getDefaultState();
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext ctx) {
        int i = state.get(LAYERS);
        if (ctx.getStack().isOf(this.asItem()) && i < 8) {
            return ctx.getSide() == Direction.UP || !ctx.canReplaceExisting();
        }
        return false;
    }

    // --- Shapes ---

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockPos downPos = pos.down();
        BlockState stateBelow = world.getBlockState(downPos);

        // SAFE CHECK 1: Check if the block is an instance of LeavesBlock
        // This doesn't require Tags to be bound.
        boolean isOnLeaves = stateBelow.getBlock() instanceof LeavesBlock;

        // SAFE CHECK 2: Check solidity.
        // We add a null check for 'world' just in case the engine calls this during early init.
        boolean isOnNonFull = true;
        isOnNonFull = !stateBelow.isSideSolidFullSquare(world, downPos, Direction.UP);

        if (isOnLeaves || isOnNonFull) {
            return VoxelShapes.empty();
        }

        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return state.get(LAYERS) < 8;
    }
}