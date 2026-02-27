package net.colorixer.util;

import net.colorixer.entity.passive.goals.AnimalDataAccessor;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import java.util.List;

public class WorldUtil {
    public static void scareNearbyAnimals(World world, BlockPos pos, double radius) {
        if (world == null || world.isClient) return;

        Box box = new Box(pos).expand(radius);
        // Changed from CowEntity to AnimalEntity
        List<AnimalEntity> nearbyAnimals = world.getEntitiesByClass(AnimalEntity.class, box, animal -> !animal.isBaby());

        for (AnimalEntity animal : nearbyAnimals) {
            // Cast to the new generic accessor
            ((AnimalDataAccessor) animal).ttll$setBlockScared(true);
        }
    }
}