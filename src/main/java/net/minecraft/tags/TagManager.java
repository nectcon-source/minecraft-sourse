package net.minecraft.tags;

import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagManager implements PreparableReloadListener {
   private final TagLoader<Block> blocks = new TagLoader<>(Registry.BLOCK::getOptional, "tags/blocks", "block");
   private final TagLoader<Item> items = new TagLoader<>(Registry.ITEM::getOptional, "tags/items", "item");
   private final TagLoader<Fluid> fluids = new TagLoader<>(Registry.FLUID::getOptional, "tags/fluids", "fluid");
   private final TagLoader<EntityType<?>> entityTypes = new TagLoader<>(Registry.ENTITY_TYPE::getOptional, "tags/entity_types", "entity_type");
   private TagContainer tags = TagContainer.EMPTY;

   public TagContainer getTags() {
      return this.tags;
   }

   @Override
   public CompletableFuture<Void> reload(
      PreparableReloadListener.PreparationBarrier var1, ResourceManager var2, ProfilerFiller var3, ProfilerFiller var4, Executor var5, Executor var6
   ) {
      CompletableFuture<Map<ResourceLocation, Tag.Builder>> var7 = this.blocks.prepare(var2, var5);
      CompletableFuture<Map<ResourceLocation, Tag.Builder>> var8x = this.items.prepare(var2, var5);
      CompletableFuture<Map<ResourceLocation, Tag.Builder>> var9xx = this.fluids.prepare(var2, var5);
      CompletableFuture<Map<ResourceLocation, Tag.Builder>> var10xxx = this.entityTypes.prepare(var2, var5);
      return CompletableFuture.allOf(var7, var8x, var9xx, var10xxx)
         .thenCompose(var1::wait)
         .thenAcceptAsync(
            var5x -> {
               TagCollection<Block> var6x = this.blocks.load(var7.join());
               TagCollection<Item> var7x = this.items.load(var8x.join());
               TagCollection<Fluid> var8xx = this.fluids.load(var9xx.join());
               TagCollection<EntityType<?>> var9xxx = this.entityTypes.load(var10xxx.join());
               TagContainer var10xxxx = TagContainer.of(var6x, var7x, var8xx, var9xxx);
               Multimap<ResourceLocation, ResourceLocation> var11xxxxx = StaticTags.getAllMissingTags(var10xxxx);
               if (!var11xxxxx.isEmpty()) {
                  throw new IllegalStateException(
                     "Missing required tags: "
                        + (String)var11xxxxx.entries().stream().map(var0 -> var0.getKey() + ":" + var0.getValue()).sorted().collect(Collectors.joining(","))
                  );
               } else {
                  SerializationTags.bind(var10xxxx);
                  this.tags = var10xxxx;
               }
            },
                 var6
         );
   }
}
