package net.colorixer.entity.spiders;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.world.World;

public class JungleSpiderEntity extends CaveSpiderEntity {
    public JungleSpiderEntity(EntityType<? extends CaveSpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    // Call SpiderEntity.createSpiderAttributes() instead
    public static DefaultAttributeContainer.Builder createJungleSpiderAttributes() {
        return SpiderEntity.createSpiderAttributes();
    }
}