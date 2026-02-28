package net.colorixer.mixin;

import net.colorixer.entity.ModEntities;
import net.colorixer.entity.hostile.creeper.ProximityExplosionGoal;
import net.colorixer.entity.hostile.creeper.firecreeper.FireCreeperEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreeperEntity.class)
public abstract class CreeperModifier
        extends HostileEntity
        implements net.colorixer.access.CreeperStateAccessor {

    @Shadow private int currentFuseTime;
    @Shadow private int fuseTime;
    @Shadow private int explosionRadius;
    @Shadow public abstract boolean isCharged();
    @Shadow public abstract int getFuseSpeed();
    @Shadow public abstract void setFuseSpeed(int fuseSpeed);

    @Unique
    private static final TrackedData<Boolean> SHEARED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    protected CreeperModifier(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean ttll$isSheared() {
        return this.dataTracker.get(SHEARED);
    }

    @Override
    public void ttll$setSheared(boolean sheared) {
        this.dataTracker.set(SHEARED, sheared);
    }

    // --- 1. STOP IGNITION GOAL & TARGETING ---
    @Inject(method = "tick", at = @At("HEAD"))
    private void stopFuseIfSheared(CallbackInfo ci) {
        if (this.ttll$isSheared()) {
            this.setFuseSpeed(-1);
            this.currentFuseTime = 0;
            // Stop being angry at the player once defused
            if (this.getTarget() instanceof PlayerEntity) {
                this.setTarget(null);
            }
        }
    }

    // --- 2. PREVENT FLINT AND STEEL ---
    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void preventIgnition(PlayerEntity player, net.minecraft.util.Hand hand, CallbackInfoReturnable<net.minecraft.util.ActionResult> cir) {
        if (this.ttll$isSheared() && player.getStackInHand(hand).isOf(Items.FLINT_AND_STEEL)) {
            cir.setReturnValue(net.minecraft.util.ActionResult.PASS);
        }
    }

    @Inject(method = "initGoals", at = @At("HEAD"), cancellable = true)
    private void ttll$rebuildCreeperAI(CallbackInfo ci) {
        CreeperEntity creeper = (CreeperEntity) (Object) this;
        ci.cancel();

        this.goalSelector.add(1, new net.minecraft.entity.ai.goal.SwimGoal(creeper));


        this.goalSelector.add(2, new net.minecraft.entity.ai.goal.FleeEntityGoal<>(creeper, PlayerEntity.class, 16.0F, 0.8, 1.2, (entity) -> this.ttll$isSheared()));
        this.goalSelector.add(3, new net.minecraft.entity.ai.goal.CreeperIgniteGoal(creeper));
        this.goalSelector.add(4, new ProximityExplosionGoal(creeper));
        this.goalSelector.add(5, new net.minecraft.entity.ai.goal.FleeEntityGoal<>(creeper, net.minecraft.entity.passive.OcelotEntity.class, 6.0F, 1.0, 1.2));
        this.goalSelector.add(5, new net.minecraft.entity.ai.goal.FleeEntityGoal<>(creeper, net.minecraft.entity.passive.CatEntity.class, 6.0F, 1.0, 1.2));

        // Only attack if NOT sheared
        this.goalSelector.add(6, new net.minecraft.entity.ai.goal.MeleeAttackGoal(creeper, 1.0, false) {
            @Override public boolean canStart() { return super.canStart() && !ttll$isSheared(); }});
        this.goalSelector.add(7, new net.minecraft.entity.ai.goal.WanderAroundFarGoal(creeper, 0.8));
        this.targetSelector.add(1, new net.minecraft.entity.ai.goal.ActiveTargetGoal<>(creeper, PlayerEntity.class, false) {
            @Override public boolean canStart() { return super.canStart() && !ttll$isSheared(); }
        });
        this.targetSelector.add(2, new net.minecraft.entity.ai.goal.RevengeGoal(creeper));
    }

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void handleHeadExplosion(CallbackInfo ci) {
        if (this.ttll$isSheared()) {
            ci.cancel();
            return;
        }

        World world = this.getWorld();
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            float power = (float)this.explosionRadius * (this.isCharged() ? 2.0F : 1.0F);
            serverWorld.createExplosion(this, this.getX(), this.getEyeY(), this.getZ(), power, World.ExplosionSourceType.MOB);

            if ((Object)this instanceof FireCreeperEntity fireCreeper) {
                fireCreeper.spawnFireFountain(serverWorld);
            }
            this.explosionRadius = 0;
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setFuseTimes(EntityType<? extends CreeperEntity> type, World world, CallbackInfo ci) {
        if (type == ModEntities.FIRE_CREEPER) {  // Use your registered EntityType
            this.fuseTime = 21;
            this.currentFuseTime = 0;
        } else {
            this.fuseTime = 25;
            this.currentFuseTime = 0;
        }
    }




    @Inject(method = "tick", at = @At("HEAD"))
    private void lungeAndFuse(CallbackInfo ci) {
        CreeperEntity creeper = (CreeperEntity) (Object) this;
        World world = creeper.getWorld();

        // Feature: Smell/Follow logic (Only if NOT sheared)
        if (!world.isClient && !this.ttll$isSheared()) {
            PlayerEntity nearestPlayer = world.getClosestPlayer(creeper.getX(), creeper.getY(), creeper.getZ(), 10.0, true);
            if (nearestPlayer != null) {
                creeper.setTarget(nearestPlayer);
                creeper.getLookControl().lookAt(nearestPlayer, 30.0F, 30.0F);
            }
        }

        if (!this.ttll$isSheared() && this.getFuseSpeed() > 0 && this.getTarget() != null && this.isOnGround()) {
            int jumpFuseTime;

            // Set fuse tick for jump based on entity type
            if (((Object)this) instanceof FireCreeperEntity) {
                jumpFuseTime = 14;
            } else {
                jumpFuseTime = 18;
            }

            if (this.currentFuseTime == jumpFuseTime) {
                LivingEntity target = this.getTarget();
                if (target != null) {
                    double distance = Math.sqrt(this.squaredDistanceTo(target)); // distance in blocks
                    Vec3d dir = new Vec3d(target.getX() - this.getX(), 0, target.getZ() - this.getZ()).normalize();

                    // Scale jump with distance: (distance / 3) * current horizontal power, capped at 3
                    double horizontalPower = Math.min(2.0, (distance / 4.0) * 0.7); // 1.0 is base jump power, adjust as needed

                    // Vertical jump component can scale with distance too, optional
                    double vertical = 0.25;
                    if (distance < 1.0) vertical = 0.1;

                    this.addVelocity(dir.x * horizontalPower, vertical, dir.z * horizontalPower);
                    this.velocityDirty = true;
                }
            }
        }


    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initShearTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(SHEARED, false);
    }

    @Override
    public net.minecraft.util.ActionResult interactMob(PlayerEntity player, net.minecraft.util.Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.isOf(Items.SHEARS) && !this.ttll$isSheared()) {
            boolean isFireCreeper = (Object)this instanceof FireCreeperEntity;

            if (!this.getWorld().isClient) {
                float chance = this.random.nextFloat();
                float threshold = isFireCreeper ? 0.33F : 0.10F;

                if (chance < threshold) {
                    ((CreeperEntity)(Object)this).ignite();
                    this.currentFuseTime = this.fuseTime;
                    return net.minecraft.util.ActionResult.SUCCESS_SERVER;
                }

                this.ttll$setSheared(true);
                this.setFuseSpeed(-1);
                this.currentFuseTime = 0;
                this.setTarget(null); // Stop attacking immediately

                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sound.SoundEvents.ENTITY_SHEEP_SHEAR, this.getSoundCategory(), 1.0F, 1.0F);

                String id = isFireCreeper ? "fire_creeper_sack" : "creeper_sack";
                Item sackItem = Registries.ITEM.get(Identifier.of("ttll", id));

                if (sackItem != Items.AIR) {
                    ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), new ItemStack(sackItem));
                    this.getWorld().spawnEntity(itemEntity);
                }

                if (!player.getAbilities().creativeMode) {
                    stack.damage(1, player, hand == net.minecraft.util.Hand.MAIN_HAND ?
                            net.minecraft.entity.EquipmentSlot.MAINHAND : net.minecraft.entity.EquipmentSlot.OFFHAND);
                }

                if (this.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.getChunkManager().markForUpdate(this.getBlockPos());
                }
                return net.minecraft.util.ActionResult.SUCCESS_SERVER;
            }
            return net.minecraft.util.ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeShearedNbt(net.minecraft.nbt.NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("IsSheared", this.ttll$isSheared());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readShearedNbt(net.minecraft.nbt.NbtCompound nbt, CallbackInfo ci) {
        this.ttll$setSheared(nbt.getBoolean("IsSheared"));
    }
}