package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.entity.projectile.ProjectileUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class AttackThroughNonSolids {

    @Shadow @Final private MinecraftClient client;

    private static final TagKey<net.minecraft.block.Block> TTLL_ATTACK_THROUGH_BLOCKS =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of("ttll", "attack_through"));

    @Inject(method = "updateCrosshairTarget(F)V", at = @At("TAIL"))
    private void ttll$attackThroughNonSolids(float tickDelta, CallbackInfo ci) {
        if (client == null || client.world == null) return;

        ClientPlayerEntity player = client.player;
        Entity camera = client.getCameraEntity();
        if (player == null || camera == null) return;

        //ItemStack weapon = player.getMainHandStack();
        //if (weapon.isEmpty() || !weapon.isIn(ItemTags.SWORDS)) return;

        if (player.isSneaking()) return;

        HitResult current = client.crosshairTarget;
        if (!(current instanceof BlockHitResult blockHit)) return;
        if (blockHit.getType() != HitResult.Type.BLOCK) return;

        BlockState state = client.world.getBlockState(blockHit.getBlockPos());
        if (!isAttackThroughBlock(state)) return;

        double reach = player.getEntityInteractionRange();

        Vec3d start = camera.getCameraPosVec(tickDelta);
        Vec3d direction = camera.getRotationVec(tickDelta);
        Vec3d end = start.add(direction.multiply(reach));

        double maxEntityDistance = distanceToFirstSolid(camera, start, end);

        EntityHitResult entityHit = raycastEntity(camera, start, end, maxEntityDistance);
        if (entityHit != null) {
            client.crosshairTarget = entityHit;
        }
    }

    private boolean isAttackThroughBlock(BlockState state) {
        if (state.isIn(BlockTags.LEAVES)) return true;
        return state.isIn(TTLL_ATTACK_THROUGH_BLOCKS);
    }

    private double distanceToFirstSolid(Entity camera, Vec3d start, Vec3d end) {
        Vec3d direction = end.subtract(start);
        double length = direction.length();
        if (length < 0.001) return 0.0;

        Vec3d stepDir = direction.normalize();
        Vec3d rayStart = start;

        for (int i = 0; i < 32; i++) {
            HitResult hit = client.world.raycast(new RaycastContext(
                    rayStart,
                    end,
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE,
                    camera
            ));

            if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) {
                return length;
            }

            BlockState state = client.world.getBlockState(bhr.getBlockPos());
            if (!isAttackThroughBlock(state)) {
                return start.distanceTo(bhr.getPos());
            }

            rayStart = bhr.getPos().add(stepDir.multiply(0.05));
        }

        return length;
    }

    @Nullable
    private EntityHitResult raycastEntity(Entity camera, Vec3d start, Vec3d end, double maxDistance) {
        if (maxDistance <= 0.001) return null;

        Vec3d dir = end.subtract(start);
        double length = Math.min(dir.length(), maxDistance);
        Vec3d clampedEnd = start.add(dir.normalize().multiply(length));

        Box box = camera.getBoundingBox()
                .stretch(dir.normalize().multiply(length))
                .expand(1.0);

        return ProjectileUtil.raycast(
                camera,
                start,
                clampedEnd,
                box,
                e -> e instanceof LivingEntity && e.isAttackable() && !e.isSpectator(),
                length * length
        );
    }
}
