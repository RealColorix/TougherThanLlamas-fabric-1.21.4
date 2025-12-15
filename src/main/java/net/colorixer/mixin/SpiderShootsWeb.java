package net.colorixer.mixin;

import net.colorixer.access.SpiderCobwebAccessor;
import net.colorixer.entity.ModEntities;
import net.colorixer.entity.projectile.CobwebProjectileEntity;
import net.colorixer.util.GoalSelectorUtilForSpiderMixin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpiderEntity.class)
public abstract class SpiderShootsWeb {

    /* ---------------- INTERNAL STATE ---------------- */

    @Unique
    private int ttll$seeingTicks = 0;

    /* ---------------- AI ---------------- */

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void ttll$addGoals(CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity)(Object)this;

        /* ---- ALWAYS TARGET PLAYERS (DAY + NIGHT) ---- */
        GoalSelectorUtilForSpiderMixin.addTargetGoal(
                spider,
                1, // hög prio, samma som hostile mobs
                new ActiveTargetGoal<>(
                        spider,
                        PlayerEntity.class,
                        true
                )
        );

        /* ---- MELEE ATTACK ---- */
        GoalSelectorUtilForSpiderMixin.addGoal(
                spider,
                2,
                new MeleeAttackGoal(spider, 1.1D, true)
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

        // First second
        if (ttll$seeingTicks <= 20) {
            shouldShoot = serverWorld.random.nextInt(50) == 0;
        }
        // After that
        else if (ttll$seeingTicks % 20 == 0) {
            shouldShoot = serverWorld.random.nextInt(25) == 0;
        }

        if (!shouldShoot) return;

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

        serverWorld.playSound(
                null,
                spider.getX(),
                spider.getY(),
                spider.getZ(),
                SoundEvents.ENTITY_SPIDER_HURT,
                net.minecraft.sound.SoundCategory.HOSTILE,
                1.0F,
                0.9F + serverWorld.random.nextFloat() * 0.2F
        );

        serverWorld.playSound(
                null,
                spider.getX(),
                spider.getY(),
                spider.getZ(),
                SoundEvents.BLOCK_COBWEB_BREAK,
                net.minecraft.sound.SoundCategory.HOSTILE,
                1.0F,
                0.9F + serverWorld.random.nextFloat() * 0.2F
        );
    }
}
