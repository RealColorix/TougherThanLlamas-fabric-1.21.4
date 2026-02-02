package net.colorixer.util;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandlerContext;

public class ContextUtil {
    public static boolean canUse(ScreenHandlerContext context, PlayerEntity player, Block block) {
        return context.get((world, pos) -> {
            return world.getBlockState(pos).isOf(block) && player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
        }, true);
    }
}