package net.fasttravel.block.entity;

import net.fasttravel.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class MonolithEntity extends BlockEntity {

    public MonolithEntity(BlockPos pos, BlockState state) {
        super(BlockInit.MONOLITH_ENTITY, pos, state);
    }
}
