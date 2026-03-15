package net.fasttravel.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fasttravel.accessor.PlayerEntityAccess;
import net.fasttravel.block.screen.MonolithOpScreen;
import net.fasttravel.block.screen.MonolithScreen;
import net.fasttravel.init.SoundInit;
import net.fasttravel.map.ClientMapStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class FastTravelClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(FastTravelServerPacket.TELEPORT_PACKET, (client, handler, buf, sender) -> {
            int ticks = buf.readInt();
            client.execute(() -> {
                if (client.player == null) {
                    return;
                }
                ((PlayerEntityAccess) client.player).setTeleportTick(ticks);
                if (ticks >= 0) {
                    ((PlayerEntityAccess) client.player).startTeleporting(BlockPos.ORIGIN);
                    client.player.playSound(SoundInit.TELEPORTING, 1.0f, 1.0f);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FastTravelServerPacket.AFTER_TELEPORT_PACKET, (client, handler, buf, sender) -> {
            int ticks = buf.readInt();
            client.execute(() -> {
                if (client.player == null) {
                    return;
                }
                ((PlayerEntityAccess) client.player).setAfterTeleportTick(ticks);
                client.player.playSound(SoundInit.AFTER_TELEPORTING, 1.0f, 1.0f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FastTravelServerPacket.TELEPORTER_SCREEN_PACKET, (client, handler, buf, sender) -> {
            BlockPos openedAt = buf.readBlockPos();

            int teleporterCount = buf.readInt();
            List<ClientMapStorage.TeleporterInfo> teleporters = new ArrayList<>(teleporterCount);
            for (int i = 0; i < teleporterCount; i++) {
                BlockPos pos = buf.readBlockPos();
                Text name = buf.readText();
                ItemStack icon = buf.readItemStack();
                teleporters.add(new ClientMapStorage.TeleporterInfo(pos, name, icon));
            }

            int visitedCount = buf.readInt();
            Set<BlockPos> visited = new HashSet<>(visitedCount);
            for (int i = 0; i < visitedCount; i++) {
                visited.add(buf.readBlockPos());
            }
            int exploredCount = buf.readInt();
            Set<Long> exploredChunks = new HashSet<>(exploredCount);
            for (int i = 0; i < exploredCount; i++) {
                exploredChunks.add(buf.readLong());
            }

            client.execute(() -> {
                if (client.player == null || client.world == null) return;
                ClientMapStorage storage = ClientMapStorage.get(client.world.getRegistryKey());
                storage.setTeleporters(teleporters);
                storage.setVisitedTeleporters(visited);
                storage.setExploredChunks(exploredChunks);
                client.setScreen(new MonolithScreen(openedAt));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FastTravelServerPacket.TELEPORTER_OP_SCREEN_PACKET, (client, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            Text name = buf.readText();
            ItemStack icon = buf.readItemStack();
            client.execute(() -> client.setScreen(new MonolithOpScreen(pos, name, icon)));
        });

        ClientPlayNetworking.registerGlobalReceiver(FastTravelServerPacket.MAP_CHUNK_PACKET, (client, handler, buf, sender) -> {
            ChunkPos chunkPos = new ChunkPos(buf.readLong());
            int[] pixels = new int[256];
            for (int i = 0; i < 256; i++) {
                pixels[i] = buf.readInt();
            }
            client.execute(() -> {
                if (client.world == null) return;
                ClientMapStorage storage = ClientMapStorage.get(client.world.getRegistryKey());
                storage.receiveChunkPixels(chunkPos, pixels);
            });
        });
    }

    public static void sendRequestTeleportPacket(BlockPos pos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(FastTravelServerPacket.REQUEST_TELEPORT_PACKET, buf));
    }

    public static void sendTeleporterOpPacket(BlockPos pos, String name, ItemStack icon) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeText(Text.of(name));
        buf.writeItemStack(icon);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(FastTravelServerPacket.TELEPORTER_OP_PACKET, buf));
    }
}