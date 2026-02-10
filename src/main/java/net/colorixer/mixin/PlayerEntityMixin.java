package net.colorixer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.colorixer.access.PlayerArmorWeightAccessor;
import net.colorixer.util.IdentifierUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Unique
    private static final TagKey<Block> GRASSLIKE_SLOWERS = TagKey.of(RegistryKeys.BLOCK, IdentifierUtil.createIdentifier("ttll", "grasslikeslowers"));
    @Unique
    private static final TagKey<Block> SANDLIKE_SLOWERS = TagKey.of(RegistryKeys.BLOCK, IdentifierUtil.createIdentifier("ttll", "sandlikeslowers"));
    @Unique
    private static final TagKey<Block> WOODLIKE_SLOWERS = TagKey.of(RegistryKeys.BLOCK, IdentifierUtil.createIdentifier("ttll", "woodlikeslowers"));
    @Unique
    private static final TagKey<Block> FOLIAGE_SLOWERS = TagKey.of(RegistryKeys.BLOCK, IdentifierUtil.createIdentifier("ttll", "foliageslowers"));



    // This stores the last block's penalty so it carries into your jump
    @Unique
    private float lastTerrainMultiplier = 1.0f;

    @ModifyReturnValue(
            method = "createPlayerAttributes",
            at = @At("RETURN")
    )
    private static DefaultAttributeContainer.Builder hardmod$defaults(
            DefaultAttributeContainer.Builder original) {
        return original
                .add(EntityAttributes.SAFE_FALL_DISTANCE, 3.0D)
                .add(EntityAttributes.FALL_DAMAGE_MULTIPLIER, 1.5D);
    }



    @Inject(method = "getMovementSpeed", at = @At("RETURN"), cancellable = true)
    private void ttll$applyDynamicSpeed(CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.isCreative() || player.isSpectator()) return;

        int armorWeight = ((PlayerArmorWeightAccessor) player)
                .ttll$getArmorWeight();

        /*
         * 0.1% slow per weight
         * 1 weight = 0.001 speed reduction
         */
        float armorMultiplier = 1.0f - (armorWeight * 0.015f);

// hard floor so speed never collapses
        armorMultiplier = MathHelper.clamp(armorMultiplier, 0.7f, 1.0f);


        // --- 1. HEALTH & HUNGER PENALTY ---
        float healthRatio = player.getHealth() / player.getMaxHealth();
        float hungerRatio = player.getHungerManager().getFoodLevel() / 20.0f;

        float healthMultiplier;
        if (healthRatio > 0.25f) {
            healthMultiplier = 0.8f + (healthRatio - 0.25f) * (0.2f / 0.75f);
        } else {
            float lowRange = healthRatio / 0.25f;
            healthMultiplier = 0.1f + 0.7f * lowRange * lowRange;
        }

        float hungerMultiplier;
        if (hungerRatio > 0.25f) {
            hungerMultiplier = 0.8f + (hungerRatio - 0.25f) * (0.2f / 0.75f);
        } else {
            float lowRange = hungerRatio / 0.25f;
            hungerMultiplier = 0.1f + 0.7f * lowRange * lowRange;
        }

        // --- 2. GLOOM PENALTY (NEW) ---
        // If player is fumbling in the dark, apply a 0.5x speed multiplier
        float gloomMultiplier = 1.0f;
        if (net.colorixer.util.GloomHelper.gloomLevel > 0.05f) {
            gloomMultiplier = 0.5f;
        }

        float statsMultiplier = healthMultiplier * hungerMultiplier * gloomMultiplier;

        // --- 3. TERRAIN PENALTY (PERSISTENT) ---
        if (player.isOnGround()) {
            float terrainMultiplier = 1.0f;
            BlockState floorState = player.getSteppingBlockState();

            if (floorState.isIn(SANDLIKE_SLOWERS)) {
                terrainMultiplier = 0.7f;
            } else if (floorState.isIn(GRASSLIKE_SLOWERS)) {
                terrainMultiplier = 0.85f;
            } else if (floorState.isIn(WOODLIKE_SLOWERS)) {
                terrainMultiplier = 0.95f;
            }
            this.lastTerrainMultiplier = terrainMultiplier;
        }

        // --- 4. FOLIAGE PENALTY ---
        boolean inFoliage = false;
        Box feetBox = player.getBoundingBox().offset(0.0, -0.1, 0.0);

        int minX = MathHelper.floor(feetBox.minX);
        int maxX = MathHelper.floor(feetBox.maxX);
        int minY = MathHelper.floor(feetBox.minY);
        int maxY = MathHelper.floor(feetBox.maxY);
        int minZ = MathHelper.floor(feetBox.minZ);
        int maxZ = MathHelper.floor(feetBox.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = player.getWorld().getBlockState(pos);

                    if (state.isAir()) continue;
                    if (FOLIAGE_SLOWERS != null && state.isIn(FOLIAGE_SLOWERS)) {
                        inFoliage = true;
                        break;
                    }

                    if (!state.getCollisionShape(player.getWorld(), pos).isEmpty()) continue;
                    if (state.getBlock().getDefaultState().isReplaceable()) {
                        inFoliage = true;
                        break;
                    }
                }
                if (inFoliage) break;
            }
            if (inFoliage) break;
        }

        float currentFoliageMultiplier = inFoliage ? 0.66f : 1.0f;

        // --- 5. FINAL COMBINATION ---
        float originalSpeed = cir.getReturnValue();
        float totalMultiplier = statsMultiplier * lastTerrainMultiplier * currentFoliageMultiplier * armorMultiplier;

        cir.setReturnValue(originalSpeed * totalMultiplier);

        // --- 6. THE AIR FIX ---
        if (!player.isOnGround() && !player.getAbilities().flying) {
            player.setVelocity(player.getVelocity().multiply(totalMultiplier, 1.0, totalMultiplier));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void ttll$applyCriticalHealthEffects(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Only run on the server side to avoid ghost effects,
        // and skip for creative/spectator
        if (player.getWorld().isClient || player.isCreative() || player.isSpectator()) return;

        // 2 points = 1 Heart
        if (player.getHealth() < 2.01f && player.isAlive()) {
            // We apply for 21 ticks (1 second + 1 tick buffer)
            // with amplifier 0, ambient = true, and showParticles = false
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS, 99, 0, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.DARKNESS, 99, 0, true, false, false));

            // Optional: Add a heartbeat sound every second
            if (player.getWorld().getTime() % 20 == 0) {
                player.playSound(SoundEvents.ENTITY_PLAYER_BREATH, 1.0f, 1.0f);
            }
        }
    }

}