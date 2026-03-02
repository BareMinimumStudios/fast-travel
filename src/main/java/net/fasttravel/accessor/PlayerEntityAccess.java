package net.fasttravel.accessor;

import net.minecraft.util.math.BlockPos;

public interface PlayerEntityAccess {

    void startTeleporting(BlockPos pos);

    void setTeleportTick(int teleportTick);

    int getTeleportTick();

    void setAfterTeleportTick(int teleportTick);

    int getAfterTeleportTick();
}
