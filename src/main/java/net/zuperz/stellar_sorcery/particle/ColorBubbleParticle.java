package net.zuperz.stellar_sorcery.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;

public class ColorBubbleParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected ColorBubbleParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            ColorBubbleData data,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xd, yd, zd);

        this.sprites = sprites;

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.rCol = data.red();
        this.gCol = data.green();
        this.bCol = data.blue();

        this.alpha = 1F;

        this.lifetime = 20;

        this.gravity = -0.004F;

        this.scale(0.6F);

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<ColorBubbleData> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                ColorBubbleData data,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd
        ) {
            return new ColorBubbleParticle(
                    level,
                    x,
                    y,
                    z,
                    xd,
                    yd,
                    zd,
                    data,
                    this.sprites
            );
        }
    }
}