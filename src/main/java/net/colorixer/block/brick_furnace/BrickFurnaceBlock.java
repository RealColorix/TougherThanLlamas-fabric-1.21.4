package net.colorixer.block.brick_furnace;

import com.mojang.serialization.MapCodec;
import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.colorixer.item.ModItems;
import net.colorixer.util.IdentifierUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class BrickFurnaceBlock extends BlockWithEntity implements Waterloggable {
    public static final BooleanProperty LIT = Properties.LIT;
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty USED = BooleanProperty.of("used");
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final TagKey<Item> SHOVEL_TAG = TagKey.of(RegistryKeys.ITEM, IdentifierUtil.createIdentifier("ttll", "shovels"));



    public BrickFurnaceBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(LIT, false)
                .with(USED, false)
                .with(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView worldView, ScheduledTickView tickView,
                                                   BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState,
                                                   Random random) {
        // Preserve water logics
        if (state.get(WATERLOGGED) && worldView instanceof World world) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
            if (state.get(LIT)) {
                world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
                        SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            BlockState newState = state.with(LIT, false).with(USED, false);
            world.setBlockState(pos, newState, Block.NOTIFY_ALL);
            state = newState;
        }
        // Check support: if the neighbor update is from below and the block below is not full, break the furnace.
        if (direction == Direction.DOWN && !this.canPlaceAt(state, worldView, pos)) {
            if (worldView instanceof World world) {
                ItemEntity itemEntity = new ItemEntity(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        new ItemStack(ModBlocks.DRIED_BRICK, 0));
                world.spawnEntity(itemEntity);

                return net.minecraft.block.Blocks.AIR.getDefaultState();
            }
        }
        return super.getStateForNeighborUpdate(state, worldView, tickView, pos, direction, neighborPos, neighborState, random);
    }


    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BrickFurnaceBlockEntity(pos, state);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.BRICK_FURNACE_BLOCK_ENTITY) {
            return null;
        }
        return (BlockEntityTicker<T>) (world1, pos, state1, blockEntity) ->
                BrickFurnaceBlockEntity.tick(world1, pos, state1, (BrickFurnaceBlockEntity) blockEntity);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, USED, WATERLOGGED); // ✅ include WATERLOGGED
    }


    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        BlockPos below = pos.down();
        BlockState belowState = ctx.getWorld().getBlockState(below);
        if (!belowState.isSideSolidFullSquare(ctx.getWorld(), below, Direction.UP)) {
            return null;  // Prevent placement if the block beneath is not a full block.
        }
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(WATERLOGGED, ctx.getWorld().getFluidState(pos).getFluid() == Fluids.WATER);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        // Only allow placement if the block below is a full solid block.
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        return belowState.isSideSolidFullSquare(world, below, Direction.UP);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST  -> SHAPE_EAST;
            case WEST  -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    private boolean isFlexibleIngredient(Item item) {
        for (BrickFurnaceBlockEntity.CookingRecipe recipe : BrickFurnaceBlockEntity.COOKING_RECIPES) {
            if ((recipe.ingredient.getCustomIngredient() == item) || (recipe.result.getItem() == item)) {
                return true;
            }
        }
        return false;
    }



    private boolean isCookingRecipeOutput(ItemStack stack) {
        for (BrickFurnaceBlockEntity.CookingRecipe recipe : BrickFurnaceBlockEntity.COOKING_RECIPES) {
            if (ItemStack.areEqual(recipe.result, stack)) {
                return true;
            }
        }
        return false;
    }



    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack stack = player.getMainHandStack();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof BrickFurnaceBlockEntity furnace)) {
            return ActionResult.PASS;
        }


        if (stack.isOf(Items.FLINT_AND_STEEL) && furnace.getBurnTime() > 0 &&
                !state.get(BrickFurnaceBlock.LIT) && !state.get(BrickFurnaceBlock.WATERLOGGED)) {

            if (world.getRandom().nextFloat() < 0.25f) {
                world.setBlockState(pos, state.with(BrickFurnaceBlock.LIT, true));
                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 1.2f);

            }
            world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.5f, 1.2f);

            stack.damage(1, player, EquipmentSlot.MAINHAND);

            // Check if the tool's damage is equal to or exceeds its maximum,
            // indicating that it should break.
            if (stack.getDamage() >= stack.getMaxDamage()) {
                // Remove the tool from the player's main hand.
                player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                // Play the break sound.
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }

            player.swingHand(Hand.MAIN_HAND);
            return ActionResult.SUCCESS;
        }

        if (stack.isIn(SHOVEL_TAG) && furnace.isBurning()) {
            world.setBlockState(pos, state.with(BrickFurnaceBlock.LIT, false).with(BrickFurnaceBlock.USED, true));
            player.swingHand(Hand.MAIN_HAND);
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1f, 1f);
            return ActionResult.SUCCESS;
        }
        if (stack.isEmpty()) {
            DefaultedList<ItemStack> inventory = furnace.getInventory();

            // FIRST: Check each ingredient slot (slots 4 to 1) for a cooked item from a CookingRecipe.
            for (int i = inventory.size() - 1; i >= 1; i--) {
                ItemStack current = inventory.get(i);
                if (!current.isEmpty() && isCookingRecipeOutput(current)) {
                    ItemStack removed = current.copy();
                    inventory.set(i, ItemStack.EMPTY);
                    if (!player.getInventory().insertStack(removed)) {
                        player.dropItem(removed, false);
                    }
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                    return ActionResult.SUCCESS;
                }
            }

            // SECOND: If no cooked ingredient is found, fall back to the original behavior:
            for (int i = inventory.size() - 1; i >= 1; i--) {
                if (!inventory.get(i).isEmpty()) {
                    ItemStack removed = inventory.get(i).copy();
                    inventory.set(i, ItemStack.EMPTY);
                    if (!player.getInventory().insertStack(removed)) {
                        player.dropItem(removed, false);
                    }
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                    return ActionResult.SUCCESS;
                }
            }

            // Then try to pick up the cast item if no ingredient was picked up.
            if (!furnace.getCastItem().isEmpty()) {
                ItemStack removed = furnace.getCastItem().copy();
                furnace.setCastItem(ItemStack.EMPTY);
                if (!player.getInventory().insertStack(removed)) {
                    player.dropItem(removed, false);
                }
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                return ActionResult.SUCCESS;
            }

            // Finally, if the furnace is not lit and everything else has been picked up, allow taking remaining fuel.
            if (!state.get(LIT)) {
                ItemStack fuelStack = inventory.getFirst();
                if (!fuelStack.isEmpty()) {
                    ItemStack removed = fuelStack.copy();
                    int burnTime = furnace.getBurnTime(); // Store burn time before clearing
                    inventory.set(0, ItemStack.EMPTY);
                    if (!player.getInventory().insertStack(removed)) {
                        player.dropItem(removed, false);
                    }
                    // Deduct the burn time.
                    furnace.setBurnTime(0);
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1f, 0.8f);
                    return ActionResult.SUCCESS;
                }
            }
        }


        if (furnace.getCastItem().isEmpty() && BrickFurnaceBlockEntity.isValidCast(stack.getItem())) {

            furnace.setCastItem(new ItemStack(stack.getItem()));
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
            return ActionResult.SUCCESS;
        }
        if (!furnace.getCastItem().isEmpty() &&
                BrickFurnaceBlockEntity.isValidIngredient(stack, furnace.getCastItem())) {

            DefaultedList<ItemStack> inventory = furnace.getInventory();
            // If slot 1 already has an ingredient, enforce matching only if it is not a flexible ingredient.
            if (!inventory.get(1).isEmpty() &&
                    !isFlexibleIngredient(inventory.get(1).getItem()) &&
                    stack.getItem() != inventory.get(1).getItem()) {
                return ActionResult.PASS;
            }

            for (int i = 1; i < inventory.size(); i++) {
                if (inventory.get(i).isEmpty()) {
                    inventory.set(i, new ItemStack(stack.getItem()));
                    if (!player.isCreative()) {
                        stack.decrement(1);
                    }
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                    return ActionResult.SUCCESS;
                }
            }
        }
        if (isValidFuel(stack.getItem())) {
            player.swingHand(Hand.MAIN_HAND);
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.5f, 1.2f);
            if (furnace.addFuel(stack, player)) {
                return ActionResult.CONSUME;
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }


    private boolean isValidFuel(Item item) {
        return item == Items.COAL || item == Items.CHARCOAL;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            BrickFurnaceBlockEntity entity = (BrickFurnaceBlockEntity) world.getBlockEntity(pos);
            if (entity != null) {
                entity.dropAllContents();
            }
        }
        super.onBreak(world, pos, state, player);
        return state;
    }



    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1;
            double z = pos.getZ() + 0.5;

            // Adjust based on furnace facing
            Direction facing = state.get(HorizontalFacingBlock.FACING);
            switch (facing) {
                case NORTH:
                    z = pos.getZ();
                    break;
                case SOUTH:
                    z = pos.getZ() + 1.0;
                    break;
                case WEST:
                    x = pos.getX();
                    break;
                case EAST:
                    x = pos.getX() + 1.0;
                    break;
            }

            // Always spawn one LARGE_SMOKE particle for a general effect
            double offsetXSmoke = random.nextDouble() * 0.6 - 0.3;
            double offsetZSmoke = random.nextDouble() * 0.6 - 0.3;
            world.addParticle(ParticleTypes.LARGE_SMOKE, x + offsetXSmoke, y, z + offsetZSmoke, 0.0, 0.0, 0.0);

            // Every 5 ticks (instead of 20), check if there's a working recipe inside
            if (world.getTime() % 1 == 0) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof BrickFurnaceBlockEntity furnaceEntity) {
                    boolean hasWorkingRecipe = false;
                    for (BrickFurnaceBlockEntity.HardcodedRecipe recipe : BrickFurnaceBlockEntity.HARDCODED_RECIPES) {
                        if (recipe.matches(furnaceEntity.getInventory())) {
                            hasWorkingRecipe = true;
                            break;
                        }
                    }
                    if (hasWorkingRecipe) {
                        double baseX = pos.getX() + 0.1;
                        double baseY = pos.getY() + 0.6725;  // Y offset inside the furnace
                        double baseZ = pos.getZ() + 0.1;
                        double offsetX = random.nextDouble()*0.8 ;
                        double offsetZ = random.nextDouble()*0.8 ;
                        world.addParticle(ParticleTypes.FLAME, baseX + offsetX, baseY, baseZ + offsetZ, 0, 0.001, 0);
                    }
                }
            }
        }
    }





    // === NORTH (original)
    public static final VoxelShape SHAPE_NORTH = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, 2, 16),
            Block.createCuboidShape(0, 2, 15, 16, 16, 16),
            Block.createCuboidShape(0, 2, 0, 1, 16, 15),
            Block.createCuboidShape(15, 2, 0, 16, 16, 15),
            Block.createCuboidShape(2, 6, 0, 14, 8, 2),
            Block.createCuboidShape(1, 2, 0, 2, 8, 15),
            Block.createCuboidShape(14, 2, 0, 15, 8, 15),
            Block.createCuboidShape(2, 2, 0, 14, 2, 14),
            Block.createCuboidShape(8, 2, 14, 14, 8, 15),
            Block.createCuboidShape(2, 2, 14, 8, 8, 15),
            Block.createCuboidShape(1, 14, 0, 15, 16, 2),
            Block.createCuboidShape(1, 15, 2, 15, 16, 15),
            Block.createCuboidShape(1, 8, 0, 2, 14, 1),
            Block.createCuboidShape(14, 8, 0, 15, 14, 1),
            Block.createCuboidShape(2, 13, 0, 3, 14, 1),
            Block.createCuboidShape(13, 13, 0, 14, 14, 1),
            Block.createCuboidShape(13, 5, 0, 14, 6, 1),
            Block.createCuboidShape(2, 5, 0, 3, 6, 1)
    );

    // === SOUTH (manually rotated 180°)
    public static final VoxelShape SHAPE_SOUTH = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, 2, 16),
            Block.createCuboidShape(0, 2, 0, 16, 16, 1),
            Block.createCuboidShape(15, 2, 1, 16, 16, 16),
            Block.createCuboidShape(0, 2, 1, 1, 16, 16),
            Block.createCuboidShape(2, 6, 14, 14, 8, 16),
            Block.createCuboidShape(14, 2, 1, 15, 8, 16),
            Block.createCuboidShape(1, 2, 1, 2, 8, 16),
            Block.createCuboidShape(2, 2, 2, 14, 2, 16),
            Block.createCuboidShape(2, 2, 1, 8, 8, 2),
            Block.createCuboidShape(8, 2, 1, 14, 8, 2),
            Block.createCuboidShape(1, 14, 14, 15, 16, 16),
            Block.createCuboidShape(1, 15, 1, 15, 16, 14),
            Block.createCuboidShape(14, 8, 15, 15, 14, 16),
            Block.createCuboidShape(1, 8, 15, 2, 14, 16),
            Block.createCuboidShape(13, 13, 15, 14, 14, 16),
            Block.createCuboidShape(2, 13, 15, 3, 14, 16),
            Block.createCuboidShape(2, 5, 15, 3, 6, 16),
            Block.createCuboidShape(13, 5, 15, 14, 6, 16)
    );

    // === EAST (manually rotated 90° CW)
    public static final VoxelShape SHAPE_EAST = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, 2, 16),
            Block.createCuboidShape(0, 2, 0, 1, 16, 16),
            Block.createCuboidShape(1, 2, 0, 16, 16, 1),
            Block.createCuboidShape(1, 2, 15, 16, 16, 16),
            Block.createCuboidShape(14, 6, 2, 16, 8, 14),
            Block.createCuboidShape(1, 2, 14, 16, 8, 15),
            Block.createCuboidShape(1, 2, 1, 16, 8, 2),
            Block.createCuboidShape(2, 2, 2, 16, 2, 14),
            Block.createCuboidShape(1, 2, 2, 2, 8, 8),
            Block.createCuboidShape(1, 2, 8, 2, 8, 14),
            Block.createCuboidShape(14, 14, 1, 16, 16, 15),
            Block.createCuboidShape(1, 15, 1, 14, 16, 15),
            Block.createCuboidShape(15, 8, 14, 16, 14, 15),
            Block.createCuboidShape(15, 8, 1, 16, 14, 2),
            Block.createCuboidShape(15, 13, 13, 16, 14, 14),
            Block.createCuboidShape(15, 13, 2, 16, 14, 3),
            Block.createCuboidShape(15, 5, 13, 16, 6, 14),
            Block.createCuboidShape(15, 5, 2, 16, 6, 3)
    );

    // === WEST (manually rotated 270° CW)
    public static final VoxelShape SHAPE_WEST = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, 2, 16),
            Block.createCuboidShape(15, 2, 0, 16, 16, 16),
            Block.createCuboidShape(0, 2, 15, 15, 16, 16),
            Block.createCuboidShape(0, 2, 0, 15, 16, 1),
            Block.createCuboidShape(0, 6, 2, 2, 8, 14),
            Block.createCuboidShape(0, 2, 1, 15, 8, 2),
            Block.createCuboidShape(0, 2, 14, 15, 8, 15),
            Block.createCuboidShape(0, 2, 2, 14, 2, 14),
            Block.createCuboidShape(14, 2, 8, 15, 8, 14),
            Block.createCuboidShape(14, 2, 2, 15, 8, 8),
            Block.createCuboidShape(0, 14, 1, 2, 16, 15),
            Block.createCuboidShape(2, 15, 1, 15, 16, 15),
            Block.createCuboidShape(0, 8, 1, 1, 14, 2),
            Block.createCuboidShape(0, 8, 14, 1, 14, 15),
            Block.createCuboidShape(0, 13, 2, 1, 14, 3),
            Block.createCuboidShape(0, 13, 13, 1, 14, 14),
            Block.createCuboidShape(0, 5, 2, 1, 6, 3),
            Block.createCuboidShape(0, 5, 13, 1, 6, 14)
    );
}
