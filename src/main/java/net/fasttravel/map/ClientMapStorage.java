package net.fasttravel.map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fasttravel.FastTravelMain;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ClientMapStorage {

    public record TeleporterInfo(BlockPos pos, Text name, ItemStack icon) {
    }

    private List<TeleporterInfo> teleporters = new ArrayList<>();
    private Set<BlockPos> visitedTeleporters = new HashSet<>();

    public final Map<Long, Identifier> regionTextures = new HashMap<>();
    private final Map<Long, NativeImageBackedTexture> regionNativeTextures = new HashMap<>();

    private static final Map<RegistryKey<World>, ClientMapStorage> INSTANCES = new HashMap<>();

    public static ClientMapStorage get(RegistryKey<World> dimension) {
        return INSTANCES.computeIfAbsent(dimension, k -> new ClientMapStorage());
    }

    public static void clearAll() {
        for (ClientMapStorage storage : INSTANCES.values()) {
            storage.destroy();
        }
        INSTANCES.clear();
    }

    public void setTeleporters(List<TeleporterInfo> teleporters) {
        this.teleporters = new ArrayList<>(teleporters);
    }

    public List<TeleporterInfo> getTeleporters() {
        return Collections.unmodifiableList(teleporters);
    }

    public void setVisitedTeleporters(Set<BlockPos> visited) {
        this.visitedTeleporters = new HashSet<>(visited);
    }

    public Set<BlockPos> getVisitedTeleporters() {
        return Collections.unmodifiableSet(visitedTeleporters);
    }

    public boolean hasVisited(BlockPos pos) {
        return visitedTeleporters.contains(pos);
    }

    public void receiveChunkPixels(ChunkPos chunkPos, int[] pixels) {
        long key = regionKey(Math.floorDiv(chunkPos.x, 32), Math.floorDiv(chunkPos.z, 32));

        NativeImageBackedTexture texture = regionNativeTextures.computeIfAbsent(key, k -> {
            NativeImage image = new NativeImage(NativeImage.Format.RGBA, 512, 512, true);
            NativeImageBackedTexture nativeTexture = new NativeImageBackedTexture(image);

            Identifier textureId = FastTravelMain.identifierOf("map/region_%d_%d".formatted(regionX(k), regionZ(k)));
            MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, nativeTexture);
            regionTextures.put(k, textureId);
            return nativeTexture;
        });

        int offsetX = Math.floorMod(chunkPos.x, 32) * 16;
        int offsetZ = Math.floorMod(chunkPos.z, 32) * 16;

        NativeImage image = texture.getImage();
        if (image == null) return;

        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                int argb = pixels[lx + lz * 16];
                image.setColor(offsetX + lx, offsetZ + lz, argbToAbgr(argb));
            }
        }

        texture.upload();
    }

    public Identifier getRegionTexture(int regionX, int regionZ) {
        return regionTextures.get(regionKey(regionX, regionZ));
    }

    private void destroy() {
        MinecraftClient client = MinecraftClient.getInstance();
        for (Identifier id : regionTextures.values()) {
            client.getTextureManager().destroyTexture(id);
        }
        regionTextures.clear();
        regionNativeTextures.clear();
    }

    public static long regionKey(int regionX, int regionZ) {
        return (long) regionX | ((long) regionZ << 32);
    }

    public static int regionX(long key) {
        return (int) (key & 0xFFFFFFFFL);
    }

    public static int regionZ(long key) {
        return (int) (key >>> 32);
    }

    private static int argbToAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}