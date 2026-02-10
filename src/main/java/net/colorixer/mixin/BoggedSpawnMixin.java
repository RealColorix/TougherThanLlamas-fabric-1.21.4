package net.colorixer.mixin;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HostileEntity.class)
public class BoggedSpawnMixin {

    @Inject(method = "isSpawnDark", at = @At("HEAD"), cancellable = true)
    private static void ttll$allowJungleDaySpawn(ServerWorldAccess world, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        // If the location is a Jungle, we return 'true' to trick the game
        // into thinking it is dark enough for monsters.
        if (world.getBiome(pos).isIn(BiomeTags.IS_JUNGLE)) {
            cir.setReturnValue(true);
        }
    }
}