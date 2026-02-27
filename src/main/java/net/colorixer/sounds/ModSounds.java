package net.colorixer.sounds;

import net.colorixer.TougherThanLlamas;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    // This "entity.classic_hurt" must match the name you put in sounds.json
    public static final SoundEvent CLASSIC_HURT = registerSoundEvent("classic_hurt");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of("ttll", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        TougherThanLlamas.LOGGER.info("Registering Sound Effects for " + TougherThanLlamas.MOD_ID);
    }
}