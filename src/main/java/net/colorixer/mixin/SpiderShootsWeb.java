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
import net.minecraft.entity.passive.ChickenEntity;
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

    @Unique
    private int ttll$seeingTicks = 0;

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void ttll$addGoals(CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity)(Object)this;

        // 1. Target Players (Light sensitive + Revenge)
        GoalSelectorUtilForSpiderMixin.addTargetGoal(
                spider,
                1,
                new SpiderTargetGoal<>(spider, PlayerEntity.class)
        );

        // 2. NEW: Target Chickens (Within 12 blocks)
        // We use a custom predicate/logic to limit the range to 12.0 blocks
        // 2. Target Chickens (Within 12 blocks)
        GoalSelectorUtilForSpiderMixin.addTargetGoal(
                spider,
                3,
                new ActiveTargetGoal<>(spider, ChickenEntity.class, false) {
                    @Override
                    public boolean canStart() {
                        // super.canStart() finds the target and puts it in this.targetEntity
                        if (super.canStart() && this.targetEntity != null) {
                            return spider.squaredDistanceTo(this.targetEntity) <= 100; // 10 blocks squared
                        }
                        return false;
                    }
                }
        );

        // 3. Melee Attack Goal (Works for both Players and Chickens)
        GoalSelectorUtilForSpiderMixin.addGoal(
                spider,
                2,
                new MeleeAttackGoal(spider, 1.1D, true)
        );
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void ttll$tryShootCobweb(CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity)(Object)this;

        LivingEntity target = spider.getTarget();

        // FORGIVENESS: Reset if target died
        if (target != null && !target.isAlive()) {
            spider.setAttacker(null);
            spider.setTarget(null);
            return;
        }

        // AMBIENT LIGHT CHECK:
        // Only ignore shooting if there's NO target and it's bright.
        // If it has a target (Player or Chicken), it SHOULD shoot.
        if (spider.getTarget() == null && spider.getBrightnessAtEyes() >= 0.5F) {
            return;
        }

        World world = spider.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) return;

        SpiderCobwebAccessor accessor = (SpiderCobwebAccessor) spider;
        if (accessor.ttll$hasShotCobweb()) return;

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

        if (ttll$seeingTicks <= 20) {
            shouldShoot = serverWorld.random.nextInt(50) == 0;
        } else if (ttll$seeingTicks % 20 == 0) {
            shouldShoot = serverWorld.random.nextInt(25) == 0;
        }

        if (!shouldShoot) return;

        accessor.ttll$setHasShotCobweb(true);

        /* -------- ARC COMPENSATION AIMING -------- */

        double dx = target.getX() - spider.getX();
        double dz = target.getZ() - spider.getZ();

// Calculate horizontal distance (how far away they are on the ground)
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

// ARC COMPENSATION:
// target.getBodyY(0.5D) targets the center of the entity.
// (horizontalDistance * 0.12D) raises the aim to compensate for gravity drop.
        double dy = target.getBodyY(0.5D) - spider.getEyeY() + (horizontalDistance * 0.12D);

        CobwebProjectileEntity projectile = new CobwebProjectileEntity(ModEntities.COBWEB_PROJECTILE, serverWorld);
        projectile.setOwner(spider);
        projectile.setPosition(spider.getX(), spider.getEyeY() - 0.1D, spider.getZ());

// 1.6F is speed. 0.0F is divergence (0.0 = 100% accuracy, no randomness)
        projectile.setVelocity(dx, dy, dz, 1.6F, 0.0F);
        serverWorld.spawnEntity(projectile);

        serverWorld.playSound(null, spider.getX(), spider.getY(), spider.getZ(), SoundEvents.ENTITY_SPIDER_HURT, net.minecraft.sound.SoundCategory.HOSTILE, 1.0F, 0.9F + serverWorld.random.nextFloat() * 0.2F);
        serverWorld.playSound(null, spider.getX(), spider.getY(), spider.getZ(), SoundEvents.BLOCK_COBWEB_BREAK, net.minecraft.sound.SoundCategory.HOSTILE, 1.0F, 0.9F + serverWorld.random.nextFloat() * 0.2F);
    }

    /* ---------------- CUSTOM GOAL CLASS ---------------- */

    @Unique
    private static class SpiderTargetGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
        private final SpiderEntity spider;

        public SpiderTargetGoal(SpiderEntity spider, Class<T> targetClass) {
            super(spider, targetClass, true);
            this.spider = spider;
        }

        @Override
        public boolean canStart() {
            if (this.spider.getAttacker() != null && !this.spider.getAttacker().isAlive()) {
                this.spider.setAttacker(null);
            }
            float brightness = this.spider.getBrightnessAtEyes();
            return (brightness < 0.5F || this.spider.getAttacker() != null) && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity target = this.spider.getTarget();
            if (target != null && !target.isAlive()) {
                this.spider.setAttacker(null);
                return false;
            }
            return super.shouldContinue();
        }
    }
}