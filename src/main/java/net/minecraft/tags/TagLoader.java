package net.minecraft.tags;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagLoader<T> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new Gson();
   private static final int PATH_SUFFIX_LENGTH = ".json".length();
   private final Function<ResourceLocation, Optional<T>> idToValue;
   private final String directory;
   private final String name;

   public TagLoader(Function<ResourceLocation, Optional<T>> var1, String var2, String var3) {
      this.idToValue = var1;
      this.directory = var2;
      this.name = var3;
   }

   public CompletableFuture<Map<ResourceLocation, Tag.Builder>> prepare(ResourceManager var1, Executor var2) {
      return CompletableFuture.supplyAsync(() -> {
         Map<ResourceLocation, Tag.Builder> var2x = Maps.newHashMap();

         for(ResourceLocation var4 : var1.listResources(this.directory, var0 -> var0.endsWith(".json"))) {
            String var5x = var4.getPath();
            ResourceLocation var6xx = new ResourceLocation(var4.getNamespace(), var5x.substring(this.directory.length() + 1, var5x.length() - PATH_SUFFIX_LENGTH));

            try {
               for(Resource var8 : var1.getResources(var4)) {
                  try (
                     InputStream var9xxx = var8.getInputStream();
                     Reader var11xxxx = new BufferedReader(new InputStreamReader(var9xxx, StandardCharsets.UTF_8));
                  ) {
                     JsonObject var13xxxxx = GsonHelper.fromJson(GSON, var11xxxx, JsonObject.class);
                     if (var13xxxxx == null) {
                        LOGGER.error("Couldn't load {} tag list {} from {} in data pack {} as it is empty or null", this.name, var6xx, var4, var8.getSourceName());
                     } else {
                        var2x.computeIfAbsent(var6xx, var0 -> Tag.Builder.tag()).addFromJson(var13xxxxx, var8.getSourceName());
                     }
                  } catch (RuntimeException | IOException var57) {
                     LOGGER.error("Couldn't read {} tag list {} from {} in data pack {}", this.name, var6xx, var4, var8.getSourceName(), var57);
                  } finally {
                     IOUtils.closeQuietly(var8);
                  }
               }
            } catch (IOException var59) {
               LOGGER.error("Couldn't read {} tag list {} from {}", this.name, var6xx, var4, var59);
            }
         }

         return var2x;
      }, var2);
   }

   public TagCollection<T> load(Map<ResourceLocation, Tag.Builder> var1) {
      Map<ResourceLocation, Tag<T>> var2 = Maps.newHashMap();
      Function<ResourceLocation, Tag<T>> var3 = var2::get;
      Function<ResourceLocation, T> var4 = (var1x) -> (this.idToValue.apply(var1x)).orElse(null);

      while(!var1.isEmpty()) {
         boolean var5 = false;
         Iterator<Map.Entry<ResourceLocation, Tag.Builder>> var6 = var1.entrySet().iterator();

         while(var6.hasNext()) {
            Map.Entry<ResourceLocation, Tag.Builder> var7 = var6.next();
            Optional<Tag<T>> var8 = (var7.getValue()).build(var3, var4);
            if (var8.isPresent()) {
               var2.put(var7.getKey(), var8.get());
               var6.remove();
               var5 = true;
            }
         }

         if (!var5) {
            break;
         }
      }

      var1.forEach((var3x, var4x) -> LOGGER.error("Couldn't load {} tag {} as it is missing following references: {}", this.name, var3x, var4x.getUnresolvedEntries(var3, var4).map(Objects::toString).collect(Collectors.joining(","))));
      return TagCollection.of(var2);
   }
}
