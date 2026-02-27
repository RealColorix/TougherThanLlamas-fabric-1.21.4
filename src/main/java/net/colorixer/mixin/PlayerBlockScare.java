package net.colorixer.mixin;

import net.colorixer.util.WorldUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class PlayerBlockScare {

    @Shadow @Final protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void ttll$scareOnBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Now calling the generalized method for all animals
        // This will trigger the AnimalDataAccessor 'isBlockScared' flag
        WorldUtil.scareNearbyAnimals(this.player.getWorld(), pos, 5.0);
    }
}