

package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class SpiderModel<T extends Entity> extends ListModel<T> {
   private final ModelPart head;
   private final ModelPart body0;
   private final ModelPart body1;
   private final ModelPart leg0;
   private final ModelPart leg1;
   private final ModelPart leg2;
   private final ModelPart leg3;
   private final ModelPart leg4;
   private final ModelPart leg5;
   private final ModelPart leg6;
   private final ModelPart leg7;

   public SpiderModel() {
      float var1 = 0.0F;
      int var2 = 15;
      this.head = new ModelPart(this, 32, 4);
      this.head.addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F);
      this.head.setPos(0.0F, 15.0F, -3.0F);
      this.body0 = new ModelPart(this, 0, 0);
      this.body0.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, 0.0F);
      this.body0.setPos(0.0F, 15.0F, 0.0F);
      this.body1 = new ModelPart(this, 0, 12);
      this.body1.addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F, 0.0F);
      this.body1.setPos(0.0F, 15.0F, 9.0F);
      this.leg0 = new ModelPart(this, 18, 0);
      this.leg0.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
      this.leg0.setPos(-4.0F, 15.0F, 2.0F);
      this.leg1 = new ModelPart(this, 18, 0);
      this.leg1.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
      this.leg1.setPos(4.0F, 15.0F, 2.0F);
      this.leg2 = new ModelPart(this, 18, 0);
      this.leg2.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
      this.leg2.setPos(-4.0F, 15.0F, 1.0F);
      this.leg3 = new ModelPart(this, 18, 0);
      this.leg3.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
      this.leg3.setPos(4.0F, 15.0F, 1.0F);
      this.leg4 = new ModelPart(this, 18, 0);
      this.leg4.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
      this.leg4.setPos(-4.0F, 15.0F, 0.0F);
      this.leg5 = new ModelPart(this, 18, 0);
      this.leg5.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
      this.leg5.setPos(4.0F, 15.0F, 0.0F);
      this.leg6 = new ModelPart(this, 18, 0);
      this.leg6.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
      this.leg6.setPos(-4.0F, 15.0F, -1.0F);
      this.leg7 = new ModelPart(this, 18, 0);
      this.leg7.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
      this.leg7.setPos(4.0F, 15.0F, -1.0F);
   }

   public Iterable<ModelPart> parts() {
      return ImmutableList.of(this.head, this.body0, this.body1, this.leg0, this.leg1, this.leg2, this.leg3, this.leg4, this.leg5, this.leg6, this.leg7);
   }

   public void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6) {
      this.head.yRot = var5 * ((float)Math.PI / 180F);
      this.head.xRot = var6 * ((float)Math.PI / 180F);
      float var7 = ((float)Math.PI / 4F);
      this.leg0.zRot = (-(float)Math.PI / 4F);
      this.leg1.zRot = ((float)Math.PI / 4F);
      this.leg2.zRot = -0.58119464F;
      this.leg3.zRot = 0.58119464F;
      this.leg4.zRot = -0.58119464F;
      this.leg5.zRot = 0.58119464F;
      this.leg6.zRot = (-(float)Math.PI / 4F);
      this.leg7.zRot = ((float)Math.PI / 4F);
      float var8 = -0.0F;
      float var9 = ((float)Math.PI / 8F);
      this.leg0.yRot = ((float)Math.PI / 4F);
      this.leg1.yRot = (-(float)Math.PI / 4F);
      this.leg2.yRot = ((float)Math.PI / 8F);
      this.leg3.yRot = (-(float)Math.PI / 8F);
      this.leg4.yRot = (-(float)Math.PI / 8F);
      this.leg5.yRot = ((float)Math.PI / 8F);
      this.leg6.yRot = (-(float)Math.PI / 4F);
      this.leg7.yRot = ((float)Math.PI / 4F);
      float var10 = -(Mth.cos(var2 * 0.6662F * 2.0F + 0.0F) * 0.4F) * var3;
      float var11 = -(Mth.cos(var2 * 0.6662F * 2.0F + (float)Math.PI) * 0.4F) * var3;
      float var12 = -(Mth.cos(var2 * 0.6662F * 2.0F + ((float)Math.PI / 2F)) * 0.4F) * var3;
      float var13 = -(Mth.cos(var2 * 0.6662F * 2.0F + ((float)Math.PI * 1.5F)) * 0.4F) * var3;
      float var14 = Math.abs(Mth.sin(var2 * 0.6662F + 0.0F) * 0.4F) * var3;
      float var15 = Math.abs(Mth.sin(var2 * 0.6662F + (float)Math.PI) * 0.4F) * var3;
      float var16 = Math.abs(Mth.sin(var2 * 0.6662F + ((float)Math.PI / 2F)) * 0.4F) * var3;
      float var17 = Math.abs(Mth.sin(var2 * 0.6662F + ((float)Math.PI * 1.5F)) * 0.4F) * var3;
    /*  ModelPart var10000 = this.leg0;
      var10000.yRot += var10;
      var10000 = this.leg1;
      var10000.yRot += -var10;
      var10000 = this.leg2;
      var10000.yRot += var11;
      var10000 = this.leg3;
      var10000.yRot += -var11;
      var10000 = this.leg4;
      var10000.yRot += var12;
      var10000 = this.leg5;
      var10000.yRot += -var12;
      var10000 = this.leg6;
      var10000.yRot += var13;
      var10000 = this.leg7;
      var10000.yRot += -var13;
      var10000 = this.leg0;
      var10000.zRot += var14;
      var10000 = this.leg1;
      var10000.zRot += -var14;
      var10000 = this.leg2;
      var10000.zRot += var15;
      var10000 = this.leg3;
      var10000.zRot += -var15;
      var10000 = this.leg4;
      var10000.zRot += var16;
      var10000 = this.leg5;
      var10000.zRot += -var16;
      var10000 = this.leg6;
      var10000.zRot += var17;
      var10000 = this.leg7;
      var10000.zRot += -var17;*/
       this.leg0.yRot +=  var10;
       this.leg1.yRot += -var10;
       this.leg2.yRot +=  var11;
       this.leg3.yRot += -var11;
       this.leg4.yRot +=  var12;
       this.leg5.yRot += -var12;
       this.leg6.yRot +=  var13;
       this.leg7.yRot += -var13;

       this.leg0.zRot +=  var14;
       this.leg1.zRot += -var14;
       this.leg2.zRot +=  var15;
       this.leg3.zRot += -var15;
       this.leg4.zRot +=  var16;
       this.leg5.zRot += -var16;
       this.leg6.zRot +=  var17;
       this.leg7.zRot += -var17;
   }
}
