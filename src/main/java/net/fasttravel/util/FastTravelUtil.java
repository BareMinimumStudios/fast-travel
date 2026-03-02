package net.fasttravel.util;

import net.fasttravel.accessor.PlayerEntityAccess;
import net.fasttravel.config.FastTravelConfig;
import net.fasttravel.network.FastTravelServerPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FastTravelUtil {

    public static void teleport(ServerPlayerEntity serverPlayerEntity, BlockPos blockPos) {
        World world = serverPlayerEntity.getWorld();
        List<BlockPos> availableTeleportPositions = new ArrayList<>();
        for (Direction direction : Direction.Type.HORIZONTAL) {
            if (world.getBlockState(blockPos.offset(direction, 2)).isAir() && world.getBlockState(blockPos.offset(direction, 2).up()).isAir()
                    && !world.getBlockState(blockPos.offset(direction, 2).down()).isAir()) {
                availableTeleportPositions.add(blockPos.offset(direction, 2));
            }
        }
        if (!availableTeleportPositions.isEmpty()) {
            BlockPos pos = availableTeleportPositions.get(world.getRandom().nextInt(availableTeleportPositions.size()));
            serverPlayerEntity.teleport(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        } else {
            serverPlayerEntity.teleport(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        ((PlayerEntityAccess) serverPlayerEntity).setAfterTeleportTick(FastTravelConfig.CONFIG.instance().afterTeleportTime);
        FastTravelServerPacket.sendAfterTeleportPacket(serverPlayerEntity, FastTravelConfig.CONFIG.instance().afterTeleportTime);
    }

}
