package net.fasttravel.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fasttravel.FastTravelMain;
import net.fasttravel.accessor.PlayerEntityAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RenderInit {

    private static final Identifier TELEPORT_BARS_TEXTURE = FastTravelMain.identifierOf("textures/gui/teleport_bars.png");

    public static void init() {
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.TELEPORTER, RenderLayer.getCutout());

        ParticleFactoryRegistry.getInstance().register(ParticleInit.TELEPORT_PARTICLE, ParticleInit.TeleportParticle.TeleportFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleInit.AFTER_TELEPORT_PARTICLE, ParticleInit.TeleportParticle.AfterTeleportFactory::new);

        HudRenderCallback.EVENT.register((context, value) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player != null) {
                        if (((PlayerEntityAccess) client.player).getTeleportTick() > 0) {
                            int teleportTime = (int) (Math.abs((((PlayerEntityAccess) client.player).getTeleportTick() / 100f -1f)) * 182.0f);

                            context.getMatrices().push();
                            context.getMatrices().translate(0.0f, 0.0f, 51.0f);
                            context.drawTexture(TELEPORT_BARS_TEXTURE, context.getScaledWindowWidth() / 2 - 91, context.getScaledWindowHeight() - 57, 0, 0, 182, 5);
                            if (teleportTime > 0) {
                                context.drawTexture(TELEPORT_BARS_TEXTURE, context.getScaledWindowWidth() / 2 - 91, context.getScaledWindowHeight() - 57, 0, 5, teleportTime, 5);
                            }
                            context.getMatrices().pop();
                        }
                    }
                }
        );
    }
}
