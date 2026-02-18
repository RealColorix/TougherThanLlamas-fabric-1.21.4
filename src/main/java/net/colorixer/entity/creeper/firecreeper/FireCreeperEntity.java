package net.colorixer.entity.creeper.firecreeper;

import net.colorixer.access.CreeperStateAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireCreeperEntity extends CreeperEntity {

    public FireCreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }

    // You NO LONGER need getShearedKey() or the ttll$ method overrides here.
    // The Mixin applies them to the base CreeperEntity class, so FireCreeper inherits them!

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource damageSource) {
        return damageSource.isIn(DamageTypeTags.IS_FIRE) || super.isInvulnerableTo(world, damageSource);
    }

    public void spawnFireFountain(ServerWorld serverWorld) {
        // Ignite nearby entities
        var entities = serverWorld.getOtherEntities(this, this.getBoundingBox().expand(2.0));
        for (var entity : entities) {
            if (entity instanceof LivingEntity living) {
                float randomFireSeconds = this.random.nextFloat() * 3.0F + 4.0F;
                living.setOnFireFor(randomFireSeconds);
            }
        }

        Vec3d creeperVel = this.getVelocity();

        for (int i = 0; i < 30; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;

            FallingBlockEntity fire = FallingBlockEntity.spawnFromBlock(
                    serverWorld,
                    this.getBlockPos().up(),
                    Blocks.FIRE.getDefaultState()
            );

            if (fire != null) {
                fire.refreshPositionAfterTeleport(this.getX() + offsetX, this.getY() + 1.0, this.getZ() + offsetZ);

                double vx = ((this.random.nextDouble() - 0.5) * 0.9) + creeperVel.x;
                double vz = ((this.random.nextDouble() - 0.5) * 0.9) + creeperVel.z;
                double vy = (0.2 + this.random.nextDouble() * 0.5) + Math.max(0, creeperVel.y);

                fire.setVelocity(vx, vy, vz);
                fire.velocityDirty = true;
                fire.timeFalling = 1;
                fire.dropItem = false;
                fire.addCommandTag("creeper_fire");
            }
        }
    }


}