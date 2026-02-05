package net.colorixer.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
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

        // Check if the world is brand new (age is 0)
        if (overworld.getTime() == 0) {
            // 18000 is Midnight. 13000 is Sunset.
            overworld.setTimeOfDay(18000L);
        }
    }
}