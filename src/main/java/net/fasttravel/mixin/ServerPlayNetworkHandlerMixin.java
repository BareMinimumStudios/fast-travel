package net.fasttravel.mixin;

import net.fasttravel.event.ChunkExplorationListener;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Unique
    private ChunkPos lastChunkPos = null;

    @Inject(method = "onPlayerMove", at = @At("TAIL"))
    private void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo info) {
        ChunkPos currentChunk = player.getChunkPos();

        if (!currentChunk.equals(lastChunkPos)) {
            lastChunkPos = currentChunk;
            ChunkExplorationListener.onPlayerEnterChunk(player, (ServerWorld) player.getWorld(), currentChunk);
        }
    }

}
