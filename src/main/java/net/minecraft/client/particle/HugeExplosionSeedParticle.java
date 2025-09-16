

package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

public class HugeExplosionSeedParticle extends NoRenderParticle {
   private int life;
   private final int lifeTime;

   private HugeExplosionSeedParticle(ClientLevel var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6, (double)0.0F, (double)0.0F, (double)0.0F);
      this.lifeTime = 8;
   }

   public void tick() {
      for(int var1 = 0; var1 < 6; ++var1) {
         double var2 = this.x + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
         double var4 = this.y + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
         double var6 = this.z + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
         this.level.addParticle(ParticleTypes.EXPLOSION, var2, var4, var6, (double)((float)this.life / (float)this.lifeTime), (double)0.0F, (double)0.0F);
      }

      ++this.life;
      if (this.life == this.lifeTime) {
         this.remove();
      }

   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public Provider() {
      }

      public Particle createParticle(SimpleParticleType var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13) {
         return new HugeExplosionSeedParticle(var2, var3, var5, var7);
      }
   }
}
