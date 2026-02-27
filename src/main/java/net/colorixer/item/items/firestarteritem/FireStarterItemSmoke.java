package net.colorixer.item.items.firestarteritem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireStarterItemSmoke {

    // Notice I added PlayerEntity and ItemStack to the parameters here!
    public static void spawnFrictionEffects(World world, Vec3d hitPos, Direction side, PlayerEntity player, ItemStack stack) {
        if (world instanceof ServerWorld serverWorld) {
            if (stack.isEmpty()) return;
            // --- 1. SMOKE AT THE BLOCK ---
            // Tiny offset so particles spawn slightly in front of the surface hit
            double ox = side.getOffsetX() * 0.02;
            double oy = side.getOffsetY() * 0.02;
            double oz = side.getOffsetZ() * 0.02;

            double x = hitPos.x + ox;
            double y = hitPos.y + oy;
            double z = hitPos.z + oz;

            serverWorld.spawnParticles(
                    ParticleTypes.SMOKE,
                    x, y, z,
                    2,
                    0.01, 0.01, 0.01,
                    0.02
            );

            // --- 2. ITEM PARTICLES FROM THE PLAYER ---
            // Grab the direction the player is looking
            Vec3d look = player.getRotationVec(1.0F);

            // Push the spawn point ~0.6 blocks forward and up to chest/hand height
            double itemX = player.getX() + (look.x * 0.6);
            double itemY = player.getY() + 1.2 + (look.y * 0.5);
            double itemZ = player.getZ() + (look.z * 0.6);

            serverWorld.spawnParticles(
                    new ItemStackParticleEffect(ParticleTypes.ITEM, stack),
                    itemX, itemY, itemZ,
                    3, // Amount of wood chips
                    0.05, 0.1, 0.05, // Spread so they pop outward
                    0.02 // Speed
            );
        }
    }
}