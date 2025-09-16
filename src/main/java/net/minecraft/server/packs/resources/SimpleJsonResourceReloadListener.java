package net.minecraft.server.packs.resources;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SimpleJsonResourceReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int PATH_SUFFIX_LENGTH = ".json".length();
   private final Gson gson;
   private final String directory;

   public SimpleJsonResourceReloadListener(Gson var1, String var2) {
      this.gson = var1;
      this.directory = var2;
   }

   protected Map<ResourceLocation, JsonElement> prepare(ResourceManager var1, ProfilerFiller var2) {
      Map<ResourceLocation, JsonElement> var3 = Maps.newHashMap();
      int var4 = this.directory.length() + 1;

      for(ResourceLocation var6 : var1.listResources(this.directory, (var0) -> var0.endsWith(".json"))) {
         String var7 = var6.getPath();
         ResourceLocation var8 = new ResourceLocation(var6.getNamespace(), var7.substring(var4, var7.length() - PATH_SUFFIX_LENGTH));

         try (Resource var9 = var1.getResource(var6)) {
            InputStream var11 = var9.getInputStream();
            Throwable var12 = null;

            try {
               Reader var13 = new BufferedReader(new InputStreamReader(var11, StandardCharsets.UTF_8));
               Throwable var14 = null;

               try {
                  JsonElement var15 = GsonHelper.fromJson(this.gson, var13, JsonElement.class);
                  if (var15 != null) {
                     JsonElement var16 = var3.put(var8, var15);
                     if (var16 != null) {
                        throw new IllegalStateException("Duplicate data file ignored with ID " + var8);
                     }
                  } else {
                     LOGGER.error("Couldn't load data file {} from {} as it's null or empty", var8, var6);
                  }
               } catch (Throwable var62) {
                  var14 = var62;
                  throw var62;
               } finally {
                  if (var13 != null) {
                     if (var14 != null) {
                        try {
                           var13.close();
                        } catch (Throwable var61) {
                           var14.addSuppressed(var61);
                        }
                     } else {
                        var13.close();
                     }
                  }

               }
            } catch (Throwable var64) {
               var12 = var64;
               throw var64;
            } finally {
               if (var11 != null) {
                  if (var12 != null) {
                     try {
                        var11.close();
                     } catch (Throwable var60) {
                        var12.addSuppressed(var60);
                     }
                  } else {
                     var11.close();
                  }
               }

            }
         } catch (IllegalArgumentException | IOException | JsonParseException var9_1) {
            LOGGER.error("Couldn't parse data file {} from {}", var8, var6, var9_1);
         }
      }

      return var3;
   }
}
