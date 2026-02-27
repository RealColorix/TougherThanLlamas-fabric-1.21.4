package net.colorixer.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;


public class BleedingStatusEffect extends StatusEffect {
    public BleedingStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x990000); // Dark red color
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {

        var damageSource = world.getRegistryManager()
                .getOrThrow(RegistryKeys.DAMAGE_TYPE)
                .getOrThrow(BleedingDamageType.BLEEDING);


        //entity.damage(world, new net.minecraft.entity.damage.DamageSource(damageSource), 0.25f);
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {


        return duration % 20 == 0;
    }
}