package net.colorixer.mixin;

import net.colorixer.TougherThanLlamas;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
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
public class BreakBlockForAdvancement {

    @Shadow @Final protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void ttll$triggerBlockDestroyers(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // We don't need IF statements for specific blocks here anymore!
        // We trigger the generic criterion, and the JSON handles the rest.
        TougherThanLlamas.DESTROY_BLOCK.trigger(this.player, pos);
    }
}