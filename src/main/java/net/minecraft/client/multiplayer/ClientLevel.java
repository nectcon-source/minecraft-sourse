package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;

public class ClientLevel extends Level {
   private final Int2ObjectMap<Entity> entitiesById = new Int2ObjectOpenHashMap();
   private final ClientPacketListener connection;
   private final LevelRenderer levelRenderer;
   private final ClientLevel.ClientLevelData clientLevelData;
   private final DimensionSpecialEffects effects;
   private final Minecraft minecraft = Minecraft.getInstance();
   private final List<AbstractClientPlayer> players = Lists.newArrayList();
   private Scoreboard scoreboard = new Scoreboard();
   private final Map<String, MapItemSavedData> mapData = Maps.newHashMap();
   private int skyFlashTime;
   private final Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches = Util.make(new Object2ObjectArrayMap(3), var0 -> {
      var0.put(BiomeColors.GRASS_COLOR_RESOLVER, new BlockTintCache());
      var0.put(BiomeColors.FOLIAGE_COLOR_RESOLVER, new BlockTintCache());
      var0.put(BiomeColors.WATER_COLOR_RESOLVER, new BlockTintCache());
   });
   private final ClientChunkCache chunkSource;

   public ClientLevel(
      ClientPacketListener var1,
      ClientLevel.ClientLevelData var2,
      ResourceKey<Level> var3,
      DimensionType var4,
      int var5,
      Supplier<ProfilerFiller> var6,
      LevelRenderer var7,
      boolean var8,
      long var9
   ) {
      super(var2, var3, var4, var6, true, var8, var9);
      this.connection = var1;
      this.chunkSource = new ClientChunkCache(this, var5);
      this.clientLevelData = var2;
      this.levelRenderer = var7;
      this.effects = DimensionSpecialEffects.forType(var4);
      this.setDefaultSpawnPos(new BlockPos(8, 64, 8), 0.0F);
      this.updateSkyBrightness();
      this.prepareWeather();
   }

   public DimensionSpecialEffects effects() {
      return this.effects;
   }

   public void tick(BooleanSupplier var1) {
      this.getWorldBorder().tick();
      this.tickTime();
      this.getProfiler().push("blocks");
      this.chunkSource.tick(var1);
      this.getProfiler().pop();
   }

