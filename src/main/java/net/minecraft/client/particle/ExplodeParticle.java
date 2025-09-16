

package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class ExplodeParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   protected ExplodeParticle(ClientLevel var1, double var2, double var4, double var6, double var8, double var10, double var12, SpriteSet var14) {
      super(var1, var2, var4, var6);
      this.sprites = var14;
      this.xd = var8 + (Math.random() * (double)2.0F - (double)1.0F) * (double)0.05F;
      this.yd = var10 + (Math.random() * (double)2.0F - (double)1.0F) * (double)0.05F;
      this.zd = var12 + (Math.random() * (double)2.0F - (double)1.0F) * (double)0.05F;
      float var15 = this.random.nextFloat() * 0.3F + 0.7F;
      this.rCol = var15;
      this.gCol = var15;
      this.bCol = var15;
      this.quadSize = 0.1F * (this.random.nextFloat() * this.random.nextFloat() * 6.0F + 1.0F);
      this.lifetime = (int)((double)16.0F / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
      this.setSpriteFromAge(var14);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.setSpriteFromAge(this.sprites);
         this.yd += 0.004;
         this.move(this.xd, this.yd, this.zd);
         this.xd *= (double)0.9F;
         this.yd *= (double)0.9F;
         this.zd *= (double)0.9F;
         if (this.onGround) {
            this.xd *= (double)0.7F;
            this.zd *= (double)0.7F;
         }

      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet var1) {
         this.sprites = var1;
      }

      public Particle createParticle(SimpleParticleType var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13) {
         return new ExplodeParticle(var2, var3, var5, var7, var9, var11, var13, this.sprites);
      }
   }
}
