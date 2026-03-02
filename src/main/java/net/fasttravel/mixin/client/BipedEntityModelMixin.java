package net.fasttravel.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fasttravel.accessor.PlayerEntityAccess;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> {

    @Shadow
    @Mutable
    @Final
    public ModelPart head;
    @Shadow
    @Mutable
    @Final
    public ModelPart hat;
    @Shadow
    @Mutable
    @Final
    public ModelPart body;
    @Shadow
    @Mutable
    @Final
    public ModelPart rightArm;
    @Shadow
    @Mutable
    @Final
    public ModelPart leftArm;
    @Shadow
    @Mutable
    @Final
    public ModelPart rightLeg;
    @Shadow
    @Mutable
    @Final
    public ModelPart leftLeg;

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;copyTransform(Lnet/minecraft/client/model/ModelPart;)V"))
    private void setAnglesMixin(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo info) {
        if (!this.riding && livingEntity instanceof PlayerEntity && (((PlayerEntityAccess) livingEntity).getTeleportTick() > 0 || ((PlayerEntityAccess) livingEntity).getAfterTeleportTick() > 0)) {
            this.rightLeg.pivotZ = -4F;
            this.rightLeg.pitch = 0.2F;
            this.leftLeg.pitch = 1F;
            this.leftLeg.pivotY = 16.5F;

            this.rightArm.pitch = -0.76F;
            this.rightArm.roll = -0.4F;

            this.head.pivotY = 4.0F;
            this.body.pivotY = 4.0F;
            this.leftArm.pivotY = 5.0F;
            this.rightArm.pivotY = 5.0F;
        }
    }
}
