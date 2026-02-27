package net.colorixer.mixin;

import net.colorixer.entity.passive.cow.CowHungerAccessor;
import net.colorixer.entity.passive.goals.AnimalDataAccessor;
import net.colorixer.util.Kickable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class CowEntityModifier implements Kickable, CowHungerAccessor {
    // COW SPECIFIC DATA
    @Unique private int ttll$kickTicks = 0;
    @Unique private int ttll$kickCooldown = 0;
    @Unique private int ttll$hunger = 4;
    @Unique private int ttll$eatAnimTicks = 0;
    @Unique private long ttll$lastBirthTime = -12001;
    @Unique private long ttll$lastMilkTime = -3601;

    // Interface Overrides (Points to variables now hosted in LivingEntityModifier via AnimalDataAccessor)
    @Override
    public void ttll$triggerPanic() {
        ((AnimalDataAccessor)this).ttll$setPanicking(true);
    }

    // COW ACCESSORS - FIXED: Returning fields instead of calling methods
    @Override public int ttll$getKickTicks() { return this.ttll$kickTicks; }
    @Override public void ttll$setKickTicks(int ticks) { this.ttll$kickTicks = ticks; }

    @Override
    public int ttll$getKickCooldown() {
        return this.ttll$kickCooldown;
    }

    @Override public void ttll$setKickCooldown(int ticks) { this.ttll$kickCooldown = ticks; }
    @Override public int ttll$getHunger() { return this.ttll$hunger; }
    @Override public void ttll$setHunger(int value) { this.ttll$hunger = value; }
    @Override public void ttll$setEatAnimTicks(int ticks) { this.ttll$eatAnimTicks = ticks; }
    @Override public long ttll$getLastBirthTime() { return this.ttll$lastBirthTime; }
    @Override public void ttll$setLastBirthTime(long time) { this.ttll$lastBirthTime = time; }
    @Override public long ttll$getLastMilkTime() { return this.ttll$lastMilkTime; }
    @Override public void ttll$setLastMilkTime(long time) { this.ttll$lastMilkTime = time; }

    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$tickTimers(CallbackInfo ci) {
        if (this.ttll$kickTicks > 0) this.ttll$kickTicks--;
        if (this.ttll$kickCooldown > 0) this.ttll$kickCooldown--;
        if (this.ttll$eatAnimTicks > 0) this.ttll$eatAnimTicks--;

        // Auto-reset enrage if not kicking/attacking via the interface
        if (((AnimalDataAccessor)this).ttll$isEnraged() && this.ttll$kickTicks <= 0) {
            ((AnimalDataAccessor)this).ttll$setEnraged(false);
        }

        if ((Object) this instanceof CowEntity cow) {
            if (!cow.getWorld().isClient) {
                // Hunger logic
                if (cow.age % 1200 == 0 && this.ttll$hunger > 0) this.ttll$hunger--;
                if (this.ttll$hunger <= 0 && cow.age % 40 == 0) {
                    cow.damage((ServerWorld) cow.getWorld(), cow.getDamageSources().starve(), 1.0f);
                }
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void ttll$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if ((Object) this instanceof CowEntity) {
            nbt.putInt("ttll_hunger", this.ttll$hunger);
            nbt.putLong("ttll_last_birth", this.ttll$lastBirthTime);
            nbt.putLong("ttll_last_milk", this.ttll$lastMilkTime);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void ttll$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if ((Object) this instanceof CowEntity) {
            this.ttll$hunger = nbt.getInt("ttll_hunger");
            this.ttll$lastBirthTime = nbt.getLong("ttll_last_birth");
            this.ttll$lastMilkTime = nbt.getLong("ttll_last_milk");
        }
    }
}