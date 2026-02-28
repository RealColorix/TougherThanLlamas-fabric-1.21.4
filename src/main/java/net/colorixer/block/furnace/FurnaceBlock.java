package net.colorixer.block.furnace;

import com.mojang.serialization.MapCodec;
import net.colorixer.block.ModBlocks;
import net.colorixer.item.ModItems;
import net.colorixer.item.items.firestarteritem.FireStarterItem;
import net.colorixer.item.items.firestarteritem.FireStarterItemSmoke;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Map;

public class FurnaceBlock extends FallingBlock implements BlockEntityProvider {

    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty LIT = Properties.LIT;
    public static final IntProperty FUEL_LEVEL = IntProperty.of("fuel_level", 0, 8);

    public FurnaceBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(LIT, false)
                .with(FUEL_LEVEL, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, FUEL_LEVEL);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState()
                .with(FACING, context.getHorizontalPlayerFacing().getOpposite())
                .with(LIT, false)
                .with(FUEL_LEVEL, 0);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (!state.get(LIT)) return;

        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.0;
        double z = (double)pos.getZ() + 0.5;

        // Sound and particles now scale based on FUEL_LEVEL
        int level = state.get(FUEL_LEVEL);

        // Example: Cracks more often when fuel is high (level 6 = 12% chance, level 1 = 2% chance)
        float soundChance = 0.02f + (level * 0.015f);
        if (random.nextFloat() < soundChance) {
            world.playSound(x, y, z, net.minecraft.sound.SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f, true);
        }

        Direction direction = state.get(FACING);
        Direction.Axis axis = direction.getAxis();
        double offset = random.nextDouble() * 0.6 - 0.3;

        double dx = axis == Direction.Axis.X ? (double)direction.getOffsetX() * 0.52 : offset;
        double dy = random.nextDouble() * 6.0 / 16.0;
        double dz = axis == Direction.Axis.Z ? (double)direction.getOffsetZ() * 0.52 : offset;

        // Reduce particles if fuel is very low (Level 0 or 1)
        if (level <= 1 && random.nextFloat() < 0.6f) return;

