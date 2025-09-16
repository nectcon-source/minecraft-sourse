//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public abstract class RenderStateShard {
   protected final String name;
   private final Runnable setupState;
   private final Runnable clearState;
   protected static final TransparencyStateShard NO_TRANSPARENCY = new TransparencyStateShard("no_transparency", () -> RenderSystem.disableBlend(), () -> {
   });
   protected static final TransparencyStateShard ADDITIVE_TRANSPARENCY = new TransparencyStateShard("additive_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.ONE, DestFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   protected static final TransparencyStateShard LIGHTNING_TRANSPARENCY = new TransparencyStateShard("lightning_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   protected static final TransparencyStateShard GLINT_TRANSPARENCY = new TransparencyStateShard("glint_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(SourceFactor.SRC_COLOR, DestFactor.ONE, SourceFactor.ZERO, DestFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   protected static final TransparencyStateShard CRUMBLING_TRANSPARENCY = new TransparencyStateShard("crumbling_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(SourceFactor.DST_COLOR, DestFactor.SRC_COLOR, SourceFactor.ONE, DestFactor.ZERO);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   protected static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new TransparencyStateShard("translucent_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   protected static final AlphaStateShard NO_ALPHA = new AlphaStateShard(0.0F);
   protected static final AlphaStateShard DEFAULT_ALPHA = new AlphaStateShard(0.003921569F);
   protected static final AlphaStateShard MIDWAY_ALPHA = new AlphaStateShard(0.5F);
   protected static final ShadeModelStateShard FLAT_SHADE = new ShadeModelStateShard(false);
   protected static final ShadeModelStateShard SMOOTH_SHADE = new ShadeModelStateShard(true);
   protected static final TextureStateShard BLOCK_SHEET_MIPPED  = new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, true);
   protected static final TextureStateShard BLOCK_SHEET = new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false);
   protected static final TextureStateShard NO_TEXTURE  = new TextureStateShard();
   protected static final TexturingStateShard DEFAULT_TEXTURING  = new TexturingStateShard("default_texturing", () -> {
   }, () -> {
   });
   protected static final TexturingStateShard OUTLINE_TEXTURING = new TexturingStateShard("outline_texturing", () -> RenderSystem.setupOutline(), () -> RenderSystem.teardownOutline());
   protected static final TexturingStateShard GLINT_TEXTURING = new TexturingStateShard("glint_texturing", () -> setupGlintTexturing(8.0F), () -> {
       RenderSystem.matrixMode(5890);
       RenderSystem.popMatrix();
       RenderSystem.matrixMode(5888);
   });
   protected static final TexturingStateShard ENTITY_GLINT_TEXTURING = new TexturingStateShard("entity_glint_texturing", () -> setupGlintTexturing(0.16F), () -> {
       RenderSystem.matrixMode(5890);
       RenderSystem.popMatrix();
       RenderSystem.matrixMode(5888);
   });
   protected static final LightmapStateShard LIGHTMAP = new LightmapStateShard(true);
   protected static final LightmapStateShard NO_LIGHTMAP = new LightmapStateShard(false);
   protected static final OverlayStateShard OVERLAY = new OverlayStateShard(true);
   protected static final OverlayStateShard NO_OVERLAY = new OverlayStateShard(false);
   protected static final DiffuseLightingStateShard DIFFUSE_LIGHTING = new DiffuseLightingStateShard(true);
   protected static final DiffuseLightingStateShard NO_DIFFUSE_LIGHTING = new DiffuseLightingStateShard(false);
   protected static final CullStateShard CULL = new CullStateShard(true);
   protected static final CullStateShard NO_CULL = new CullStateShard(false);
   protected static final DepthTestStateShard NO_DEPTH_TEST = new DepthTestStateShard("always", 519);
   protected static final DepthTestStateShard EQUAL_DEPTH_TEST = new DepthTestStateShard("==", 514);
   protected static final DepthTestStateShard LEQUAL_DEPTH_TEST = new DepthTestStateShard("<=", 515);
   protected static final WriteMaskStateShard COLOR_DEPTH_WRITE = new WriteMaskStateShard(true, true);
   protected static final WriteMaskStateShard COLOR_WRITE = new WriteMaskStateShard(true, false);
   protected static final WriteMaskStateShard DEPTH_WRITE = new WriteMaskStateShard(false, true);
   protected static final LayeringStateShard NO_LAYERING = new LayeringStateShard("no_layering", () -> {
   }, () -> {
   });
   protected static final LayeringStateShard POLYGON_OFFSET_LAYERING = new LayeringStateShard("polygon_offset_layering", () -> {
       RenderSystem.polygonOffset(-1.0F, -10.0F);
       RenderSystem.enablePolygonOffset();
   }, () -> {
       RenderSystem.polygonOffset(0.0F, 0.0F);
       RenderSystem.disablePolygonOffset();
   });
   protected static final LayeringStateShard VIEW_OFFSET_Z_LAYERING = new LayeringStateShard("view_offset_z_layering", () -> {
       RenderSystem.pushMatrix();
       RenderSystem.scalef(0.99975586F, 0.99975586F, 0.99975586F);
   }, RenderSystem::popMatrix);
   protected static final FogStateShard NO_FOG = new FogStateShard("no_fog", () -> {
   }, () -> {
   });
   protected static final FogStateShard FOG = new FogStateShard("fog", () -> {
       FogRenderer.levelFogColor();
       RenderSystem.enableFog();
   }, () -> RenderSystem.disableFog());
   protected static final FogStateShard BLACK_FOG = new FogStateShard("black_fog", () -> {
       RenderSystem.fog(2918, 0.0F, 0.0F, 0.0F, 1.0F);
       RenderSystem.enableFog();
   }, () -> {
       FogRenderer.levelFogColor();
       RenderSystem.disableFog();
   });
   protected static final OutputStateShard MAIN_TARGET = new OutputStateShard("main_target", () -> {
   }, () -> {
   });
   protected static final OutputStateShard OUTLINE_TARGET = new OutputStateShard("outline_target", () -> Minecraft.getInstance().levelRenderer.entityTarget().bindWrite(false), () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));
   protected static final OutputStateShard TRANSLUCENT_TARGET = new OutputStateShard("translucent_target", () -> {
       if (Minecraft.useShaderTransparency()) {
           Minecraft.getInstance().levelRenderer.getTranslucentTarget().bindWrite(false);
       }

   }, () -> {
       if (Minecraft.useShaderTransparency()) {
           Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
       }

   });
   protected static final OutputStateShard PARTICLES_TARGET = new OutputStateShard("particles_target", () -> {
       if (Minecraft.useShaderTransparency()) {
           Minecraft.getInstance().levelRenderer.getParticlesTarget().bindWrite(false);
       }

   }, () -> {
       if (Minecraft.useShaderTransparency()) {
           Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
       }

   });
   protected static final OutputStateShard WEATHER_TARGET = new OutputStateShard("weather_target", () -> {
       if (Minecraft.useShaderTransparency()) {
           Minecraft.getInstance().levelRenderer.getWeatherTarget().bindWrite(false);
       }

   }, () -> {
       if (Minecraft.useShaderTransparency()) {
           Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
       }

   });
   protected static final OutputStateShard CLOUDS_TARGET = new OutputStateShard("clouds_target", () -> {
       if (Minecraft.useShaderTransparency()) {
           Minecraft.getInstance().levelRenderer.getCloudsTarget().bindWrite(false);
       }

   }, () -> {
       if (Minecraft.useShaderTransparency()) {
           Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
       }

   });
   protected static final OutputStateShard ITEM_ENTITY_TARGET = new OutputStateShard("item_entity_target", () -> {
       if (Minecraft.useShaderTransparency()) {
           Minecraft.getInstance().levelRenderer.getItemEntityTarget().bindWrite(false);
       }

   }, () -> {
       if (Minecraft.useShaderTransparency()) {
           Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
       }

   });
   protected static final LineStateShard DEFAULT_LINE = new LineStateShard(OptionalDouble.of((double)1.0F));

   public RenderStateShard(String var1, Runnable var2, Runnable var3) {
      this.name = var1;
      this.setupState = var2;
      this.clearState = var3;
   }

   public void setupRenderState() {
      this.setupState.run();
   }

   public void clearRenderState() {
      this.clearState.run();
   }

   public boolean equals(@Nullable Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 != null && this.getClass() == var1.getClass()) {
         RenderStateShard var2 = (RenderStateShard)var1;
         return this.name.equals(var2.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   public String toString() {
      return this.name;
   }

   private static void setupGlintTexturing(float var0) {
      RenderSystem.matrixMode(5890);
      RenderSystem.pushMatrix();
      RenderSystem.loadIdentity();
      long var1 = Util.getMillis() * 8L;
      float var3 = (float)(var1 % 110000L) / 110000.0F;
      float var4 = (float)(var1 % 30000L) / 30000.0F;
      RenderSystem.translatef(-var3, var4, 0.0F);
      RenderSystem.rotatef(10.0F, 0.0F, 0.0F, 1.0F);
      RenderSystem.scalef(var0, var0, var0);
      RenderSystem.matrixMode(5888);
   }



   public static class TransparencyStateShard extends RenderStateShard {
      public TransparencyStateShard(String var1, Runnable var2, Runnable var3) {
         super(var1, var2, var3);
      }
   }

   public static class AlphaStateShard extends RenderStateShard {
      private final float cutoff;

      public AlphaStateShard(float var1) {
         super("alpha", () -> {
            if (var1 > 0.0F) {
               RenderSystem.enableAlphaTest();
               RenderSystem.alphaFunc(516, var1);
            } else {
               RenderSystem.disableAlphaTest();
            }

         }, () -> {
            RenderSystem.disableAlphaTest();
            RenderSystem.defaultAlphaFunc();
         });
         this.cutoff = var1;
      }

      public boolean equals(@Nullable Object var1) {
         if (this == var1) {
            return true;
         } else if (var1 != null && this.getClass() == var1.getClass()) {
            if (!super.equals(var1)) {
               return false;
            } else {
               return this.cutoff == ((AlphaStateShard)var1).cutoff;
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(super.hashCode(), this.cutoff);
      }

      public String toString() {
         return this.name + '[' + this.cutoff + ']';
      }
   }

   public static class ShadeModelStateShard extends RenderStateShard {
      private final boolean smooth;

      public ShadeModelStateShard(boolean var1) {
         super("shade_model", () -> RenderSystem.shadeModel(var1 ? 7425 : 7424), () -> RenderSystem.shadeModel(7424));
         this.smooth = var1;
      }

      public boolean equals(Object var1) {
         if (this == var1) {
            return true;
         } else if (var1 != null && this.getClass() == var1.getClass()) {
            ShadeModelStateShard var2 = (ShadeModelStateShard)var1;
            return this.smooth == var2.smooth;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Boolean.hashCode(this.smooth);
      }

      public String toString() {
         return this.name + '[' + (this.smooth ? "smooth" : "flat") + ']';
      }
   }

   public static class TextureStateShard extends RenderStateShard {
      private final Optional<ResourceLocation> texture;
      private final boolean blur;
      private final boolean mipmap;

      public TextureStateShard(ResourceLocation var1, boolean var2, boolean var3) {
         super("texture", () -> {
            RenderSystem.enableTexture();
            TextureManager var3x = Minecraft.getInstance().getTextureManager();
            var3x.bind(var1);
            var3x.getTexture(var1).setFilter(var2, var3);
         }, () -> {
         });
         this.texture = Optional.of(var1);
         this.blur = var2;
         this.mipmap = var3;
      }

      public TextureStateShard() {
         super("texture", () -> RenderSystem.disableTexture(), () -> RenderSystem.enableTexture());
         this.texture = Optional.empty();
         this.blur = false;
         this.mipmap = false;
      }

      public boolean equals(Object var1) {
         if (this == var1) {
            return true;
         } else if (var1 != null && this.getClass() == var1.getClass()) {
            TextureStateShard var2 = (TextureStateShard)var1;
            return this.texture.equals(var2.texture) && this.blur == var2.blur && this.mipmap == var2.mipmap;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.texture.hashCode();
      }

      public String toString() {
         return this.name + '[' + this.texture + "(blur=" + this.blur + ", mipmap=" + this.mipmap + ")]";
      }

      protected Optional<ResourceLocation> texture() {
         return this.texture;
      }
   }

   public static class TexturingStateShard extends RenderStateShard {
      public TexturingStateShard(String var1, Runnable var2, Runnable var3) {
         super(var1, var2, var3);
      }
   }

   public static final class OffsetTexturingStateShard extends TexturingStateShard {
      private final float uOffset;
      private final float vOffset;

      public OffsetTexturingStateShard(float var1, float var2) {
         super("offset_texturing", () -> {
            RenderSystem.matrixMode(5890);
            RenderSystem.pushMatrix();
            RenderSystem.loadIdentity();
            RenderSystem.translatef(var1, var2, 0.0F);
            RenderSystem.matrixMode(5888);
         }, () -> {
            RenderSystem.matrixMode(5890);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
         });
         this.uOffset = var1;
         this.vOffset = var2;
      }

      public boolean equals(Object var1) {
         if (this == var1) {
            return true;
         } else if (var1 != null && this.getClass() == var1.getClass()) {
            OffsetTexturingStateShard var2 = (OffsetTexturingStateShard)var1;
            return Float.compare(var2.uOffset, this.uOffset) == 0 && Float.compare(var2.vOffset, this.vOffset) == 0;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.uOffset, this.vOffset});
      }
   }

   public static final class PortalTexturingStateShard extends TexturingStateShard {
      private final int iteration;

      public PortalTexturingStateShard(int var1) {
         super("portal_texturing", () -> {
            RenderSystem.matrixMode(5890);
            RenderSystem.pushMatrix();
            RenderSystem.loadIdentity();
            RenderSystem.translatef(0.5F, 0.5F, 0.0F);
            RenderSystem.scalef(0.5F, 0.5F, 1.0F);
            RenderSystem.translatef(17.0F / (float)var1, (2.0F + (float)var1 / 1.5F) * ((float)(Util.getMillis() % 800000L) / 800000.0F), 0.0F);
            RenderSystem.rotatef(((float)(var1 * var1) * 4321.0F + (float)var1 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.scalef(4.5F - (float)var1 / 4.0F, 4.5F - (float)var1 / 4.0F, 1.0F);
            RenderSystem.mulTextureByProjModelView();
            RenderSystem.matrixMode(5888);
            RenderSystem.setupEndPortalTexGen();
         }, () -> {
            RenderSystem.matrixMode(5890);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
            RenderSystem.clearTexGen();
         });
         this.iteration = var1;
      }

      public boolean equals(Object var1) {
         if (this == var1) {
            return true;
         } else if (var1 != null && this.getClass() == var1.getClass()) {
            PortalTexturingStateShard var2 = (PortalTexturingStateShard)var1;
            return this.iteration == var2.iteration;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Integer.hashCode(this.iteration);
      }
   }

   static class BooleanStateShard extends RenderStateShard {
      private final boolean enabled;

      public BooleanStateShard(String var1, Runnable var2, Runnable var3, boolean var4) {
         super(var1, var2, var3);
         this.enabled = var4;
      }

      public boolean equals(Object var1) {
         if (this == var1) {
            return true;
         } else if (var1 != null && this.getClass() == var1.getClass()) {
            BooleanStateShard var2 = (BooleanStateShard)var1;
            return this.enabled == var2.enabled;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Boolean.hashCode(this.enabled);
      }

      public String toString() {
         return this.name + '[' + this.enabled + ']';
      }
   }

   public static class LightmapStateShard extends BooleanStateShard {
      public LightmapStateShard(boolean var1) {
         super("lightmap", () -> {
            if (var1) {
               Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            }

         }, () -> {
            if (var1) {
               Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
            }

         }, var1);
      }
   }

   public static class OverlayStateShard extends BooleanStateShard {
      public OverlayStateShard(boolean var1) {
         super("overlay", () -> {
            if (var1) {
               Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();
            }

         }, () -> {
            if (var1) {
               Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor();
            }

         }, var1);
      }
   }

   public static class DiffuseLightingStateShard extends BooleanStateShard {
      public DiffuseLightingStateShard(boolean var1) {
         super("diffuse_lighting", () -> {
            if (var1) {
               Lighting.turnBackOn();
            }

         }, () -> {
            if (var1) {
               Lighting.turnOff();
            }

         }, var1);
      }
   }

   public static class CullStateShard extends BooleanStateShard {
      public CullStateShard(boolean var1) {
         super("cull", () -> {
            if (!var1) {
               RenderSystem.disableCull();
            }

         }, () -> {
            if (!var1) {
               RenderSystem.enableCull();
            }

         }, var1);
      }
   }

   public static class DepthTestStateShard extends RenderStateShard {
      private final String functionName;
      private final int function;

      public DepthTestStateShard(String var1, int var2) {
         super("depth_test", () -> {
            if (var2 != 519) {
               RenderSystem.enableDepthTest();
               RenderSystem.depthFunc(var2);
            }

         }, () -> {
            if (var2 != 519) {
               RenderSystem.disableDepthTest();
               RenderSystem.depthFunc(515);
            }

         });
         this.functionName = var1;
         this.function = var2;
      }

      public boolean equals(Object var1) {
         if (this == var1) {
            return true;
         } else if (var1 != null && this.getClass() == var1.getClass()) {
            DepthTestStateShard var2 = (DepthTestStateShard)var1;
            return this.function == var2.function;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Integer.hashCode(this.function);
      }

      public String toString() {
         return this.name + '[' + this.functionName + ']';
      }
   }

   public static class WriteMaskStateShard extends RenderStateShard {
      private final boolean writeColor;
      private final boolean writeDepth;

      public WriteMaskStateShard(boolean var1, boolean var2) {
         super("write_mask_state", () -> {
            if (!var2) {
               RenderSystem.depthMask(var2);
            }

            if (!var1) {
               RenderSystem.colorMask(var1, var1, var1, var1);
            }

         }, () -> {
            if (!var2) {
               RenderSystem.depthMask(true);
            }

            if (!var1) {
               RenderSystem.colorMask(true, true, true, true);
            }

         });
         this.writeColor = var1;
         this.writeDepth = var2;
      }

      public boolean equals(Object var1) {
         if (this == var1) {
            return true;
         } else if (var1 != null && this.getClass() == var1.getClass()) {
            WriteMaskStateShard var2 = (WriteMaskStateShard)var1;
            return this.writeColor == var2.writeColor && this.writeDepth == var2.writeDepth;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.writeColor, this.writeDepth});
      }

      public String toString() {
         return this.name + "[writeColor=" + this.writeColor + ", writeDepth=" + this.writeDepth + ']';
      }
   }

   public static class LayeringStateShard extends RenderStateShard {
      public LayeringStateShard(String var1, Runnable var2, Runnable var3) {
         super(var1, var2, var3);
      }
   }

   public static class FogStateShard extends RenderStateShard {
      public FogStateShard(String var1, Runnable var2, Runnable var3) {
         super(var1, var2, var3);
      }
   }

   public static class OutputStateShard extends RenderStateShard {
      public OutputStateShard(String var1, Runnable var2, Runnable var3) {
         super(var1, var2, var3);
      }
   }

   public static class LineStateShard extends RenderStateShard {
      private final OptionalDouble width;

      public LineStateShard(OptionalDouble var1) {
         super("line_width", () -> {
            if (!Objects.equals(var1, OptionalDouble.of((double)1.0F))) {
               if (var1.isPresent()) {
                  RenderSystem.lineWidth((float)var1.getAsDouble());
               } else {
                  RenderSystem.lineWidth(Math.max(2.5F, (float)Minecraft.getInstance().getWindow().getWidth() / 1920.0F * 2.5F));
               }
            }

         }, () -> {
            if (!Objects.equals(var1, OptionalDouble.of((double)1.0F))) {
               RenderSystem.lineWidth(1.0F);
            }

         });
         this.width = var1;
      }

      public boolean equals(@Nullable Object var1) {
         if (this == var1) {
            return true;
         } else if (var1 != null && this.getClass() == var1.getClass()) {
            return !super.equals(var1) ? false : Objects.equals(this.width, ((LineStateShard)var1).width);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{super.hashCode(), this.width});
      }

      public String toString() {
         return this.name + '[' + (this.width.isPresent() ? this.width.getAsDouble() : "window_scale") + ']';
      }
   }
}
