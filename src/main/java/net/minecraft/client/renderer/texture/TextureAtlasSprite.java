package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;

public class TextureAtlasSprite implements AutoCloseable {
   private final TextureAtlas atlas;
   private final TextureAtlasSprite.Info info;
   private final AnimationMetadataSection metadata;
   protected final NativeImage[] mainImage;
   private final int[] framesX;
   private final int[] framesY;
   @Nullable
   private final TextureAtlasSprite.InterpolationData interpolationData;
   private final int x;
   private final int y;
   private final float u0;
   private final float u1;
   private final float v0;
   private final float v1;
   private int frame;
   private int subFrame;

   protected TextureAtlasSprite(TextureAtlas var1, TextureAtlasSprite.Info var2, int var3, int var4, int var5, int var6, int var7, NativeImage var8) {
      this.atlas = var1;
      AnimationMetadataSection var9 = var2.metadata;
      int var10x = var2.width;
      int var11xx = var2.height;
      this.x = var6;
      this.y = var7;
      this.u0 = (float)var6 / (float)var4;
      this.u1 = (float)(var6 + var10x) / (float)var4;
      this.v0 = (float)var7 / (float)var5;
      this.v1 = (float)(var7 + var11xx) / (float)var5;
      int var12xxx = var8.getWidth() / var9.getFrameWidth(var10x);
      int var13xxxx = var8.getHeight() / var9.getFrameHeight(var11xx);
      if (var9.getFrameCount() > 0) {
         int var14xxxxx = var9.getUniqueFrameIndices().stream().max(Integer::compareTo).get() + 1;
         this.framesX = new int[var14xxxxx];
         this.framesY = new int[var14xxxxx];
         Arrays.fill(this.framesX, -1);
         Arrays.fill(this.framesY, -1);

         for(int var16 : var9.getUniqueFrameIndices()) {
            if (var16 >= var12xxx * var13xxxx) {
               throw new RuntimeException("invalid frameindex " + var16);
            }

            int var17 = var16 / var12xxx;
            int var18 = var16 % var12xxx;
            this.framesX[var16] = var18;
            this.framesY[var16] = var17;
         }
      } else {
         List<AnimationFrame> var21 = Lists.newArrayList();
         int var22x = var12xxx * var13xxxx;
         this.framesX = new int[var22x];
         this.framesY = new int[var22x];

         for(int var25 = 0; var25 < var13xxxx; ++var25) {
            for(int var28 = 0; var28 < var12xxx; ++var28) {
               int var29 = var25 * var12xxx + var28;
               this.framesX[var29] = var28;
               this.framesY[var29] = var25;
               var21.add(new AnimationFrame(var29, -1));
            }
         }

         var9 = new AnimationMetadataSection(var21, var10x, var11xx, var9.getDefaultFrameTime(), var9.isInterpolatedFrames());
      }

      this.info = new TextureAtlasSprite.Info(var2.name, var10x, var11xx, var9);
      this.metadata = var9;

      try {
         try {
            this.mainImage = MipmapGenerator.generateMipLevels(var8, var3);
         } catch (Throwable var19) {
            CrashReport var24 = CrashReport.forThrowable(var19, "Generating mipmaps for frame");
            CrashReportCategory var27x = var24.addCategory("Frame being iterated");
            var27x.setDetail("First frame", () -> {
               StringBuilder var1x = new StringBuilder();
               if (var1x.length() > 0) {
                  var1x.append(", ");
               }

               var1x.append(var8.getWidth()).append("x").append(var8.getHeight());
               return var1x.toString();
            });
            throw new ReportedException(var24);
         }
      } catch (Throwable var20) {
         CrashReport var23 = CrashReport.forThrowable(var20, "Applying mipmap");
         CrashReportCategory var26x = var23.addCategory("Sprite being mipmapped");
         var26x.setDetail("Sprite name", () -> this.getName().toString());
         var26x.setDetail("Sprite size", () -> this.getWidth() + " x " + this.getHeight());
         var26x.setDetail("Sprite frames", () -> this.getFrameCount() + " frames");
         var26x.setDetail("Mipmap levels", var3);
         throw new ReportedException(var23);
      }

      if (var9.isInterpolatedFrames()) {
         this.interpolationData = new TextureAtlasSprite.InterpolationData(var2, var3);
      } else {
         this.interpolationData = null;
      }
   }

