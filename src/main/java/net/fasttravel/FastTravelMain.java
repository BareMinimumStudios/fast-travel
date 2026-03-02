package net.fasttravel;

import net.fabricmc.api.ModInitializer;

import net.fasttravel.config.FastTravelConfig;
import net.fasttravel.init.*;
import net.fasttravel.network.FastTravelServerPacket;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastTravelMain implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("fasttravel");

    @Override
    public void onInitialize() {
        FastTravelConfig.load();
        ItemInit.init();
        BlockInit.init();
        EventInit.init();
        SoundInit.init();
        FastTravelServerPacket.init();
        ParticleInit.init();
    }

    public static Identifier identifierOf(String name) {
        return Identifier.of("fasttravel", name);
    }
}