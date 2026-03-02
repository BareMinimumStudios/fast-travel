package net.fasttravel.init;

import net.fasttravel.FastTravelMain;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public class SoundInit {

    public static SoundEvent TELEPORTING = register("teleporting");
    public static SoundEvent AFTER_TELEPORTING = register("after_teleporting");
    public static SoundEvent DISCOVERY = register("discovery");

    private static SoundEvent register(String id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(FastTravelMain.identifierOf(id)));
    }

    public static void init() {

    }
}
