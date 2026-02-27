package net.colorixer.mixin;

import net.colorixer.player.Chopable;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Mixin(ClientPlayerInteractionManager.class)
public class ChopableHelperMixin {

    @Shadow private float currentBreakingProgress;
    @Shadow private BlockPos currentBreakingPos;

    @Unique
    private final Map<BlockPos, DisplayEntity.BlockDisplayEntity> localGhosts = new HashMap<>();
    @Unique
    private final Map<BlockPos, ShulkerEntity> localShulkers = new HashMap<>();
    @Unique
    private BlockPos activeTrackedPos = null;

    @Inject(method = "tick", at = @At("HEAD"))
    private void manageGhostVisual(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        // 1. CLEANUP: Removes ghost AND shulker floor after 150ms
        if (activeTrackedPos != null && (currentBreakingProgress < 0.5f) || (currentBreakingProgress >= 1f)) {
            final BlockPos posToClean = activeTrackedPos;
            DisplayEntity.BlockDisplayEntity ghost = localGhosts.remove(posToClean);
            ShulkerEntity shulker = localShulkers.remove(posToClean);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    client.execute(() -> {
                        if (ghost != null && ghost.isAlive()) ghost.discard();
                        if (shulker != null && shulker.isAlive()) shulker.discard();
                    });
                }
            }, 50);

            activeTrackedPos = null;
            return;
        }

        // 2. SPAWN: Spawns ghost AND invisible shulker floor at 0.5 progress
        if (currentBreakingPos != null && currentBreakingProgress >= 0.01f) {
            BlockPos immutablePos = currentBreakingPos.toImmutable();
            if (localGhosts.containsKey(immutablePos)) return;

            BlockState resultState = Chopable.getGhostResult(client.world, immutablePos, client.player.getMainHandStack());

            if (resultState != null) {
                activeTrackedPos = immutablePos;

                // --- GHOST DISPLAY ---
                DisplayEntity.BlockDisplayEntity ghost = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, client.world);
                ghost.setBlockState(resultState);

                int light = client.world.getLightLevel(immutablePos);
                int packedLight = (light & 15) << 4 | ((light >> 20) & 15) << 20;
                ghost.getDataTracker().set(net.minecraft.entity.data.TrackedDataHandlerRegistry.INTEGER.create(9), packedLight);

                ghost.setTransformation(new net.minecraft.util.math.AffineTransformation(
                        new Vector3f(0.0005f, 0.0005f, 0.0005f),
                        null,
                        new Vector3f(0.999f, 0.999f, 0.999f),
                        null
                ));

                ghost.refreshPositionAndAngles(immutablePos.getX(), immutablePos.getY(), immutablePos.getZ(), 0, 0);
                client.world.addEntity(ghost);
                localGhosts.put(immutablePos, ghost);

                // --- INVISIBLE SHULKER (THE FLOOR) ---
                if (resultState.isFullCube(client.world, immutablePos)) {
                    ShulkerEntity shulker = new ShulkerEntity(EntityType.SHULKER, client.world);
                    shulker.setInvisible(true);
                    shulker.setAiDisabled(true);
                    shulker.setSilent(true);
                    shulker.setInvulnerable(true);

                    // Position shulker precisely at the block center
                    shulker.refreshPositionAndAngles(immutablePos.getX() + 0.5, immutablePos.getY(), immutablePos.getZ() + 0.5, 0, 0);

                    client.world.addEntity(shulker);
                    localShulkers.put(immutablePos, shulker);
                }
            }
        }
    }
}