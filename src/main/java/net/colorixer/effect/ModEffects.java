package net.colorixer.effect;

import net.colorixer.TougherThanLlamas;
import net.colorixer.effect.BleedingStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEffects {
    // 1. Create the Effect Instance
    public static final StatusEffect BLEEDING_EFFECT = new BleedingStatusEffect();

    // 2. Create the Registry Entry (This is what you use in code)
    public static RegistryEntry<StatusEffect> BLEEDING;

    public static void registerEffects() {
        // 3. Perform the actual registration
        BLEEDING = Registry.registerReference(
                Registries.STATUS_EFFECT,
                Identifier.of(TougherThanLlamas.MOD_ID, "bleeding"),
                BLEEDING_EFFECT
        );

        TougherThanLlamas.LOGGER.info("Registering Status Effects for " + TougherThanLlamas.MOD_ID);
    }
}