package net.colorixer.mixin;

import net.colorixer.access.SpiderCobwebAccessor;
import net.colorixer.entity.ModEntities;
import net.colorixer.entity.projectile.CobwebProjectileEntity;
import net.colorixer.util.GoalSelectorUtilForSpiderMixin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpiderEntity.class)
public abstract class SpiderMixin {

    /* ---------------- INTERNAL STATE ---------------- */

    @Unique
    private int ttll$seeingTicks = 0;

    /* ---------------- MELEE AI ---------------- */

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void ttll$addMeleeGoal(CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity)(Object)this;

        GoalSelectorUtilForSpiderMixin.addGoal(
                spider,
                2,
                new MeleeAttackGoal(spider, 1.2D, true)
        );
    }

    /* ---------------- PARALLEL SHOOTING ---------------- */

    @Inject(method = "tick", at = @At("TAIL"))
    private void ttll$tryShootCobweb(CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity)(Object)this;
        World world = spider.getWorld();

        if (!(world instanceof ServerWorld serverWorld)) return;

        SpiderCobwebAccessor accessor = (SpiderCobwebAccessor) spider;

        // Already shot → never shoot again
        if (accessor.ttll$hasShotCobweb()) return;

        LivingEntity target = spider.getTarget();
        if (target == null || !target.isAlive()) {
            ttll$seeingTicks = 0;
            return;
        }

        if (!spider.getVisibilityCache().canSee(target)) {
            ttll$seeingTicks = 0;
            return;
        }

        ttll$seeingTicks++;

        boolean shouldShoot = false;

        // First second: ~33% chance
        if (ttll$seeingTicks <= 20) {
            shouldShoot = serverWorld.random.nextInt(30) == 0;
        }
        // After that: 1/25 chance per second
        else if (ttll$seeingTicks % 20 == 0) {
            shouldShoot = serverWorld.random.nextInt(30) == 0;
        }

        if (!shouldShoot) return;

        // Mark as used (THIS is the persisted flag)
        accessor.ttll$setHasShotCobweb(true);

        /* -------- FIRE PROJECTILE -------- */

        double dx = target.getX() - spider.getX();
        double dy = target.getY() + 1.4D - spider.getEyeY();
        double dz = target.getZ() - spider.getZ();

        CobwebProjectileEntity projectile =
                new CobwebProjectileEntity(ModEntities.COBWEB_PROJECTILE, serverWorld);

        projectile.setOwner(spider);
        projectile.setPosition(
                spider.getX(),
                spider.getEyeY() - 0.1D,
                spider.getZ()
        );

        projectile.setVelocity(dx, dy, dz, 1.6F, 0.0F);

        serverWorld.spawnEntity(projectile);
    }
}
