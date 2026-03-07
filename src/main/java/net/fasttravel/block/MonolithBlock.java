package net.fasttravel.block;

import net.fasttravel.block.entity.MonolithEntity;
import net.fasttravel.init.BlockInit;
import net.fasttravel.init.SoundInit;
import net.fasttravel.network.FastTravelServerPacket;
import net.fasttravel.state.TeleporterState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MonolithBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public MonolithBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MonolithEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        if (ctx.getWorld().getBlockState(pos.up()).canReplace(ctx)) {
            return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
        }
        return null;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient() && player.getVelocity().length() < 0.1f) {
            if (player.isCreativeLevelTwoOp() && player.isSneaking()) {
                FastTravelServerPacket.sendTeleporterOpScreenPacket((ServerPlayerEntity) player, pos);
            } else {
                player.playSound(SoundInit.DISCOVERY, 1.0f, 1.0f);

                TeleporterState.get((ServerWorld) world).addVisitor(pos, player.getUuid());
                FastTravelServerPacket.sendTeleporterScreenPacket((ServerPlayerEntity) player, pos);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public boolean canBucketPlace(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            TeleporterState teleporterState = TeleporterState.get(serverWorld);
            teleporterState.addTeleporter(pos, Text.translatable("block.fasttravel.monolith"), Items.ENDER_PEARL.getDefaultStack());
        }
        super.onBlockAdded(state, world, pos, oldState, notify);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        if (!world.isClient() && world.getBlockState(pos.up()).isOf(BlockInit.MONOLITH_TOP)) {
            world.removeBlock(pos.up(), false);
        }
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            TeleporterState.get(serverWorld).removeTeleporter(pos);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient()) {
            world.setBlockState(pos.up(), BlockInit.MONOLITH_TOP.getDefaultState().with(MonolithTopBlock.FACING, state.get(FACING)), Block.NOTIFY_ALL);
        }
    }

}
