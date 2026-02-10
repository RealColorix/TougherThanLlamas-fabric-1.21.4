package net.colorixer.block.torch;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.colorixer.item.ModItems;

public class CrudeTorchItem extends BlockItem {

    public CrudeTorchItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // 1. Check for fluids (Lava)
        BlockHitResult hit = raycast(world, user, RaycastContext.FluidHandling.ANY);
        BlockState state = world.getBlockState(hit.getBlockPos());

        // 2. Manual Fire Check (since Fire now has no hitbox for the raycast to hit)
        // We check the block the player is actually looking at within 5 blocks
        BlockHitResult blockHit = (BlockHitResult) user.raycast(5.0, 0.0f, false);
        BlockPos fireCheckPos = blockHit.getBlockPos();
        BlockState maybeFire = world.getBlockState(fireCheckPos);

        if (isIgniter(state) || isIgniter(maybeFire)) {
            return performIgnition(world, user, hand);
        }

        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity user = context.getPlayer();
        if (user == null) return super.useOnBlock(context);

        // Check for Lava/Fire in the path
        BlockHitResult fluidHit = raycast(world, user, RaycastContext.FluidHandling.ANY);
        BlockState fluidState = world.getBlockState(fluidHit.getBlockPos());

        // Check the specific block clicked + the one above/beside it
        BlockPos pos = context.getBlockPos();
        BlockState clickedState = world.getBlockState(pos);
        BlockPos placePos = pos.offset(context.getSide());
        BlockState placeState = world.getBlockState(placePos);

        if (isIgniter(fluidState) || isIgniter(clickedState) || isIgniter(placeState)) {
            return performIgnition(world, user, context.getHand());
        }

        return super.useOnBlock(context);
    }

    private ActionResult performIgnition(World world, PlayerEntity user, Hand hand) {
        if (user == null) return ActionResult.PASS;

        ItemStack itemStack = user.getStackInHand(hand);

        // Sound played on both Client (immediate) and Server (others)
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (!world.isClient) {
            ItemStack burningTorch = new ItemStack(ModItems.BURNING_CRUDE_TORCH_ITEM);

            if (user.getAbilities().creativeMode) {
                // Creative: Just give the item if not present to avoid clearing infinite stack
                if (!user.getInventory().contains(burningTorch)) {
                    user.getInventory().insertStack(burningTorch);
                }
            } else {
                if (itemStack.getCount() == 1) {
                    // Replace the hand stack directly for survival
                    user.setStackInHand(hand, burningTorch);
                } else {
                    // Decrement and insert
                    itemStack.decrement(1);
                    if (!user.getInventory().insertStack(burningTorch)) {
                        user.dropItem(burningTorch, false);
                    }
                }
            }
            // Return SUCCESS_SERVER to force an inventory sync packet
            return ActionResult.SUCCESS_SERVER;
        }

        return ActionResult.SUCCESS;
    }

    private boolean isIgniter(BlockState state) {
        return state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE) || state.isOf(Blocks.SOUL_FIRE);
    }
}