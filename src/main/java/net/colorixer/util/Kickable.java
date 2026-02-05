package net.colorixer.util;

public interface Kickable {
    void ttll$requestKick();
    int ttll$getKickTicks();
    void ttll$setKickTicks(int ticks);
    int ttll$getKickCooldown();
    void ttll$setKickCooldown(int ticks);
    // New: Track rage state
    boolean ttll$isEnraged();
    void ttll$setEnraged(boolean enraged);
}