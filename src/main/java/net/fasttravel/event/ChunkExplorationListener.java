package net.fasttravel.event;

import net.fasttravel.config.FastTravelConfig;
import net.fasttravel.map.MapColorGenerator;
import net.fasttravel.network.FastTravelServerPacket;
import net.fasttravel.state.PlayerExplorationState;
import net.fasttravel.state.RegionMapState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

public class ChunkExplorationListener {

    public static void onPlayerEnterChunk(ServerPlayerEntity player, ServerWorld world, ChunkPos newChunk) {
        PlayerExplorationState explorationState = PlayerExplorationState.get(world);
        RegionMapState regionMapState = RegionMapState.get(world);

        int radius = FastTravelConfig.CONFIG.instance().chunkExplorationRadius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;

                ChunkPos chunk = new ChunkPos(newChunk.x + dx, newChunk.z + dz);
                explorationState.addChunk(player.getUuid(), chunk);

                if (!regionMapState.isChunkProcessed(chunk)) {
                    int[] pixels = MapColorGenerator.generateChunkColors(world, chunk);
                    if (pixels != null) {
                        regionMapState.writeChunkPixels(chunk, pixels);
                        FastTravelServerPacket.sendMapChunkPacket(player, chunk, pixels);
                    }
                } else {
                    int[] pixels = regionMapState.getChunkPixels(chunk);
                    if (pixels != null) {
                        FastTravelServerPacket.sendMapChunkPacket(player, chunk, pixels);
                    }
                }
            }
        }
    }
}