        world.addParticle(net.minecraft.particle.ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
        world.addParticle(net.minecraft.particle.ParticleTypes.FLAME, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
    }



    @Override
    protected MapCodec<? extends FallingBlock> getCodec() {
        return null;
    }



    /* ==================== INTERACTION ==================== */

    @Override
    public boolean canPlaceAt(BlockState state, net.minecraft.world.WorldView world, BlockPos pos) {
        BlockPos downwardPos = pos.down();
        BlockState downwardState = world.getBlockState(downwardPos);
        // Only allow placement if the block below has a solid top surface (Full Block)
        return downwardState.isSideSolidFullSquare(world, downwardPos, Direction.UP);
    }

    @Override
    public void onLanding(World world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos, net.minecraft.entity.FallingBlockEntity fallingBlockEntity) {
        if (!world.isClient) {
            // 1. Play the break sound
            world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_STONE_BREAK, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 0.8f);


            int count = world.random.nextBetween(6, 16);

            // 3. Spawn each rock individually with random velocity
            for (int i = 0; i < count; i++) {
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.3;
                double z = pos.getZ() + 0.5;

                net.minecraft.entity.ItemEntity rockEntity = new net.minecraft.entity.ItemEntity(world, x, y, z,
                        new ItemStack(ModBlocks.DRIED_BRICK, 1));

                // Apply random "scatter" velocity
                // Horizontal spread (-0.1 to 0.1) and a slight upward pop (0.2)
                rockEntity.setVelocity(
                        (world.random.nextDouble() - 0.5) * 0.2,
                        0.2,
                        (world.random.nextDouble() - 0.5) * 0.2
                );

                world.spawnEntity(rockEntity);
            }

            // 4. Remove the block (it shattered)
            world.removeBlock(pos, false);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        if (world.getBlockEntity(pos) instanceof FurnaceBlockEntity furnace) {
            ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);
            Direction facing = state.get(FACING);
            Direction hitSide = hit.getSide();

            if (hitSide == facing.getOpposite()) return ActionResult.PASS;

            // --- ZONE CALCULATION ---
            double hitX = hit.getPos().x - pos.getX();
            double hitY = hit.getPos().y - pos.getY();
            double hitZ = hit.getPos().z - pos.getZ();

            boolean withinHoleWidth = (facing.getAxis() == Direction.Axis.Z) ? (hitX > 0.0625 && hitX < 0.9375) : (hitZ > 0.0625 && hitZ < 0.9375);
            boolean lookingInHole = withinHoleWidth && hitY > 0 && hitY < 0.9375;
            boolean inBottomHole = lookingInHole && hitY <= 0.3125;

            if (inBottomHole && furnace.getFuel() > 0 && !state.get(Properties.LIT) && player.getStackInHand(player.getActiveHand()).getItem() instanceof FireStarterItem){
                Hand hand = player.getActiveHand();
                ItemStack stack = player.getStackInHand(hand);
                if (!stack.isEmpty()) {
                    FireStarterItemSmoke.spawnFrictionEffects(
                            world,
                            hit.getPos(),
                            hit.getSide(),
                            player,
                            stack // Pass the stack here
                    );
                }
            }
            // --- 1. LIGHTING CRUDE TORCH FROM LIT FURNACE (NEW) ---
            if (inBottomHole  && state.get(LIT) && stackInHand.isOf(net.colorixer.block.ModBlocks.CRUDE_TORCH.asItem())) {
                ItemStack burningTorch = new ItemStack(net.colorixer.block.ModBlocks.BURNING_CRUDE_TORCH);

                if (stackInHand.getCount() == 1) {
                    player.setStackInHand(Hand.MAIN_HAND, burningTorch);
                } else {
                    stackInHand.decrement(1);
                    if (!player.getInventory().insertStack(burningTorch)) {
                        player.dropItem(burningTorch, false);
                    }
                }

                world.playSound(null, pos, net.minecraft.sound.SoundEvents.ITEM_FIRECHARGE_USE, net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.2f);
                return ActionResult.SUCCESS;
            }

            // --- 2. LIGHT CHECK (Igniting the Furnace) ---
            if (inBottomHole && furnace.getFuel() > 0 && !state.get(LIT)) {

                // A. BURNING CRUDE TORCH (100% Success)
                if (stackInHand.isOf(net.colorixer.block.ModBlocks.BURNING_CRUDE_TORCH.asItem())) {
                    furnace.ignite();
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.ITEM_FIRECHARGE_USE, net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.2f);
                    return ActionResult.SUCCESS;
                }

                // B. FLINT AND STEEL (1/5 Success + 10 Tick Cooldown)
                if (stackInHand.isOf(net.minecraft.item.Items.FLINT_AND_STEEL)) {
                    player.getItemCooldownManager().set(stackInHand, 10);

                    if (world.random.nextInt(5) == 0) {
                        furnace.ignite();
                        world.playSound(null, pos, net.minecraft.sound.SoundEvents.ITEM_FIRECHARGE_USE, net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.0f);
                    } else {
                        world.playSound(null, pos, net.minecraft.sound.SoundEvents.ITEM_FLINTANDSTEEL_USE, net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.5f);
                    }

                    if (!player.getAbilities().creativeMode) {
                        stackInHand.damage(1, player, net.minecraft.entity.EquipmentSlot.MAINHAND);
                    }
                    return ActionResult.SUCCESS;
                }

                // C. FIRE STARTER (Let the item handle its own chance/logic)
                if (stackInHand.getItem() instanceof FireStarterItem) {
                    return ActionResult.PASS;
                }
            }

            // --- 3. FUEL LOGIC (Logs) ---
            final Map<Item, Integer> fuelItems = Map.ofEntries(
                    Map.entry(Items.COAL, 16000),
                    Map.entry(Items.CHARCOAL, 16000),
                    Map.entry(Items.OAK_LOG, 13000),
                    Map.entry(Items.SPRUCE_LOG, 13000),
                    Map.entry(Items.BIRCH_LOG, 14000),
                    Map.entry(Items.JUNGLE_LOG, 11000),
                    Map.entry(Items.ACACIA_LOG, 13000),
                    Map.entry(Items.DARK_OAK_LOG, 11000),
                    Map.entry(Items.MANGROVE_LOG, 13000),
                    Map.entry(Items.CHERRY_LOG, 13000),
                    Map.entry(Items.PALE_OAK_LOG, 11000),
                    Map.entry(Items.STRIPPED_OAK_LOG, 13000),
                    Map.entry(Items.STRIPPED_SPRUCE_LOG, 13000),
                    Map.entry(Items.STRIPPED_BIRCH_LOG, 14000),
                    Map.entry(Items.STRIPPED_JUNGLE_LOG, 11000),
                    Map.entry(Items.STRIPPED_ACACIA_LOG, 13000),
                    Map.entry(Items.STRIPPED_DARK_OAK_LOG, 11000),
                    Map.entry(Items.STRIPPED_MANGROVE_LOG, 13000),
                    Map.entry(Items.STRIPPED_CHERRY_LOG, 13000),
                    Map.entry(Items.STRIPPED_PALE_OAK_LOG, 11000),

                    Map.entry(Items.STICK, 200),
                    Map.entry(ModItems.POINTY_STICK, 200),
                    Map.entry(ModItems.BRANCH, 200),
                    Map.entry(ModItems.OAK_BARK, 200),
                    Map.entry(ModItems.BIRCH_BARK, 200),
                    Map.entry(ModItems.SPRUCE_BARK, 200),
                    Map.entry(ModItems.JUNGLE_BARK, 200)


            );

            if (inBottomHole) {
                Item itemInHand = stackInHand.getItem();

                if (fuelItems.containsKey(itemInHand)) {
                    int fuelValue = fuelItems.get(itemInHand);

                    furnace.addFuel(fuelValue);

                    if (!player.getAbilities().creativeMode) {
                            stackInHand.decrement(1);}

                    if (!state.get(LIT)) {
                        world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_WOOD_PLACE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }else {
                        world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                    return ActionResult.SUCCESS;
                }
            }


            // --- 4. INVENTORY ZONE (Top half of hole) ---
            if (lookingInHole && hitY >= 0.375) {
                boolean hadItem = !furnace.getInventory().isEmpty();
                if (furnace.onRightClick(player, Hand.MAIN_HAND)) {
                    if (hadItem) {
                        world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_ITEM_PICKUP, net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.5f);
                    } else {
                        world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, net.minecraft.sound.SoundCategory.BLOCKS, 0.8f, 1.2f);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.CONSUME;
    }




    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FurnaceBlockEntity furnace) {
                if (!furnace.getInventory().isEmpty()) {
                    // Use the double coordinates to avoid the BlockPos resolution error
                    ItemScatterer.spawn(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), furnace.getInventory());
                }
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
    /* ==================== BLOCK ENTITY ==================== */

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FurnaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        // Tick only on the server
        return world.isClient ? null : (w, p, s, be) -> {
            if (be instanceof FurnaceBlockEntity furnace) {
                furnace.tick();
            }
        };
    }

