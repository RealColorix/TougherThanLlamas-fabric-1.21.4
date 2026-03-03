package net.colorixer.mixin.options.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class StartAtMidnight {

    @Inject(method = "prepareStartRegion", at = @At("HEAD"))
    private void ttll$setMidnightOnFirstStart(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerWorld overworld = server.getOverworld();

        if (overworld == null) return;

        boolean isCreativeWorld = server.getDefaultGameMode() == net.minecraft.world.GameMode.CREATIVE;

        if (overworld.getTime() == 0 && !isCreativeWorld) {

            if (overworld.getDifficulty() == Difficulty.HARD) {
                overworld.setTimeOfDay(184200L);
            } else {
                overworld.setTimeOfDay(189000L);
            }
        }
    }
}