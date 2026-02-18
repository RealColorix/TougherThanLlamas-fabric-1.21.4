package net.colorixer.block.campfire;

import com.mojang.serialization.MapCodec;
import net.colorixer.item.ModItems;
import net.colorixer.item.items.firestarteritem.FireStarterItem;
import net.colorixer.item.items.firestarteritem.FireStarterItemSmoke;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class CampfireBlock extends FallingBlock implements BlockEntityProvider, Waterloggable {
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty LIT = Properties.LIT;
    public static final IntProperty STAGE = IntProperty.of("stage", 0, 5);
    public static final BooleanProperty STICK = BooleanProperty.of("stick");
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final VoxelShape BOX_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 7, 16);

    public CampfireBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(LIT, false)
                .with(STAGE, 0)
                .with(STICK, false)
                .with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, STAGE, STICK, WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
        return this.getDefaultState()
                .with(FACING, context.getHorizontalPlayerFacing().getOpposite())
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    // --- FIXED SIGNATURE FOR 1.21.2+ ---
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
        if (state.get(WATERLOGGED)) {
            // Tells the liquid to start ticking/flowing
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BOX_SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {

        if (!state.get(Properties.LIT) && player.getStackInHand(player.getActiveHand()).getItem() instanceof FireStarterItem){
            if (!world.isClient) {
                FireStarterItemSmoke.spawnFrictionEffects(world, hit.getPos(), hit.getSide());
            }
        }

        if (world.isClient) return ActionResult.SUCCESS;
        if (world.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
            return campfire.onRightClick(player, player.getActiveHand());
        }
        return ActionResult.CONSUME;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CampfireBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : (w, p, s, be) -> { if (be instanceof CampfireBlockEntity cf) cf.tick(); };
    }

    @Override
    protected MapCodec<? extends FallingBlock> getCodec() { return null; }

    // 1. Only allow placement on solid, full-face blocks
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos downPos = pos.down();
        return world.getBlockState(downPos).isSideSolidFullSquare(world, downPos, Direction.UP);
    }

    // 2. Break the attachments when the block falls and lands
    @Override
    public void onLanding(World world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof CampfireBlockEntity campfire) {

                // Drop the Pointy Stick if it was attached
                if (fallingBlockState.get(STICK)) {
                    world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            new ItemStack(ModItems.POINTY_STICK)));

                    // Remove the stick from the block state upon landing
                    world.setBlockState(pos, world.getBlockState(pos).with(STICK, false), 3);
                }

                // Drop the inventory (food/cooking item)
                if (!campfire.getInventory().isEmpty()) {
                    world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            campfire.getInventory().copy()));

                    campfire.getInventory().setCount(0); // Clear it
                    campfire.markDirty();
                }

                // Optional: Play a wood breaking sound on landing
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.0f, 0.8f);
            }
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!state.get(LIT)) return;

        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.3; // Base of the logs
        double z = (double)pos.getZ() + 0.5;

        // 1. STANDARD CAMPFIRE SMOKE
        if (random.nextInt(10) == 0) {
            world.playSound(x, y, z, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5f + random.nextFloat(), random.nextFloat() * 0.7f + 0.6f, false);
        }

        // Spawn campfire smoke particles
        world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x + random.nextDouble() * 0.2 - 0.1, y + 0.4, z + random.nextDouble() * 0.2 - 0.1, 0.0, 0.07, 0.0);

        // 2. LAVA PARTICLES (Sparks)
        if (random.nextInt(5) == 0) {
            world.addParticle(ParticleTypes.LAVA, x, y + 0.2, z, 0.0, 0.0, 0.0);
        }

        // 3. COOKING ASH (Black Particles)
        // Check if an item is currently in the inventory
        if (world.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
            if (!campfire.getInventory().isEmpty()) {
                double ashX = pos.getX() + 0.5;
                double ashY = pos.getY() + 0.75; // Your requested position
                double ashZ = pos.getZ() + 0.5;

                // Spawn black ash/smoke from the cooking item
                // LARGE_SMOKE or SMOKE look best for black ash
                if (random.nextInt(3) != 0) {
                    world.addParticle(ParticleTypes.SMOKE,
                            ashX + (random.nextDouble() * 0.1 - 0.05),
                            ashY + (random.nextDouble() * 0.1 - 0.05),
                            ashZ + (random.nextDouble() * 0.1 - 0.05),
                            0.0, 0.02, 0.0);
                }
            }
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // Damage must happen on the server side
        if (world instanceof ServerWorld serverWorld && state.get(LIT) && entity instanceof LivingEntity) {

            float damageAmount = switch (state.get(STAGE)) {
                case 1 -> 0.7f;
                case 2 -> 1.0f;
                case 3 -> 1.3f;
                default -> 1.0f;
            };

            // Argument 1: The ServerWorld
            // Argument 2: The DamageSource
            // Argument 3: The Amount
            entity.damage(serverWorld, world.getDamageSources().campfire(), damageAmount);

            if (!entity.isFireImmune()) {
                entity.setOnFireFor(5);
            }
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CampfireBlockEntity campfire) {
                // 1. Drop the cooking item if it exists
                if (!campfire.getInventory().isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, campfire.getInventory().copy());
                    world.spawnEntity(itemEntity);
                }

                // 2. Drop the Pointy Stick if the state had one
                if (state.get(STICK)) {
                    ItemEntity stickEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.POINTY_STICK));
                    world.spawnEntity(stickEntity);
                }

                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}