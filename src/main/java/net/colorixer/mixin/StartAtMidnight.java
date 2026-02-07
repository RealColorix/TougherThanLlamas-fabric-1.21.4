package net.colorixer.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Add these missing imports!
import java.util.List;

@Mixin(MinecraftServer.class)
public class StartAtMidnight {

    @Inject(method = "prepareStartRegion", at = @At("HEAD"))
    private void ttll$setMidnightOnFirstStart(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerWorld overworld = server.getOverworld();

        if (overworld == null) return;

        // 1. Check the server's default game mode instead of the player list
        // This is set when you create the world (Survival, Creative, etc.)
        boolean isCreativeWorld = server.getDefaultGameMode() == net.minecraft.world.GameMode.CREATIVE;

        // 2. Only set to midnight if time is 0 and it's NOT a creative world
        if (overworld.getTime() == 0 && !isCreativeWorld) {
            // 18000 is Midnight
            overworld.setTimeOfDay(18000L);
        }
    }
}