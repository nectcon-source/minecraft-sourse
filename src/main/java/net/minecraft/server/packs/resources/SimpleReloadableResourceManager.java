package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;

public class SimpleReloadableResourceManager implements ReloadableResourceManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<String, FallbackResourceManager> namespacedPacks = Maps.newHashMap();
   private final List<PreparableReloadListener> listeners = Lists.newArrayList();
   private final List<PreparableReloadListener> recentlyRegistered = Lists.newArrayList();
   private final Set<String> namespaces = Sets.newLinkedHashSet();
   private final List<PackResources> packs = Lists.newArrayList();
   private final PackType type;

   public SimpleReloadableResourceManager(PackType var1) {
      this.type = var1;
   }

   public void add(PackResources var1) {
      this.packs.add(var1);

      for(String var3 : var1.getNamespaces(this.type)) {
         this.namespaces.add(var3);
         FallbackResourceManager var4 = this.namespacedPacks.get(var3);
         if (var4 == null) {
            var4 = new FallbackResourceManager(this.type, var3);
            this.namespacedPacks.put(var3, var4);
         }

         var4.add(var1);
      }
   }

   @Override
   public Set<String> getNamespaces() {
      return this.namespaces;
   }

   @Override
   public Resource getResource(ResourceLocation var1) throws IOException {
      ResourceManager var2 = this.namespacedPacks.get(var1.getNamespace());
      if (var2 != null) {
         return var2.getResource(var1);
      } else {
         throw new FileNotFoundException(var1.toString());
      }
   }

   @Override
   public boolean hasResource(ResourceLocation var1) {
      ResourceManager var2 = this.namespacedPacks.get(var1.getNamespace());
      return var2 != null ? var2.hasResource(var1) : false;
   }

   @Override
   public List<Resource> getResources(ResourceLocation var1) throws IOException {
      ResourceManager var2 = this.namespacedPacks.get(var1.getNamespace());
      if (var2 != null) {
         return var2.getResources(var1);
      } else {
         throw new FileNotFoundException(var1.toString());
      }
   }

   @Override
   public Collection<ResourceLocation> listResources(String var1, Predicate<String> var2) {
      Set<ResourceLocation> var3 = Sets.newHashSet();

      for(FallbackResourceManager var5 : this.namespacedPacks.values()) {
         var3.addAll(var5.listResources(var1, var2));
      }

      List<ResourceLocation> var6 = Lists.newArrayList(var3);
      Collections.sort(var6);
      return var6;
   }

   private void clear() {
      this.namespacedPacks.clear();
      this.namespaces.clear();
      this.packs.forEach(PackResources::close);
      this.packs.clear();
   }

   @Override
   public void close() {
      this.clear();
   }

   @Override
   public void registerReloadListener(PreparableReloadListener var1) {
      this.listeners.add(var1);
      this.recentlyRegistered.add(var1);
   }

   protected ReloadInstance createReload(Executor var1, Executor var2, List<PreparableReloadListener> var3, CompletableFuture<Unit> var4) {
      ReloadInstance var5;
      if (LOGGER.isDebugEnabled()) {
         var5 = new ProfiledReloadInstance(this, Lists.newArrayList(var3), var1, var2, var4);
      } else {
         var5 = SimpleReloadInstance.of(this, Lists.newArrayList(var3), var1, var2, var4);
      }

      this.recentlyRegistered.clear();
      return var5;
   }

   @Override
   public ReloadInstance createFullReload(Executor var1, Executor var2, CompletableFuture<Unit> var3, List<PackResources> var4) {
      this.clear();
      LOGGER.info("Reloading ResourceManager: {}", new Supplier[]{() -> (String)var4.stream().map(PackResources::getName).collect(Collectors.joining(", "))});

      for(PackResources var6 : var4) {
         try {
            this.add(var6);
         } catch (Exception var7_1) {
            LOGGER.error("Failed to add resource pack {}", var6.getName(), var7_1);
            return new FailingReloadInstance(new ResourcePackLoadingFailure(var6, var7_1));
         }
      }

      return this.createReload(var1, var2, this.listeners, var3);
   }

   @Override
   public Stream<PackResources> listPacks() {
      return this.packs.stream();
   }

   static class FailingReloadInstance implements ReloadInstance {
      private final SimpleReloadableResourceManager.ResourcePackLoadingFailure exception;
      private final CompletableFuture<Unit> failedFuture;

      public FailingReloadInstance(SimpleReloadableResourceManager.ResourcePackLoadingFailure var1) {
         this.exception = var1;
         this.failedFuture = new CompletableFuture<>();
         this.failedFuture.completeExceptionally(var1);
      }

      @Override
      public CompletableFuture<Unit> done() {
         return this.failedFuture;
      }

      @Override
      public float getActualProgress() {
         return 0.0F;
      }

      @Override
      public boolean isApplying() {
         return false;
      }

      @Override
      public boolean isDone() {
         return true;
      }

      @Override
      public void checkExceptions() {
         throw this.exception;
      }
   }

   public static class ResourcePackLoadingFailure extends RuntimeException {
      private final PackResources pack;

      public ResourcePackLoadingFailure(PackResources var1, Throwable var2) {
         super(var1.getName(), var2);
         this.pack = var1;
      }

      public PackResources getPack() {
         return this.pack;
      }
   }
}
