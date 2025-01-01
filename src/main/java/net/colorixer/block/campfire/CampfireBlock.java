package net.colorixer.block.campfire;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.block.CampfireBlock.extinguish;

public class CampfireBlock extends Block implements Waterloggable {

    public static final IntProperty STATE = IntProperty.of("state", 0, 5);
    public static final EnumProperty<Direction> FACING =
            EnumProperty.of("facing", Direction.class, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 7, 16);

    public CampfireBlock(Settings settings) {
        super(settings.nonOpaque());
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(STATE, 0)
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false));
    }

    public static void extinguish(@Nullable Entity entity, WorldAccess world, BlockPos pos, BlockState state) {
        if (world.isClient()) {
            for (int i = 0; i < 20; i++) {
                spawnSmokeParticle((World)world, pos,  true);
            }
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof net.minecraft.block.entity.CampfireBlockEntity) {
            ((CampfireBlockEntity)blockEntity).spawnItemsBeingCooked();
        }

        world.emitGameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
    }

    public static void spawnSmokeParticle(World world, BlockPos pos, boolean lotsOfSmoke) {
        Random random = world.getRandom();
        SimpleParticleType simpleParticleType = ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
        world.addImportantParticle(
                simpleParticleType,
                true,
                (double)pos.getX() + 0.5 + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1),
                (double)pos.getY() + random.nextDouble() + random.nextDouble(),
                (double)pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1),
                0.0,
                0.07,
                0.0
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STATE, FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world,
                                      BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction playerFacing = context.getHorizontalPlayerFacing();
        Direction blockFacing = playerFacing;
        FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
        boolean waterlogged = fluidState.getFluid() == Fluids.WATER;
        return this.getDefaultState()
                .with(FACING, blockFacing)
                .with(WATERLOGGED, waterlogged);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        super.onBlockAdded(state, world, pos, oldState, notify);
    }

    /**
     * Ensures that if the campfire is waterlogged, water flows correctly when
     * neighboring blocks change. This is the crucial override for updating.
     */
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
        if ((Boolean)state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }


        return state;
    }


    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        int currentState = state.get(STATE);

        // Only do these effects if the campfire is in states 1..3
        if (currentState >= 1 && currentState <= 3) {
            // -- Existing sound effect --
            if (random.nextInt(10) == 0) {
                world.playSound(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        SoundEvents.BLOCK_CAMPFIRE_CRACKLE,
                        SoundCategory.BLOCKS,
                        1F + random.nextFloat(),
                        random.nextFloat() * 0.7F + 0.6F,
                        false
                );
            }
            if (random.nextInt(10) == 0) {
                world.playSound(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        SoundEvents.BLOCK_FIRE_AMBIENT,
                        SoundCategory.BLOCKS,
                        0.5F + random.nextFloat(),
                        random.nextFloat() * 0.7F + 0.6F,
                        false
                );
            }

            // -- Existing lava-spark code --
            if (random.nextInt(5) == 0) {
                for (int i = 0; i < random.nextInt(1) + 1; i++) {
                    world.addParticle(
                            ParticleTypes.LAVA,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            random.nextFloat() / 2.0F,
                            5.0E-5,
                            random.nextFloat() / 2.0F
                    );
                }
            }

            // --------------------------------------------------
            // NEW: Spawn Large Smoke for states 1..3
            // --------------------------------------------------
            if (random.nextInt(5) == 0 && currentState == 1) {
                double sx = pos.getX() + 0.5 + (random.nextDouble() * 0.2 - 0.1);
                double sy = pos.getY() + 0.6 + (random.nextDouble() * 0.3);
                double sz = pos.getZ() + 0.5 + (random.nextDouble() * 0.2 - 0.1);

                world.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        sx, sy, sz,
                        0.0D,
                        0.02D,
                        0.0D
                );
            } else if (random.nextInt(3) == 0 && currentState == 2) {
                double sx = pos.getX() + 0.5 + (random.nextDouble() * 0.2 - 0.1);
                double sy = pos.getY() + 0.6 + (random.nextDouble() * 0.3);
                double sz = pos.getZ() + 0.5 + (random.nextDouble() * 0.2 - 0.1);

                world.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        sx, sy, sz,
                        0.0D,
                        0.02D,
                        0.0D
                );
            } else if (random.nextInt(1) == 0 && currentState == 3) {
                double sx = pos.getX() + 0.5 + (random.nextDouble() * 0.2 - 0.1);
                double sy = pos.getY() + 0.6 + (random.nextDouble() * 0.3);
                double sz = pos.getZ() + 0.5 + (random.nextDouble() * 0.2 - 0.1);

                world.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        sx, sy, sz,
                        0.0D,
                        0.02D,
                        0.0D
                );
                }

            // --------------------------------------------------
            // NEW: If state=2 → spawn Cozy Smoke; if state=3 → spawn Signal Smoke
            // --------------------------------------------------
            if (currentState == 2) {
                if (random.nextInt(2) == 0) {
                    double sx = pos.getX() + 0.5 + (random.nextDouble() * 0.4 - 0.2);
                    double sy = pos.getY() + 0.6 + (random.nextDouble() * 0.3);
                    double sz = pos.getZ() + 0.5 + (random.nextDouble() * 0.4 - 0.2);

                    world.addParticle(
                            ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            sx, sy, sz,
                            0.0D,
                            0.07D,
                            0.0D
                    );
                }
            } else if (currentState == 3) {
                if (random.nextInt(1) == 0) {
                    double sx = pos.getX() + 0.5 + (random.nextDouble() * 0.4 - 0.2);
                    double sy = pos.getY() + 0.6 + (random.nextDouble() * 0.3);
                    double sz = pos.getZ() + 0.5 + (random.nextDouble() * 0.4 - 0.2);

                    world.addParticle(
                            ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                            sx, sy, sz,
                            0.0D,
                            0.07D,
                            0.0D
                    );
                }
            }
        }
    }


    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        // We only waterlog if the block is not already WATERLOGGED and the fluid is WATER
        if (!state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {

            int currentState = state.get(STATE);
            // If it's currently lit, play extinguish sound and run the extinguish logic
            if (currentState >= 1 && currentState <= 3) {
                if (!world.isClient()) {
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                extinguish(null, world, pos, state);
            }


            if (currentState >= 1 && currentState <= 3) {
                state = state.with(STATE, 4);
            }

            // Update block to WATERLOGGED + unlit
            world.setBlockState(
                    pos,
                    state.with(WATERLOGGED, true),
                    Block.NOTIFY_ALL
            );

            // Schedule water flow updates
            world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            return true;
        }
        return false;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);

        if (entity instanceof LivingEntity livingEntity) {
            double localX = entity.getX() - pos.getX();
            double localZ = entity.getZ() - pos.getZ();

            if (localX > 0.06125 && localX < 0.93875 && localZ > 0.06125 && localZ < 0.93875) {
                int currentState = state.get(STATE);
                Random random = world.getRandom();

                switch (currentState) {
                    case 1 -> {
                        // Fire for 1..3 seconds
                        // (Note: random.nextInt(4) returns 0..3. If you really want 1..3, do random.nextInt(3)+1.)
                        int fireSeconds = 1 + random.nextInt(3);
                        livingEntity.setOnFireFor(fireSeconds);
                    }
                    case 2 -> {
                        // Fire for 3..5 seconds
                        // (Note: random.nextInt(5) returns 0..4, so 2+ that is 2..6.)
                        int fireSeconds = 2 + random.nextInt(5);
                        livingEntity.setOnFireFor(fireSeconds);
                    }
                    case 3 -> {
                        // Fire for 5..9 seconds
                        // (Note: random.nextInt(7) returns 0..6, so 4+ that is 4..10.)
                        int fireSeconds = 4 + random.nextInt(7);
                        livingEntity.setOnFireFor(fireSeconds);
                    }
                    default -> {
                        // STATE = 0, 4, 5, etc. do nothing
                    }
                }
            }
        }
    }


}
