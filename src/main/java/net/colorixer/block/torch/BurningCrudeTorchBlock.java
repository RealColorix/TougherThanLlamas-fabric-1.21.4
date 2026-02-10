package net.colorixer.block.torch;

import net.colorixer.block.ModBlocks;
import net.colorixer.component.ModDataComponentTypes;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
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
import org.jetbrains.annotations.Nullable;
import net.colorixer.block.ModBlockEntities;

public class BurningCrudeTorchBlock extends Block implements BlockEntityProvider {
    public static final BooleanProperty LOW_FUEL = BooleanProperty.of("low_fuel");
    public static final EnumProperty<Direction> FACING = Properties.FACING;

    public BurningCrudeTorchBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(LOW_FUEL, false)
                .with(FACING, Direction.UP));
    }

    // --- PLACEMENT LOGIC ---

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);

        // If holding a Crude Torch, light it FROM this block
        if (stackInHand.isOf(ModBlocks.CRUDE_TORCH.asItem())) {
            if (world.isClient) return ActionResult.SUCCESS;

            // Use the logic you wanted: Replace if count is 1, otherwise decrement and add to inv
            ItemStack burningTorch = new ItemStack(ModBlocks.BURNING_CRUDE_TORCH);

            if (stackInHand.getCount() == 1) {
                player.setStackInHand(Hand.MAIN_HAND, burningTorch);
            } else {
                stackInHand.decrement(1);
                if (!player.getInventory().insertStack(burningTorch)) {
                    player.dropItem(burningTorch, false);
                }
            }

            world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 1.2f);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    // Helper method (Copy this into this class too)
    private void handleTorchIgnition(PlayerEntity player, ItemStack stackInHand) {
        ItemStack burningTorch = new ItemStack(ModBlocks.BURNING_CRUDE_TORCH);
        if (stackInHand.getCount() == 1) {
            player.setStackInHand(Hand.MAIN_HAND, burningTorch);
        } else {
            stackInHand.decrement(1);
            if (!player.getInventory().insertStack(burningTorch)) {
                player.dropItem(burningTorch, false);
            }
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity be) {
            // DIRECT TRANSFER: Just get the ticks from the item and push to block
            // Default to 24000 if it's a brand new torch
            int totalRemaining = itemStack.getOrDefault(ModDataComponentTypes.FUEL_TIME, 24000L).intValue();
            be.setBurnTime(totalRemaining);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BurningCrudeTorchBlockEntity torchBe && torchBe.getBurnTime() > 0) {
                ItemStack stack = new ItemStack(this);
                int totalTicks = torchBe.getBurnTime();

                // 1. Save exact ticks to the component
                stack.set(ModDataComponentTypes.FUEL_TIME, (long)totalTicks);

                // 2. Set the visual durability bar
                // (Max - Remaining) = Damage
                stack.setDamage(24000 - totalTicks);

                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
            // IMPORTANT: Clear the block entity so the super call doesn't find it
            world.removeBlockEntity(pos);
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    // --- BOILERPLATE & VISUALS ---

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        Direction direction = state.get(FACING);
        double x = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.1;
        double y = (double)pos.getY() + 0.7 + (random.nextDouble() - 0.5) * 0.1;
        double z = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.1;
        double offset = 0.27;

        if (direction.getAxis().isHorizontal()) {
            Direction opposite = direction.getOpposite();
            x += offset * (double)opposite.getOffsetX();
            y += 0.22;
            z += offset * (double)opposite.getOffsetZ();
        }

        if (!state.get(LOW_FUEL) || random.nextFloat() > 0.3f) {
            world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
            world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.05, 0.0);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LOW_FUEL, FACING);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BurningCrudeTorchBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) return null;
        return type == ModBlockEntities.BURNING_CRUDE_TORCH ? (w, p, s, be) -> BurningCrudeTorchBlockEntity.tick(w, p, s, (BurningCrudeTorchBlockEntity)be) : null;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (ctx.getSide() == Direction.UP) {
            BlockState floorState = this.getDefaultState().with(FACING, Direction.UP);
            if (floorState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) return floorState;
        }
        for (Direction direction : ctx.getPlacementDirections()) {
            if (direction.getAxis().isHorizontal()) {
                BlockState wallState = this.getDefaultState().with(FACING, direction.getOpposite());
                if (wallState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) return wallState;
            }
        }
        return null;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos anchorPos = (direction == Direction.UP) ? pos.down() : pos.offset(direction.getOpposite());
        return world.getBlockState(anchorPos).isSideSolidFullSquare(world, anchorPos, direction == Direction.UP ? Direction.UP : direction);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> STANDING_SHAPE;
        };
    }

    protected static final VoxelShape STANDING_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);
    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5.5, 3.0, 11.0, 10.5, 13.0, 16.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5.5, 3.0, 0.0, 10.5, 13.0, 5.0);
    protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(11.0, 3.0, 5.5, 16.0, 13.0, 10.5);
    protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 3.0, 5.5, 5.0, 13.0, 10.5);
}