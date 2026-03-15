package net.fasttravel.mixin;

import net.fasttravel.accessor.PlayerEntityAccess;
import net.fasttravel.config.FastTravelConfig;
import net.fasttravel.init.ItemInit;
import net.fasttravel.init.ParticleInit;
import net.fasttravel.network.FastTravelServerPacket;
import net.fasttravel.util.FastTravelUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityAccess {

    @Unique
    private int teleportTick = 0;
    @Unique
    private int afterTeleportTick = 0;
    @Unique
    private BlockPos teleportPos = null;

    @Unique
    private static final List<BlockPos> TELEPORT_PARTICLE_POS = BlockPos.stream(-2, 0, -2, 2, 1, 2)
            .filter(pos -> Math.abs(pos.getX()) == 2 || Math.abs(pos.getZ()) == 2)
            .map(BlockPos::toImmutable).toList();

    public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickMixin(CallbackInfo info) {
        if (this.teleportTick > 0) {
            if (this.teleportTick == 1) {
                if (this.teleportPos != null && !this.getWorld().isClient()) {
                    FastTravelUtil.teleport((ServerPlayerEntity) (Object) this, this.teleportPos);
                }
            }
            this.teleportTick--;
            if (this.getWorld().isClient() && this.teleportTick > 2) {
                for (BlockPos blockPos : TELEPORT_PARTICLE_POS) {
                    if (this.getWorld().getRandom().nextFloat() < Math.abs(this.teleportTick / (float) (FastTravelConfig.CONFIG.instance().teleportTime) - 1f)) {
                        this.getWorld().addParticle(
                                ParticleInit.TELEPORT_PARTICLE,
                                this.getX(), this.getRandomBodyY(), this.getZ(),
                                blockPos.getX() + this.getWorld().getRandom().nextFloat() - 0.5f,
                                blockPos.getY() - this.getWorld().getRandom().nextFloat() - 1.0f,
                                blockPos.getZ() + this.getWorld().getRandom().nextFloat() - 0.5f
                        );
                    }
                }
            }
        }
        if (this.afterTeleportTick > 0) {
            this.afterTeleportTick--;
            if (this.getWorld().isClient() && this.afterTeleportTick > 2) {
                for (BlockPos blockPos : TELEPORT_PARTICLE_POS) {
                    if (this.getWorld().getRandom().nextFloat() < this.afterTeleportTick / (float) (FastTravelConfig.CONFIG.instance().afterTeleportTime)) {
                        this.getWorld().addParticle(
                                ParticleInit.AFTER_TELEPORT_PARTICLE,
                                this.getX(), this.getRandomBodyY() + this.getWorld().getRandom().nextFloat(), this.getZ(),
                                blockPos.getX() + this.getWorld().getRandom().nextFloat() - 0.5f,
                                blockPos.getY() - this.getWorld().getRandom().nextFloat() - 1.0f,
                                blockPos.getZ() + this.getWorld().getRandom().nextFloat() - 0.5f
                        );
                    }
                }
            }
            if (!this.getWorld().isClient() && this.afterTeleportTick == 0) {
                ((PlayerEntity) (Object) this).getItemCooldownManager().set(ItemInit.DIVINITY_SHRAPNEL, FastTravelConfig.CONFIG.instance().teleportCooldown);
            }
        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;dropShoulderEntities()V"))
    private void damageMixin(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        this.teleportTick = 0;
        FastTravelServerPacket.sendTeleportPacket((ServerPlayerEntity) (Object) this, -1);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void tickMovementMixin(CallbackInfo info) {
        if (this.teleportTick > 0 || this.afterTeleportTick > 0) {
            info.cancel();
        }
    }

    @Override
    public void startTeleporting(BlockPos pos) {
        this.teleportPos = pos;
        this.teleportTick = FastTravelConfig.CONFIG.instance().teleportTime;
    }

    @Override
    public int getTeleportTick() {
        return this.teleportTick;
    }

    @Override
    public void setTeleportTick(int t) {
        this.teleportTick = t;
    }

    @Override
    public void setAfterTeleportTick(int t) {
        this.afterTeleportTick = t;
    }

    @Override
    public int getAfterTeleportTick() {
        return this.afterTeleportTick;
    }
}