package net.fasttravel.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;

import java.util.*;

public class RegionMapState extends PersistentState {

    private final Map<Long, int[]> regionPixels = new HashMap<>();
    private final Map<Long, BitSet> processedChunks = new HashMap<>();

    public static long regionKey(int regionX, int regionZ) {
        return (long) regionX | ((long) regionZ << 32);
    }

    public static long regionKeyFromChunk(ChunkPos chunk) {
        return regionKey(Math.floorDiv(chunk.x, 32), Math.floorDiv(chunk.z, 32));
    }

    public static int chunkBitIndex(ChunkPos chunk) {
        int localX = Math.floorMod(chunk.x, 32);
        int localZ = Math.floorMod(chunk.z, 32);
        return localX + localZ * 32;
    }

    public boolean isChunkProcessed(ChunkPos chunk) {
        BitSet bits = processedChunks.get(regionKeyFromChunk(chunk));
        return bits != null && bits.get(chunkBitIndex(chunk));
    }

    public void writeChunkPixels(ChunkPos chunk, int[] pixels) {
        long key = regionKeyFromChunk(chunk);
        int[] regionArray = regionPixels.computeIfAbsent(key, k -> new int[512 * 512]);

        int regionX = Math.floorDiv(chunk.x, 32);
        int regionZ = Math.floorDiv(chunk.z, 32);
        int offsetX = Math.floorMod(chunk.x, 32) * 16;
        int offsetZ = Math.floorMod(chunk.z, 32) * 16;

        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                regionArray[(offsetX + lx) + (offsetZ + lz) * 512] = pixels[lx + lz * 16];
            }
        }

        processedChunks.computeIfAbsent(key, k -> new BitSet(1024)).set(chunkBitIndex(chunk));
        markDirty();
    }

    public int[] getRegionPixels(int regionX, int regionZ) {
        return regionPixels.get(regionKey(regionX, regionZ));
    }

    public Set<Long> getRegionKeys() {
        return Collections.unmodifiableSet(regionPixels.keySet());
    }

    public static int regionX(long key) {
        return (int) (key & 0xFFFFFFFFL);
    }

    public static int regionZ(long key) {
        return (int) (key >>> 32);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList regionList = new NbtList();
        for (Map.Entry<Long, int[]> entry : regionPixels.entrySet()) {
            NbtCompound regionTag = new NbtCompound();
            long key = entry.getKey();
            regionTag.putInt("RX", regionX(key));
            regionTag.putInt("RZ", regionZ(key));

            int[] pixels = entry.getValue();
            byte[] bytes = new byte[pixels.length * 4];
            for (int i = 0; i < pixels.length; i++) {
                bytes[i * 4] = (byte) (pixels[i] >> 24);
                bytes[i * 4 + 1] = (byte) (pixels[i] >> 16);
                bytes[i * 4 + 2] = (byte) (pixels[i] >> 8);
                bytes[i * 4 + 3] = (byte) (pixels[i]);
            }
            regionTag.putByteArray("Pixels", bytes);

            BitSet bits = processedChunks.get(key);
            if (bits != null) {
                regionTag.putLongArray("ProcessedChunks", bits.toLongArray());
            }

            regionList.add(regionTag);
        }
        nbt.put("Regions", regionList);
        return nbt;
    }

    public static RegionMapState fromNbt(NbtCompound nbt) {
        RegionMapState state = new RegionMapState();
        NbtList regionList = nbt.getList("Regions", 10);
        for (int i = 0; i < regionList.size(); i++) {
            NbtCompound regionTag = regionList.getCompound(i);
            int rx = regionTag.getInt("RX");
            int rz = regionTag.getInt("RZ");
            long key = regionKey(rx, rz);

            byte[] bytes = regionTag.getByteArray("Pixels");
            int[] pixels = new int[bytes.length / 4];
            for (int j = 0; j < pixels.length; j++) {
                pixels[j] = ((bytes[j * 4] & 0xFF) << 24)
                        | ((bytes[j * 4 + 1] & 0xFF) << 16)
                        | ((bytes[j * 4 + 2] & 0xFF) << 8)
                        | (bytes[j * 4 + 3] & 0xFF);
            }
            state.regionPixels.put(key, pixels);

            if (regionTag.contains("ProcessedChunks")) {
                state.processedChunks.put(key, BitSet.valueOf(regionTag.getLongArray("ProcessedChunks")));
            }
        }
        return state;
    }

    // In RegionMapState.java ergänzen:
    /**
     * Gibt die 256 Pixel eines einzelnen Chunks zurück, oder null wenn nicht vorhanden.
     */
    public int[] getChunkPixels(ChunkPos chunk) {
        int[] regionArray = regionPixels.get(regionKeyFromChunk(chunk));
        if (regionArray == null) return null;

        int offsetX = Math.floorMod(chunk.x, 32) * 16;
        int offsetZ = Math.floorMod(chunk.z, 32) * 16;

        int[] pixels = new int[256];
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                pixels[lx + lz * 16] = regionArray[(offsetX + lx) + (offsetZ + lz) * 512];
            }
        }
        return pixels;
    }

    public static RegionMapState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                RegionMapState::fromNbt,
                RegionMapState::new,
                "fasttravel_mapdata"
        );
    }
}