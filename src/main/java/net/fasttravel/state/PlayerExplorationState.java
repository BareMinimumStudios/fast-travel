package net.fasttravel.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;

import java.util.*;

public class PlayerExplorationState extends PersistentState {

    private final Map<UUID, Set<Long>> exploredChunks = new HashMap<>();

    public void addChunk(UUID player, ChunkPos chunk) {
        Set<Long> chunks = exploredChunks.computeIfAbsent(player, k -> new HashSet<>());
        if (chunks.add(chunk.toLong())) {
            markDirty();
        }
    }

    public boolean hasExplored(UUID player, ChunkPos chunk) {
        Set<Long> chunks = exploredChunks.get(player);
        return chunks != null && chunks.contains(chunk.toLong());
    }

    public Set<Long> getExploredChunks(UUID player) {
        return exploredChunks.getOrDefault(player, Collections.emptySet());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList playerList = new NbtList();
        for (Map.Entry<UUID, Set<Long>> entry : exploredChunks.entrySet()) {
            NbtCompound playerTag = new NbtCompound();
            playerTag.putUuid("UUID", entry.getKey());
            long[] chunksArray = entry.getValue().stream().mapToLong(Long::longValue).toArray();
            playerTag.putLongArray("Chunks", chunksArray);
            playerList.add(playerTag);
        }
        nbt.put("Players", playerList);
        return nbt;
    }

    public static PlayerExplorationState fromNbt(NbtCompound nbt) {
        PlayerExplorationState state = new PlayerExplorationState();
        NbtList playerList = nbt.getList("Players", 10);
        for (int i = 0; i < playerList.size(); i++) {
            NbtCompound playerTag = playerList.getCompound(i);
            UUID uuid = playerTag.getUuid("UUID");
            long[] chunksArray = playerTag.getLongArray("Chunks");
            Set<Long> chunks = new HashSet<>();
            for (long l : chunksArray) chunks.add(l);
            state.exploredChunks.put(uuid, chunks);
        }
        return state;
    }

    public static PlayerExplorationState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                PlayerExplorationState::fromNbt,
                PlayerExplorationState::new,
                "fasttravel_exploration"
        );
    }
}