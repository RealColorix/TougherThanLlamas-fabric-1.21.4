package net.colorixer.entity.passive.goals;

public interface AnimalDataAccessor {

    // Block Scare (The one you already had)
    boolean ttll$isBlockScared();
    void ttll$setBlockScared(boolean value);

    // Panic (The one the error is complaining about)
    boolean ttll$isPanicking();
    void ttll$setPanicking(boolean value);

    // Enraged (For the stampede/attack logic)
    boolean ttll$isEnraged();
    void ttll$setEnraged(boolean value);
}