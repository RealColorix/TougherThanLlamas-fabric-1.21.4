package net.colorixer.mixin.options.world;

import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class RespawnTimeMixin {

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void ttll$advanceTimeToRespawn(
            ServerPlayerEntity oldPlayer,
            boolean alive,
            Entity.RemovalReason removalReason,
            CallbackInfoReturnable<ServerPlayerEntity> cir
    ) {
        ServerPlayerEntity newPlayer = cir.getReturnValue();
        if (newPlayer == null) return;

        ServerWorld world = newPlayer.getServerWorld();

        if (world.getPlayers().size() <= 1) {
            // ALWAYS read from getTimeOfDay(), not getTime()
            // getTime() tracks total server ticks, getTimeOfDay() tracks astronomical time!
            long currentTime = world.getTimeOfDay();

            // 23000 is Dawn (right before sunrise), 18000 is Midnight
            long targetTick = world.getDifficulty() == Difficulty.HARD ? 18000L : 23000L;

            long currentTickInDay = currentTime % 24000L;
            long ticksToAdvance = targetTick - currentTickInDay;

            // If the target time has already passed today, push it to tomorrow!
            // This prevents time from EVER going backward.
            if (ticksToAdvance <= 0) {
                ticksToAdvance += 24000L;
            }

            // ADD the offset to the CURRENT time so the day counter permanently goes up
            world.setTimeOfDay(currentTime + ticksToAdvance);
        }
    }
}