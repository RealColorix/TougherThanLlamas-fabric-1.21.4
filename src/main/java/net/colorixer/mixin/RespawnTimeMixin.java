package net.colorixer.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class RespawnTimeMixin {

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void ttll$resetTimeToMorning(
            ServerPlayerEntity oldPlayer,
            boolean alive,
            Entity.RemovalReason removalReason,
            CallbackInfoReturnable<ServerPlayerEntity> cir
    ) {
        // cir.getReturnValue() is the NEW ServerPlayerEntity created in the code you pasted
        ServerPlayerEntity newPlayer = cir.getReturnValue();
        if (newPlayer == null) return;

        ServerWorld world = newPlayer.getServerWorld();

        // Check if this player is alone in the dimension they just spawned into
        if (world.getPlayers().size() <= 1) {
            long currentTime = world.getTime();
            long startOfCurrentDay = currentTime - (currentTime % 24000L);

            world.setTimeOfDay(startOfCurrentDay);
        }
    }
}