package net.colorixer.mixin;

import net.colorixer.access.PlayerArmorWeightAccessor;
import net.colorixer.util.ExhaustionHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class MiningExhaustionMixin {

    @Shadow protected ServerPlayerEntity player;
    @Shadow private boolean mining;

    private final float drainAmount = 0.001f;

    @Inject(method = "update", at = @At("HEAD"))
    private void ttll$exhaustWhileMining(CallbackInfo ci) {


        if (!this.mining) return;
        if (player.isCreative()) return;


        int armorWeight = ((PlayerArmorWeightAccessor) player).ttll$getArmorWeight();
        float weightMultiplier = 1.0f + (armorWeight * 0.015f);

        player.getHungerManager().addExhaustion(drainAmount * weightMultiplier);
    }
}