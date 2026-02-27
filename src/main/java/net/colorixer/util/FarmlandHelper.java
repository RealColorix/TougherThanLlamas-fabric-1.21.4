package net.colorixer.util;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FarmlandHelper {
    public static final BooleanProperty FERTILIZED = BooleanProperty.of("fertilized");
    public static final IntProperty WEEDS = IntProperty.of("weeds", 0, 4);

    public static ActionResult fertilize(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack stack) {
        if (state.contains(FERTILIZED) && !state.get(FERTILIZED)) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(FERTILIZED, true), 3);
                if (player != null && !player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
                world.scheduleBlockTick(pos, state.getBlock(), world.random.nextBetween(72000, 120000));
                return ActionResult.SUCCESS_SERVER;
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public static ActionResult cutWeeds(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack stack) {
        int weedLevel = state.get(WEEDS);

        if (weedLevel > 0) {
            if (!world.isClient && world instanceof ServerWorld serverWorld) {
                // Logic
                world.setBlockState(pos, state.with(WEEDS, 0), 3);
                world.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.BLOCKS, 1.0f, 1.0f);

                // ERROR 1 & 2 FIX: Center of block + Use ModBlocks.WEEDS texture
                serverWorld.spawnParticles(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, ModBlocks.WEEDS.getDefaultState()),
                        pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                        20, 0.2, 0.1, 0.2, 0.15
                );

                // Tool damage
                stack.damage(1, serverWorld, (ServerPlayerEntity) player, item -> {});
                return ActionResult.SUCCESS_SERVER;
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}