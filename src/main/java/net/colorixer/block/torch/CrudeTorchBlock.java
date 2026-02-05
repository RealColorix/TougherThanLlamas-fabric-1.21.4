package net.colorixer.block.torch;

import net.colorixer.block.ModBlocks;
import net.colorixer.item.items.FireStarterItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class CrudeTorchBlock extends Block {
    public static final BooleanProperty BURNED = BooleanProperty.of("burned");
    public static final EnumProperty<Direction> FACING = Properties.FACING;

    protected static final VoxelShape STANDING_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);
    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5.5, 3.0, 11.0, 10.5, 13.0, 16.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5.5, 3.0, 0.0, 10.5, 13.0, 5.0);
    protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(11.0, 3.0, 5.5, 16.0, 13.0, 10.5);
    protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 3.0, 5.5, 5.0, 13.0, 10.5);

    public CrudeTorchBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(BURNED, false)
                .with(FACING, Direction.UP));
    }

    // --- DROP LOGIC ---

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // We handle the drop here manually when a player breaks it.
        // This won't trigger during world.setBlockState calls.
        if (!world.isClient && !player.isCreative() && !state.get(BURNED)) {
            ItemStack stack = new ItemStack(this);
            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
        }
        return super.onBreak(world, pos, state, player);
    }

    // REMOVED onStateReplaced entirely to stop the transformation drop bug.
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
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);

        // --- 1. BURNING TORCH ITEM LOGIC (100% SUCCESS) ---
        // If you right-click a Crude Torch block with a Burning Torch item
        if (stackInHand.isOf(ModBlocks.BURNING_CRUDE_TORCH.asItem())) {
            if (world.isClient) return ActionResult.SUCCESS;

            Direction currentFacing = state.get(FACING);
            world.setBlockState(pos, ModBlocks.BURNING_CRUDE_TORCH.getDefaultState()
                    .with(Properties.FACING, currentFacing), 3);

            world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 1.2f);
            return ActionResult.SUCCESS;
        }

        // --- 2. FLINT AND STEEL LOGIC (1/5 SUCCESS) ---
        if (stackInHand.isOf(Items.FLINT_AND_STEEL)) {
            player.getItemCooldownManager().set(stackInHand, 10);
            if (world.isClient) return ActionResult.SUCCESS;

            if (world.random.nextInt(5) == 0) {
                Direction currentFacing = state.get(FACING);
                world.setBlockState(pos, ModBlocks.BURNING_CRUDE_TORCH.getDefaultState()
                        .with(Properties.FACING, currentFacing), 3);

                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.8f, 1.0f);
            } else {
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.5f, 1.5f);
            }

            if (!player.getAbilities().creativeMode) {
                stackInHand.damage(1, player, EquipmentSlot.MAINHAND);
            }
            return ActionResult.SUCCESS;
        }

        // --- 3. PASS FOR FIRE STARTER ITEM ---
        if (stackInHand.getItem() instanceof net.colorixer.item.items.FireStarterItem) {
            return ActionResult.PASS;
        }

        if (!stackInHand.isEmpty()) {
            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    // --- PROPERTIES & PLACEMENT ---

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(BURNED, FACING);
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
}