   private void tickTime() {
      this.setGameTime(this.levelData.getGameTime() + 1L);
      if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
         this.setDayTime(this.levelData.getDayTime() + 1L);
      }
   }

   public void setGameTime(long var1) {
      this.clientLevelData.setGameTime(var1);
   }

   public void setDayTime(long var1) {
      if (var1 < 0L) {
         var1 = -var1;
         this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, null);
      } else {
         this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, null);
      }

      this.clientLevelData.setDayTime(var1);
   }

   public Iterable<Entity> entitiesForRendering() {
      return this.entitiesById.values();
   }

   public void tickEntities() {
      ProfilerFiller var1 = this.getProfiler();
      var1.push("entities");
      ObjectIterator<Entry<Entity>> var2x = this.entitiesById.int2ObjectEntrySet().iterator();

      while(var2x.hasNext()) {
         Entry<Entity> var3xx = (Entry)var2x.next();
         Entity var4xxx = (Entity)var3xx.getValue();
         if (!var4xxx.isPassenger()) {
            var1.push("tick");
            if (!var4xxx.removed) {
               this.guardEntityTick(this::tickNonPassenger, var4xxx);
            }

            var1.pop();
            var1.push("remove");
            if (var4xxx.removed) {
               var2x.remove();
               this.onEntityRemoved(var4xxx);
            }

            var1.pop();
         }
      }

      this.tickBlockEntities();
      var1.pop();
   }

   public void tickNonPassenger(Entity var1) {
      if (!(var1 instanceof Player) && !this.getChunkSource().isEntityTickingChunk(var1)) {
         this.updateChunkPos(var1);
      } else {
         var1.setPosAndOldPos(var1.getX(), var1.getY(), var1.getZ());
         var1.yRotO = var1.yRot;
         var1.xRotO = var1.xRot;
         if (var1.inChunk || var1.isSpectator()) {
            ++var1.tickCount;
            this.getProfiler().push(() -> Registry.ENTITY_TYPE.getKey(var1.getType()).toString());
            var1.tick();
            this.getProfiler().pop();
         }

         this.updateChunkPos(var1);
         if (var1.inChunk) {
            for(Entity var3 : var1.getPassengers()) {
               this.tickPassenger(var1, var3);
            }
         }
      }
   }

   public void tickPassenger(Entity var1, Entity var2) {
      if (!var2.removed && var2.getVehicle() == var1) {
         if (var2 instanceof Player || this.getChunkSource().isEntityTickingChunk(var2)) {
            var2.setPosAndOldPos(var2.getX(), var2.getY(), var2.getZ());
            var2.yRotO = var2.yRot;
            var2.xRotO = var2.xRot;
            if (var2.inChunk) {
               ++var2.tickCount;
                try {
                    var2.rideTick();
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            this.updateChunkPos(var2);
            if (var2.inChunk) {
               for(Entity var4 : var2.getPassengers()) {
                  this.tickPassenger(var2, var4);
               }
            }

         }
      } else {
         var2.stopRiding();
      }
   }

   private void updateChunkPos(Entity var1) {
      if (var1.checkAndResetUpdateChunkPos()) {
         this.getProfiler().push("chunkCheck");
         int var2 = Mth.floor(var1.getX() / (double)16.0F);
         int var3 = Mth.floor(var1.getY() / (double)16.0F);
         int var4 = Mth.floor(var1.getZ() / (double)16.0F);
         if (!var1.inChunk || var1.xChunk != var2 || var1.yChunk != var3 || var1.zChunk != var4) {
            if (var1.inChunk && this.hasChunk(var1.xChunk, var1.zChunk)) {
               this.getChunk(var1.xChunk, var1.zChunk).removeEntity(var1, var1.yChunk);
            }

            if (!var1.checkAndResetForcedChunkAdditionFlag() && !this.hasChunk(var2, var4)) {
               if (var1.inChunk) {
                  LOGGER.warn("Entity {} left loaded chunk area", var1);
               }

               var1.inChunk = false;
            } else {
               this.getChunk(var2, var4).addEntity(var1);
            }
         }

         this.getProfiler().pop();
      }
   }

   public void unload(LevelChunk var1) {
      this.blockEntitiesToUnload.addAll(var1.getBlockEntities().values());
      this.chunkSource.getLightEngine().enableLightSources(var1.getPos(), false);
   }

   public void onChunkLoaded(int var1, int var2) {
      this.tintCaches.forEach((var2x, var3) -> var3.invalidateForChunk(var1, var2));
   }

   public void clearTintCaches() {
      this.tintCaches.forEach((var0, var1) -> var1.invalidateAll());
   }

   @Override
   public boolean hasChunk(int var1, int var2) {
      return true;
   }

   public int getEntityCount() {
      return this.entitiesById.size();
   }

   public void addPlayer(int var1, AbstractClientPlayer var2) {
      this.addEntity(var1, var2);
      this.players.add(var2);
   }

   public void putNonPlayerEntity(int var1, Entity var2) {
      this.addEntity(var1, var2);
   }

   private void addEntity(int var1, Entity var2) {
      this.removeEntity(var1);
      this.entitiesById.put(var1, var2);
      this.getChunkSource().getChunk(Mth.floor(var2.getX() / 16.0), Mth.floor(var2.getZ() / 16.0), ChunkStatus.FULL, true).addEntity(var2);
   }

   public void removeEntity(int var1) {
      Entity var2 = (Entity)this.entitiesById.remove(var1);
      if (var2 != null) {
         var2.remove();
         this.onEntityRemoved(var2);
      }
   }

   private void onEntityRemoved(Entity var1) {
      var1.unRide();
      if (var1.inChunk) {
         this.getChunk(var1.xChunk, var1.zChunk).removeEntity(var1);
      }

      this.players.remove(var1);
   }

   public void reAddEntitiesToChunk(LevelChunk var1) {
      ObjectIterator var2 = this.entitiesById.int2ObjectEntrySet().iterator();

      while(var2.hasNext()) {
         Int2ObjectMap.Entry<Entity> var3 = (Int2ObjectMap.Entry)var2.next();
         Entity var4 = (Entity)var3.getValue();
         int var5 = Mth.floor(var4.getX() / (double)16.0F);
         int var6 = Mth.floor(var4.getZ() / (double)16.0F);
         if (var5 == var1.getPos().x && var6 == var1.getPos().z) {
            var1.addEntity(var4);
         }
      }
   }

   @Nullable
   @Override
   public Entity getEntity(int var1) {
      return (Entity)this.entitiesById.get(var1);
   }

   public void setKnownState(BlockPos var1, BlockState var2) {
      this.setBlock(var1, var2, 19);
   }

   @Override
   public void disconnect() {
      this.connection.getConnection().disconnect(new TranslatableComponent("multiplayer.status.quitting"));
   }

   public void animateTick(int var1, int var2, int var3) {
      int var4 = 32;
      Random var5 = new Random();
      boolean var6 = false;
      if (this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE) {
         for(ItemStack var8 : this.minecraft.player.getHandSlots()) {
            if (var8.getItem() == Blocks.BARRIER.asItem()) {
               var6 = true;
               break;
            }
         }
      }

      BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();

      for(int var10 = 0; var10 < 667; ++var10) {
         this.doAnimateTick(var1, var2, var3, 16, var5, var6, var9);
         this.doAnimateTick(var1, var2, var3, 32, var5, var6, var9);
      }
   }

   public void doAnimateTick(int var1, int var2, int var3, int var4, Random var5, boolean var6, BlockPos.MutableBlockPos var7) {
      int var8 = var1 + this.random.nextInt(var4) - this.random.nextInt(var4);
      int var9 = var2 + this.random.nextInt(var4) - this.random.nextInt(var4);
      int var10 = var3 + this.random.nextInt(var4) - this.random.nextInt(var4);
      var7.set(var8, var9, var10);
      BlockState var11 = this.getBlockState(var7);
      var11.getBlock().animateTick(var11, this, var7, var5);
      FluidState var12 = this.getFluidState(var7);
      if (!var12.isEmpty()) {
         var12.animateTick(this, var7, var5);
         ParticleOptions var13 = var12.getDripParticle();
         if (var13 != null && this.random.nextInt(10) == 0) {
            boolean var14 = var11.isFaceSturdy(this, var7, Direction.DOWN);
            BlockPos var15 = var7.below();
            this.trySpawnDripParticles(var15, this.getBlockState(var15), var13, var14);
         }
      }

      if (var6 && var11.is(Blocks.BARRIER)) {
         this.addParticle(ParticleTypes.BARRIER, (double)var8 + (double)0.5F, (double)var9 + (double)0.5F, (double)var10 + (double)0.5F, (double)0.0F, (double)0.0F, (double)0.0F);
      }

      if (!var11.isCollisionShapeFullBlock(this, var7)) {
         this.getBiome(var7).getAmbientParticle().ifPresent((var2x) -> {
            if (var2x.canSpawn(this.random)) {
               this.addParticle(var2x.getOptions(), (double)var7.getX() + this.random.nextDouble(), (double)var7.getY() + this.random.nextDouble(), (double)var7.getZ() + this.random.nextDouble(), (double)0.0F, (double)0.0F, (double)0.0F);
            }

         });
      }

   }

   private void trySpawnDripParticles(BlockPos var1, BlockState var2, ParticleOptions var3, boolean var4) {
      if (var2.getFluidState().isEmpty()) {
         VoxelShape var5 = var2.getCollisionShape(this, var1);
         double var6x = var5.max(Direction.Axis.Y);
         if (var6x < 1.0) {
            if (var4) {
               this.spawnFluidParticle((double)var1.getX(), (double)(var1.getX() + 1), (double)var1.getZ(), (double)(var1.getZ() + 1), (double)(var1.getY() + 1) - 0.05, var3);
            }
         } else if (!var2.is(BlockTags.IMPERMEABLE)) {
            double var8 = var5.min(Direction.Axis.Y);
            if (var8 > 0.0) {
               this.spawnParticle(var1, var3, var5, (double)var1.getY() + var8 - 0.05);
            } else {
               BlockPos var10 = var1.below();
               BlockState var11x = this.getBlockState(var10);
               VoxelShape var12xx = var11x.getCollisionShape(this, var10);
               double var13xxx = var12xx.max(Direction.Axis.Y);
               if (var13xxx < 1.0 && var11x.getFluidState().isEmpty()) {
                  this.spawnParticle(var1, var3, var5, (double)var1.getY() - 0.05);
               }
            }
         }
      }
   }

   private void spawnParticle(BlockPos var1, ParticleOptions var2, VoxelShape var3, double var4) {
      this.spawnFluidParticle(
         (double)var1.getX() + var3.min(Direction.Axis.X),
         (double)var1.getX() + var3.max(Direction.Axis.X),
         (double)var1.getZ() + var3.min(Direction.Axis.Z),
         (double)var1.getZ() + var3.max(Direction.Axis.Z),
              var4,
              var2
      );
   }

   private void spawnFluidParticle(double var1, double var3, double var5, double var7, double var9, ParticleOptions var11) {
      this.addParticle(var11, Mth.lerp(this.random.nextDouble(), var1, var3), var9, Mth.lerp(this.random.nextDouble(), var5, var7), (double)0.0F, (double)0.0F, (double)0.0F);
   }

   public void removeAllPendingEntityRemovals() {
      ObjectIterator<Int2ObjectMap.Entry<Entity>> var1 = this.entitiesById.int2ObjectEntrySet().iterator();

      while(var1.hasNext()) {
         Int2ObjectMap.Entry<Entity> var2 = var1.next();
         Entity var3 = (Entity)var2.getValue();
         if (var3.removed) {
            var1.remove();
            this.onEntityRemoved(var3);
         }
      }
   }

   @Override
   public CrashReportCategory fillReportDetails(CrashReport var1) {
      CrashReportCategory var2 = super.fillReportDetails(var1);
      var2.setDetail("Server brand", () -> this.minecraft.player.getServerBrand());
      var2.setDetail("Server type", () -> this.minecraft.getSingleplayerServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server");
      return var2;
   }

   @Override
   public void playSound(@Nullable Player var1, double var2, double var4, double var6, SoundEvent var8, SoundSource var9, float var10, float var11) {
      if (var1 == this.minecraft.player) {
         this.playLocalSound(var2, var4, var6, var8, var9, var10, var11, false);
      }
   }

   @Override
   public void playSound(@Nullable Player var1, Entity var2, SoundEvent var3, SoundSource var4, float var5, float var6) {
      if (var1 == this.minecraft.player) {
         this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(var3, var4, var2));
      }
   }

   public void playLocalSound(BlockPos var1, SoundEvent var2, SoundSource var3, float var4, float var5, boolean var6) {
      this.playLocalSound((double)var1.getX() + (double)0.5F, (double)var1.getY() + (double)0.5F, (double)var1.getZ() + (double)0.5F, var2, var3, var4, var5, var6);
   }

   @Override
   public void playLocalSound(double var1, double var3, double var5, SoundEvent var7, SoundSource var8, float var9, float var10, boolean var11) {
      double var12 = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(var1, var3, var5);
      SimpleSoundInstance var14 = new SimpleSoundInstance(var7, var8, var9, var10, var1, var3, var5);
      if (var11 && var12 > (double)100.0F) {
         double var15 = Math.sqrt(var12) / (double)40.0F;
         this.minecraft.getSoundManager().playDelayed(var14, (int)(var15 * (double)20.0F));
      } else {
         this.minecraft.getSoundManager().play(var14);
      }
   }

   @Override
   public void createFireworks(double var1, double var3, double var5, double var7, double var9, double var11, @Nullable CompoundTag var13) {
      this.minecraft.particleEngine.add(new FireworkParticles.Starter(this, var1, var3, var5, var7, var9, var11, this.minecraft.particleEngine, var13));
   }

   @Override
   public void sendPacketToServer(Packet<?> var1) {
      this.connection.send(var1);
   }

   @Override
   public RecipeManager getRecipeManager() {
      return this.connection.getRecipeManager();
   }

   public void setScoreboard(Scoreboard var1) {
      this.scoreboard = var1;
   }

   @Override
   public TickList<Block> getBlockTicks() {
      return EmptyTickList.empty();
   }

   @Override
   public TickList<Fluid> getLiquidTicks() {
      return EmptyTickList.empty();
   }

   public ClientChunkCache getChunkSource() {
      return this.chunkSource;
   }

   @Nullable
   @Override
   public MapItemSavedData getMapData(String var1) {
      return this.mapData.get(var1);
   }

   @Override
   public void setMapData(MapItemSavedData var1) {
      this.mapData.put(var1.getId(), var1);
   }

   @Override
   public int getFreeMapId() {
      return 0;
   }

   @Override
   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   @Override
   public TagContainer getTagManager() {
      return this.connection.getTags();
   }

   @Override
   public RegistryAccess registryAccess() {
      return this.connection.registryAccess();
   }

   @Override
   public void sendBlockUpdated(BlockPos var1, BlockState var2, BlockState var3, int var4) {
      this.levelRenderer.blockChanged(this, var1, var2, var3, var4);
   }

   @Override
   public void setBlocksDirty(BlockPos var1, BlockState var2, BlockState var3) {
      this.levelRenderer.setBlockDirty(var1, var2, var3);
   }

   public void setSectionDirtyWithNeighbors(int var1, int var2, int var3) {
      this.levelRenderer.setSectionDirtyWithNeighbors(var1, var2, var3);
   }

   @Override
   public void destroyBlockProgress(int var1, BlockPos var2, int var3) {
      this.levelRenderer.destroyBlockProgress(var1, var2, var3);
   }

   @Override
   public void globalLevelEvent(int var1, BlockPos var2, int var3) {
      this.levelRenderer.globalLevelEvent(var1, var2, var3);
   }

   @Override
   public void levelEvent(@Nullable Player var1, int var2, BlockPos var3, int var4) {
      try {
         this.levelRenderer.levelEvent(var1, var2, var3, var4);
      } catch (Throwable var5_1) {
         CrashReport var6 = CrashReport.forThrowable(var5_1, "Playing level event");
         CrashReportCategory var7 = var6.addCategory("Level event being played");
         var7.setDetail("Block coordinates", CrashReportCategory.formatLocation(var3));
         var7.setDetail("Event source", var1);
         var7.setDetail("Event type", var2);
         var7.setDetail("Event data", var4);
         throw new ReportedException(var6);
      }
   }

   @Override
   public void addParticle(ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      this.levelRenderer.addParticle(var1, var1.getType().getOverrideLimiter(), var2, var4, var6, var8, var10, var12);
   }

   @Override
   public void addParticle(ParticleOptions var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13) {
      this.levelRenderer.addParticle(var1, var1.getType().getOverrideLimiter() || var2, var3, var5, var7, var9, var11, var13);
   }

   @Override
   public void addAlwaysVisibleParticle(ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      this.levelRenderer.addParticle(var1, false, true, var2, var4, var6, var8, var10, var12);
   }

   @Override
   public void addAlwaysVisibleParticle(ParticleOptions var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13) {
      this.levelRenderer.addParticle(var1, var1.getType().getOverrideLimiter() || var2, true, var3, var5, var7, var9, var11, var13);
   }

   @Override
   public List<AbstractClientPlayer> players() {
      return this.players;
   }

   @Override
   public Biome getUncachedNoiseBiome(int var1, int var2, int var3) {
      return this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS);
   }

   public float getSkyDarken(float var1) {
      float var2 = this.getTimeOfDay(var1);
      float var3 = 1.0F - (Mth.cos(var2 * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
      var3 = Mth.clamp(var3, 0.0F, 1.0F);
      var3 = 1.0F - var3;
      var3 = (float)((double)var3 * ((double)1.0F - (double)(this.getRainLevel(var1) * 5.0F) / (double)16.0F));
      var3 = (float)((double)var3 * ((double)1.0F - (double)(this.getThunderLevel(var1) * 5.0F) / (double)16.0F));
      return var3 * 0.8F + 0.2F;
   }

   public Vec3 getSkyColor(BlockPos var1, float var2) {
      float var3 = this.getTimeOfDay(var2);
      float var4 = Mth.cos(var3 * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      var4 = Mth.clamp(var4, 0.0F, 1.0F);
      Biome var5 = this.getBiome(var1);
      int var6 = var5.getSkyColor();
      float var7 = (float)(var6 >> 16 & 255) / 255.0F;
      float var8 = (float)(var6 >> 8 & 255) / 255.0F;
      float var9 = (float)(var6 & 255) / 255.0F;
      var7 *= var4;
      var8 *= var4;
      var9 *= var4;
      float var10 = this.getRainLevel(var2);
      if (var10 > 0.0F) {
         float var11 = (var7 * 0.3F + var8 * 0.59F + var9 * 0.11F) * 0.6F;
         float var12 = 1.0F - var10 * 0.75F;
         var7 = var7 * var12 + var11 * (1.0F - var12);
         var8 = var8 * var12 + var11 * (1.0F - var12);
         var9 = var9 * var12 + var11 * (1.0F - var12);
      }

      float var18 = this.getThunderLevel(var2);
      if (var18 > 0.0F) {
         float var19 = (var7 * 0.3F + var8 * 0.59F + var9 * 0.11F) * 0.2F;
         float var13 = 1.0F - var18 * 0.75F;
         var7 = var7 * var13 + var19 * (1.0F - var13);
         var8 = var8 * var13 + var19 * (1.0F - var13);
         var9 = var9 * var13 + var19 * (1.0F - var13);
      }

      if (this.skyFlashTime > 0) {
         float var20 = (float)this.skyFlashTime - var2;
         if (var20 > 1.0F) {
            var20 = 1.0F;
         }

         var20 *= 0.45F;
         var7 = var7 * (1.0F - var20) + 0.8F * var20;
         var8 = var8 * (1.0F - var20) + 0.8F * var20;
         var9 = var9 * (1.0F - var20) + 1.0F * var20;
      }

      return new Vec3((double)var7, (double)var8, (double)var9);
   }

   public Vec3 getCloudColor(float var1) {
      float var2 = this.getTimeOfDay(var1);
      float var3 = Mth.cos(var2 * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      var3 = Mth.clamp(var3, 0.0F, 1.0F);
      float var4 = 1.0F;
      float var5 = 1.0F;
      float var6 = 1.0F;
      float var7 = this.getRainLevel(var1);
      if (var7 > 0.0F) {
         float var8 = (var4 * 0.3F + var5 * 0.59F + var6 * 0.11F) * 0.6F;
         float var9 = 1.0F - var7 * 0.95F;
         var4 = var4 * var9 + var8 * (1.0F - var9);
         var5 = var5 * var9 + var8 * (1.0F - var9);
         var6 = var6 * var9 + var8 * (1.0F - var9);
      }

      var4 *= var3 * 0.9F + 0.1F;
      var5 *= var3 * 0.9F + 0.1F;
      var6 *= var3 * 0.85F + 0.15F;
      float var15 = this.getThunderLevel(var1);
      if (var15 > 0.0F) {
         float var16 = (var4 * 0.3F + var5 * 0.59F + var6 * 0.11F) * 0.2F;
         float var10 = 1.0F - var15 * 0.95F;
         var4 = var4 * var10 + var16 * (1.0F - var10);
         var5 = var5 * var10 + var16 * (1.0F - var10);
         var6 = var6 * var10 + var16 * (1.0F - var10);
      }

      return new Vec3((double)var4, (double)var5, (double)var6);
   }

   public float getStarBrightness(float var1) {
      float var2 = this.getTimeOfDay(var1);
      float var3 = 1.0F - (Mth.cos(var2 * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
      var3 = Mth.clamp(var3, 0.0F, 1.0F);
      return var3 * var3 * 0.5F;
   }

   public int getSkyFlashTime() {
      return this.skyFlashTime;
   }

   @Override
   public void setSkyFlashTime(int var1) {
      this.skyFlashTime = var1;
   }

   @Override
   public float getShade(Direction var1, boolean var2) {
      boolean var3 = this.effects().constantAmbientLight();
      if (!var2) {
         return var3 ? 0.9F : 1.0F;
      } else {
         switch (var1) {
            case DOWN:
               return var3 ? 0.9F : 0.5F;
            case UP:
               return var3 ? 0.9F : 1.0F;
            case NORTH:
            case SOUTH:
               return 0.8F;
            case WEST:
            case EAST:
               return 0.6F;
            default:
               return 1.0F;
         }
      }
   }

   @Override
   public int getBlockTint(BlockPos var1, ColorResolver var2) {
      BlockTintCache var3 = (BlockTintCache)this.tintCaches.get(var2);
      return var3.getColor(var1, () -> this.calculateBlockTint(var1, var2));
   }

   public int calculateBlockTint(BlockPos var1, ColorResolver var2) {
      int var3 = Minecraft.getInstance().options.biomeBlendRadius;
      if (var3 == 0) {
         return var2.getColor(this.getBiome(var1), (double)var1.getX(), (double)var1.getZ());
      } else {
         int var4 = (var3 * 2 + 1) * (var3 * 2 + 1);
         int var5 = 0;
         int var6 = 0;
         int var7 = 0;
         Cursor3D var8 = new Cursor3D(var1.getX() - var3, var1.getY(), var1.getZ() - var3, var1.getX() + var3, var1.getY(), var1.getZ() + var3);

         int var10;
         for(BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos(); var8.advance(); var7 += var10 & 255) {
            var9.set(var8.nextX(), var8.nextY(), var8.nextZ());
            var10 = var2.getColor(this.getBiome(var9), (double)var9.getX(), (double)var9.getZ());
            var5 += (var10 & 16711680) >> 16;
            var6 += (var10 & '\uff00') >> 8;
         }

         return (var5 / var4 & 255) << 16 | (var6 / var4 & 255) << 8 | var7 / var4 & 255;
      }
   }

   public BlockPos getSharedSpawnPos() {
      BlockPos var1 = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());
      if (!this.getWorldBorder().isWithinBounds(var1)) {
         var1 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
      }

      return var1;
   }

   public float getSharedSpawnAngle() {
      return this.levelData.getSpawnAngle();
   }

   public void setDefaultSpawnPos(BlockPos var1, float var2) {
      this.levelData.setSpawn(var1, var2);
   }

   @Override
   public String toString() {
      return "ClientLevel";
   }

   public ClientLevel.ClientLevelData getLevelData() {
      return this.clientLevelData;
   }

   public static class ClientLevelData implements WritableLevelData {
      private final boolean hardcore;
      private final GameRules gameRules;
      private final boolean isFlat;
      private int xSpawn;
      private int ySpawn;
      private int zSpawn;
      private float spawnAngle;
      private long gameTime;
      private long dayTime;
      private boolean raining;
      private Difficulty difficulty;
      private boolean difficultyLocked;

      public ClientLevelData(Difficulty var1, boolean var2, boolean var3) {
         this.difficulty = var1;
         this.hardcore = var2;
         this.isFlat = var3;
         this.gameRules = new GameRules();
      }

      @Override
      public int getXSpawn() {
         return this.xSpawn;
      }

      @Override
      public int getYSpawn() {
         return this.ySpawn;
      }

      @Override
      public int getZSpawn() {
         return this.zSpawn;
      }

      @Override
      public float getSpawnAngle() {
         return this.spawnAngle;
      }

      @Override
      public long getGameTime() {
         return this.gameTime;
      }

      @Override
      public long getDayTime() {
         return this.dayTime;
      }

      @Override
      public void setXSpawn(int var1) {
         this.xSpawn = var1;
      }

      @Override
      public void setYSpawn(int var1) {
         this.ySpawn = var1;
      }

      @Override
      public void setZSpawn(int var1) {
         this.zSpawn = var1;
      }

      @Override
      public void setSpawnAngle(float var1) {
         this.spawnAngle = var1;
      }

      public void setGameTime(long var1) {
         this.gameTime = var1;
      }

      public void setDayTime(long var1) {
         this.dayTime = var1;
      }

      @Override
      public void setSpawn(BlockPos var1, float var2) {
         this.xSpawn = var1.getX();
         this.ySpawn = var1.getY();
         this.zSpawn = var1.getZ();
         this.spawnAngle = var2;
      }

      @Override
      public boolean isThundering() {
         return false;
      }

      @Override
      public boolean isRaining() {
         return this.raining;
      }

      @Override
      public void setRaining(boolean var1) {
         this.raining = var1;
      }

      @Override
      public boolean isHardcore() {
         return this.hardcore;
      }

      @Override
      public GameRules getGameRules() {
         return this.gameRules;
      }

      @Override
      public Difficulty getDifficulty() {
         return this.difficulty;
      }

      @Override
      public boolean isDifficultyLocked() {
         return this.difficultyLocked;
      }

      @Override
      public void fillCrashReportCategory(CrashReportCategory var1) {
         WritableLevelData.super.fillCrashReportCategory(var1);
      }

      public void setDifficulty(Difficulty var1) {
         this.difficulty = var1;
      }

      public void setDifficultyLocked(boolean var1) {
         this.difficultyLocked = var1;
      }

      public double getHorizonHeight() {
         return this.isFlat ? 0.0 : 63.0;
      }

      public double getClearColorScale() {
         return this.isFlat ? 1.0 : 0.03125;
      }
   }
}
