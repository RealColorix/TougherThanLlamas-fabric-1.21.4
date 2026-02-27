package net.colorixer.mixin;

import net.colorixer.entity.passive.goals.AnimalDataAccessor;
import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin implements AnimalDataAccessor {
    @Unique private boolean ttll$isBlockScared = false;
    @Unique private boolean ttll$isPanicking = false;
    @Unique private boolean ttll$isEnraged = false;

    // --- Block Scared Logic ---
    @Override
    public boolean ttll$isBlockScared() {
        return this.ttll$isBlockScared;
    }

    @Override
    public void ttll$setBlockScared(boolean value) {
        this.ttll$isBlockScared = value;
    }

    // --- Panic Logic ---
    @Override
    public boolean ttll$isPanicking() {
        return this.ttll$isPanicking;
    }

    @Override
    public void ttll$setPanicking(boolean value) {
        this.ttll$isPanicking = value;
    }

    // --- Enraged Logic ---
    @Override
    public boolean ttll$isEnraged() {
        return this.ttll$isEnraged;
    }

    @Override
    public void ttll$setEnraged(boolean value) {
        this.ttll$isEnraged = value;
    }
}