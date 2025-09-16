//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

public class PostChain implements AutoCloseable {
   private final RenderTarget screenTarget;
   private final ResourceManager resourceManager;
   private final String name;
   private final List<PostPass> passes = Lists.newArrayList();
   private final Map<String, RenderTarget> customRenderTargets = Maps.newHashMap();
   private final List<RenderTarget> fullSizedTargets = Lists.newArrayList();
   private Matrix4f shaderOrthoMatrix;
   private int screenWidth;
   private int screenHeight;
   private float time;
   private float lastStamp;

   public PostChain(TextureManager var1, ResourceManager var2, RenderTarget var3, ResourceLocation var4) throws IOException, JsonSyntaxException {
      this.resourceManager = var2;
      this.screenTarget = var3;
      this.time = 0.0F;
      this.lastStamp = 0.0F;
      this.screenWidth = var3.viewWidth;
      this.screenHeight = var3.viewHeight;
      this.name = var4.toString();
      this.updateOrthoMatrix();
      this.load(var1, var4);
   }

   private void load(TextureManager var1, ResourceLocation var2) throws IOException, JsonSyntaxException {
      Resource var3 = null;

      try {
         var3 = this.resourceManager.getResource(var2);
         JsonObject var4 = GsonHelper.parse(new InputStreamReader(var3.getInputStream(), StandardCharsets.UTF_8));
         if (GsonHelper.isArrayNode(var4, "targets")) {
            JsonArray var20 = var4.getAsJsonArray("targets");
            int var22 = 0;

            for(JsonElement var8 : var20) {
               try {
                  this.parseTargetNode(var8);
               } catch (Exception var9_1) {
                  ChainedJsonException var10 = ChainedJsonException.forException(var9_1);
                  var10.prependJsonKey("targets[" + var22 + "]");
                  throw var10;
               }

               ++var22;
            }
         }

         if (GsonHelper.isArrayNode(var4, "passes")) {
            JsonArray var21 = var4.getAsJsonArray("passes");
            int var23 = 0;

            for(JsonElement var25 : var21) {
               try {
                  this.parsePassNode(var1, var25);
               } catch (Exception var9_3) {
                  ChainedJsonException var26 = ChainedJsonException.forException(var9_3);
                  var26.prependJsonKey("passes[" + var23 + "]");
                  throw var26;
               }

               ++var23;
            }
         }
      } catch (Exception var4_1) {
         String var5;
         if (var3 != null) {
            var5 = " (" + var3.getSourceName() + ")";
         } else {
            var5 = "";
         }

         ChainedJsonException var6 = ChainedJsonException.forException(var4_1);
         var6.setFilenameAndFlush(var2.getPath() + var5);
         throw var6;
      } finally {
         IOUtils.closeQuietly(var3);
      }

   }

   private void parseTargetNode(JsonElement var1) throws ChainedJsonException {
      if (GsonHelper.isStringValue(var1)) {
         this.addTempTarget(var1.getAsString(), this.screenWidth, this.screenHeight);
      } else {
         JsonObject var2 = GsonHelper.convertToJsonObject(var1, "target");
         String var3 = GsonHelper.getAsString(var2, "name");
         int var4 = GsonHelper.getAsInt(var2, "width", this.screenWidth);
         int var5 = GsonHelper.getAsInt(var2, "height", this.screenHeight);
         if (this.customRenderTargets.containsKey(var3)) {
            throw new ChainedJsonException(var3 + " is already defined");
         }

         this.addTempTarget(var3, var4, var5);
      }

   }

   private void parsePassNode(TextureManager var1, JsonElement var2) throws IOException {
      JsonObject var3 = GsonHelper.convertToJsonObject(var2, "pass");
      String var4 = GsonHelper.getAsString(var3, "name");
      String var5 = GsonHelper.getAsString(var3, "intarget");
      String var6 = GsonHelper.getAsString(var3, "outtarget");
      RenderTarget var7 = this.getRenderTarget(var5);
      RenderTarget var8 = this.getRenderTarget(var6);
      if (var7 == null) {
         throw new ChainedJsonException("Input target '" + var5 + "' does not exist");
      } else if (var8 == null) {
         throw new ChainedJsonException("Output target '" + var6 + "' does not exist");
      } else {
         PostPass var9 = this.addPass(var4, var7, var8);
         JsonArray var10 = GsonHelper.getAsJsonArray(var3, "auxtargets", (JsonArray)null);
         if (var10 != null) {
            int var11 = 0;

            for(JsonElement var13 : var10) {
               try {
                  JsonObject var14 = GsonHelper.convertToJsonObject(var13, "auxtarget");
                  String var38 = GsonHelper.getAsString(var14, "name");
                  String var16 = GsonHelper.getAsString(var14, "id");
                  boolean var17;
                  String var18;
                  if (var16.endsWith(":depth")) {
                     var17 = true;
                     var18 = var16.substring(0, var16.lastIndexOf(58));
                  } else {
                     var17 = false;
                     var18 = var16;
                  }

                  RenderTarget var19 = this.getRenderTarget(var18);
                  if (var19 == null) {
                     if (var17) {
                        throw new ChainedJsonException("Render target '" + var18 + "' can't be used as depth buffer");
                     }

                     ResourceLocation var20 = new ResourceLocation("textures/effect/" + var18 + ".png");
                     Resource var21 = null;

                     try {
                        var21 = this.resourceManager.getResource(var20);
                     } catch (FileNotFoundException var31) {
                        throw new ChainedJsonException("Render target or texture '" + var18 + "' does not exist");
                     } finally {
                        IOUtils.closeQuietly(var21);
                     }

                     var1.bind(var20);
                     AbstractTexture var22 = var1.getTexture(var20);
                     int var23 = GsonHelper.getAsInt(var14, "width");
                     int var24 = GsonHelper.getAsInt(var14, "height");
                     boolean var25 = GsonHelper.getAsBoolean(var14, "bilinear");
                     if (var25) {
                        RenderSystem.texParameter(3553, 10241, 9729);
                        RenderSystem.texParameter(3553, 10240, 9729);
                     } else {
                        RenderSystem.texParameter(3553, 10241, 9728);
                        RenderSystem.texParameter(3553, 10240, 9728);
                     }

                     var9.addAuxAsset(var38, var22::getId, var23, var24);
                  } else if (var17) {
                     var9.addAuxAsset(var38, var19::getDepthTextureId, var19.width, var19.height);
                  } else {
                     var9.addAuxAsset(var38, var19::getColorTextureId, var19.width, var19.height);
                  }
               } catch (Exception var14_1) {
                  ChainedJsonException var15 = ChainedJsonException.forException(var14_1);
                  var15.prependJsonKey("auxtargets[" + var11 + "]");
                  throw var15;
               }

               ++var11;
            }
         }

         JsonArray var34 = GsonHelper.getAsJsonArray(var3, "uniforms", (JsonArray)null);
         if (var34 != null) {
            int var35 = 0;

            for(JsonElement var37 : var34) {
               try {
                  this.parseUniformNode(var37);
               } catch (Exception var15_8) {
                  ChainedJsonException var39 = ChainedJsonException.forException(var15_8);
                  var39.prependJsonKey("uniforms[" + var35 + "]");
                  throw var39;
               }

               ++var35;
            }
         }

      }
   }

