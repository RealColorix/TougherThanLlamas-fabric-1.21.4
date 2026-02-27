package net.colorixer.effect;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class BleedingDamageType {
    public static final RegistryKey<DamageType> BLEEDING = RegistryKey.of(
            RegistryKeys.DAMAGE_TYPE,
            Identifier.of("ttll", "bleeding")
    );
}