package net.colorixer.mixin;

import net.colorixer.util.FarmlandHelper;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class CropMixin extends PlantBlock {

    @Shadow public abstract int getAge(BlockState state);
    @Shadow public abstract int getMaxAge();
    @Shadow public abstract BlockState withAge(int age);

    // This fixes the "protected access" error by shadowing the static method
    @Shadow protected static float getAvailableMoisture(Block block, BlockView world, BlockPos pos) {
        throw new AssertionError();
    }

    @Unique
    private static final IntProperty CROP_STATUS = IntProperty.of("crop_status", 0, 2);
    @Unique
    private static final BooleanProperty FERTILIZED = FarmlandHelper.FERTILIZED;
    @Unique
    private static final IntProperty WEEDS = FarmlandHelper.WEEDS;

    protected CropMixin(Settings settings) { super(settings); }

    // --- PROPERTY INJECTION ---
    // --- PROPERTY INJECTION ---
    @Inject(method = "appendProperties", at = @At("TAIL"))
    private void addStatusProperty(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(CROP_STATUS);
    }

    // --- INITIALIZATION FIX ---
    @Inject(method = "<init>", at = @At("RETURN"))
    private void setDefaultStatus(AbstractBlock.Settings settings, CallbackInfo ci) {
        // We get the state manager's default state directly.
        // This ensures even "unregistered" blocks (during init) get the property.
        BlockState baseState = this.stateManager.getDefaultState();

        // Safety check: Only set it if the property actually exists in this block's manager
        if (baseState.contains(CROP_STATUS)) {
            this.setDefaultState(baseState.with(CROP_STATUS, 0));
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        // Stop if the block doesn't have our property (safety gate)
        if (!state.contains(CROP_STATUS)) return;

        ci.cancel();

        int status = state.get(CROP_STATUS);
        int age = this.getAge(state);

        if (status == 2) return; // Dead

        if (world.getBaseLightLevel(pos, 0) >= 9) {
            // FARMLAND SCAN: 1 down or 2 down for tall hemp
            BlockPos farmlandPos = pos.down();
            BlockState farmlandState = world.getBlockState(farmlandPos);
            if (!farmlandState.isOf(Blocks.FARMLAND)) {
                farmlandPos = pos.down(2);
                farmlandState = world.getBlockState(farmlandPos);
            }

            float multiplier = 1.0F;
            boolean isFarmland = farmlandState.isOf(Blocks.FARMLAND);
            int weedLevel = isFarmland && farmlandState.contains(WEEDS) ? farmlandState.get(WEEDS) : 0;

            // --- HEALTH TRANSITIONS ---
            if (weedLevel >= 3) {
                multiplier = 0.0F;
                if (weedLevel == 3) {
                    if (status == 0 && random.nextInt(20) == 0) {
                        world.setBlockState(pos, state.with(CROP_STATUS, 1), 2);
                    }
                } else { // Level 4
                    if (status == 0 && random.nextInt(10) == 0) {
                        world.setBlockState(pos, state.with(CROP_STATUS, 1), 2);
                    } else if (status == 1 && random.nextInt(10) == 0) {
                        world.setBlockState(pos, state.with(CROP_STATUS, 2), 2);
                        return;
                    }
                }
            } else if (weedLevel <= 1 && status == 1) {
                if (random.nextInt(15) == 0) {
                    world.setBlockState(pos, state.with(CROP_STATUS, 0), 2);
                }
            }

            // --- GROWTH LOGIC ---
            if (multiplier > 0 && age < this.getMaxAge()) {
                if (isFarmland && farmlandState.contains(FERTILIZED) && farmlandState.get(FERTILIZED)) {
                    multiplier += 0.35F;
                }

                if (weedLevel == 1) multiplier *= 0.9F;
                if (weedLevel == 2) multiplier *= 0.8F;

                // Call the shadowed moisture method
                float moisture = getAvailableMoisture((Block)(Object)this, world, pos);
                int chanceBound = (int)((25.0F / moisture) / multiplier);

                if (chanceBound <= 0 || random.nextInt(chanceBound + 1) == 0) {
                    // Update state with new Age while keeping current Crop Status
                    world.setBlockState(pos, this.withAge(age + 1).with(CROP_STATUS, status), 2);
                }
            }
        }
    }
}