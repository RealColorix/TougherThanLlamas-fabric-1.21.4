package net.colorixer.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

public class OreWorldGen {

    // These Keys must match the filename in src/main/resources/data/ttll/worldgen/placed_feature/
    public static final RegistryKey<PlacedFeature> DEEPSLATE_ORE_KEY = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            Identifier.of("ttll", "ore_deepslate")
    );
    public static final RegistryKey<PlacedFeature> VOLCANIC_TUFF_VEIN_KEY = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            Identifier.of("ttll", "volcanic_tuff_vein")
    );
    public static final RegistryKey<PlacedFeature> LAVA_LAKES_KEY = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            Identifier.of("ttll", "lava_lakes")
    );

    public static void registerWorldGen() {
        // 1. Register the Deepslate Layer/Ore
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                DEEPSLATE_ORE_KEY
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                VOLCANIC_TUFF_VEIN_KEY
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.LAKES,
                LAVA_LAKES_KEY
        );
    }
}