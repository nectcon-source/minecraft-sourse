package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerFunctionLibrary implements PreparableReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int PATH_PREFIX_LENGTH = "functions/".length();
   private static final int PATH_SUFFIX_LENGTH = ".mcfunction".length();
   private volatile Map<ResourceLocation, CommandFunction> functions = ImmutableMap.of();
   private final TagLoader<CommandFunction> tagsLoader = new TagLoader<>(this::getFunction, "tags/functions", "function");
   private volatile TagCollection<CommandFunction> tags = TagCollection.empty();
   private final int functionCompilationLevel;
   private final CommandDispatcher<CommandSourceStack> dispatcher;

   public Optional<CommandFunction> getFunction(ResourceLocation var1) {
      return Optional.ofNullable(this.functions.get(var1));
   }

   public Map<ResourceLocation, CommandFunction> getFunctions() {
      return this.functions;
   }

   public TagCollection<CommandFunction> getTags() {
      return this.tags;
   }

   public Tag<CommandFunction> getTag(ResourceLocation var1) {
      return this.tags.getTagOrEmpty(var1);
   }

   public ServerFunctionLibrary(int var1, CommandDispatcher<CommandSourceStack> var2) {
      this.functionCompilationLevel = var1;
      this.dispatcher = var2;
   }

   @Override
   public CompletableFuture<Void> reload(
      PreparableReloadListener.PreparationBarrier var1, ResourceManager var2, ProfilerFiller var3, ProfilerFiller var4, Executor var5, Executor var6
   ) {
      CompletableFuture<Map<ResourceLocation, Tag.Builder>> var7 = this.tagsLoader.prepare(var2, var5);
      CompletableFuture<Map<ResourceLocation, CompletableFuture<CommandFunction>>> var8x = CompletableFuture.<Collection<ResourceLocation>>supplyAsync(
            () -> var2.listResources("functions", var0x -> var0x.endsWith(".mcfunction")), var5
         )
         .thenCompose(
            var3x -> {
               Map<ResourceLocation, CompletableFuture<CommandFunction>> var4x = Maps.newHashMap();
               CommandSourceStack var5x = new CommandSourceStack(
                  CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, this.functionCompilationLevel, "", TextComponent.EMPTY, null, null
               );
      
               for(ResourceLocation var7x : var3x) {
                  String var8xx = var7x.getPath();
                  ResourceLocation var9xxx = new ResourceLocation(var7x.getNamespace(), var8xx.substring(PATH_PREFIX_LENGTH, var8xx.length() - PATH_SUFFIX_LENGTH));
                  var4x.put(var9xxx, CompletableFuture.supplyAsync(() -> {
                     List<String> var5xx = readLines(var2, var7x);
                     return CommandFunction.fromLines(var9xxx, this.dispatcher, var5x, var5xx);
                  }, var5));
               }
      
               CompletableFuture<?>[] var10xx = var4x.values().toArray(new CompletableFuture[0]);
               return CompletableFuture.allOf(var10xx).handle((var1xx, var2xx) -> var4x);
            }
         );
      return var7.thenCombine(var8x, Pair::of).thenCompose(var1::wait).thenAcceptAsync(var1x -> {
         Map<ResourceLocation, CompletableFuture<CommandFunction>> var2x = var1x.getSecond();
         Builder<ResourceLocation, CommandFunction> var3x = ImmutableMap.builder();
         var2x.forEach((var1xx, var2xx) -> var2xx.handle((var2xxx, var3xx) -> {
               if (var3xx != null) {
                  LOGGER.error("Failed to load function {}", var1xx, var3xx);
               } else {
                  var3x.put(var1xx, var2xxx);
               }

               return null;
            }).join());
         this.functions = var3x.build();
         this.tags = this.tagsLoader.load(var1x.getFirst());
      }, var6);
   }

   private static List<String> readLines(ResourceManager var0, ResourceLocation var1) {
      try (Resource var2 = var0.getResource(var1)) {
         List var4 = IOUtils.readLines(var2.getInputStream(), StandardCharsets.UTF_8);
         return var4;
      } catch (IOException var2_1) {
         throw new CompletionException(var2_1);
      }
   }
}
