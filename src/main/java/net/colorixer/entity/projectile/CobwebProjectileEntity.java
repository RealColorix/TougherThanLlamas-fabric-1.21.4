package net.colorixer.entity.projectile;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
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

            if (entity instanceof LivingEntity living) {
                placePos = living.getBlockPos();
            }
        }

        /* ---- BLOCK HIT: PLACE NEAR IMPACT ---- */
        else if (hitResult instanceof BlockHitResult bhr) {
            placePos = bhr.getBlockPos().offset(bhr.getSide());
        }

        if (placePos != null) {
            BlockPos grounded = findGroundedPos(serverWorld, placePos);
            if (grounded != null) {
                placeCobwebReplacing(serverWorld, grounded);
            }
        }

        this.discard();
    }

    /* ---------------- RENDERED ITEM ---------------- */

    @Override
    public ItemStack getStack() {
        return new ItemStack(Items.COBWEB);
    }

    /* ---------------- PLACE LOGIC ---------------- */

    private void placeCobwebReplacing(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        // Only replace air or replaceable blocks (grass, flowers, snow, etc)
        if (!state.isAir() && !state.isReplaceable()) {
            return;
        }

        // Break existing replaceable block properly (drops items)
        if (!state.isAir()) {
            world.breakBlock(pos, true);
        }

        world.setBlockState(pos, Blocks.COBWEB.getDefaultState());
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
