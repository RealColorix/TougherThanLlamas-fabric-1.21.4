package net.colorixer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Mixin(CreateWorldScreen.class)
public abstract class SeedFinderMixin {

    @Shadow protected abstract void createLevel();

    private boolean isSearching = false;

    // --- CONFIG ---
    private static final List<RegistryKey<Biome>> SPAWN_BIOMES = List.of(
            BiomeKeys.PLAINS,
            BiomeKeys.FLOWER_FOREST,
            BiomeKeys.FOREST,
            BiomeKeys.BIRCH_FOREST,
            BiomeKeys.SWAMP,
            BiomeKeys.WINDSWEPT_HILLS,
            BiomeKeys.WINDSWEPT_FOREST
    );
    private static final List<RegistryKey<Biome>> NEARBY_BIOMES = List.of(
            BiomeKeys.JUNGLE,
            BiomeKeys.PLAINS,
            BiomeKeys.FOREST,
            BiomeKeys.SWAMP,
            BiomeKeys.DESERT,
            BiomeKeys.WINDSWEPT_HILLS,
            BiomeKeys.WINDSWEPT_FOREST,
            BiomeKeys.OCEAN,
            BiomeKeys.COLD_OCEAN
    );
    private static final List<RegistryKey<Biome>> FORBIDDEN_BIOMES = List.of(

    );
    // --------------

    @Inject(method = "createLevel", at = @At("HEAD"), cancellable = true)
    private void interceptCreateLevel(CallbackInfo ci) {
        if (isSearching) return; // Allow the second call to proceed normally

        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        // Cancel the original immediate world gen
        ci.cancel();
        isSearching = true;

        // Show a "Searching" screen to the user
        client.setScreen(new MessageScreen(Text.translatable("mixins.seedfinder.title")));

        CompletableFuture.supplyAsync(() -> {
            GeneratorOptionsHolder holder = screen.getWorldCreator().getGeneratorOptionsHolder();
            var registryManager = holder.combinedDynamicRegistries().getCombinedRegistryManager();
            var paramsRegistry = registryManager.getOrThrow(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            RegistryEntry<MultiNoiseBiomeSourceParameterList> overworldParams = paramsRegistry.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);

            long attemptSeed = new Random().nextLong();
            int attempts = 0;

            while (attempts < 20000) {
                attempts++;
                if (checkSeed(attemptSeed, overworldParams, holder)) {
                    return attemptSeed;
                }
                attemptSeed = new Random().nextLong();
            }
            return -1L; // Failed to find
        }).thenAcceptAsync(foundSeed -> {
            if (foundSeed != -1L) {
                screen.getWorldCreator().setSeed(String.valueOf(foundSeed));
                // Return to the CreateWorldScreen and trigger the real generation
                client.setScreen(screen);
                this.createLevel();
            } else {
                // Return to screen if we timed out/failed
                client.setScreen(screen);
                isSearching = false;
            }
        }, client); // Just pass 'client' here
    }

    private boolean checkSeed(long seed, RegistryEntry<MultiNoiseBiomeSourceParameterList> params, GeneratorOptionsHolder holder) {
        var registryManager = holder.combinedDynamicRegistries().getCombinedRegistryManager();
        NoiseConfig noiseConfig = NoiseConfig.create(
                registryManager.getOrThrow(RegistryKeys.CHUNK_GENERATOR_SETTINGS).getOrThrow(ChunkGeneratorSettings.OVERWORLD).value(),
                registryManager.getOrThrow(RegistryKeys.NOISE_PARAMETERS),
                seed
        );

        MultiNoiseUtil.MultiNoiseSampler sampler = noiseConfig.getMultiNoiseSampler();
        MultiNoiseBiomeSource source = MultiNoiseBiomeSource.create(params);

        var currentSpawnBiome = source.getBiome(0, 16, 0, sampler);
        if (SPAWN_BIOMES.stream().noneMatch(currentSpawnBiome::matchesKey)) return false;

        Set<RegistryKey<Biome>> foundRequired = new HashSet<>();
        for (int x = -1000; x <= 1000; x += 128) {
            for (int z = -1000; z <= 1000; z += 128) {
                var biome = source.getBiome(x >> 2, 16, z >> 2, sampler);
                if (FORBIDDEN_BIOMES.stream().anyMatch(biome::matchesKey)) return false;
                for (RegistryKey<Biome> target : NEARBY_BIOMES) {
                    if (biome.matchesKey(target)) foundRequired.add(target);
                }
            }
        }
        return foundRequired.size() == NEARBY_BIOMES.size();
    }
}