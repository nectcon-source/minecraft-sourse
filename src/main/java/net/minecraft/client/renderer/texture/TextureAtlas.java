//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.InventoryMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextureAtlas extends AbstractTexture implements Tickable {
   private static final Logger LOGGER = LogManager.getLogger();
   @Deprecated
   public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
   @Deprecated
   public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
   private final List<TextureAtlasSprite> animatedTextures = Lists.newArrayList();
   private final Set<ResourceLocation> sprites = Sets.newHashSet();
   private final Map<ResourceLocation, TextureAtlasSprite> texturesByName = Maps.newHashMap();
   private final ResourceLocation location;
   private final int maxSupportedTextureSize;

   public TextureAtlas(ResourceLocation var1) {
      this.location = var1;
      this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
   }

   public void load(ResourceManager var1) throws IOException {}

   public void reload(Preparations var1) {
      this.sprites.clear();
      this.sprites.addAll(var1.sprites);
      LOGGER.info("Created: {}x{}x{} {}-atlas", var1.width, var1.height, var1.mipLevel, this.location);
      TextureUtil.prepareImage(this.getId(), var1.mipLevel, var1.width, var1.height);
      this.clearTextureData();

      for(TextureAtlasSprite var3 : var1.regions) {
         this.texturesByName.put(var3.getName(), var3);

         try {
            var3.uploadFirstFrame();
         } catch (Throwable var4_1) {
            CrashReport var5 = CrashReport.forThrowable(var4_1, "Stitching texture atlas");
            CrashReportCategory var6 = var5.addCategory("Texture being stitched together");
            var6.setDetail("Atlas path", this.location);
            var6.setDetail("Sprite", var3);
            throw new ReportedException(var5);
         }

         if (var3.isAnimation()) {
            this.animatedTextures.add(var3);
         }
      }

   }

   public Preparations prepareToStitch(ResourceManager var1, Stream<ResourceLocation> var2, ProfilerFiller var3, int var4) {
      var3.push("preparing");
      Set<ResourceLocation> var5 = var2.peek((var0) -> {
         if (var0 == null) {
            throw new IllegalArgumentException("Location cannot be null!");
         }
      }).collect(Collectors.toSet());
      int var6 = this.maxSupportedTextureSize;
      Stitcher var7 = new Stitcher(var6, var6, var4);
      int var8 = Integer.MAX_VALUE;
      int var9 = 1 << var4;
      var3.popPush("extracting_frames");

      for(TextureAtlasSprite.Info var11 : this.getBasicSpriteInfos(var1, var5)) {
         var8 = Math.min(var8, Math.min(var11.width(), var11.height()));
         int var12 = Math.min(Integer.lowestOneBit(var11.width()), Integer.lowestOneBit(var11.height()));
         if (var12 < var9) {
            LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", var11.name(), var11.width(), var11.height(), Mth.log2(var9), Mth.log2(var12));
            var9 = var12;
         }

         var7.registerSprite(var11);
      }

      int var17 = Math.min(var8, var9);
      int var18 = Mth.log2(var17);
      int var19;
      if (var18 < var4) {
         LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, var4, var18, var17);
         var19 = var18;
      } else {
         var19 = var4;
      }

      var3.popPush("register");
      var7.registerSprite(MissingTextureAtlasSprite.info());
      var3.popPush("stitching");

      try {
         var7.stitch();
      } catch (StitcherException var13_1) {
         CrashReport var14 = CrashReport.forThrowable(var13_1, "Stitching");
         CrashReportCategory var15 = var14.addCategory("Stitcher");
         var15.setDetail("Sprites", var13_1.getAllSprites().stream().map((var0) -> String.format("%s[%dx%d]", var0.name(), var0.width(), var0.height())).collect(Collectors.joining(",")));
         var15.setDetail("Max Texture Size", var6);
         throw new ReportedException(var14);
      }

      var3.popPush("loading");
      List<TextureAtlasSprite> var13 = this.getLoadedSprites(var1, var7, var19);
      var3.pop();
      return new Preparations(var5, var7.getWidth(), var7.getHeight(), var19, var13);
   }

   private Collection<TextureAtlasSprite.Info> getBasicSpriteInfos(ResourceManager var1, Set<ResourceLocation> var2) {
      List<CompletableFuture<?>> var3 = Lists.newArrayList();
      ConcurrentLinkedQueue<TextureAtlasSprite.Info> var4 = new ConcurrentLinkedQueue();

      for(ResourceLocation var6 : var2) {
         if (!MissingTextureAtlasSprite.getLocation().equals(var6)) {
            var3.add(CompletableFuture.runAsync(() -> {
               ResourceLocation var4x = this.getResourceLocation(var6);

               TextureAtlasSprite.Info var5;
               try (Resource var6x = var1.getResource(var4x)) {
                  PngInfo var8 = new PngInfo(var6x.toString(), var6x.getInputStream());
                  AnimationMetadataSection var9 = (AnimationMetadataSection)var6x.getMetadata(AnimationMetadataSection.SERIALIZER);
                  if (var9 == null) {
                     var9 = AnimationMetadataSection.EMPTY;
                  }

                  Pair<Integer, Integer> var10 = var9.getFrameSize(var8.width, var8.height);
                  var5 = new TextureAtlasSprite.Info(var6, (Integer)var10.getFirst(), (Integer)var10.getSecond(), var9);
               } catch (RuntimeException var6_3) {
                  LOGGER.error("Unable to parse metadata from {} : {}", var4x, var6_3);
                  return;
               } catch (IOException var6_1) {
                  LOGGER.error("Using missing texture, unable to load {} : {}", var4x, var6_1);
                  return;
               }

               var4.add(var5);
            }, Util.backgroundExecutor()));
         }
      }

      CompletableFuture.allOf(var3.toArray(new CompletableFuture[0])).join();
      return var4;
   }

   private List<TextureAtlasSprite> getLoadedSprites(ResourceManager var1, Stitcher var2, int var3) {
      ConcurrentLinkedQueue<TextureAtlasSprite> var4 = new ConcurrentLinkedQueue<TextureAtlasSprite>();
      List<CompletableFuture<?>> var5 = Lists.newArrayList();
      var2.gatherSprites((var5x, var6, var7, var8, var9) -> {
         if (var5x == MissingTextureAtlasSprite.info()) {
            MissingTextureAtlasSprite var10 = MissingTextureAtlasSprite.newInstance(this, var3, var6, var7, var8, var9);
            var4.add(var10);
         } else {
            var5.add(CompletableFuture.runAsync(() -> {
               TextureAtlasSprite var9x = this.load(var1, var5x, var6, var7, var3, var8, var9);
               if (var9x != null) {
                  var4.add(var9x);
               }

            }, Util.backgroundExecutor()));
         }

      });
      CompletableFuture.allOf(var5.toArray(new CompletableFuture[0])).join();
      return Lists.newArrayList(var4);
   }
   

   @Nullable
   private TextureAtlasSprite load(ResourceManager var1, TextureAtlasSprite.Info var2, int var3, int var4, int var5, int var6, int var7) {
      ResourceLocation var8 = this.getResourceLocation(var2.name());

      try (Resource var9 = var1.getResource(var8)) {
         NativeImage var11 = NativeImage.read(var9.getInputStream());
         TextureAtlasSprite var12 = new TextureAtlasSprite(this, var2, var5, var3, var4, var6, var7, var11);
         return var12;
      } catch (RuntimeException var9_3) {
         LOGGER.error("Unable to parse metadata from {}", var8, var9_3);
         return null;
      } catch (IOException var9_1) {
         LOGGER.error("Using missing texture, unable to load {}", var8, var9_1);
         return null;
      }
   }

   private ResourceLocation getResourceLocation(ResourceLocation var1) {
      return new ResourceLocation(var1.getNamespace(), String.format("textures/%s%s", var1.getPath(), ".png"));
   }

   public void cycleAnimationFrames() {
      this.bind();

      for(TextureAtlasSprite var2 : this.animatedTextures) {
         var2.cycleFrames();
      }

   }

   public void tick() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::cycleAnimationFrames);
      } else {
         this.cycleAnimationFrames();
      }

   }

   public TextureAtlasSprite getSprite(ResourceLocation var1) {
      TextureAtlasSprite var2 = this.texturesByName.get(var1);
      return var2 == null ? this.texturesByName.get(MissingTextureAtlasSprite.getLocation()) : var2;
   }

   public void clearTextureData() {
      for(TextureAtlasSprite var2 : this.texturesByName.values()) {
         var2.close();
      }

      this.texturesByName.clear();
      this.animatedTextures.clear();
   }

   public ResourceLocation location() {
      return this.location;
   }

   public void updateFilter(Preparations var1) {
      this.setFilter(false, var1.mipLevel > 0);
   }



   public static class Preparations {
      final Set<ResourceLocation> sprites;
      final int width;
      final int height;
      final int mipLevel;
      final List<TextureAtlasSprite> regions;

      public Preparations(Set<ResourceLocation> var1, int var2, int var3, int var4, List<TextureAtlasSprite> var5) {
         this.sprites = var1;
         this.width = var2;
         this.height = var3;
         this.mipLevel = var4;
         this.regions = var5;
      }
   }
}
