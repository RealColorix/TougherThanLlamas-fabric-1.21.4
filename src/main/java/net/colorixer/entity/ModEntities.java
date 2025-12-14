package net.colorixer.entity;

import net.colorixer.TougherThanLlamas;
import net.colorixer.entity.projectile.CobwebProjectileEntity;
import net.colorixer.util.IdentifierUtil;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModEntities {

    public static final RegistryKey<EntityType<?>> COBWEB_PROJECTILE_KEY =
            RegistryKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    IdentifierUtil.createIdentifier(
                            TougherThanLlamas.MOD_ID,
                            "cobweb_projectile"
                    )
            );

    public static final EntityType<CobwebProjectileEntity> COBWEB_PROJECTILE =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    COBWEB_PROJECTILE_KEY.getValue(), // Identifier
                    FabricEntityTypeBuilder
                            .<CobwebProjectileEntity>create(SpawnGroup.MISC)
                            .entityFactory(CobwebProjectileEntity::new)
                            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                            .trackRangeBlocks(4)
                            .trackedUpdateRate(10)
                            .build(COBWEB_PROJECTILE_KEY)
            );

    public static void register() {
        // force class loading
    }
}
