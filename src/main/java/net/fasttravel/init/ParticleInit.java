package net.fasttravel.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fasttravel.FastTravelMain;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ParticleInit {

    public static final DefaultParticleType TELEPORT_PARTICLE = FabricParticleTypes.simple();
    public static final DefaultParticleType AFTER_TELEPORT_PARTICLE = FabricParticleTypes.simple();

    public static void init() {
        Registry.register(Registries.PARTICLE_TYPE, FastTravelMain.identifierOf("teleport_particle"), TELEPORT_PARTICLE);
        Registry.register(Registries.PARTICLE_TYPE, FastTravelMain.identifierOf("after_teleport_particle"), AFTER_TELEPORT_PARTICLE);
    }

    public static class TeleportParticle extends SpriteBillboardParticle {

        private final SpriteProvider spriteProvider;

        private final double startX;
        private final double startY;
        private final double startZ;

        private final boolean down;

        public TeleportParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, boolean down, SpriteProvider spriteProvider) {
            super(clientWorld, d, e, f);
            this.spriteProvider = spriteProvider;
            this.velocityX = g;
            this.velocityY = h;
            this.velocityZ = i;
            this.startX = d;
            this.startY = e;
            this.startZ = f;
            this.down = down;
            this.prevPosX = d + g;
            this.prevPosY = e + h;
            this.prevPosZ = f + i;
            this.x = this.prevPosX;
            this.y = this.prevPosY;
            this.z = this.prevPosZ;
            this.scale = 0.1F * (this.random.nextFloat() * 0.5F + 0.2F);
            float j = this.random.nextFloat() * 0.6F + 0.4F;
            this.red = 0.9F * j;
            this.green = 0.9F * j;
            this.blue = j;
            this.collidesWithWorld = false;
            this.maxAge = (int) (Math.random() * 10.0) + 30;
        }

        @Override
        public ParticleTextureSheet getType() {
            return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
        }

        @Override
        public void move(double dx, double dy, double dz) {
            this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
            this.repositionFromBoundingBox();
        }

        @Override
        public int getBrightness(float tint) {
            int i = super.getBrightness(tint);
            float f = (float) this.age / this.maxAge;
            f *= f;
            f *= f;
            int j = i & 0xFF;
            int k = i >> 16 & 0xFF;
            k += (int) (f * 15.0F * 16.0F);
            if (k > 240) {
                k = 240;
            }

            return j | k << 16;
        }

        @Override
        public void tick() {
            this.prevPosX = this.x;
            this.prevPosY = this.y;
            this.prevPosZ = this.z;
            if (this.age++ >= this.maxAge) {
                this.markDead();
            } else {
                float f = (float) this.age / this.maxAge;
                f = 1.0F - f;
                float g = 1.0F - f;
                g *= g;
                g *= g;
                this.x = this.startX + this.velocityX * f;
                if (this.down) {
                    this.y = this.startY + this.velocityY * f - g * 1.1F;
                } else {
                    this.y = this.startY + this.velocityY * f + g * 1.1F;
                }
                this.z = this.startZ + this.velocityZ * f;
            }
        }

        @Environment(EnvType.CLIENT)
        public static class TeleportFactory implements ParticleFactory<DefaultParticleType> {
            private final SpriteProvider spriteProvider;

            public TeleportFactory(FabricSpriteProvider sprites) {
                this.spriteProvider = sprites;
            }

            @Override
            public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
                TeleportParticle teleportParticle = new TeleportParticle(clientWorld, d, e, f, g, h, i, false, this.spriteProvider);
                teleportParticle.setSprite(this.spriteProvider);
                return teleportParticle;
            }
        }

        @Environment(EnvType.CLIENT)
        public static class AfterTeleportFactory implements ParticleFactory<DefaultParticleType> {
            private final SpriteProvider spriteProvider;

            public AfterTeleportFactory(FabricSpriteProvider sprites) {
                this.spriteProvider = sprites;
            }

            @Override
            public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
                TeleportParticle teleportParticle = new TeleportParticle(clientWorld, d, e, f, g, h, i, true, this.spriteProvider);
                teleportParticle.setSprite(this.spriteProvider);
                return teleportParticle;
            }
        }

    }

}
