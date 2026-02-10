package net.colorixer.util;

import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class GloomHelper {
    // Note: In a multiplayer environment, static variables will sync poorly.
    // This works for Singleplayer/Internal Server.
    public static float gloomLevel = 0.0f;
    public static int gloomTicks = 0;

    public static boolean isPlayerInGloom(PlayerEntity player) {
        if (player == null || player.getAbilities().invulnerable) return false;

        BlockPos pos = BlockPos.ofFloored(player.getEyePos());
        World world = player.getWorld();

        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);

        // Dynamic light check (Only works on Client!)
        int dynamicLight = 0;
        if (world.isClient) {
            dynamicLight = (int) LambDynLights.get().getDynamicLightLevel(pos);
        }

        return blockLight <= 2 && skyLight <= 2 && dynamicLight <= 2;
    }

    public static void updateGloom(PlayerEntity player) {
        if (player == null || player.isCreative() || player.isSpectator()) {
            gloomLevel = 0.0f;
            gloomTicks = 0;
            return;
        }

        World world = player.getWorld();
        boolean isServer = !world.isClient;

        if (isPlayerInGloom(player)) {
            // Update the values on whichever side is currently ticking
            gloomLevel = Math.min(1.0f, gloomLevel + (1.0f / 200.0f));

            if (gloomLevel >= 1.0f) {
                gloomTicks++;

                // --- SERVER ONLY: Damage and Status Effects ---
                if (isServer) {
                    // Darkness Effect
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 40, 0, false, false, false));

                    // Terror Damage
                    if (gloomTicks > 2400 && player.getRandom().nextInt(100) == 0) {
                        applyGloomDamage((ServerWorld) world, player);
                    }
                }
            }
        } else {
            gloomLevel = Math.max(0.0f, gloomLevel - (1.0f / 20.0f));
            gloomTicks = 0;
        }

        // --- CLIENT ONLY: Ambient Sounds ---
        if (world.isClient && isPlayerInGloom(player)) {
            if (player.getRandom().nextInt(100) < (gloomLevel * 5)) {
                float pitch = 0.4f + player.getRandom().nextFloat() * 0.8f;
                world.playSound(player, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 1.0f, pitch);
            }
        }
    }

    private static void applyGloomDamage(ServerWorld world, PlayerEntity player) {
        var registryManager = world.getRegistryManager();
        var damageTypeRegistry = registryManager.getOrThrow(net.minecraft.registry.RegistryKeys.DAMAGE_TYPE);
        var forceKey = net.minecraft.registry.RegistryKey.of(
                net.minecraft.registry.RegistryKeys.DAMAGE_TYPE,
                net.minecraft.util.Identifier.of("ttll", "the_dark_force")
        );

        var forceTypeEntry = damageTypeRegistry.getOptional(forceKey)
                .orElseGet(() -> damageTypeRegistry.getOrThrow(net.minecraft.entity.damage.DamageTypes.MAGIC));

        net.minecraft.entity.damage.DamageSource forceSource = new net.minecraft.entity.damage.DamageSource(forceTypeEntry);
        player.damage(world, forceSource, 1.0f);
    }

    public static float getFovMultiplier() {
        if (gloomLevel <= 0) return 1.0f;
        return 1.0f + (gloomLevel * 0.5f) + (gloomTicks / 600.0f);
    }
}