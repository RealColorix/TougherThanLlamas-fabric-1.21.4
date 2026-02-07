package net.colorixer.sounds;

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
        // This method can be empty, we just call it in our main Mod Initializer
        // to ensure the static fields (the sounds) are loaded.
    }
}