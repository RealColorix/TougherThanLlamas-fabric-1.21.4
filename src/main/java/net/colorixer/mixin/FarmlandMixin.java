package net.colorixer.mixin;

import net.colorixer.util.FarmlandHelper;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandMixin extends Block {
    @Unique
    private static final BooleanProperty FERTILIZED = FarmlandHelper.FERTILIZED;
    @Unique
    private static final IntProperty WEEDS = FarmlandHelper.WEEDS;

    public FarmlandMixin(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(Items.BONE_MEAL)) {
            ActionResult result = FarmlandHelper.fertilize(world, pos, state, player, stack);
            if (result.isAccepted()) return result;
        }

        if (stack.isOf(Items.SHEARS) && hit.getSide() == net.minecraft.util.math.Direction.UP) {
            // Let the helper handle EVERYTHING (logic, sound, and the centered particles)
            return FarmlandHelper.cutWeeds(world, pos, state, player, stack);
        }

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }


    // --- REST OF THE MIXIN LOGIC ---

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setDefaultStates(Settings settings, CallbackInfo ci) {
        this.setDefaultState(this.getDefaultState().with(FERTILIZED, false).with(WEEDS, 0));
    }

    @Redirect(method = "setToDirt", at = @At(value = "FIELD", target = "Lnet/minecraft/block/Blocks;DIRT:Lnet/minecraft/block/Block;"))
    private static Block redirectToCoarseDirt() {
        return Blocks.COARSE_DIRT;
    }

    @Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
    private void onScheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random, CallbackInfo ci) {
        // If this tick was triggered by our timer and the block is fertilized...
        if (state.contains(FERTILIZED) && state.get(FERTILIZED)) {
            // 1. Remove the fertilizer
            world.setBlockState(pos, state.with(FERTILIZED, false), 3);

            // 2. Stop the method here so vanilla farmland logic doesn't
            // trigger accidentally on this same tick.
            ci.cancel();
        }
    }

    @Inject(method = "randomTick", at = @At("TAIL"))
    private void handleRandomTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        BlockState currentState = state;
        if (random.nextInt(15) == 0) {
            int currentWeeds = state.get(WEEDS);
            if (currentWeeds < 4) {
                currentState = currentState.with(WEEDS, currentWeeds + 1);
                world.setBlockState(pos, currentState, 3);
            }
        }
        if (currentState.get(FERTILIZED)) {
            BlockPos cropPos = pos.up();
            BlockState cropState = world.getBlockState(cropPos);
            if (!cropState.isAir() && cropState.getBlock() != null) {
                ((FarmlandAbstractBlockAccessor) cropState.getBlock()).invokeRandomTick(cropState, world, cropPos, random);
            }
        }
    }

    @Inject(method = "appendProperties", at = @At("TAIL"))
    private void addProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(FERTILIZED, WEEDS);
    }
}