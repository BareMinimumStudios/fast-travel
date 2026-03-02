package net.fasttravel.map;

import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

public class MapColorGenerator {

    public static int[] generateChunkColors(ServerWorld world, ChunkPos chunkPos) {
        WorldChunk chunk = world.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z, false);
        if (chunk == null) return null;

        int[] pixels = new int[256];

        WorldChunk northChunk = world.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z - 1, false);

        for (int lx = 0; lx < 16; lx++) {
            double previousHeight = 0.0;

            for (int lz = 0; lz < 16; lz++) {
                int worldX = chunkPos.getStartX() + lx;
                int worldZ = chunkPos.getStartZ() + lz;

                MapColor mapColor = MapColor.CLEAR;
                MapColor.Brightness brightness = MapColor.Brightness.NORMAL;

                int topY = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, lx, lz);

                BlockPos.Mutable mutablePos = new BlockPos.Mutable(worldX, topY, worldZ);

                if (topY > world.getBottomY()) {
                    int waterDepth = 0;
                    BlockState state;

                    do {
                        mutablePos.setY(topY--);
                        state = world.getBlockState(mutablePos);
                        mapColor = state.getMapColor(world, mutablePos);

                        if (mapColor == MapColor.WATER_BLUE) {
                            waterDepth++;
                        }
                    } while (topY > world.getBottomY() && mapColor == MapColor.CLEAR);

                    if (mapColor == MapColor.WATER_BLUE) {
                        FluidState fluidState = world.getFluidState(mutablePos);
                        if (!fluidState.isEmpty() && fluidState.isIn(FluidTags.LAVA)) {
                            mapColor = MapColor.BRIGHT_RED;
                        }
                        double depthFactor = waterDepth * 0.1 + (double) (lx + lz & 1) * 0.2;
                        if (depthFactor < 0.5) {
                            brightness = MapColor.Brightness.HIGH;
                        } else if (depthFactor > 0.9) {
                            brightness = MapColor.Brightness.LOW;
                        } else {
                            brightness = MapColor.Brightness.NORMAL;
                        }
                    } else {
                        double currentHeight = (double) (topY + 1);

                        double northHeight;
                        if (lz == 0) {
                            if (northChunk != null) {
                                int northTopY = northChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, lx, 15);
                                northHeight = northTopY + 1;
                            } else {
                                northHeight = currentHeight;
                            }
                        } else {
                            northHeight = previousHeight;
                        }

                        if (currentHeight > northHeight) {
                            brightness = MapColor.Brightness.HIGH;
                        } else if (currentHeight < northHeight) {
                            brightness = MapColor.Brightness.LOW;
                        } else {
                            brightness = MapColor.Brightness.NORMAL;
                        }

                        previousHeight = currentHeight;
                    }
                } else {
                    mapColor = MapColor.CLEAR;
                }

                int abgr = mapColor.getRenderColor(brightness);
                pixels[lx + lz * 16] = abgrToArgb(abgr);
            }
        }

        return pixels;
    }

    private static int abgrToArgb(int abgr) {
        int a = (abgr >> 24) & 0xFF;
        int b = (abgr >> 16) & 0xFF;
        int g = (abgr >> 8) & 0xFF;
        int r = abgr & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}