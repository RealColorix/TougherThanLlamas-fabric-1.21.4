package net.colorixer.item.items.firestarteritem;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireStarterItemSmoke {
    public static void spawnFrictionEffects(World world, Vec3d hitPos, Direction side) {
        if (world instanceof ServerWorld serverWorld) {
            // Tiny offset so particles spawn slightly in front of the surface hit
            double ox = side.getOffsetX() * 0.02;
            double oy = side.getOffsetY() * 0.02;
            double oz = side.getOffsetZ() * 0.02;

            double x = hitPos.x + ox;
            double y = hitPos.y + oy;
            double z = hitPos.z + oz;


            // Friction Sparks (Flame)
            serverWorld.spawnParticles(
                    ParticleTypes.SMOKE,
                    x, y, z,
                    2,
                    0.01, 0.01, 0.01,
                    0.02
            );
        }
    }
}