

package net.minecraft.client.particle;

import com.google.common.base.Charsets;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.logging.Log;

public class ParticleEngine implements PreparableReloadListener {
   private static final List<ParticleRenderType> RENDER_ORDER;
   protected ClientLevel level;
   private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.newIdentityHashMap();
   private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
   private final TextureManager textureManager;
   private final Random random = new Random();
   private final Int2ObjectMap<ParticleProvider<?>> providers = new Int2ObjectOpenHashMap();
   private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
   private final Map<ResourceLocation, MutableSpriteSet> spriteSets = Maps.newHashMap();
   private final TextureAtlas textureAtlas;

   public ParticleEngine(ClientLevel var1, TextureManager var2) {
      this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
      var2.register(this.textureAtlas.location(), this.textureAtlas);
      this.level = var1;
      this.textureManager = var2;
      this.registerProviders();
   }

   private void registerProviders() {
      this.register(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobProvider::new);
      this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
      this.register(ParticleTypes.BARRIER, new BarrierParticle.Provider());
      this.register(ParticleTypes.BLOCK, new TerrainParticle.Provider());
      this.register(ParticleTypes.BUBBLE, BubbleParticle.Provider::new);
      this.register(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Provider::new);
      this.register(ParticleTypes.BUBBLE_POP, BubblePopParticle.Provider::new);
      this.register(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosyProvider::new);
      this.register(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalProvider::new);
      this.register(ParticleTypes.CLOUD, PlayerCloudParticle.Provider::new);
      this.register(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFillProvider::new);
      this.register(ParticleTypes.CRIT, CritParticle.Provider::new);
      this.register(ParticleTypes.CURRENT_DOWN, WaterCurrentDownParticle.Provider::new);
      this.register(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorProvider::new);
      this.register(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
      this.register(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedProvider::new);
      this.register(ParticleTypes.DRIPPING_LAVA, DripParticle.LavaHangProvider::new);
      this.register(ParticleTypes.FALLING_LAVA, DripParticle.LavaFallProvider::new);
      this.register(ParticleTypes.LANDING_LAVA, DripParticle.LavaLandProvider::new);
      this.register(ParticleTypes.DRIPPING_WATER, DripParticle.WaterHangProvider::new);
      this.register(ParticleTypes.FALLING_WATER, DripParticle.WaterFallProvider::new);
      this.register(ParticleTypes.DUST, DustParticle.Provider::new);
      this.register(ParticleTypes.EFFECT, SpellParticle.Provider::new);
      this.register(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
      this.register(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
      this.register(ParticleTypes.ENCHANT, EnchantmentTableParticle.Provider::new);
      this.register(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
      this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobProvider::new);
      this.register(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
      this.register(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
      this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
      this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
      this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
      this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
      this.register(ParticleTypes.SOUL, SoulParticle.Provider::new);
      this.register(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Provider::new);
      this.register(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
      this.register(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerProvider::new);
      this.register(ParticleTypes.HEART, HeartParticle.Provider::new);
      this.register(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
      this.register(ParticleTypes.ITEM, new BreakingItemParticle.Provider());
      this.register(ParticleTypes.ITEM_SLIME, new BreakingItemParticle.SlimeProvider());
      this.register(ParticleTypes.ITEM_SNOWBALL, new BreakingItemParticle.SnowballProvider());
      this.register(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Provider::new);
      this.register(ParticleTypes.LAVA, LavaParticle.Provider::new);
      this.register(ParticleTypes.MYCELIUM, SuspendedTownParticle.Provider::new);
      this.register(ParticleTypes.NAUTILUS, EnchantmentTableParticle.NautilusProvider::new);
      this.register(ParticleTypes.NOTE, NoteParticle.Provider::new);
      this.register(ParticleTypes.POOF, ExplodeParticle.Provider::new);
      this.register(ParticleTypes.PORTAL, PortalParticle.Provider::new);
      this.register(ParticleTypes.RAIN, WaterDropParticle.Provider::new);
      this.register(ParticleTypes.SMOKE, SmokeParticle.Provider::new);
      this.register(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
      this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
      this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
      this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
      this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
      this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
      this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
      this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
      this.register(ParticleTypes.DRIPPING_HONEY, DripParticle.HoneyHangProvider::new);
      this.register(ParticleTypes.FALLING_HONEY, DripParticle.HoneyFallProvider::new);
      this.register(ParticleTypes.LANDING_HONEY, DripParticle.HoneyLandProvider::new);
      this.register(ParticleTypes.FALLING_NECTAR, DripParticle.NectarFallProvider::new);
      this.register(ParticleTypes.ASH, AshParticle.Provider::new);
      this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
      this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
      this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle.ObsidianTearHangProvider::new);
      this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle.ObsidianTearFallProvider::new);
      this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle.ObsidianTearLandProvider::new);
      this.register(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
      this.register(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
   }

   private <T extends ParticleOptions> void register(ParticleType<T> var1, ParticleProvider<T> var2) {
      this.providers.put(Registry.PARTICLE_TYPE.getId(var1), var2);
   }

   private <T extends ParticleOptions> void register(ParticleType<T> var1, SpriteParticleRegistration<T> var2) {
      MutableSpriteSet var3 = new MutableSpriteSet();
      this.spriteSets.put(Registry.PARTICLE_TYPE.getKey(var1), var3);
      this.providers.put(Registry.PARTICLE_TYPE.getId(var1), var2.create(var3));
   }

   public CompletableFuture<Void> reload(PreparationBarrier var1, ResourceManager var2, ProfilerFiller var3, ProfilerFiller var4, Executor var5, Executor var6) {
      Map<ResourceLocation, List<ResourceLocation>> var7 = Maps.newConcurrentMap();
      CompletableFuture<?>[] var8 = Registry.PARTICLE_TYPE.keySet().stream().map((var4x) -> CompletableFuture.runAsync(() -> this.loadParticleDescription(var2, var4x, var7), var5)).toArray((var0) -> new CompletableFuture[var0]);

      return CompletableFuture.allOf(var8).thenApplyAsync((var4x) -> {
         var3.startTick();
         var3.push("stitching");
         TextureAtlas.Preparations var5_ = this.textureAtlas.prepareToStitch(var2, var7.values().stream().flatMap(Collection::stream), var3, 0);
         var3.pop();
         var3.endTick();
         return var5_;
      }, var5).thenCompose(var1::wait).thenAcceptAsync((var3x) -> {
         this.particles.clear();
         var4.startTick();
         var4.push("upload");
         this.textureAtlas.reload(var3x);
         var4.popPush("bindSpriteSets");
         TextureAtlasSprite var4x = this.textureAtlas.getSprite(MissingTextureAtlasSprite.getLocation());
//         var7.forEach((var2_, var3_) -> {
//            ImmutableList var10000;
//            if (var3_.isEmpty()) {
//               var10000 = ImmutableList.of(var4x);
//            } else {
//               var10000 = var3_.stream().map(this.textureAtlas::getSprite).collect(ImmutableList.toImmutableList());
//            }
//
//            this.spriteSets.get(var2).rebind(var10000);
//         });
          var7.forEach((particleId, textureList) -> {
              MutableSpriteSet spriteSet = this.spriteSets.get(particleId);
                  ImmutableList<TextureAtlasSprite> sprites = textureList.isEmpty()
                          ? ImmutableList.of(var4x)
                          : textureList.stream()
                          .map(this.textureAtlas::getSprite)
                          .collect(ImmutableList.toImmutableList());
                  spriteSet.rebind(sprites);

          });
         var4.pop();
         var4.endTick();
      }, var6);
   }

   public void close() {
      this.textureAtlas.clearTextureData();
   }

   private void loadParticleDescription(ResourceManager var1, ResourceLocation var2, Map<ResourceLocation, List<ResourceLocation>> var3) {
      ResourceLocation var4 = new ResourceLocation(var2.getNamespace(), "particles/" + var2.getPath() + ".json");

      try (Resource var5 = var1.getResource(var4)) {
         Reader var7 = new InputStreamReader(var5.getInputStream(), Charsets.UTF_8);
         Throwable var8 = null;

         try {
            ParticleDescription var9 = ParticleDescription.fromJson(GsonHelper.parse(var7));
            List<ResourceLocation> var10 = var9.getTextures();
            boolean var11 = this.spriteSets.containsKey(var2);
            if (var10 == null) {
               if (var11) {
                  throw new IllegalStateException("Missing texture list for particle " + var2);
               }
            } else {
               if (!var11) {
                  throw new IllegalStateException("Redundant texture list for particle " + var2);
               }

               var3.put(var2, var10.stream().map((var0) -> new ResourceLocation(var0.getNamespace(), "particle/" + var0.getPath())).collect(Collectors.toList()));
            }
         } catch (Throwable var35) {
            var8 = var35;
            throw var35;
         } finally {
            if (var7 != null) {
               if (var8 != null) {
                  try {
                     var7.close();
                  } catch (Throwable var34) {
                     var8.addSuppressed(var34);
                  }
               } else {
                  var7.close();
               }
            }

         }

      } catch (IOException var5_1) {
         throw new IllegalStateException("Failed to load description for particle " + var2, var5_1);
      }
   }

   public void createTrackingEmitter(Entity var1, ParticleOptions var2) {
      this.trackingEmitters.add(new TrackingEmitter(this.level, var1, var2));
   }

   public void createTrackingEmitter(Entity var1, ParticleOptions var2, int var3) {
      this.trackingEmitters.add(new TrackingEmitter(this.level, var1, var2, var3));
   }

   @Nullable
   public Particle createParticle(ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      Particle var14 = this.makeParticle(var1, var2, var4, var6, var8, var10, var12);
      if (var14 != null) {
         this.add(var14);
         return var14;
      } else {
         return null;
      }
   }

   @Nullable
   private <T extends ParticleOptions> Particle makeParticle(T var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      ParticleProvider<T> var14 = (ParticleProvider)this.providers.get(Registry.PARTICLE_TYPE.getId(var1.getType()));
      return var14 == null ? null : var14.createParticle(var1, this.level, var2, var4, var6, var8, var10, var12);
   }

   public void add(Particle var1) {
      this.particlesToAdd.add(var1);
   }

   public void tick() {
      this.particles.forEach((var1x, var2) -> {
         this.level.getProfiler().push(var1x.toString());
         this.tickParticleList(var2);
         this.level.getProfiler().pop();
      });
      if (!this.trackingEmitters.isEmpty()) {
         List<TrackingEmitter> var1 = Lists.newArrayList();

         for(TrackingEmitter var3 : this.trackingEmitters) {
            var3.tick();
            if (!var3.isAlive()) {
               var1.add(var3);
            }
         }

         this.trackingEmitters.removeAll(var1);
      }

      Particle var4;
      if (!this.particlesToAdd.isEmpty()) {
         while((var4 = this.particlesToAdd.poll()) != null) {
            (this.particles.computeIfAbsent(var4.getRenderType(), (var0) -> EvictingQueue.create(16384))).add(var4);
         }
      }

   }

   private void tickParticleList(Collection<Particle> var1) {
      if (!var1.isEmpty()) {
         Iterator<Particle> var2 = var1.iterator();

         while(var2.hasNext()) {
            Particle var3 = var2.next();
            this.tickParticle(var3);
            if (!var3.isAlive()) {
               var2.remove();
            }
         }
      }

   }

   private void tickParticle(Particle var1) {
      try {
         var1.tick();
      } catch (Throwable var2_1) {
         CrashReport var3 = CrashReport.forThrowable(var2_1, "Ticking Particle");
         CrashReportCategory var4 = var3.addCategory("Particle being ticked");
         var4.setDetail("Particle", var1::toString);
         var4.setDetail("Particle Type",  var1.getRenderType()::toString);
         throw new ReportedException(var3);
      }
   }

   public void render(PoseStack var1, MultiBufferSource.BufferSource var2, LightTexture var3, Camera var4, float var5) {
      var3.turnOnLightLayer();
      RenderSystem.enableAlphaTest();
      RenderSystem.defaultAlphaFunc();
      RenderSystem.enableDepthTest();
      RenderSystem.enableFog();
      RenderSystem.pushMatrix();
      RenderSystem.multMatrix(var1.last().pose());

      for(ParticleRenderType var7 : RENDER_ORDER) {
         Iterable<Particle> var8 = this.particles.get(var7);
         if (var8 != null) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Tesselator var9 = Tesselator.getInstance();
            BufferBuilder var10 = var9.getBuilder();
            var7.begin(var10, this.textureManager);

            for(Particle var12 : var8) {
               try {
                  var12.render(var10, var4, var5);
               } catch (Throwable var13_1) {
                  CrashReport var14 = CrashReport.forThrowable(var13_1, "Rendering Particle");
                  CrashReportCategory var15 = var14.addCategory("Particle being rendered");
                  var15.setDetail("Particle", var12::toString);
                  var15.setDetail("Particle Type", var7::toString);
                  throw new ReportedException(var14);
               }
            }

            var7.end(var9);
         }
      }

      RenderSystem.popMatrix();
      RenderSystem.depthMask(true);
      RenderSystem.depthFunc(515);
      RenderSystem.disableBlend();
      RenderSystem.defaultAlphaFunc();
      var3.turnOffLightLayer();
      RenderSystem.disableFog();
   }

   public void setLevel(@Nullable ClientLevel var1) {
      this.level = var1;
      this.particles.clear();
      this.trackingEmitters.clear();
   }

   public void destroy(BlockPos var1, BlockState var2) {
      if (!var2.isAir()) {
         VoxelShape var3 = var2.getShape(this.level, var1);
         double var4 = (double)0.25F;
         var3.forAllBoxes((var3x, var5, var7, var9, var11, var13) -> {
            double var15 = Math.min((double)1.0F, var9 - var3x);
            double var17 = Math.min((double)1.0F, var11 - var5);
            double var19 = Math.min((double)1.0F, var13 - var7);
            int var21 = Math.max(2, Mth.ceil(var15 / (double)0.25F));
            int var22 = Math.max(2, Mth.ceil(var17 / (double)0.25F));
            int var23 = Math.max(2, Mth.ceil(var19 / (double)0.25F));

            for(int var24 = 0; var24 < var21; ++var24) {
               for(int var25 = 0; var25 < var22; ++var25) {
                  for(int var26 = 0; var26 < var23; ++var26) {
                     double var27 = ((double)var24 + (double)0.5F) / (double)var21;
                     double var29 = ((double)var25 + (double)0.5F) / (double)var22;
                     double var31 = ((double)var26 + (double)0.5F) / (double)var23;
                     double var33 = var27 * var15 + var3x;
                     double var35 = var29 * var17 + var5;
                     double var37 = var31 * var19 + var7;
                     this.add((new TerrainParticle(this.level, (double)var1.getX() + var33, (double)var1.getY() + var35, (double)var1.getZ() + var37, var27 - (double)0.5F, var29 - (double)0.5F, var31 - (double)0.5F, var2)).init(var1));
                  }
               }
            }

         });
      }
   }

   public void crack(BlockPos var1, Direction var2) {
      BlockState var3 = this.level.getBlockState(var1);
      if (var3.getRenderShape() != RenderShape.INVISIBLE) {
         int var4 = var1.getX();
         int var5 = var1.getY();
         int var6 = var1.getZ();
         float var7 = 0.1F;
         AABB var8 = var3.getShape(this.level, var1).bounds();
         double var9 = (double)var4 + this.random.nextDouble() * (var8.maxX - var8.minX - (double)0.2F) + (double)0.1F + var8.minX;
         double var11 = (double)var5 + this.random.nextDouble() * (var8.maxY - var8.minY - (double)0.2F) + (double)0.1F + var8.minY;
         double var13 = (double)var6 + this.random.nextDouble() * (var8.maxZ - var8.minZ - (double)0.2F) + (double)0.1F + var8.minZ;
         if (var2 == Direction.DOWN) {
            var11 = (double)var5 + var8.minY - (double)0.1F;
         }

         if (var2 == Direction.UP) {
            var11 = (double)var5 + var8.maxY + (double)0.1F;
         }

         if (var2 == Direction.NORTH) {
            var13 = (double)var6 + var8.minZ - (double)0.1F;
         }

         if (var2 == Direction.SOUTH) {
            var13 = (double)var6 + var8.maxZ + (double)0.1F;
         }

         if (var2 == Direction.WEST) {
            var9 = (double)var4 + var8.minX - (double)0.1F;
         }

         if (var2 == Direction.EAST) {
            var9 = (double)var4 + var8.maxX + (double)0.1F;
         }

         this.add((new TerrainParticle(this.level, var9, var11, var13, (double)0.0F, (double)0.0F, (double)0.0F, var3)).init(var1).setPower(0.2F).scale(0.6F));
      }
   }

   public String countParticles() {
      return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
   }

   static {
      RENDER_ORDER = ImmutableList.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_LIT, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, ParticleRenderType.CUSTOM);
   }

   class MutableSpriteSet implements SpriteSet {
      private List<TextureAtlasSprite> sprites;

      private MutableSpriteSet() {
      }

      public TextureAtlasSprite get(int var1, int var2) {
         return this.sprites.get(var1 * (this.sprites.size() - 1) / var2);
      }

      public TextureAtlasSprite get(Random var1) {
         return this.sprites.get(var1.nextInt(this.sprites.size()));
      }

      public void rebind(List<TextureAtlasSprite> var1) {
         this.sprites = ImmutableList.copyOf(var1);
      }
   }

   @FunctionalInterface
   interface SpriteParticleRegistration<T extends ParticleOptions> {
      ParticleProvider<T> create(SpriteSet var1);
   }
}
