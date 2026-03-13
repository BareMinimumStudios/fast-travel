package net.fasttravel.block.entity;

import net.fasttravel.config.FastTravelConfig;
import net.fasttravel.init.BlockInit;
import net.fasttravel.init.SoundInit;
import net.fasttravel.state.TeleporterState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class MonolithEntity extends BlockEntity {

    public MonolithEntity(BlockPos pos, BlockState state) {
        super(BlockInit.MONOLITH_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, MonolithEntity blockEntity) {

    }

    public static void serverTick(World world, BlockPos pos, BlockState state, MonolithEntity blockEntity) {
        if (world.getTime() % 20 == 0) {
            List<PlayerEntity> list = world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), new Box(pos).expand(FastTravelConfig.CONFIG.instance().monolithExploreRadius), Entity::isAlive);
            if (!list.isEmpty()) {
                for (PlayerEntity player : list) {
                    if (!TeleporterState.get((ServerWorld) world).hasVisited(pos, player.getUuid())) {
                        player.playSound(SoundInit.DISCOVERY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        TeleporterState.get((ServerWorld) world).addVisitor(pos, player.getUuid());
                    }
                }
            }
        }
    }
}
