

package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

public class BoatModel extends ListModel<Boat> {
   private final ModelPart[] paddles = new ModelPart[2];
   private final ModelPart waterPatch;
   private final ImmutableList<ModelPart> parts;

   public BoatModel() {
      ModelPart[] var1 = new ModelPart[5];
      var1[0] = (new ModelPart(this, 0, 0)).setTexSize(128, 64);
      var1[1] = (new ModelPart(this, 0, 19)).setTexSize(128, 64);
      var1[2] = (new ModelPart(this, 0, 27)).setTexSize(128, 64);
      var1[3] = (new ModelPart(this, 0, 35)).setTexSize(128, 64);
      var1[4] = (new ModelPart(this, 0, 43)).setTexSize(128, 64);
      int var2 = 32;
      int var3 = 6;
      int var4 = 20;
      int var5 = 4;
      int var6 = 28;
      var1[0].addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, 0.0F);
      var1[0].setPos(0.0F, 3.0F, 1.0F);
      var1[1].addBox(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F, 0.0F);
      var1[1].setPos(-15.0F, 4.0F, 4.0F);
      var1[2].addBox(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F, 0.0F);
      var1[2].setPos(15.0F, 4.0F, 0.0F);
      var1[3].addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, 0.0F);
      var1[3].setPos(0.0F, 4.0F, -9.0F);
      var1[4].addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, 0.0F);
      var1[4].setPos(0.0F, 4.0F, 9.0F);
      var1[0].xRot = ((float)Math.PI / 2F);
      var1[1].yRot = ((float)Math.PI * 1.5F);
      var1[2].yRot = ((float)Math.PI / 2F);
      var1[3].yRot = (float)Math.PI;
      this.paddles[0] = this.makePaddle(true);
      this.paddles[0].setPos(3.0F, -5.0F, 9.0F);
      this.paddles[1] = this.makePaddle(false);
      this.paddles[1].setPos(3.0F, -5.0F, -9.0F);
      this.paddles[1].yRot = (float)Math.PI;
      this.paddles[0].zRot = 0.19634955F;
      this.paddles[1].zRot = 0.19634955F;
      this.waterPatch = (new ModelPart(this, 0, 0)).setTexSize(128, 64);
      this.waterPatch.addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, 0.0F);
      this.waterPatch.setPos(0.0F, -3.0F, 1.0F);
      this.waterPatch.xRot = ((float)Math.PI / 2F);
      ImmutableList.Builder<ModelPart> var7 = ImmutableList.builder();
      var7.addAll(Arrays.asList(var1));
      var7.addAll(Arrays.asList(this.paddles));
      this.parts = var7.build();
   }

   public void setupAnim(Boat var1, float var2, float var3, float var4, float var5, float var6) {
      this.animatePaddle(var1, 0, var2);
      this.animatePaddle(var1, 1, var2);
   }

   public ImmutableList<ModelPart> parts() {
      return this.parts;
   }

   public ModelPart waterPatch() {
      return this.waterPatch;
   }

   protected ModelPart makePaddle(boolean var1) {
      ModelPart var2 = (new ModelPart(this, 62, var1 ? 0 : 20)).setTexSize(128, 64);
      int var3 = 20;
      int var4 = 7;
      int var5 = 6;
      float var6 = -5.0F;
      var2.addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F);
      var2.addBox(var1 ? -1.001F : 0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F);
      return var2;
   }

   protected void animatePaddle(Boat var1, int var2, float var3) {
      float var4 = var1.getRowingTime(var2, var3);
      ModelPart var5 = this.paddles[var2];
      var5.xRot = (float)Mth.clampedLerp((double)(-(float)Math.PI / 3F), (double)-0.2617994F, (double)((Mth.sin(-var4) + 1.0F) / 2.0F));
      var5.yRot = (float)Mth.clampedLerp((double)(-(float)Math.PI / 4F), (double)((float)Math.PI / 4F), (double)((Mth.sin(-var4 + 1.0F) + 1.0F) / 2.0F));
      if (var2 == 1) {
         var5.yRot = (float)Math.PI - var5.yRot;
      }

   }
}