   private void upload(int var1) {
      int var2 = this.framesX[var1] * this.info.width;
      int var3x = this.framesY[var1] * this.info.height;
      this.upload(var2, var3x, this.mainImage);
   }

   private void upload(int var1, int var2, NativeImage[] var3) {
      for(int var4 = 0; var4 < this.mainImage.length; ++var4) {
         var3[var4].upload(var4, this.x >> var4, this.y >> var4, var1 >> var4, var2 >> var4, this.info.width >> var4, this.info.height >> var4, this.mainImage.length > 1, false);
      }
   }

   public int getWidth() {
      return this.info.width;
   }

   public int getHeight() {
      return this.info.height;
   }

   public float getU0() {
      return this.u0;
   }

   public float getU1() {
      return this.u1;
   }

   public float getU(double var1) {
      float var3 = this.u1 - this.u0;
      return this.u0 + var3 * (float)var1 / 16.0F;
   }

   public float getV0() {
      return this.v0;
   }

   public float getV1() {
      return this.v1;
   }

   public float getV(double var1) {
      float var3 = this.v1 - this.v0;
      return this.v0 + var3 * (float)var1 / 16.0F;
   }

   public ResourceLocation getName() {
      return this.info.name;
   }

   public TextureAtlas atlas() {
      return this.atlas;
   }

   public int getFrameCount() {
      return this.framesX.length;
   }

   @Override
   public void close() {
      for(NativeImage var4 : this.mainImage) {
         if (var4 != null) {
            var4.close();
         }
      }

      if (this.interpolationData != null) {
         this.interpolationData.close();
      }
   }

   @Override
   public String toString() {
      int var1 = this.framesX.length;
      return "TextureAtlasSprite{name='"
         + this.info.name
         + '\''
         + ", frameCount="
         + var1
         + ", x="
         + this.x
         + ", y="
         + this.y
         + ", height="
         + this.info.height
         + ", width="
         + this.info.width
         + ", u0="
         + this.u0
         + ", u1="
         + this.u1
         + ", v0="
         + this.v0
         + ", v1="
         + this.v1
         + '}';
   }

   public boolean isTransparent(int var1, int var2, int var3) {
      return (this.mainImage[0].getPixelRGBA(var2 + this.framesX[var1] * this.info.width, var3 + this.framesY[var1] * this.info.height) >> 24 & 0xFF) == 0;
   }

   public void uploadFirstFrame() {
      this.upload(0);
   }

   private float atlasSize() {
      float var1 = (float)this.info.width / (this.u1 - this.u0);
      float var2x = (float)this.info.height / (this.v1 - this.v0);
      return Math.max(var2x, var1);
   }

   public float uvShrinkRatio() {
      return 4.0F / this.atlasSize();
   }

