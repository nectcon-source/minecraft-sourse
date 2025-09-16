

package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidModel<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
   public ModelPart head;
   public ModelPart hat;
   public ModelPart body;
   public ModelPart rightArm;
   public ModelPart leftArm;
   public ModelPart rightLeg;
   public ModelPart leftLeg;
   public ArmPose leftArmPose;
   public ArmPose rightArmPose;
   public boolean crouching;
   public float swimAmount;

   public HumanoidModel(float var1) {
      this(RenderType::entityCutoutNoCull, var1, 0.0F, 64, 32);
   }

   protected HumanoidModel(float var1, float var2, int var3, int var4) {
      this(RenderType::entityCutoutNoCull, var1, var2, var3, var4);
   }

   public HumanoidModel(Function<ResourceLocation, RenderType> var1, float var2, float var3, int var4, int var5) {
      super(var1, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
      this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
      this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
      this.texWidth = var4;
      this.texHeight = var5;
      this.head = new ModelPart(this, 0, 0);
      this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, var2);
      this.head.setPos(0.0F, 0.0F + var3, 0.0F);
      this.hat = new ModelPart(this, 32, 0);
      this.hat.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, var2 + 0.5F);
      this.hat.setPos(0.0F, 0.0F + var3, 0.0F);
      this.body = new ModelPart(this, 16, 16);
      this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, var2);
      this.body.setPos(0.0F, 0.0F + var3, 0.0F);
      this.rightArm = new ModelPart(this, 40, 16);
      this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, var2);
      this.rightArm.setPos(-5.0F, 2.0F + var3, 0.0F);
      this.leftArm = new ModelPart(this, 40, 16);
      this.leftArm.mirror = true;
      this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, var2);
      this.leftArm.setPos(5.0F, 2.0F + var3, 0.0F);
      this.rightLeg = new ModelPart(this, 0, 16);
      this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, var2);
      this.rightLeg.setPos(-1.9F, 12.0F + var3, 0.0F);
      this.leftLeg = new ModelPart(this, 0, 16);
      this.leftLeg.mirror = true;
      this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, var2);
      this.leftLeg.setPos(1.9F, 12.0F + var3, 0.0F);
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
   }

   public void prepareMobModel(T var1, float var2, float var3, float var4) {
      this.swimAmount = var1.getSwimAmount(var4);
      super.prepareMobModel(var1, var2, var3, var4);
   }

   public void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6) {
      boolean var7 = var1.getFallFlyingTicks() > 4;
      boolean var8 = var1.isVisuallySwimming();
      this.head.yRot = var5 * ((float)Math.PI / 180F);
      if (var7) {
         this.head.xRot = (-(float)Math.PI / 4F);
      } else if (this.swimAmount > 0.0F) {
         if (var8) {
            this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (-(float)Math.PI / 4F));
         } else {
            this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, var6 * ((float)Math.PI / 180F));
         }
      } else {
         this.head.xRot = var6 * ((float)Math.PI / 180F);
      }

      this.body.yRot = 0.0F;
      this.rightArm.z = 0.0F;
      this.rightArm.x = -5.0F;
      this.leftArm.z = 0.0F;
      this.leftArm.x = 5.0F;
      float var9 = 1.0F;
      if (var7) {
         var9 = (float)var1.getDeltaMovement().lengthSqr();
         var9 /= 0.2F;
         var9 *= var9 * var9;
      }

      if (var9 < 1.0F) {
         var9 = 1.0F;
      }

      this.rightArm.xRot = Mth.cos(var2 * 0.6662F + (float)Math.PI) * 2.0F * var3 * 0.5F / var9;
      this.leftArm.xRot = Mth.cos(var2 * 0.6662F) * 2.0F * var3 * 0.5F / var9;
      this.rightArm.zRot = 0.0F;
      this.leftArm.zRot = 0.0F;
      this.rightLeg.xRot = Mth.cos(var2 * 0.6662F) * 1.4F * var3 / var9;
      this.leftLeg.xRot = Mth.cos(var2 * 0.6662F + (float)Math.PI) * 1.4F * var3 / var9;
      this.rightLeg.yRot = 0.0F;
      this.leftLeg.yRot = 0.0F;
      this.rightLeg.zRot = 0.0F;
      this.leftLeg.zRot = 0.0F;
      if (this.riding) {
         ModelPart var10000 = this.rightArm;
         var10000.xRot += (-(float)Math.PI / 5F);
         var10000 = this.leftArm;
         var10000.xRot += (-(float)Math.PI / 5F);
         this.rightLeg.xRot = -1.4137167F;
         this.rightLeg.yRot = ((float)Math.PI / 10F);
         this.rightLeg.zRot = 0.07853982F;
         this.leftLeg.xRot = -1.4137167F;
         this.leftLeg.yRot = (-(float)Math.PI / 10F);
         this.leftLeg.zRot = -0.07853982F;
      }

      this.rightArm.yRot = 0.0F;
      this.leftArm.yRot = 0.0F;
      boolean var10 = var1.getMainArm() == HumanoidArm.RIGHT;
      boolean var11 = var10 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
      if (var10 != var11) {
         this.poseLeftArm(var1);
         this.poseRightArm(var1);
      } else {
         this.poseRightArm(var1);
         this.poseLeftArm(var1);
      }

      this.setupAttackAnimation(var1, var4);
      if (this.crouching) {
         this.body.xRot = 0.5F;
         ModelPart var23 = this.rightArm;
         var23.xRot += 0.4F;
         var23 = this.leftArm;
         var23.xRot += 0.4F;
         this.rightLeg.z = 4.0F;
         this.leftLeg.z = 4.0F;
         this.rightLeg.y = 12.2F;
         this.leftLeg.y = 12.2F;
         this.head.y = 4.2F;
         this.body.y = 3.2F;
         this.leftArm.y = 5.2F;
         this.rightArm.y = 5.2F;
      } else {
         this.body.xRot = 0.0F;
         this.rightLeg.z = 0.1F;
         this.leftLeg.z = 0.1F;
         this.rightLeg.y = 12.0F;
         this.leftLeg.y = 12.0F;
         this.head.y = 0.0F;
         this.body.y = 0.0F;
         this.leftArm.y = 2.0F;
         this.rightArm.y = 2.0F;
      }

      AnimationUtils.bobArms(this.rightArm, this.leftArm, var4);
      if (this.swimAmount > 0.0F) {
         float var12 = var2 % 26.0F;
         HumanoidArm var13 = this.getAttackArm(var1);
         float var14 = var13 == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
         float var15 = var13 == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
         if (var12 < 14.0F) {
            this.leftArm.xRot = this.rotlerpRad(var15, this.leftArm.xRot, 0.0F);
            this.rightArm.xRot = Mth.lerp(var14, this.rightArm.xRot, 0.0F);
            this.leftArm.yRot = this.rotlerpRad(var15, this.leftArm.yRot, (float)Math.PI);
            this.rightArm.yRot = Mth.lerp(var14, this.rightArm.yRot, (float)Math.PI);
            this.leftArm.zRot = this.rotlerpRad(var15, this.leftArm.zRot, (float)Math.PI + 1.8707964F * this.quadraticArmUpdate(var12) / this.quadraticArmUpdate(14.0F));
            this.rightArm.zRot = Mth.lerp(var14, this.rightArm.zRot, (float)Math.PI - 1.8707964F * this.quadraticArmUpdate(var12) / this.quadraticArmUpdate(14.0F));
         } else if (var12 >= 14.0F && var12 < 22.0F) {
            float var20 = (var12 - 14.0F) / 8.0F;
            this.leftArm.xRot = this.rotlerpRad(var15, this.leftArm.xRot, ((float)Math.PI / 2F) * var20);
            this.rightArm.xRot = Mth.lerp(var14, this.rightArm.xRot, ((float)Math.PI / 2F) * var20);
            this.leftArm.yRot = this.rotlerpRad(var15, this.leftArm.yRot, (float)Math.PI);
            this.rightArm.yRot = Mth.lerp(var14, this.rightArm.yRot, (float)Math.PI);
            this.leftArm.zRot = this.rotlerpRad(var15, this.leftArm.zRot, 5.012389F - 1.8707964F * var20);
            this.rightArm.zRot = Mth.lerp(var14, this.rightArm.zRot, 1.2707963F + 1.8707964F * var20);
         } else if (var12 >= 22.0F && var12 < 26.0F) {
            float var16 = (var12 - 22.0F) / 4.0F;
            this.leftArm.xRot = this.rotlerpRad(var15, this.leftArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * var16);
            this.rightArm.xRot = Mth.lerp(var14, this.rightArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * var16);
            this.leftArm.yRot = this.rotlerpRad(var15, this.leftArm.yRot, (float)Math.PI);
            this.rightArm.yRot = Mth.lerp(var14, this.rightArm.yRot, (float)Math.PI);
            this.leftArm.zRot = this.rotlerpRad(var15, this.leftArm.zRot, (float)Math.PI);
            this.rightArm.zRot = Mth.lerp(var14, this.rightArm.zRot, (float)Math.PI);
         }

         float var21 = 0.3F;
         float var17 = 0.33333334F;
         this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(var2 * 0.33333334F + (float)Math.PI));
         this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(var2 * 0.33333334F));
      }

      this.hat.copyFrom(this.head);
   }

   private void poseRightArm(T var1) {
      switch (this.rightArmPose) {
         case EMPTY:
            this.rightArm.yRot = 0.0F;
            break;
         case BLOCK:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F;
            this.rightArm.yRot = (-(float)Math.PI / 6F);
            break;
         case ITEM:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float)Math.PI / 10F);
            this.rightArm.yRot = 0.0F;
            break;
         case THROW_SPEAR:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float)Math.PI;
            this.rightArm.yRot = 0.0F;
            break;
         case BOW_AND_ARROW:
            this.rightArm.yRot = -0.1F + this.head.yRot;
            this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
            this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            break;
         case CROSSBOW_CHARGE:
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, var1, true);
            break;
         case CROSSBOW_HOLD:
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
      }

   }

   private void poseLeftArm(T var1) {
      switch (this.leftArmPose) {
         case EMPTY:
            this.leftArm.yRot = 0.0F;
            break;
         case BLOCK:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.9424779F;
            this.leftArm.yRot = ((float)Math.PI / 6F);
            break;
         case ITEM:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float)Math.PI / 10F);
            this.leftArm.yRot = 0.0F;
            break;
         case THROW_SPEAR:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float)Math.PI;
            this.leftArm.yRot = 0.0F;
            break;
         case BOW_AND_ARROW:
            this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
            this.leftArm.yRot = 0.1F + this.head.yRot;
            this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            break;
         case CROSSBOW_CHARGE:
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, var1, false);
            break;
         case CROSSBOW_HOLD:
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
      }

   }

   protected void setupAttackAnimation(T var1, float var2) {
      if (!(this.attackTime <= 0.0F)) {
         HumanoidArm var3 = this.getAttackArm(var1);
         ModelPart var4 = this.getArm(var3);
         float var5 = this.attackTime;
         this.body.yRot = Mth.sin(Mth.sqrt(var5) * ((float)Math.PI * 2F)) * 0.2F;
         if (var3 == HumanoidArm.LEFT) {
            ModelPart var10000 = this.body;
            var10000.yRot *= -1.0F;
         }

         this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
         this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
         this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
         this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
         ModelPart var12 = this.rightArm;
         var12.yRot += this.body.yRot;
         var12 = this.leftArm;
         var12.yRot += this.body.yRot;
         var12 = this.leftArm;
         var12.xRot += this.body.yRot;
         var5 = 1.0F - this.attackTime;
         var5 *= var5;
         var5 *= var5;
         var5 = 1.0F - var5;
         float var6 = Mth.sin(var5 * (float)Math.PI);
         float var7 = Mth.sin(this.attackTime * (float)Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
         var4.xRot = (float)((double)var4.xRot - ((double)var6 * 1.2 + (double)var7));
         var4.yRot += this.body.yRot * 2.0F;
         var4.zRot += Mth.sin(this.attackTime * (float)Math.PI) * -0.4F;
      }
   }

   protected float rotlerpRad(float var1, float var2, float var3) {
      float var4 = (var3 - var2) % ((float)Math.PI * 2F);
      if (var4 < -(float)Math.PI) {
         var4 += ((float)Math.PI * 2F);
      }

      if (var4 >= (float)Math.PI) {
         var4 -= ((float)Math.PI * 2F);
      }

      return var2 + var1 * var4;
   }

   private float quadraticArmUpdate(float var1) {
      return -65.0F * var1 + var1 * var1;
   }

   public void copyPropertiesTo(HumanoidModel<T> var1) {
      super.copyPropertiesTo(var1);
      var1.leftArmPose = this.leftArmPose;
      var1.rightArmPose = this.rightArmPose;
      var1.crouching = this.crouching;
      var1.head.copyFrom(this.head);
      var1.hat.copyFrom(this.hat);
      var1.body.copyFrom(this.body);
      var1.rightArm.copyFrom(this.rightArm);
      var1.leftArm.copyFrom(this.leftArm);
      var1.rightLeg.copyFrom(this.rightLeg);
      var1.leftLeg.copyFrom(this.leftLeg);
   }

   public void setAllVisible(boolean var1) {
      this.head.visible = var1;
      this.hat.visible = var1;
      this.body.visible = var1;
      this.rightArm.visible = var1;
      this.leftArm.visible = var1;
      this.rightLeg.visible = var1;
      this.leftLeg.visible = var1;
   }

   public void translateToHand(HumanoidArm var1, PoseStack var2) {
      this.getArm(var1).translateAndRotate(var2);
   }

   protected ModelPart getArm(HumanoidArm var1) {
      return var1 == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
   }

   public ModelPart getHead() {
      return this.head;
   }

   protected HumanoidArm getAttackArm(T var1) {
      HumanoidArm var2 = var1.getMainArm();
      return var1.swingingArm == InteractionHand.MAIN_HAND ? var2 : var2.getOpposite();
   }

   public static enum ArmPose {
      EMPTY(false),
      ITEM(false),
      BLOCK(false),
      BOW_AND_ARROW(true),
      THROW_SPEAR(false),
      CROSSBOW_CHARGE(true),
      CROSSBOW_HOLD(true);

      private final boolean twoHanded;

      private ArmPose(boolean var3) {
         this.twoHanded = var3;
      }

      public boolean isTwoHanded() {
         return this.twoHanded;
      }
   }
}
