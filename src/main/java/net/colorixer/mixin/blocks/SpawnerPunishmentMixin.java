package net.colorixer.mixin.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class SpawnerPunishmentMixin {

    @Inject(method = "onBreak", at = @At("HEAD"))
    private void ttll$punishSpawnerBreaking(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<BlockState> cir) {

        // Added "!player.isCreative()" to the check
        if (!world.isClient && state.isOf(Blocks.SPAWNER) && !player.isCreative()) {

            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 1));

            // Pitch set back to 1.0f so the native audio file plays at normal speed (approx. 2 seconds)
            world.playSound(null, pos, SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.BLOCKS, 1.3f, 0.3f);
        }
    }
}