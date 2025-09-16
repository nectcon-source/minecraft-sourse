//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.blaze3d.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class TrueTypeGlyphProvider implements GlyphProvider {
   private final ByteBuffer fontMemory;
   private final STBTTFontinfo font;
   private final float oversample;
   private final IntSet skip = new IntArraySet();
   private final float shiftX;
   private final float shiftY;
   private final float pointScale;
   private final float ascent;

   public TrueTypeGlyphProvider(ByteBuffer var1, STBTTFontinfo var2, float var3, float var4, float var5, float var6, String var7) {
      this.fontMemory = var1;
      this.font = var2;
      this.oversample = var4;
       var7.codePoints().forEach(this.skip::add);
      this.shiftX = var5 * var4;
      this.shiftY = var6 * var4;
      this.pointScale = STBTruetype.stbtt_ScaleForPixelHeight(var2, var3 * var4);

      try (MemoryStack var8 = MemoryStack.stackPush()) {
         IntBuffer var10 = var8.mallocInt(1);
         IntBuffer var11 = var8.mallocInt(1);
         IntBuffer var12 = var8.mallocInt(1);
         STBTruetype.stbtt_GetFontVMetrics(var2, var10, var11, var12);
         this.ascent = (float)var10.get(0) * this.pointScale;
      }

   }

   @Nullable
   public Glyph getGlyph(int var1) {
      if (this.skip.contains(var1)) {
         return null;
      } else {
         Object var9;
         try (MemoryStack var2 = MemoryStack.stackPush()) {
            IntBuffer var4 = var2.mallocInt(1);
            IntBuffer var5 = var2.mallocInt(1);
            IntBuffer var6 = var2.mallocInt(1);
            IntBuffer var7 = var2.mallocInt(1);
            int var8 = STBTruetype.stbtt_FindGlyphIndex(this.font, var1);
            if (var8 != 0) {
               STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(this.font, var8, this.pointScale, this.pointScale, this.shiftX, this.shiftY, var4, var5, var6, var7);
               int var26 = var6.get(0) - var4.get(0);
               int var10 = var7.get(0) - var5.get(0);
               if (var26 != 0 && var10 != 0) {
                  IntBuffer var27 = var2.mallocInt(1);
                  IntBuffer var12 = var2.mallocInt(1);
                  STBTruetype.stbtt_GetGlyphHMetrics(this.font, var8, var27, var12);
                  Glyph var13 = new Glyph(var4.get(0), var6.get(0), -var5.get(0), -var7.get(0), (float)var27.get(0) * this.pointScale, (float)var12.get(0) * this.pointScale, var8);
                  return var13;
               }

               Object var11 = null;
               return (Glyph)var11;
            }

            var9 = null;
         }

         return (Glyph)var9;
      }
   }

   public void close() {
      this.font.free();
      MemoryUtil.memFree(this.fontMemory);
   }

   public IntSet getSupportedGlyphs() {
      return (IntSet)IntStream.range(0, 65535).filter((var1) -> !this.skip.contains(var1)).collect(IntOpenHashSet::new, IntCollection::add, IntCollection::addAll);
   }

   class Glyph implements RawGlyph {
      private final int width;
      private final int height;
      private final float bearingX;
      private final float bearingY;
      private final float advance;
      private final int index;

      private Glyph(int var2, int var3, int var4, int var5, float var6, float var7, int var8) {
         this.width = var3 - var2;
         this.height = var4 - var5;
         this.advance = var6 / TrueTypeGlyphProvider.this.oversample;
         this.bearingX = (var7 + (float)var2 + TrueTypeGlyphProvider.this.shiftX) / TrueTypeGlyphProvider.this.oversample;
         this.bearingY = (TrueTypeGlyphProvider.this.ascent - (float)var4 + TrueTypeGlyphProvider.this.shiftY) / TrueTypeGlyphProvider.this.oversample;
         this.index = var8;
      }

      public int getPixelWidth() {
         return this.width;
      }

      public int getPixelHeight() {
         return this.height;
      }

      public float getOversample() {
         return TrueTypeGlyphProvider.this.oversample;
      }

      public float getAdvance() {
         return this.advance;
      }

      public float getBearingX() {
         return this.bearingX;
      }

      public float getBearingY() {
         return this.bearingY;
      }

      public void upload(int var1, int var2) {
         NativeImage var3 = new NativeImage(Format.LUMINANCE, this.width, this.height, false);
         var3.copyFromFont(TrueTypeGlyphProvider.this.font, this.index, this.width, this.height, TrueTypeGlyphProvider.this.pointScale, TrueTypeGlyphProvider.this.pointScale, TrueTypeGlyphProvider.this.shiftX, TrueTypeGlyphProvider.this.shiftY, 0, 0);
         var3.upload(0, var1, var2, 0, 0, this.width, this.height, false, true);
      }

      public boolean isColored() {
         return false;
      }
   }
}
