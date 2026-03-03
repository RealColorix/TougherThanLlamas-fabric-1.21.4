package net.colorixer.mixin.options.world;

import net.colorixer.util.SeasonTracker;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class SeasonServerSyncMixin {

    // The Server needs to constantly update our tracker so Block physics work!
    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$syncServerSeason(java.util.function.BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        long shiftedTime = world.getTimeOfDay() + 6000L;
        SeasonTracker.activeSeasonDay = shiftedTime / 24000L;

        // --- NEW: Weather Memory Logic ---
        long day = (SeasonTracker.activeSeasonDay - 12 + 96) % 96L;

        if (day < 36 || day >= 72) {
            // It is Spring/Summer/Fall. Reset the blizzard tracker!
            SeasonTracker.hasSnowedThisWinter = false;
        } else if (world.isRaining()) {
            // It is Winter and it is currently storming. The world is officially frozen.
            SeasonTracker.hasSnowedThisWinter = true;
        }
    }
}