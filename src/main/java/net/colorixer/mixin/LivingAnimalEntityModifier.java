package net.colorixer.mixin;

import net.colorixer.entity.passive.goals.AnimalDataAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingAnimalEntityModifier extends Entity implements AnimalDataAccessor {

    @Unique private boolean ttll$panicking = false;
    @Unique private boolean ttll$enraged = false;
    @Unique private boolean ttll$blockScared = false;

    public LivingAnimalEntityModifier(EntityType<?> type, World world) {
        super(type, world);
    }

    // --- Interface Overrides ---
    @Override public boolean ttll$isPanicking() { return this.ttll$panicking; }
    @Override public void ttll$setPanicking(boolean value) { this.ttll$panicking = value; }
    @Override public boolean ttll$isEnraged() { return this.ttll$enraged; }
    @Override public void ttll$setEnraged(boolean value) { this.ttll$enraged = value; }
    @Override public boolean ttll$isBlockScared() { return this.ttll$blockScared; }
    @Override public void ttll$setBlockScared(boolean value) { this.ttll$blockScared = value; }

    @Inject(method = "damage", at = @At("TAIL"))
    private void ttll$onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        EntityType<?> type = ((Entity)(Object)this).getType();
        if (type != EntityType.COW && type != EntityType.PIG && type != EntityType.SHEEP) {
            return;
        }

        if (source.getAttacker() instanceof LivingEntity attacker) {
            Box box = this.getBoundingBox().expand(16.0);

            // FIX: Route through a raw (Class) cast to bypass the IDE's inconvertible types error
            @SuppressWarnings({"unchecked", "rawtypes"})
            Class<? extends AnimalEntity> entityClass = (Class<? extends AnimalEntity>) (Class) this.getClass();

            // Find nearby adults of the same species
            List<? extends AnimalEntity> nearby = world.getEntitiesByClass(
                    entityClass,
                    box,
                    a -> !a.isBaby()
            );

            for (AnimalEntity animal : nearby) {
                AnimalDataAccessor accessor = (AnimalDataAccessor) animal;

                // Access the goal selector for EACH animal in the herd
                var selector = ((MobEntityAccessor) animal).ttll$getGoalSelector();

                // Check if THIS specific animal in the loop has the stampede goal
                boolean hasStampedeGoal = selector.getGoals().stream()
                        .anyMatch(g -> g.getGoal() instanceof net.colorixer.entity.passive.goals.AnimalStampedeAttackGoal);

                if (hasStampedeGoal && animal.getHealth() >= (animal.getMaxHealth() / 2.0f)) {
                    accessor.ttll$setEnraged(true);
                    accessor.ttll$setPanicking(false); // Stop them from fleeing
                    animal.setTarget(attacker);
                } else {
                    // If they don't have the goal (or are too hurt), they just panic
                    accessor.ttll$setEnraged(false);
                    accessor.ttll$setPanicking(true);
                    animal.setTarget(null);
                }
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void ttll$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if ((Object) this instanceof AnimalEntity) {
            nbt.putBoolean("ttll_panicking", this.ttll$panicking);
            nbt.putBoolean("ttll_enraged", this.ttll$enraged);
            nbt.putBoolean("ttll_block_scared", this.ttll$blockScared);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void ttll$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if ((Object) this instanceof AnimalEntity) {
            this.ttll$panicking = nbt.getBoolean("ttll_panicking");
            this.ttll$enraged = nbt.getBoolean("ttll_enraged");
            this.ttll$blockScared = nbt.getBoolean("ttll_block_scared");
        }
    }
}