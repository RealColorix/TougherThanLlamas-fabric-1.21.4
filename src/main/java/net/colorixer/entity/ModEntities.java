package net.colorixer.entity;

import net.colorixer.TougherThanLlamas;
import net.colorixer.entity.creeper.firecreeper.FireCreeperEntity;
import net.colorixer.entity.projectile.CobwebProjectileEntity;
import net.colorixer.entity.spiders.JungleSpiderEntity;
import net.colorixer.util.IdentifierUtil;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

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
                    COBWEB_PROJECTILE_KEY.getValue(),
                    FabricEntityTypeBuilder
                            .<CobwebProjectileEntity>create(SpawnGroup.MISC)
                            .entityFactory(CobwebProjectileEntity::new)
                            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                            .trackRangeBlocks(4)
                            .trackedUpdateRate(10)
                            .build(COBWEB_PROJECTILE_KEY)
            );

    // --- JUNGLE SPIDER ---

    // --- FIRE CREEPER ---

    public static final RegistryKey<EntityType<?>> FIRE_CREEPER_KEY =
            RegistryKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    Identifier.of(TougherThanLlamas.MOD_ID, "fire_creeper")
            );

    public static final EntityType<FireCreeperEntity> FIRE_CREEPER =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    FIRE_CREEPER_KEY.getValue(),
                    EntityType.Builder.create(FireCreeperEntity::new, SpawnGroup.MONSTER)
                            .dimensions(0.6F, 1.7f) // Creeper dimensions
                            .build(FIRE_CREEPER_KEY)
            );

    public static final RegistryKey<EntityType<?>> JUNGLE_SPIDER_KEY =
            RegistryKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    IdentifierUtil.createIdentifier(
                            TougherThanLlamas.MOD_ID,
                            "jungle_spider"
                    )
            );

    public static final EntityType<JungleSpiderEntity> JUNGLE_SPIDER =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    JUNGLE_SPIDER_KEY.getValue(),
                    FabricEntityTypeBuilder
                            .<JungleSpiderEntity>create(SpawnGroup.MONSTER)
                            .entityFactory(JungleSpiderEntity::new)
                            // Cave Spider dimensions: 0.7 wide, 0.5 high
                            .dimensions(EntityDimensions.fixed(0.7F, 0.5F))
                            .build(JUNGLE_SPIDER_KEY)
            );

    public static void registerEntities() {
        // force class loading
    }
}