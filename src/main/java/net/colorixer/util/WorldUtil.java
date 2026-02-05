package net.colorixer.util;

import net.colorixer.entity.passive.cow.CowHungerAccessor;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import java.util.List;

public class WorldUtil {
    public static void scareNearbyCows(World world, BlockPos pos, double radius) {
        if (world == null || world.isClient) return;

        Box box = new Box(pos).expand(radius);
        List<CowEntity> nearbyCows = world.getEntitiesByClass(CowEntity.class, box, cow -> !cow.isBaby());

        for (CowEntity cow : nearbyCows) {
            // FLIP THE SWITCH
            ((CowHungerAccessor) cow).ttll$setBlockScared(true);
        }
    }
}