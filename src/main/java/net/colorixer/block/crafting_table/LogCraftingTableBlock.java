package net.colorixer.block.crafting_table;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class LogCraftingTableBlock extends CraftingTableBlock {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
    private static final Text TITLE = Text.translatable("container.crafting");

    public LogCraftingTableBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.Y));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        // 1. Check if the block directly above is AIR (isAir() returns false for water/lava)
        boolean isAirAbove = world.getBlockState(pos.up()).isAir();

        // 2. Check if the player's head (Eye Height) is above the crafting table
        // pos.getY() + 1.0 is the top surface of the block.
        boolean isPlayerAbove = player.getEyeY() > (pos.getY() + 1.0);

        if (isAirAbove && isPlayerAbove) {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            return ActionResult.CONSUME;
        }

        // Optional: Send message to player if they can't open it
        // player.sendMessage(Text.literal("You must stand above the table and keep it clear!"), true);

        return ActionResult.SUCCESS;
    }

    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        final ScreenHandlerContext screenContext = ScreenHandlerContext.create(world, pos);

        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            return new CraftingScreenHandler(syncId, inventory, screenContext) {
                @Override
                public boolean canUse(PlayerEntity player) {
                    // Constant validation: If the block above becomes obstructed, the UI closes
                    boolean isAirAbove = player.getWorld().getBlockState(pos.up()).isAir();
                    return canUse(screenContext, player, state.getBlock()) && isAirAbove;
                }
            };
        }, TITLE);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(AXIS, context.getSide().getAxis());
    }
}