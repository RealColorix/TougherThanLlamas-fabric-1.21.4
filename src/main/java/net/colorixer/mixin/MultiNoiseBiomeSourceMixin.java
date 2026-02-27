package net.colorixer.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {

    private static final int SPAWN_RADIUS_SQ = 2500 * 2500;

    // --- YOUR BIOME PLOTTER ---
    // Key = The biome to be replaced
    // Value = List of potential replacements (List.of(Single) for 1-to-1, or List.of(Multiple) for random)
    private static final Map<RegistryKey<Biome>, List<RegistryKey<Biome>>> REPLACEMENT_MAP = new HashMap<>() {{
        // Cold Mapping (1-to-1)

        put(BiomeKeys.DEEP_FROZEN_OCEAN, List.of(BiomeKeys.DEEP_COLD_OCEAN));
        put(BiomeKeys.FROZEN_OCEAN, List.of(BiomeKeys.COLD_OCEAN));
        put(BiomeKeys.WARM_OCEAN, List.of(BiomeKeys.OCEAN));
        put(BiomeKeys.LUKEWARM_OCEAN, List.of(BiomeKeys.OCEAN));
        put(BiomeKeys.DEEP_LUKEWARM_OCEAN, List.of(BiomeKeys.DEEP_OCEAN));
        put(BiomeKeys.ICE_SPIKES, List.of(BiomeKeys.SNOWY_PLAINS));



        put(BiomeKeys.SAVANNA, List.of(BiomeKeys.PLAINS, BiomeKeys.FOREST));
        put(BiomeKeys.SAVANNA_PLATEAU, List.of(BiomeKeys.WINDSWEPT_FOREST));
        put(BiomeKeys.WINDSWEPT_SAVANNA, List.of(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, BiomeKeys.WINDSWEPT_HILLS));



        put(BiomeKeys.DARK_FOREST, List.of(BiomeKeys.PLAINS, BiomeKeys.FOREST, BiomeKeys.FLOWER_FOREST, BiomeKeys.SUNFLOWER_PLAINS));


        put(BiomeKeys.MANGROVE_SWAMP, List.of(BiomeKeys.SWAMP));

        put(BiomeKeys.BADLANDS, List.of(BiomeKeys.DESERT));
        put(BiomeKeys.WOODED_BADLANDS, List.of(BiomeKeys.SAVANNA));
        put(BiomeKeys.ERODED_BADLANDS, List.of(BiomeKeys.WINDSWEPT_SAVANNA));

        // Add any others here...
    }};

    @Shadow
    protected abstract Stream<RegistryEntry<Biome>> biomeStream();

    @Unique
    private Map<RegistryKey<Biome>, List<RegistryEntry<Biome>>> cachedReplacements;

    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void replaceSpawnBiomes(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        int bx = x << 2;
        int bz = z << 2;
        if (bx * bx + bz * bz > SPAWN_RADIUS_SQ) return;

        RegistryEntry<Biome> current = cir.getReturnValue();
        RegistryKey<Biome> key = current.getKey().orElse(null);

        // If this biome isn't in our map, don't do anything
        if (key == null || !REPLACEMENT_MAP.containsKey(key)) return;

        if (cachedReplacements == null) {
            cacheMappedBiomes();
        }

        List<RegistryEntry<Biome>> options = cachedReplacements.get(key);
        if (options == null || options.isEmpty()) return;

        // Use the biome's name hash so the entire patch is consistent
        int biomeHash = key.getValue().toString().hashCode();

        // Pick one of your chosen options
        RegistryEntry<Biome> selection = options.get(Math.floorMod(biomeHash, options.size()));
        cir.setReturnValue(selection);
    }

    @Unique
    private void cacheMappedBiomes() {
        cachedReplacements = new HashMap<>();
        List<RegistryEntry<Biome>> allBiomes = biomeStream().toList();

        REPLACEMENT_MAP.forEach((original, targets) -> {
            List<RegistryEntry<Biome>> entries = allBiomes.stream()
                    .filter(e -> e.getKey().map(targets::contains).orElse(false))
                    .toList();
            cachedReplacements.put(original, entries);
        });
    }
}