package net.colorixer.entity.projectile;

import net.colorixer.entity.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class BoneProjectile extends ThrownItemEntity {

    public BoneProjectile(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public BoneProjectile(World world, LivingEntity owner) {
        // FIX 1: Use the basic constructor and set the owner manually
        super(ModEntities.BONE_PROJECTILE, world);
        this.setOwner(owner);
        this.setPosition(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    @Override
    protected Item getDefaultItem() {
        return Items.BONE;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();

        // FIX 2: 1.21 damage() requires (ServerWorld, DamageSource, Amount)
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            float damageAmount = 1.5f;
            entity.damage(serverWorld, this.getDamageSources().thrown(this, this.getOwner()), damageAmount);
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // Shatter particles
            serverWorld.spawnParticles(
                    new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.BONE)),
                    this.getX(), this.getY(), this.getZ(),
                    8, 0.1, 0.1, 0.1, 0.05
            );
            this.discard();
        }
    }
}