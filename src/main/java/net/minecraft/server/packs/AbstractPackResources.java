package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractPackResources implements PackResources {
   private static final Logger LOGGER = LogManager.getLogger();
   protected final File file;

   public AbstractPackResources(File var1) {
      this.file = var1;
   }

   private static String getPathFromLocation(PackType var0, ResourceLocation var1) {
      return String.format("%s/%s/%s", var0.getDirectory(), var1.getNamespace(), var1.getPath());
   }

   protected static String getRelativePath(File var0, File var1) {
      return var0.toURI().relativize(var1.toURI()).getPath();
   }

   @Override
   public InputStream getResource(PackType var1, ResourceLocation var2) throws IOException {
      return this.getResource(getPathFromLocation(var1, var2));
   }

   @Override
   public boolean hasResource(PackType var1, ResourceLocation var2) {
      return this.hasResource(getPathFromLocation(var1, var2));
   }

   protected abstract InputStream getResource(String var1) throws IOException;

   @Override
   public InputStream getRootResource(String var1) throws IOException {
      if (!var1.contains("/") && !var1.contains("\\")) {
         return this.getResource(var1);
      } else {
         throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
      }
   }

   protected abstract boolean hasResource(String var1);

   protected void logWarning(String var1) {
      LOGGER.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", var1, this.file);
   }

   @Nullable
   @Override
   public <T> T getMetadataSection(MetadataSectionSerializer<T> var1) throws IOException {
      Object var4;
      try (InputStream var2 = this.getResource("pack.mcmeta")) {
         var4 = getMetadataFromStream(var1, var2);
      }

      return (T)var4;
   }

   @Nullable
   public static <T> T getMetadataFromStream(MetadataSectionSerializer<T> var0, InputStream var1) {
      JsonObject var2;
      try (BufferedReader var3 = new BufferedReader(new InputStreamReader(var1, StandardCharsets.UTF_8))) {
         var2 = GsonHelper.parse(var3);
      } catch (JsonParseException | IOException var3_1) {
         LOGGER.error("Couldn't load {} metadata", var0.getMetadataSectionName(), var3_1);
         return null;
      }

      if (!var2.has(var0.getMetadataSectionName())) {
         return null;
      } else {
         try {
            return var0.fromJson(GsonHelper.getAsJsonObject(var2, var0.getMetadataSectionName()));
         } catch (JsonParseException var3_13) {
            LOGGER.error("Couldn't load {} metadata", var0.getMetadataSectionName(), var3_13);
            return null;
         }
      }
   }

   @Override
   public String getName() {
      return this.file.getName();
   }
}
