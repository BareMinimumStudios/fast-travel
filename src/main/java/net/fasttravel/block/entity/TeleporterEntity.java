package net.fasttravel.block.entity;

import net.fasttravel.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class TeleporterEntity extends BlockEntity {

    public TeleporterEntity(BlockPos pos, BlockState state) {
        super(BlockInit.TELEPORTER_ENTITY, pos, state);
    }
}
