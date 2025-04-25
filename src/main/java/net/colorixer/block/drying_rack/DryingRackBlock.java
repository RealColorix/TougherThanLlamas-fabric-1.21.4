package net.colorixer.block.drying_rack;

import com.mojang.serialization.MapCodec;
import net.colorixer.item.ModItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;


public class DryingRackBlock extends BlockWithEntity {

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;


    private static final VoxelShape SHAPE_N = Block.createCuboidShape(1, 0, 4, 15, 15, 12);
    private static final VoxelShape SHAPE_S = SHAPE_N;
    private static final VoxelShape SHAPE_E = Block.createCuboidShape(4, 0, 1, 12, 15, 15);
    private static final VoxelShape SHAPE_W = SHAPE_E;

    public DryingRackBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    /* -------------------------------------------------------------- */
    @Override protected MapCodec<? extends BlockWithEntity> getCodec() { return null; }

    @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /* ------------------------------ tick -------------------------- */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T>
    getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (w, p, s, be) -> { if (be instanceof DryingRackBlockEntity r) r.tick(); };
    }

    /* ---------------------- placement & shape --------------------- */
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
    /* 1 ────────── no collision box → walk‑through ────────── */
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world,
                                        BlockPos pos, ShapeContext ctx) {
        return VoxelShapes.empty();                 // entities pass through
    }

    /* 2 ────────── break & drop when touched ────────── */
    /* …imports unchanged… */

    @Override
    public void onEntityCollision(BlockState state, World world,
                                  BlockPos pos, Entity entity) {

        /* ignore everything that isn’t a mob / player */
        if (world.isClient || !(entity instanceof LivingEntity)) return;

        /* 1 ─ sound */
        world.playSound(null, pos,
                SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS,
                1.0F, 1.0F);

        /* 2 ─ drop rack inventory (two slots) */
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof DryingRackBlockEntity rack) {
            for (int slot = 0; slot < 2; slot++) {
                ItemStack stack = rack.removeStack(slot);
                if (stack.isEmpty()) continue;

                ItemEntity drop = new ItemEntity(
                        world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
                        stack);
                drop.setPickupDelay(20);
                world.spawnEntity(drop);
            }
        }

        /* 3 ─ extra loot */
        ItemEntity branches = new ItemEntity(
                world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5,
                new ItemStack(ModItems.BRANCH, 4));
        branches.setPickupDelay(20);
        world.spawnEntity(branches);

        ItemEntity stick = new ItemEntity(
                world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5,
                new ItemStack(Items.STICK, 1));
        stick.setPickupDelay(20);
        world.spawnEntity(stick);

        /* 4 ─ remove block (drops already handled) */
        world.breakBlock(pos, false);
    }


    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world,
                                      BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST  -> SHAPE_E;
            case SOUTH -> SHAPE_S;
            case WEST  -> SHAPE_W;
            default    -> SHAPE_N;
        };
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos,
                              BlockState state, PlayerEntity player) {

        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof DryingRackBlockEntity rack) {
                rack.dropContents();
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        // Only allow placement if the block below is a full solid block.
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        return belowState.isSideSolidFullSquare(world, below, Direction.UP);
    }

    /* ------------------------- interaction ------------------------ */
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, BlockHitResult hit) {

        ItemStack hand = player.getMainHandStack();
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof DryingRackBlockEntity rack)) return ActionResult.PASS;

        int slot = pickSlot(state.get(FACING), hit.getPos().subtract(Vec3d.of(pos)));

        /* take */
        if (hand.isEmpty()) {
            ItemStack stack = rack.getStack(slot);
            if (stack.isEmpty()) return ActionResult.PASS;

            ItemStack removed = rack.removeStack(slot);
            if (!player.getInventory().insertStack(removed)) player.dropItem(removed, false);
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM,
                    SoundCategory.BLOCKS, 1f, 1f);
            return ActionResult.SUCCESS;
        }

        /* place – only crude/bloody leather and only if slot is empty */
        if (hand.getItem() != ModItems.RAW_LEATHER ||
                !rack.getStack(slot).isEmpty()) return ActionResult.PASS;

        rack.setStack(slot, hand.split(1));
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM,
                SoundCategory.BLOCKS, 1f, 1f);
        return ActionResult.SUCCESS;
    }

    /** returns 0 (north) or 1 (south) depending on hit location & facing */
    private static int pickSlot(Direction facing, Vec3d local) {
        double z;
        switch (facing) {
            case NORTH -> z = local.z;
            case SOUTH -> z = 1 - local.z;
            case EAST  -> z = local.x;
            case WEST  -> z = 1 - local.x;
            default    -> z = 0.5;
        }
        return z < 0.5 ? 0 : 1;
    }
}
