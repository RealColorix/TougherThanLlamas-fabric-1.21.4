package net.colorixer.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingFireMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void handleFireCollision(CallbackInfo ci) {
        FallingBlockEntity self = (FallingBlockEntity)(Object)this;

        // Ensure we are on server side and only affecting our specific creeper fire
        if (!self.getWorld().isClient && self.getCommandTags().contains("creeper_fire")) {
            BlockPos pos = self.getBlockPos();
            FluidState fluidState = self.getWorld().getFluidState(pos);

            // 1. Check for ANY Liquid
            if (!fluidState.isEmpty()) {
                // If the liquid is specifically WATER, play sound and smoke
                if (fluidState.isIn(FluidTags.WATER)) {
                    self.getWorld().playSound(
                            null,
                            pos,
                            SoundEvents.BLOCK_FIRE_EXTINGUISH,
                            SoundCategory.BLOCKS,
                            0.5f,
                            2.0f + self.getWorld().random.nextFloat()
                    );

                    // Add some smoke particles for flavor
                    ((ServerWorld)self.getWorld()).spawnParticles(
                            ParticleTypes.SMOKE,
                            self.getX(), self.getY(), self.getZ(),
                            3, 0.1, 0.1, 0.1, 0.05
                    );
                }

                // Discard for ANY liquid (Lava, Water, etc.)
                self.discard();
                return;
            }

            // 2. Check for Leaves (Ignite)
            if (self.getWorld().getBlockState(pos).isIn(BlockTags.LEAVES)) {
                self.getWorld().setBlockState(pos, Blocks.FIRE.getDefaultState());
                self.discard();
            }
        }
    }
}