    /* ==================== SHAPES (Unchanged) ==================== */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case SOUTH -> SHAPE_S;
            case EAST  -> SHAPE_E;
            case WEST  -> SHAPE_W;
            default    -> SHAPE_N;
        };
    }

    private static VoxelShape createFurnaceShape(Direction facing) {
        VoxelShape base = Block.createCuboidShape(0, 0, 0, 16, 6, 16);
        VoxelShape top  = Block.createCuboidShape(0, 12, 0, 16, 16, 16);

        VoxelShape walls = switch (facing) {
            case NORTH -> VoxelShapes.union(
                    Block.createCuboidShape(0, 6, 0, 4, 12, 16),
                    Block.createCuboidShape(12, 6, 0, 16, 12, 16),
                    Block.createCuboidShape(4, 6, 12, 12, 12, 16)
            );
            case SOUTH -> VoxelShapes.union(
                    Block.createCuboidShape(0, 6, 0, 4, 12, 16),
                    Block.createCuboidShape(12, 6, 0, 16, 12, 16),
                    Block.createCuboidShape(4, 6, 0, 12, 12, 4)
            );
            case WEST -> VoxelShapes.union(
                    Block.createCuboidShape(0, 6, 0, 16, 12, 4),
                    Block.createCuboidShape(0, 6, 12, 16, 12, 16),
                    Block.createCuboidShape(12, 6, 4, 16, 12, 12)
            );
            case EAST -> VoxelShapes.union(
                    Block.createCuboidShape(0, 6, 0, 16, 12, 4),
                    Block.createCuboidShape(0, 6, 12, 16, 12, 16),
                    Block.createCuboidShape(0, 6, 4, 4, 12, 12)
            );
            default -> VoxelShapes.fullCube();
        };

        return VoxelShapes.union(base, top, walls);
    }

    private static final VoxelShape SHAPE_N = createFurnaceShape(Direction.NORTH);
    private static final VoxelShape SHAPE_S = createFurnaceShape(Direction.SOUTH);
    private static final VoxelShape SHAPE_E = createFurnaceShape(Direction.EAST);
    private static final VoxelShape SHAPE_W = createFurnaceShape(Direction.WEST);
}