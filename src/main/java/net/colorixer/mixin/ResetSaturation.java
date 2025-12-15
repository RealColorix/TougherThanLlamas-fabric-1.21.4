package net.colorixer.mixin;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles saturation reset on:
 * - first world join
 * - respawn after death
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ResetSaturation {

    private static final String TTLL_JOINED_KEY = "ttll_has_joined";

    /**
     * First-ever join:
     * runs once per player.
     */
    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    private void hardmod$onFirstJoin(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.getBoolean(TTLL_JOINED_KEY)) {
            HungerManager hunger =
                    ((ServerPlayerEntity)(Object)this).getHungerManager();
            hunger.setSaturationLevel(1.0F);
        }
    }

    /**
     * Mark that the player has joined at least once.
     */
    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("TAIL")
    )
    private void hardmod$markJoined(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean(TTLL_JOINED_KEY, true);
    }

    /**
     * Respawn after death.
     * copyFrom(..., alive=false) ONLY runs on death.
     */
    @Inject(
            method = "copyFrom",
            at = @At("TAIL")
    )
    private void hardmod$resetSaturationOnDeath(
            ServerPlayerEntity oldPlayer,
            boolean alive,
            CallbackInfo ci
    ) {
        if (!alive) {
            HungerManager hunger =
                    ((ServerPlayerEntity)(Object)this).getHungerManager();
            hunger.setSaturationLevel(1.0F);
        }
    }
}
