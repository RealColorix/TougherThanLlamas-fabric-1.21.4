package net.colorixer.mixin.options.world;

import net.colorixer.util.SeasonTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class SeasonDailyRefreshMixin {

    @Unique
    private long ttll$lastMidnightDay = -1;

    @Inject(method = "tick", at = @At("TAIL"))
    private void ttll$checkMidnight(CallbackInfo ci) {
        ClientWorld world = (ClientWorld) (Object) this;

        // Shift time so the rollover happens at exactly 18000 (Midnight)
        long shiftedTime = world.getTimeOfDay() + 6000L;
        long currentMidnightDay = shiftedTime / 24000L;

        if (ttll$lastMidnightDay == -1) {
            ttll$lastMidnightDay = currentMidnightDay;
            SeasonTracker.activeSeasonDay = currentMidnightDay;
        }
        else if (currentMidnightDay != ttll$lastMidnightDay) {
            ttll$lastMidnightDay = currentMidnightDay;
            SeasonTracker.activeSeasonDay = currentMidnightDay;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.player != null) {

                // 1. Get the player's current render distance in blocks
                int renderDistance = client.options.getViewDistance().getValue() * 16;
                BlockPos playerPos = client.player.getBlockPos();

                // 2. Schedule a soft rebuild for every block in the loaded radius.
                // Sodium catches this and gracefully re-bakes the chunk meshes in the background!
                client.worldRenderer.scheduleBlockRenders(
                        playerPos.getX() - renderDistance,
                        playerPos.getY() - 256, // Cover the whole height of the world
                        playerPos.getZ() - renderDistance,
                        playerPos.getX() + renderDistance,
                        playerPos.getY() + 256,
                        playerPos.getZ() + renderDistance
                );
            }

//            MinecraftClient client = MinecraftClient.getInstance();
//            if (client.worldRenderer != null && client.player != null) {
//                client.worldRenderer.reload();
//            }
        }
    }
}