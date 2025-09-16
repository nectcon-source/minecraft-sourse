//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ItemInHandRenderer {
   private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
   private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard.png"));
   private final Minecraft minecraft;
   private ItemStack mainHandItem;
   private ItemStack offHandItem;
   private float mainHandHeight;
   private float oMainHandHeight;
   private float offHandHeight;
   private float oOffHandHeight;
   private final EntityRenderDispatcher entityRenderDispatcher;
   private final ItemRenderer itemRenderer;

   public ItemInHandRenderer(Minecraft var1) {
      this.mainHandItem = ItemStack.EMPTY;
      this.offHandItem = ItemStack.EMPTY;
      this.minecraft = var1;
      this.entityRenderDispatcher = var1.getEntityRenderDispatcher();
      this.itemRenderer = var1.getItemRenderer();
   }

   public void renderItem(LivingEntity var1, ItemStack var2, ItemTransforms.TransformType var3, boolean var4, PoseStack var5, MultiBufferSource var6, int var7) {
      if (!var2.isEmpty()) {
         this.itemRenderer.renderStatic(var1, var2, var3, var4, var5, var6, var1.level, var7, OverlayTexture.NO_OVERLAY);
      }
   }

   private float calculateMapTilt(float var1) {
      float var2 = 1.0F - var1 / 45.0F + 0.1F;
      var2 = Mth.clamp(var2, 0.0F, 1.0F);
      var2 = -Mth.cos(var2 * (float)Math.PI) * 0.5F + 0.5F;
      return var2;
   }

   private void renderMapHand(PoseStack var1, MultiBufferSource var2, int var3, HumanoidArm var4) {
      this.minecraft.getTextureManager().bind(this.minecraft.player.getSkinTextureLocation());
      PlayerRenderer var5 = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(this.minecraft.player);
      var1.pushPose();
      float var6 = var4 == HumanoidArm.RIGHT ? 1.0F : -1.0F;
      var1.mulPose(Vector3f.YP.rotationDegrees(92.0F));
      var1.mulPose(Vector3f.XP.rotationDegrees(45.0F));
      var1.mulPose(Vector3f.ZP.rotationDegrees(var6 * -41.0F));
      var1.translate((var6 * 0.3F), -1.1F, 0.45F);
      if (var4 == HumanoidArm.RIGHT) {
         var5.renderRightHand(var1, var2, var3, this.minecraft.player);
      } else {
         var5.renderLeftHand(var1, var2, var3, this.minecraft.player);
      }

      var1.popPose();
   }

   private void renderOneHandedMap(PoseStack var1, MultiBufferSource var2, int var3, float var4, HumanoidArm var5, float var6, ItemStack var7) {
      float var8 = var5 == HumanoidArm.RIGHT ? 1.0F : -1.0F;
      var1.translate((var8 * 0.125F), -0.125F, 0.0F);
      if (!this.minecraft.player.isInvisible()) {
         var1.pushPose();
         var1.mulPose(Vector3f.ZP.rotationDegrees(var8 * 10.0F));
         this.renderPlayerArm(var1, var2, var3, var4, var6, var5);
         var1.popPose();
      }

      var1.pushPose();
      var1.translate((var8 * 0.51F), (-0.08F + var4 * -1.2F), -0.75F);
      float var9 = Mth.sqrt(var6);
      float var10 = Mth.sin(var9 * (float)Math.PI);
      float var11 = -0.5F * var10;
      float var12 = 0.4F * Mth.sin(var9 * ((float)Math.PI * 2F));
      float var13 = -0.3F * Mth.sin(var6 * (float)Math.PI);
      var1.translate((double)(var8 * var11), (var12 - 0.3F * var10), var13);
      var1.mulPose(Vector3f.XP.rotationDegrees(var10 * -45.0F));
      var1.mulPose(Vector3f.YP.rotationDegrees(var8 * var10 * -30.0F));
      this.renderMap(var1, var2, var3, var7);
      var1.popPose();
   }

   private void renderTwoHandedMap(PoseStack var1, MultiBufferSource var2, int var3, float var4, float var5, float var6) {
      float var7 = Mth.sqrt(var6);
      float var8 = -0.2F * Mth.sin(var6 * (float)Math.PI);
      float var9 = -0.4F * Mth.sin(var7 * (float)Math.PI);
      var1.translate(0.0F, (-var8 / 2.0F), var9);
      float var10 = this.calculateMapTilt(var4);
      var1.translate(0.0F, (0.04F + var5 * -1.2F + var10 * -0.5F), -0.72F);
      var1.mulPose(Vector3f.XP.rotationDegrees(var10 * -85.0F));
      if (!this.minecraft.player.isInvisible()) {
         var1.pushPose();
         var1.mulPose(Vector3f.YP.rotationDegrees(90.0F));
         this.renderMapHand(var1, var2, var3, HumanoidArm.RIGHT);
         this.renderMapHand(var1, var2, var3, HumanoidArm.LEFT);
         var1.popPose();
      }

      float var11 = Mth.sin(var7 * (float)Math.PI);
      var1.mulPose(Vector3f.XP.rotationDegrees(var11 * 20.0F));
      var1.scale(2.0F, 2.0F, 2.0F);
      this.renderMap(var1, var2, var3, this.mainHandItem);
   }

   private void renderMap(PoseStack var1, MultiBufferSource var2, int var3, ItemStack var4) {
      var1.mulPose(Vector3f.YP.rotationDegrees(180.0F));
      var1.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
      var1.scale(0.38F, 0.38F, 0.38F);
      var1.translate(-0.5F, -0.5F, 0.0F);
      var1.scale(0.0078125F, 0.0078125F, 0.0078125F);
      MapItemSavedData var5 = MapItem.getOrCreateSavedData(var4, this.minecraft.level);
      VertexConsumer var6 = var2.getBuffer(var5 == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
      Matrix4f var7 = var1.last().pose();
      var6.vertex(var7, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(var3).endVertex();
      var6.vertex(var7, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(var3).endVertex();
      var6.vertex(var7, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(var3).endVertex();
      var6.vertex(var7, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(var3).endVertex();
      if (var5 != null) {
         this.minecraft.gameRenderer.getMapRenderer().render(var1, var2, var5, false, var3);
      }

   }

   private void renderPlayerArm(PoseStack var1, MultiBufferSource var2, int var3, float var4, float var5, HumanoidArm var6) {
      boolean var7 = var6 != HumanoidArm.LEFT;
      float var8 = var7 ? 1.0F : -1.0F;
      float var9 = Mth.sqrt(var5);
      float var10 = -0.3F * Mth.sin(var9 * (float)Math.PI);
      float var11 = 0.4F * Mth.sin(var9 * ((float)Math.PI * 2F));
      float var12 = -0.4F * Mth.sin(var5 * (float)Math.PI);
      var1.translate((var8 * (var10 + 0.64000005F)), (var11 + -0.6F + var4 * -0.6F), (var12 + -0.71999997F));
      var1.mulPose(Vector3f.YP.rotationDegrees(var8 * 45.0F));
      float var13 = Mth.sin(var5 * var5 * (float)Math.PI);
      float var14 = Mth.sin(var9 * (float)Math.PI);
      var1.mulPose(Vector3f.YP.rotationDegrees(var8 * var14 * 70.0F));
      var1.mulPose(Vector3f.ZP.rotationDegrees(var8 * var13 * -20.0F));
      AbstractClientPlayer var15 = this.minecraft.player;
      this.minecraft.getTextureManager().bind(var15.getSkinTextureLocation());
      var1.translate((var8 * -1.0F), 3.6F, 3.5F);
      var1.mulPose(Vector3f.ZP.rotationDegrees(var8 * 120.0F));
      var1.mulPose(Vector3f.XP.rotationDegrees(200.0F));
      var1.mulPose(Vector3f.YP.rotationDegrees(var8 * -135.0F));
      var1.translate((var8 * 5.6F), 0.0F, 0.0F);
      PlayerRenderer var16 = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(var15);
      if (var7) {
         var16.renderRightHand(var1, var2, var3, var15);
      } else {
         var16.renderLeftHand(var1, var2, var3, var15);
      }

   }

   private void applyEatTransform(PoseStack var1, float var2, HumanoidArm var3, ItemStack var4) {
      float var5 = (float)this.minecraft.player.getUseItemRemainingTicks() - var2 + 1.0F;
      float var6 = var5 / (float)var4.getUseDuration();
      if (var6 < 0.8F) {
         float var7 = Mth.abs(Mth.cos(var5 / 4.0F * (float)Math.PI) * 0.1F);
         var1.translate(0.0F, var7, 0.0F);
      }

      float var9 = 1.0F - (float)Math.pow(var6, 27.0F);
      int var8 = var3 == HumanoidArm.RIGHT ? 1 : -1;
      var1.translate((var9 * 0.6F * (float)var8), (var9 * -0.5F), (var9 * 0.0F));
      var1.mulPose(Vector3f.YP.rotationDegrees((float)var8 * var9 * 90.0F));
      var1.mulPose(Vector3f.XP.rotationDegrees(var9 * 10.0F));
      var1.mulPose(Vector3f.ZP.rotationDegrees((float)var8 * var9 * 30.0F));
   }

   private void applyItemArmAttackTransform(PoseStack var1, HumanoidArm var2, float var3) {
      int var4 = var2 == HumanoidArm.RIGHT ? 1 : -1;
      float var5 = Mth.sin(var3 * var3 * (float)Math.PI);
      var1.mulPose(Vector3f.YP.rotationDegrees((float)var4 * (45.0F + var5 * -20.0F)));
      float var6 = Mth.sin(Mth.sqrt(var3) * (float)Math.PI);
      var1.mulPose(Vector3f.ZP.rotationDegrees((float)var4 * var6 * -20.0F));
      var1.mulPose(Vector3f.XP.rotationDegrees(var6 * -80.0F));
      var1.mulPose(Vector3f.YP.rotationDegrees((float)var4 * -45.0F));
   }

   private void applyItemArmTransform(PoseStack var1, HumanoidArm var2, float var3) {
      int var4 = var2 == HumanoidArm.RIGHT ? 1 : -1;
      var1.translate(((float)var4 * 0.56F), (-0.52F + var3 * -0.6F), -0.72F);
   }

   public void renderHandsWithItems(float var1, PoseStack var2, MultiBufferSource.BufferSource var3, LocalPlayer var4, int var5) {
      float var6 = var4.getAttackAnim(var1);
      InteractionHand var7 = MoreObjects.firstNonNull(var4.swingingArm, InteractionHand.MAIN_HAND);
      float var8 = Mth.lerp(var1, var4.xRotO, var4.xRot);
      boolean var9 = true;
      boolean var10 = true;
      if (var4.isUsingItem()) {
         ItemStack var11 = var4.getUseItem();
         if (var11.getItem() == Items.BOW || var11.getItem() == Items.CROSSBOW) {
            var9 = var4.getUsedItemHand() == InteractionHand.MAIN_HAND;
            var10 = !var9;
         }

         InteractionHand var12 = var4.getUsedItemHand();
         if (var12 == InteractionHand.MAIN_HAND) {
            ItemStack var13 = var4.getOffhandItem();
            if (var13.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(var13)) {
               var10 = false;
            }
         }
      } else {
         ItemStack var15 = var4.getMainHandItem();
         ItemStack var17 = var4.getOffhandItem();
         if (var15.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(var15)) {
            var10 = !var9;
         }

         if (var17.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(var17)) {
            var9 = !var15.isEmpty();
            var10 = !var9;
         }
      }

      float var16 = Mth.lerp(var1, var4.xBobO, var4.xBob);
      float var18 = Mth.lerp(var1, var4.yBobO, var4.yBob);
      var2.mulPose(Vector3f.XP.rotationDegrees((var4.getViewXRot(var1) - var16) * 0.1F));
      var2.mulPose(Vector3f.YP.rotationDegrees((var4.getViewYRot(var1) - var18) * 0.1F));
      if (var9) {
         float var19 = var7 == InteractionHand.MAIN_HAND ? var6 : 0.0F;
         float var14 = 1.0F - Mth.lerp(var1, this.oMainHandHeight, this.mainHandHeight);
         this.renderArmWithItem(var4, var1, var8, InteractionHand.MAIN_HAND, var19, this.mainHandItem, var14, var2, var3, var5);
      }

      if (var10) {
         float var20 = var7 == InteractionHand.OFF_HAND ? var6 : 0.0F;
         float var21 = 1.0F - Mth.lerp(var1, this.oOffHandHeight, this.offHandHeight);
         this.renderArmWithItem(var4, var1, var8, InteractionHand.OFF_HAND, var20, this.offHandItem, var21, var2, var3, var5);
      }

      var3.endBatch();
   }

   private void renderArmWithItem(AbstractClientPlayer var1, float var2, float var3, InteractionHand var4, float var5, ItemStack var6, float var7, PoseStack var8, MultiBufferSource var9, int var10) {
      boolean var11 = var4 == InteractionHand.MAIN_HAND;
      HumanoidArm var12 = var11 ? var1.getMainArm() : var1.getMainArm().getOpposite();
      var8.pushPose();
      if (var6.isEmpty()) {
         if (var11 && !var1.isInvisible()) {
            this.renderPlayerArm(var8, var9, var10, var7, var5, var12);
         }
      } else if (var6.getItem() == Items.FILLED_MAP) {
         if (var11 && this.offHandItem.isEmpty()) {
            this.renderTwoHandedMap(var8, var9, var10, var3, var7, var5);
         } else {
            this.renderOneHandedMap(var8, var9, var10, var7, var12, var5, var6);
         }
      } else if (var6.getItem() == Items.CROSSBOW) {
         boolean var13 = CrossbowItem.isCharged(var6);
         boolean var14 = var12 == HumanoidArm.RIGHT;
         int var15 = var14 ? 1 : -1;
         if (var1.isUsingItem() && var1.getUseItemRemainingTicks() > 0 && var1.getUsedItemHand() == var4) {
            this.applyItemArmTransform(var8, var12, var7);
            var8.translate((double)((float)var15 * -0.4785682F), (double)-0.094387F, (double)0.05731531F);
            var8.mulPose(Vector3f.XP.rotationDegrees(-11.935F));
            var8.mulPose(Vector3f.YP.rotationDegrees((float)var15 * 65.3F));
            var8.mulPose(Vector3f.ZP.rotationDegrees((float)var15 * -9.785F));
            float var28 = (float)var6.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - var2 + 1.0F);
            float var33 = var28 / (float)CrossbowItem.getChargeDuration(var6);
            if (var33 > 1.0F) {
               var33 = 1.0F;
            }

            if (var33 > 0.1F) {
               float var37 = Mth.sin((var28 - 0.1F) * 1.3F);
               float var19 = var33 - 0.1F;
               float var20 = var37 * var19;
               var8.translate((var20 * 0.0F), (var20 * 0.004F), (var20 * 0.0F));
            }

            var8.translate((var33 * 0.0F), (var33 * 0.0F), (var33 * 0.04F));
            var8.scale(1.0F, 1.0F, 1.0F + var33 * 0.2F);
            var8.mulPose(Vector3f.YN.rotationDegrees((float)var15 * 45.0F));
         } else {
            float var16 = -0.4F * Mth.sin(Mth.sqrt(var5) * (float)Math.PI);
            float var17 = 0.2F * Mth.sin(Mth.sqrt(var5) * ((float)Math.PI * 2F));
            float var18 = -0.2F * Mth.sin(var5 * (float)Math.PI);
            var8.translate(((float)var15 * var16), var17, var18);
            this.applyItemArmTransform(var8, var12, var7);
            this.applyItemArmAttackTransform(var8, var12, var5);
            if (var13 && var5 < 0.001F) {
               var8.translate(((float)var15 * -0.641864F), 0.0F, 0.0F);
               var8.mulPose(Vector3f.YP.rotationDegrees((float)var15 * 10.0F));
            }
         }

         this.renderItem(var1, var6, var14 ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIRST_PERSON_LEFT_HAND, !var14, var8, var9, var10);
      } else {
         boolean var21 = var12 == HumanoidArm.RIGHT;
         if (var1.isUsingItem() && var1.getUseItemRemainingTicks() > 0 && var1.getUsedItemHand() == var4) {
            int var24 = var21 ? 1 : -1;
            switch (var6.getUseAnimation()) {
               case NONE:
                  this.applyItemArmTransform(var8, var12, var7);
                  break;
               case EAT:
               case DRINK:
                  this.applyEatTransform(var8, var2, var12, var6);
                  this.applyItemArmTransform(var8, var12, var7);
                  break;
               case BLOCK:
                  this.applyItemArmTransform(var8, var12, var7);
                  break;
               case BOW:
                  this.applyItemArmTransform(var8, var12, var7);
                  var8.translate(((float)var24 * -0.2785682F), 0.18344387F, 0.15731531F);
                  var8.mulPose(Vector3f.XP.rotationDegrees(-13.935F));
                  var8.mulPose(Vector3f.YP.rotationDegrees((float)var24 * 35.3F));
                  var8.mulPose(Vector3f.ZP.rotationDegrees((float)var24 * -9.785F));
                  float var27 = (float)var6.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - var2 + 1.0F);
                  float var31 = var27 / 20.0F;
                  var31 = (var31 * var31 + var31 * 2.0F) / 3.0F;
                  if (var31 > 1.0F) {
                     var31 = 1.0F;
                  }

                  if (var31 > 0.1F) {
                     float var36 = Mth.sin((var27 - 0.1F) * 1.3F);
                     float var39 = var31 - 0.1F;
                     float var41 = var36 * var39;
                     var8.translate((var41 * 0.0F), (var41 * 0.004F), (var41 * 0.0F));
                  }

                  var8.translate((var31 * 0.0F), (var31 * 0.0F), (var31 * 0.04F));
                  var8.scale(1.0F, 1.0F, 1.0F + var31 * 0.2F);
                  var8.mulPose(Vector3f.YN.rotationDegrees((float)var24 * 45.0F));
                  break;
               case SPEAR:
                  this.applyItemArmTransform(var8, var12, var7);
                  var8.translate(((float)var24 * -0.5F), 0.7F, 0.1F);
                  var8.mulPose(Vector3f.XP.rotationDegrees(-55.0F));
                  var8.mulPose(Vector3f.YP.rotationDegrees((float)var24 * 35.3F));
                  var8.mulPose(Vector3f.ZP.rotationDegrees((float)var24 * -9.785F));
                  float var26 = (float)var6.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - var2 + 1.0F);
                  float var30 = var26 / 10.0F;
                  if (var30 > 1.0F) {
                     var30 = 1.0F;
                  }

                  if (var30 > 0.1F) {
                     float var35 = Mth.sin((var26 - 0.1F) * 1.3F);
                     float var38 = var30 - 0.1F;
                     float var40 = var35 * var38;
                     var8.translate((var40 * 0.0F), (var40 * 0.004F), (var40 * 0.0F));
                  }

                  var8.translate(0.0F, 0.0F, (var30 * 0.2F));
                  var8.scale(1.0F, 1.0F, 1.0F + var30 * 0.2F);
                  var8.mulPose(Vector3f.YN.rotationDegrees((float)var24 * 45.0F));
            }
         } else if (var1.isAutoSpinAttack()) {
            this.applyItemArmTransform(var8, var12, var7);
            int var22 = var21 ? 1 : -1;
            var8.translate(((float)var22 * -0.4F), 0.8F, 0.3F);
            var8.mulPose(Vector3f.YP.rotationDegrees((float)var22 * 65.0F));
            var8.mulPose(Vector3f.ZP.rotationDegrees((float)var22 * -85.0F));
         } else {
            float var23 = -0.4F * Mth.sin(Mth.sqrt(var5) * (float)Math.PI);
            float var25 = 0.2F * Mth.sin(Mth.sqrt(var5) * ((float)Math.PI * 2F));
            float var29 = -0.2F * Mth.sin(var5 * (float)Math.PI);
            int var34 = var21 ? 1 : -1;
            var8.translate(((float)var34 * var23), var25, var29);
            this.applyItemArmTransform(var8, var12, var7);
            this.applyItemArmAttackTransform(var8, var12, var5);
         }

         this.renderItem(var1, var6, var21 ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIRST_PERSON_LEFT_HAND, !var21, var8, var9, var10);
      }

      var8.popPose();
   }

   public void tick() {
      this.oMainHandHeight = this.mainHandHeight;
      this.oOffHandHeight = this.offHandHeight;
      LocalPlayer var1 = this.minecraft.player;
      ItemStack var2 = var1.getMainHandItem();
      ItemStack var3 = var1.getOffhandItem();
      if (ItemStack.matches(this.mainHandItem, var2)) {
         this.mainHandItem = var2;
      }

      if (ItemStack.matches(this.offHandItem, var3)) {
         this.offHandItem = var3;
      }

      if (var1.isHandsBusy()) {
         this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
         this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
      } else {
         float var4 = var1.getAttackStrengthScale(1.0F);
         this.mainHandHeight += Mth.clamp((this.mainHandItem == var2 ? var4 * var4 * var4 : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
         this.offHandHeight += Mth.clamp((float)(this.offHandItem == var3 ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
      }

      if (this.mainHandHeight < 0.1F) {
         this.mainHandItem = var2;
      }

      if (this.offHandHeight < 0.1F) {
         this.offHandItem = var3;
      }

   }

   public void itemUsed(InteractionHand var1) {
      if (var1 == InteractionHand.MAIN_HAND) {
         this.mainHandHeight = 0.0F;
      } else {
         this.offHandHeight = 0.0F;
      }

   }
}
