package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

public class ChunkBorderRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;

   public ChunkBorderRenderer(Minecraft var1) {
      this.minecraft = var1;
   }

   @Override
   public void render(PoseStack var1, MultiBufferSource var2, double var3, double var5, double var7) {
      RenderSystem.enableDepthTest();
      RenderSystem.shadeModel(7425);
      RenderSystem.enableAlphaTest();
      RenderSystem.defaultAlphaFunc();
      Entity var9 = this.minecraft.gameRenderer.getMainCamera().getEntity();
      Tesselator var10 = Tesselator.getInstance();
      BufferBuilder var11 = var10.getBuilder();
      double var12 = (double)0.0F - var5;
      double var14 = (double)256.0F - var5;
      RenderSystem.disableTexture();
      RenderSystem.disableBlend();
      double var16 = (double)(var9.xChunk << 4) - var3;
      double var18 = (double)(var9.zChunk << 4) - var7;
      RenderSystem.lineWidth(1.0F);
      var11.begin(3, DefaultVertexFormat.POSITION_COLOR);

      for(int var20 = -16; var20 <= 32; var20 += 16) {
         for(int var21 = -16; var21 <= 32; var21 += 16) {
            var11.vertex(var16 + (double)var20, var12, var18 + (double)var21).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            var11.vertex(var16 + (double)var20, var12, var18 + (double)var21).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
            var11.vertex(var16 + (double)var20, var14, var18 + (double)var21).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
            var11.vertex(var16 + (double)var20, var14, var18 + (double)var21).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
         }
      }

      for(int var23 = 2; var23 < 16; var23 += 2) {
         var11.vertex(var16 + (double)var23, var12, var18).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var11.vertex(var16 + (double)var23, var12, var18).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16 + (double)var23, var14, var18).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16 + (double)var23, var14, var18).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var11.vertex(var16 + (double)var23, var12, var18 + (double)16.0F).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var11.vertex(var16 + (double)var23, var12, var18 + (double)16.0F).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16 + (double)var23, var14, var18 + (double)16.0F).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16 + (double)var23, var14, var18 + (double)16.0F).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      for(int var24 = 2; var24 < 16; var24 += 2) {
         var11.vertex(var16, var12, var18 + (double)var24).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var11.vertex(var16, var12, var18 + (double)var24).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16, var14, var18 + (double)var24).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16, var14, var18 + (double)var24).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var11.vertex(var16 + (double)16.0F, var12, var18 + (double)var24).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var11.vertex(var16 + (double)16.0F, var12, var18 + (double)var24).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16 + (double)16.0F, var14, var18 + (double)var24).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16 + (double)16.0F, var14, var18 + (double)var24).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      for(int var25 = 0; var25 <= 256; var25 += 2) {
         double var28 = (double)var25 - var5;
         var11.vertex(var16, var28, var18).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var11.vertex(var16, var28, var18).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16, var28, var18 + (double)16.0F).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16 + (double)16.0F, var28, var18 + (double)16.0F).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16 + (double)16.0F, var28, var18).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16, var28, var18).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var11.vertex(var16, var28, var18).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      var10.end();
      RenderSystem.lineWidth(2.0F);
      var11.begin(3, DefaultVertexFormat.POSITION_COLOR);

      for(int var26 = 0; var26 <= 16; var26 += 16) {
         for(int var29 = 0; var29 <= 16; var29 += 16) {
            var11.vertex(var16 + (double)var26, var12, var18 + (double)var29).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            var11.vertex(var16 + (double)var26, var12, var18 + (double)var29).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var11.vertex(var16 + (double)var26, var14, var18 + (double)var29).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var11.vertex(var16 + (double)var26, var14, var18 + (double)var29).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
         }
      }

      for(int var27 = 0; var27 <= 256; var27 += 16) {
         double var30 = (double)var27 - var5;
         var11.vertex(var16, var30, var18).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
         var11.vertex(var16, var30, var18).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         var11.vertex(var16, var30, var18 + (double)16.0F).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         var11.vertex(var16 + (double)16.0F, var30, var18 + (double)16.0F).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         var11.vertex(var16 + (double)16.0F, var30, var18).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         var11.vertex(var16, var30, var18).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         var11.vertex(var16, var30, var18).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
      }

      var10.end();
      RenderSystem.lineWidth(1.0F);
      RenderSystem.enableBlend();
      RenderSystem.enableTexture();
      RenderSystem.shadeModel(7424);
   }
}
