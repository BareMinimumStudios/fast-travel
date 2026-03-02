package net.fasttravel.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fasttravel.FastTravelMain;
import net.fasttravel.accessor.PlayerEntityAccess;
import net.fasttravel.config.FastTravelConfig;
import net.fasttravel.init.ItemInit;
import net.fasttravel.state.PlayerExplorationState;
import net.fasttravel.state.TeleporterState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.List;
import java.util.Set;

public class FastTravelServerPacket {

    public static final Identifier TELEPORT_PACKET = FastTravelMain.identifierOf("teleport");
    public static final Identifier AFTER_TELEPORT_PACKET = FastTravelMain.identifierOf("after_teleport");
    public static final Identifier TELEPORTER_SCREEN_PACKET = FastTravelMain.identifierOf("teleporter_screen");
    public static final Identifier TELEPORTER_OP_SCREEN_PACKET = FastTravelMain.identifierOf("teleporter_op_screen");
    public static final Identifier MAP_CHUNK_PACKET = FastTravelMain.identifierOf("map_chunk");

    public static final Identifier REQUEST_TELEPORT_PACKET = FastTravelMain.identifierOf("request_teleport");
    public static final Identifier TELEPORTER_OP_PACKET = FastTravelMain.identifierOf("teleporter_op");

    public static void init() {

        ServerPlayNetworking.registerGlobalReceiver(REQUEST_TELEPORT_PACKET, (server, player, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                TeleporterState state = TeleporterState.get(player.getServerWorld());
                if (!state.hasVisited(pos, player.getUuid())) {
                    player.sendMessage(Text.translatable("info.fasttravel.unvisited"));
                    return;
                }
                if (!player.getInventory().contains(ItemInit.DIVINITY_SHRAPNEL.getDefaultStack()) && !player.isCreative()) {
                    player.sendMessage(Text.translatable("info.fasttravel.missing_shrapnel"));
                    return;
                }
                if (player.getItemCooldownManager().isCoolingDown(ItemInit.DIVINITY_SHRAPNEL)) {
                    player.sendMessage(Text.translatable("info.fasttravel.cooldown"));
                    return;
                }
                ((PlayerEntityAccess) player).startTeleporting(pos);
                sendTeleportPacket(player, FastTravelConfig.CONFIG.instance().teleportTime);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TELEPORTER_OP_PACKET, (server, player, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            Text name = buf.readText();
            ItemStack icon = buf.readItemStack();
            server.execute(() -> {
                if (!player.isCreativeLevelTwoOp()) return;
                TeleporterState.get(player.getServerWorld()).updateTeleporter(pos, name, icon);
            });
        });
    }

    public static void sendTeleportPacket(ServerPlayerEntity player, int ticks) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(ticks);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(TELEPORT_PACKET, buf));
    }

    public static void sendAfterTeleportPacket(ServerPlayerEntity player, int ticks) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(ticks);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(AFTER_TELEPORT_PACKET, buf));
    }

    public static void sendTeleporterScreenPacket(ServerPlayerEntity player, BlockPos openedAt) {
        ServerWorld world = player.getServerWorld();
        TeleporterState state = TeleporterState.get(world);

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(openedAt);

        List<TeleporterState.TeleporterEntry> allEntries = List.copyOf(state.getTeleporters().values());
        buf.writeInt(allEntries.size());
        for (TeleporterState.TeleporterEntry entry : allEntries) {
            buf.writeBlockPos(entry.pos);
            buf.writeText(entry.name);
            buf.writeItemStack(entry.icon);
        }

        List<BlockPos> visited = allEntries.stream().filter(e -> e.visitors.contains(player.getUuid())).map(e -> e.pos).toList();
        buf.writeInt(visited.size());
        for (BlockPos pos : visited) {
            buf.writeBlockPos(pos);
        }

        Set<Long> exploredChunks = PlayerExplorationState.get(world).getExploredChunks(player.getUuid());
        buf.writeInt(exploredChunks.size());
        for (long chunkLong : exploredChunks) {
            buf.writeLong(chunkLong);
        }
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(TELEPORTER_SCREEN_PACKET, buf));
    }

    public static void sendTeleporterOpScreenPacket(ServerPlayerEntity player, BlockPos pos) {
        TeleporterState state = TeleporterState.get(player.getServerWorld());
        state.get(pos).ifPresentOrElse(entry -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos);
            buf.writeText(entry.name);
            buf.writeItemStack(entry.icon);
            player.networkHandler.sendPacket(new CustomPayloadS2CPacket(TELEPORTER_OP_SCREEN_PACKET, buf));
        }, () -> player.sendMessage(Text.literal("Could not find a teleporter")));
    }

    public static void sendMapChunkPacket(ServerPlayerEntity player, ChunkPos chunkPos, int[] pixels) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(chunkPos.toLong());
        for (int pixel : pixels) {
            buf.writeInt(pixel);
        }
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(MAP_CHUNK_PACKET, buf));
    }
}