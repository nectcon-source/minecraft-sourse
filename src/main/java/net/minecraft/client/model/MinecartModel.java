

package net.minecraft.client.model;

import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

public class MinecartModel<T extends Entity> extends ListModel<T> {
   private final ModelPart[] cubes = new ModelPart[6];

   public MinecartModel() {
      this.cubes[0] = new ModelPart(this, 0, 10);
      this.cubes[1] = new ModelPart(this, 0, 0);
      this.cubes[2] = new ModelPart(this, 0, 0);
      this.cubes[3] = new ModelPart(this, 0, 0);
      this.cubes[4] = new ModelPart(this, 0, 0);
      this.cubes[5] = new ModelPart(this, 44, 10);
      int var1 = 20;
      int var2 = 8;
      int var3 = 16;
      int var4 = 4;
      this.cubes[0].addBox(-10.0F, -8.0F, -1.0F, 20.0F, 16.0F, 2.0F, 0.0F);
      this.cubes[0].setPos(0.0F, 4.0F, 0.0F);
      this.cubes[5].addBox(-9.0F, -7.0F, -1.0F, 18.0F, 14.0F, 1.0F, 0.0F);
      this.cubes[5].setPos(0.0F, 4.0F, 0.0F);
      this.cubes[1].addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F, 0.0F);
      this.cubes[1].setPos(-9.0F, 4.0F, 0.0F);
      this.cubes[2].addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F, 0.0F);
      this.cubes[2].setPos(9.0F, 4.0F, 0.0F);
      this.cubes[3].addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F, 0.0F);
      this.cubes[3].setPos(0.0F, 4.0F, -7.0F);
      this.cubes[4].addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F, 0.0F);
      this.cubes[4].setPos(0.0F, 4.0F, 7.0F);
      this.cubes[0].xRot = ((float)Math.PI / 2F);
      this.cubes[1].yRot = ((float)Math.PI * 1.5F);
      this.cubes[2].yRot = ((float)Math.PI / 2F);
      this.cubes[3].yRot = (float)Math.PI;
      this.cubes[5].xRot = (-(float)Math.PI / 2F);
   }

   public void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6) {
      this.cubes[5].y = 4.0F - var4;
   }

   public Iterable<ModelPart> parts() {
      return Arrays.asList(this.cubes);
   }
}
