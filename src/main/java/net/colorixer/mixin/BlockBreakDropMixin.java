package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.colorixer.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class BlockBreakDropMixin {

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerInteractionManager self = (ServerPlayerInteractionManager)(Object)this;
        ServerPlayerEntity player = ((ServerPlayerInteractionManagerAccessor) self).getPlayer();
        ServerWorld world = player.getServerWorld();

        ItemStack heldItem = player.getMainHandStack();
        BlockState state = world.getBlockState(pos);
        BlockState above = world.getBlockState(pos.up());

        if (heldItem.getItem() == ModItems.STONE_HOE &&
                (state.isOf(net.minecraft.block.Blocks.GRASS_BLOCK)) &&
                above.isAir() &&
                world.getRandom().nextFloat() <= 0.05f) {

            Vec3d spawnPos = Vec3d.ofCenter(pos).add(0, 0.5, 0);
            ItemEntity seedDrop = new ItemEntity(world, spawnPos.x, spawnPos.y, spawnPos.z, new ItemStack(Items.WHEAT_SEEDS));
            world.spawnEntity(seedDrop);
        }
    }
}