   public void cycleFrames() {
      ++this.subFrame;
      if (this.subFrame >= this.metadata.getFrameTime(this.frame)) {
         int var1 = this.metadata.getFrameIndex(this.frame);
         int var2x = this.metadata.getFrameCount() == 0 ? this.getFrameCount() : this.metadata.getFrameCount();
         this.frame = (this.frame + 1) % var2x;
         this.subFrame = 0;
         int var3xx = this.metadata.getFrameIndex(this.frame);
         if (var1 != var3xx && var3xx >= 0 && var3xx < this.getFrameCount()) {
            this.upload(var3xx);
         }
      } else if (this.interpolationData != null) {
         if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this.interpolationData::uploadInterpolatedFrame);

         } else {
            this.interpolationData.uploadInterpolatedFrame();
         }
      }
   }

   public boolean isAnimation() {
      return this.metadata.getFrameCount() > 1;
   }

   public VertexConsumer wrap(VertexConsumer var1) {
      return new SpriteCoordinateExpander(var1, this);
   }

   public static final class Info {
      private final ResourceLocation name;
      private final int width;
      private final int height;
      private final AnimationMetadataSection metadata;

      public Info(ResourceLocation var1, int var2, int var3, AnimationMetadataSection var4) {
         this.name = var1;
         this.width = var2;
         this.height = var3;
         this.metadata = var4;
      }

      public ResourceLocation name() {
         return this.name;
      }

      public int width() {
         return this.width;
      }

      public int height() {
         return this.height;
      }
   }

   final class InterpolationData implements AutoCloseable {
      private final NativeImage[] activeFrame;

      private InterpolationData(TextureAtlasSprite.Info var2, int var3) {
         this.activeFrame = new NativeImage[var3 + 1];

         for(int var4 = 0; var4 < this.activeFrame.length; ++var4) {
            int var5 = var2.width >> var4;
            int var6 = var2.height >> var4;
            if (this.activeFrame[var4] == null) {
               this.activeFrame[var4] = new NativeImage(var5, var6, false);
            }
         }
      }

      private void uploadInterpolatedFrame() {
         double var1 = (double)1.0F - (double)TextureAtlasSprite.this.subFrame / (double)TextureAtlasSprite.this.metadata.getFrameTime(TextureAtlasSprite.this.frame);
         int var3 = TextureAtlasSprite.this.metadata.getFrameIndex(TextureAtlasSprite.this.frame);
         int var4 = TextureAtlasSprite.this.metadata.getFrameCount() == 0 ? TextureAtlasSprite.this.getFrameCount() : TextureAtlasSprite.this.metadata.getFrameCount();
         int var5 = TextureAtlasSprite.this.metadata.getFrameIndex((TextureAtlasSprite.this.frame + 1) % var4);
         if (var3 != var5 && var5 >= 0 && var5 < TextureAtlasSprite.this.getFrameCount()) {
            for(int var6 = 0; var6 < this.activeFrame.length; ++var6) {
               int var7 = TextureAtlasSprite.this.info.width >> var6;
               int var8 = TextureAtlasSprite.this.info.height >> var6;

               for(int var9 = 0; var9 < var8; ++var9) {
                  for(int var10 = 0; var10 < var7; ++var10) {
                     int var11 = this.getPixel(var3, var6, var10, var9);
                     int var12 = this.getPixel(var5, var6, var10, var9);
                     int var13 = this.mix(var1, var11 >> 16 & 255, var12 >> 16 & 255);
                     int var14 = this.mix(var1, var11 >> 8 & 255, var12 >> 8 & 255);
                     int var15 = this.mix(var1, var11 & 255, var12 & 255);
                     this.activeFrame[var6].setPixelRGBA(var10, var9, var11 & -16777216 | var13 << 16 | var14 << 8 | var15);
                  }
               }
            }

            TextureAtlasSprite.this.upload(0, 0, this.activeFrame);
         }

      }

      private int getPixel(int var1, int var2, int var3, int var4) {
         return TextureAtlasSprite.this.mainImage[var2]
            .getPixelRGBA(
                    var3 + (TextureAtlasSprite.this.framesX[var1] * TextureAtlasSprite.this.info.width >> var2),
                    var4 + (TextureAtlasSprite.this.framesY[var1] * TextureAtlasSprite.this.info.height >> var2)
            );
      }

      private int mix(double var1, int var3, int var4) {
         return (int)(var1 * (double)var3 + (1.0 - var1) * (double)var4);
      }

      @Override
      public void close() {
         for(NativeImage var4 : this.activeFrame) {
            if (var4 != null) {
               var4.close();
            }
         }
      }
   }
}
