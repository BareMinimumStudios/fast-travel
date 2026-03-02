package net.fasttravel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fasttravel.init.EventInit;
import net.fasttravel.init.RenderInit;
import net.fasttravel.network.FastTravelClientPacket;

@Environment(EnvType.CLIENT)
public class FastTravelClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FastTravelClientPacket.init();
        RenderInit.init();
        EventInit.clientInit();
    }
}
