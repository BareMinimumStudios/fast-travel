package net.fasttravel.state;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.*;

public class TeleporterState extends PersistentState {

    private final Map<BlockPos, TeleporterEntry> teleporters = new LinkedHashMap<>();

    public static class TeleporterEntry {
        public final BlockPos pos;
        public Text name;
        public ItemStack icon;
        public final Set<UUID> visitors = new HashSet<>();

        public TeleporterEntry(BlockPos pos, Text name, ItemStack icon) {
            this.pos = pos;
            this.name = name;
            this.icon = icon;
        }
    }

    public Map<BlockPos, TeleporterEntry> getTeleporters() {
        return Collections.unmodifiableMap(teleporters);
    }

    public Optional<TeleporterEntry> get(BlockPos pos) {
        return Optional.ofNullable(teleporters.get(pos));
    }

    public void addTeleporter(BlockPos pos, Text name, ItemStack icon) {
        teleporters.computeIfAbsent(pos, p -> new TeleporterEntry(p, name, icon));
        markDirty();
    }

    public void updateTeleporter(BlockPos pos, Text name, ItemStack icon) {
        TeleporterEntry entry = teleporters.get(pos);
        if (entry != null) {
            entry.name = name;
            entry.icon = icon;
            markDirty();
        }
    }

    public void removeTeleporter(BlockPos pos) {
        teleporters.remove(pos);
        markDirty();
    }

    public void addVisitor(BlockPos pos, UUID playerUuid) {
        TeleporterEntry entry = teleporters.get(pos);
        if (entry != null && entry.visitors.add(playerUuid)) {
            markDirty();
        }
    }

    public boolean hasVisited(BlockPos pos, UUID playerUuid) {
        TeleporterEntry entry = teleporters.get(pos);
        return entry != null && entry.visitors.contains(playerUuid);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (TeleporterEntry entry : teleporters.values()) {
            NbtCompound tag = new NbtCompound();
            tag.putInt("X", entry.pos.getX());
            tag.putInt("Y", entry.pos.getY());
            tag.putInt("Z", entry.pos.getZ());
            tag.putString("Name", Text.Serializer.toJson(entry.name));
            NbtCompound iconTag = new NbtCompound();
            entry.icon.writeNbt(iconTag);
            tag.put("Icon", iconTag);

            NbtList visitorsTag = new NbtList();
            for (UUID uuid : entry.visitors) {
                NbtCompound uuidTag = new NbtCompound();
                uuidTag.putUuid("UUID", uuid);
                visitorsTag.add(uuidTag);
            }
            tag.put("Visitors", visitorsTag);
            list.add(tag);
        }
        nbt.put("Teleporters", list);
        return nbt;
    }

    public static TeleporterState fromNbt(NbtCompound nbt) {
        TeleporterState state = new TeleporterState();
        NbtList list = nbt.getList("Teleporters", 10);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound tag = list.getCompound(i);
            BlockPos pos = new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
            Text name = Text.Serializer.fromJson(tag.getString("Name"));
            ItemStack icon = ItemStack.fromNbt(tag.getCompound("Icon"));
            TeleporterEntry entry = new TeleporterEntry(pos, name, icon);

            NbtList visitorsTag = tag.getList("Visitors", 10);
            for (int j = 0; j < visitorsTag.size(); j++) {
                entry.visitors.add(visitorsTag.getCompound(j).getUuid("UUID"));
            }
            state.teleporters.put(pos, entry);
        }
        return state;
    }

    public static TeleporterState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                TeleporterState::fromNbt,
                TeleporterState::new,
                "fasttravel_teleporters"
        );
    }
}