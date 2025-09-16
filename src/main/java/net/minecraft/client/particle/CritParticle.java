

package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class CritParticle extends TextureSheetParticle {
   private CritParticle(ClientLevel var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, (double)0.0F, (double)0.0F, (double)0.0F);
      this.xd *= (double)0.1F;
      this.yd *= (double)0.1F;
      this.zd *= (double)0.1F;
      this.xd += var8 * 0.4;
      this.yd += var10 * 0.4;
      this.zd += var12 * 0.4;
      float var14 = (float)(Math.random() * (double)0.3F + (double)0.6F);
      this.rCol = var14;
      this.gCol = var14;
      this.bCol = var14;
      this.quadSize *= 0.75F;
      this.lifetime = Math.max((int)((double)6.0F / (Math.random() * 0.8 + 0.6)), 1);
      this.hasPhysics = false;
      this.tick();
   }

   public float getQuadSize(float var1) {
      return this.quadSize * Mth.clamp(((float)this.age + var1) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.move(this.xd, this.yd, this.zd);
         this.gCol = (float)((double)this.gCol * 0.96);
         this.bCol = (float)((double)this.bCol * 0.9);
         this.xd *= (double)0.7F;
         this.yd *= (double)0.7F;
         this.zd *= (double)0.7F;
         this.yd -= (double)0.02F;
         if (this.onGround) {
            this.xd *= (double)0.7F;
            this.zd *= (double)0.7F;
         }

      }
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet var1) {
         this.sprite = var1;
      }

      public Particle createParticle(SimpleParticleType var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13) {
         CritParticle var15 = new CritParticle(var2, var3, var5, var7, var9, var11, var13);
         var15.pickSprite(this.sprite);
         return var15;
      }
   }

   public static class MagicProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public MagicProvider(SpriteSet var1) {
         this.sprite = var1;
      }

      public Particle createParticle(SimpleParticleType var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13) {
         CritParticle var15 = new CritParticle(var2, var3, var5, var7, var9, var11, var13);
         var15.rCol *= 0.3F;
         var15.gCol *= 0.8F;
         var15.pickSprite(this.sprite);
         return var15;
      }
   }

   public static class DamageIndicatorProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public DamageIndicatorProvider(SpriteSet var1) {
         this.sprite = var1;
      }

      public Particle createParticle(SimpleParticleType var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13) {
         CritParticle var15 = new CritParticle(var2, var3, var5, var7, var9, var11 + (double)1.0F, var13);
         var15.setLifetime(20);
         var15.pickSprite(this.sprite);
         return var15;
      }
   }
}
