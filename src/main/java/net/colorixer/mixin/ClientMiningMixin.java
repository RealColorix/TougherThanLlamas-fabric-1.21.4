package net.colorixer.mixin;

import net.colorixer.util.ExhaustionHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientMiningMixin {

    @Shadow private boolean breakingBlock;

    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$triggerClientJitter(CallbackInfo ci) {
        // On the client, we check 'breakingBlock' instead of 'mining'
        if (this.breakingBlock) {
            ExhaustionHelper.triggerJitter(2);
        }
    }
}