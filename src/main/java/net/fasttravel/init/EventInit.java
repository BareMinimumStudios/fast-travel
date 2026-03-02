package net.fasttravel.init;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fasttravel.map.ClientMapStorage;
import net.fasttravel.network.FastTravelServerPacket;
import net.fasttravel.state.PlayerExplorationState;
import net.fasttravel.state.RegionMapState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import java.util.Set;

public class EventInit {

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            ServerWorld world = player.getServerWorld();

            PlayerExplorationState explorationState = PlayerExplorationState.get(world);
            RegionMapState regionMapState = RegionMapState.get(world);

            Set<Long> exploredChunks = explorationState.getExploredChunks(player.getUuid());
            for (long chunkLong : exploredChunks) {
                ChunkPos chunkPos = new ChunkPos(chunkLong);
                int[] pixels = regionMapState.getChunkPixels(chunkPos);
                if (pixels != null) {
                    FastTravelServerPacket.sendMapChunkPacket(player, chunkPos, pixels);
                }
            }
        });
    }

    public static void clientInit() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientMapStorage.clearAll();
        });
    }
}