   private void parseUniformNode(JsonElement var1) throws ChainedJsonException {
      JsonObject var2 = GsonHelper.convertToJsonObject(var1, "uniform");
      String var3 = GsonHelper.getAsString(var2, "name");
      Uniform var4 = ((PostPass)this.passes.get(this.passes.size() - 1)).getEffect().getUniform(var3);
      if (var4 == null) {
         throw new ChainedJsonException("Uniform '" + var3 + "' does not exist");
      } else {
         float[] var5 = new float[4];
         int var6 = 0;

         for(JsonElement var9 : GsonHelper.getAsJsonArray(var2, "values")) {
            try {
               var5[var6] = GsonHelper.convertToFloat(var9, "value");
            } catch (Exception var10_1) {
               ChainedJsonException var11 = ChainedJsonException.forException(var10_1);
               var11.prependJsonKey("values[" + var6 + "]");
               throw var11;
            }

            ++var6;
         }

         switch (var6) {
            case 0:
            default:
               break;
            case 1:
               var4.set(var5[0]);
               break;
            case 2:
               var4.set(var5[0], var5[1]);
               break;
            case 3:
               var4.set(var5[0], var5[1], var5[2]);
               break;
            case 4:
               var4.set(var5[0], var5[1], var5[2], var5[3]);
         }

      }
   }

   public RenderTarget getTempTarget(String var1) {
      return (RenderTarget)this.customRenderTargets.get(var1);
   }

   public void addTempTarget(String var1, int var2, int var3) {
      RenderTarget var4 = new RenderTarget(var2, var3, true, Minecraft.ON_OSX);
      var4.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.customRenderTargets.put(var1, var4);
      if (var2 == this.screenWidth && var3 == this.screenHeight) {
         this.fullSizedTargets.add(var4);
      }

   }

   public void close() {
      for(RenderTarget var2 : this.customRenderTargets.values()) {
         var2.destroyBuffers();
      }

      for(PostPass var4 : this.passes) {
         var4.close();
      }

      this.passes.clear();
   }

   public PostPass addPass(String var1, RenderTarget var2, RenderTarget var3) throws IOException {
      PostPass var4 = new PostPass(this.resourceManager, var1, var2, var3);
      this.passes.add(this.passes.size(), var4);
      return var4;
   }

   private void updateOrthoMatrix() {
      this.shaderOrthoMatrix = Matrix4f.orthographic((float)this.screenTarget.width, (float)this.screenTarget.height, 0.1F, 1000.0F);
   }

   public void resize(int var1, int var2) {
      this.screenWidth = this.screenTarget.width;
      this.screenHeight = this.screenTarget.height;
      this.updateOrthoMatrix();

      for(PostPass var4 : this.passes) {
         var4.setOrthoMatrix(this.shaderOrthoMatrix);
      }

      for(RenderTarget var6 : this.fullSizedTargets) {
         var6.resize(var1, var2, Minecraft.ON_OSX);
      }

   }

   public void process(float var1) {
      if (var1 < this.lastStamp) {
         this.time += 1.0F - this.lastStamp;
         this.time += var1;
      } else {
         this.time += var1 - this.lastStamp;
      }

      for(this.lastStamp = var1; this.time > 20.0F; this.time -= 20.0F) {
      }

      for(PostPass var3 : this.passes) {
         var3.process(this.time / 20.0F);
      }

   }

   public final String getName() {
      return this.name;
   }

   private RenderTarget getRenderTarget(String var1) {
      if (var1 == null) {
         return null;
      } else {
         return var1.equals("minecraft:main") ? this.screenTarget : (RenderTarget)this.customRenderTargets.get(var1);
      }
   }
}
