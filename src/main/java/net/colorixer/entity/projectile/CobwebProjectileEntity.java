package net.colorixer.entity.projectile;

import net.colorixer.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags; // --- NEW IMPORT REQUIRED FOR WATER CHECK ---
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class CobwebProjectileEntity extends SnowballEntity {

    public CobwebProjectileEntity(EntityType<? extends SnowballEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isTouchingWater()) {
            this.dissolveInWater();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hitResult) {
        super.onEntityHit(hitResult);

        Entity hit = hitResult.getEntity();

        // Only mobs have AI & aggro
        if (hit instanceof net.minecraft.entity.mob.MobEntity mob) {
            // Prevent friendly-fire aggro
            mob.setAttacker(null);
            mob.setTarget(null);
        }
    }

    /* ---------------- COLLISION ---------------- */

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            this.discard();
            return;
        }

        if (!serverWorld.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            this.discard();
            return;
        }

        BlockPos placePos = null;

        /* ---- ENTITY HIT: PLACE AT ENTITY FEET ---- */
        if (hitResult instanceof EntityHitResult ehr) {
            Entity entity = ehr.getEntity();

            if (entity.isTouchingWater()) {
                this.dissolveInWater();
                return;
            }

            if (entity instanceof LivingEntity living) {
                placePos = living.getBlockPos();
            }
        }

        /* ---- BLOCK HIT: PLACE NEAR IMPACT ---- */
        else if (hitResult instanceof BlockHitResult bhr) {
            placePos = bhr.getBlockPos().offset(bhr.getSide());

            // --- FIXED: Using FluidTags instead of isWater() ---
            if (serverWorld.getBlockState(placePos).getFluidState().isIn(FluidTags.WATER)) {
                this.dissolveInWater();
                return;
            }
        }

        boolean placed = false;

        if (placePos != null) {
            BlockPos grounded = findGroundedPos(serverWorld, placePos);
            if (grounded != null) {

                // --- FIXED: Using FluidTags instead of isWater() ---
                if (serverWorld.getBlockState(grounded).getFluidState().isIn(FluidTags.WATER)) {
                    this.dissolveInWater();
                    return;
                }

                placed = placeCobwebReplacing(serverWorld, grounded);
            }
        }

        // ❗ FALLBACK: DROP ITEM IF NO COBWEB WAS PLACED
        if (!placed) {
            dropTangledWeb(serverWorld);
        }

        this.discard();
    }

    /* ---------------- RENDERED ITEM ---------------- */

    @Override
    public ItemStack getStack() {
        return new ItemStack(ModItems.TANGLED_WEB);
    }

    /* ---------------- PLACE LOGIC ---------------- */

    private boolean placeCobwebReplacing(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.isOf(Blocks.SNOW)) {
            int layers = state.get(SnowBlock.LAYERS);

            if (layers > 3) {
                return false;
            }

            world.breakBlock(pos, true);
            world.setBlockState(pos, Blocks.COBWEB.getDefaultState());
            return true;
        }

        if (!state.isAir() && !state.isReplaceable()) {
            return false;
        }

        if (!state.isAir()) {
            world.breakBlock(pos, true);
        }

        world.setBlockState(pos, Blocks.COBWEB.getDefaultState());
        return true;
    }

    /* ---------------- EFFECTS & FALLBACKS ---------------- */

    private void dissolveInWater() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 10, 0.2, 0.2, 0.2, 0.0);
            serverWorld.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.PLAYERS, 0.5f, 1.5f);
        }
        this.discard();
    }

    private void dropTangledWeb(ServerWorld world) {
        ItemEntity item = new ItemEntity(
                world,
                this.getX(),
                this.getY(),
                this.getZ(),
                new ItemStack(ModItems.TANGLED_WEB)
        );
        world.spawnEntity(item);
    }

    /* ---------------- GROUND SNAP ---------------- */

    private BlockPos findGroundedPos(ServerWorld world, BlockPos start) {
        BlockPos pos = start;

        while (pos.getY() > world.getBottomY() + 1) {
            if (world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
                break;
            }
            pos = pos.down();
        }

        return pos;
    }
}