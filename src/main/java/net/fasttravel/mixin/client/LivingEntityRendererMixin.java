package net.fasttravel.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fasttravel.accessor.PlayerEntityAccess;
import net.fasttravel.config.FastTravelConfig;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {

    @Shadow
    @Mutable
    protected M model;

    public LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @WrapOperation(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void renderMixin(EntityModel instance, MatrixStack matrixStack, VertexConsumer vertexConsumer, int a, int b, float c, float d, float e, float f, Operation<Void> original, T livingEntity, float g, float h, MatrixStack stack, VertexConsumerProvider vertexConsumerProvider, int j) {
        if (livingEntity instanceof PlayerEntity playerEntity) {
            if (((PlayerEntityAccess) playerEntity).getTeleportTick() > 0) {
                this.model.render(matrixStack, vertexConsumer, a, b, c, d, e, (float) ((PlayerEntityAccess) playerEntity).getTeleportTick() / FastTravelConfig.CONFIG.instance().teleportTime);
            } else if (((PlayerEntityAccess) playerEntity).getAfterTeleportTick() > 0) {
                this.model.render(matrixStack, vertexConsumer, a, b, c, d, e, 1.0f - (float) ((PlayerEntityAccess) playerEntity).getAfterTeleportTick() / FastTravelConfig.CONFIG.instance().afterTeleportTime);
            }else{
                original.call(instance, matrixStack, vertexConsumer, a, b, c, d, e, f);
            }
        } else {
            original.call(instance, matrixStack, vertexConsumer, a, b, c, d, e, f);
        }
    }

}
