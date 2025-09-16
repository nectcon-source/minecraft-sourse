

package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

public class SquidModel<T extends Entity> extends ListModel<T> {
   private final ModelPart body;
   private final ModelPart[] tentacles = new ModelPart[8];
   private final ImmutableList<ModelPart> parts;

   public SquidModel() {
      int var1 = -16;
      this.body = new ModelPart(this, 0, 0);
      this.body.addBox(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 12.0F);
      this.body.y += 8.0F;

      for(int var2 = 0; var2 < this.tentacles.length; ++var2) {
         this.tentacles[var2] = new ModelPart(this, 48, 0);
         double var3 = (double)var2 * Math.PI * (double)2.0F / (double)this.tentacles.length;
         float var5 = (float)Math.cos(var3) * 5.0F;
         float var6 = (float)Math.sin(var3) * 5.0F;
         this.tentacles[var2].addBox(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F);
         this.tentacles[var2].x = var5;
         this.tentacles[var2].z = var6;
         this.tentacles[var2].y = 15.0F;
         var3 = (double)var2 * Math.PI * (double)-2.0F / (double)this.tentacles.length + (Math.PI / 2D);
         this.tentacles[var2].yRot = (float)var3;
      }

      ImmutableList.Builder<ModelPart> var7 = ImmutableList.builder();
      var7.add(this.body);
      var7.addAll(Arrays.asList(this.tentacles));
      this.parts = var7.build();
   }

   public void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6) {
      for(ModelPart var10 : this.tentacles) {
         var10.xRot = var4;
      }

   }

   public Iterable<ModelPart> parts() {
      return this.parts;
   }
}
