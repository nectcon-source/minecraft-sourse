package net.minecraft.server.packs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaPackResources implements PackResources {
   public static Path generatedDir;
   private static final Logger LOGGER = LogManager.getLogger();
   public static Class<?> clientObject;
   private static final Map<PackType, FileSystem> JAR_FILESYSTEM_BY_TYPE = Util.make(Maps.newHashMap(), var0 -> {
      synchronized(VanillaPackResources.class) {
         for(PackType var5 : PackType.values()) {
            URL var6 = VanillaPackResources.class.getResource("/" + var5.getDirectory() + "/.mcassetsroot");

            try {
               URI var7 = var6.toURI();
               if ("jar".equals(var7.getScheme())) {
                  FileSystem var8;
                  try {
                     var8 = FileSystems.getFileSystem(var7);
                  } catch (FileSystemNotFoundException var11) {
                     var8 = FileSystems.newFileSystem(var7, Collections.emptyMap());
                  }

                  var0.put(var5, var8);
               }
            } catch (IOException | URISyntaxException var7_1) {
               LOGGER.error("Couldn't get a list of all vanilla resources", var7_1);
            }
         }
      }
   });
   public final Set<String> namespaces;

   public VanillaPackResources(String... var1) {
      this.namespaces = ImmutableSet.copyOf(var1);
   }

   @Override
   public InputStream getRootResource(String var1) throws IOException {
      if (!var1.contains("/") && !var1.contains("\\")) {
         if (generatedDir != null) {
            Path var2 = generatedDir.resolve(var1);
            if (Files.exists(var2)) {
               return Files.newInputStream(var2);
            }
         }

         return this.getResourceAsStream(var1);
      } else {
         throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
      }
   }

   @Override
   public InputStream getResource(PackType var1, ResourceLocation var2) throws IOException {
      InputStream var3 = this.getResourceAsStream(var1, var2);
      if (var3 != null) {
         return var3;
      } else {
         throw new FileNotFoundException(var2.getPath());
      }
   }

   @Override
   public Collection<ResourceLocation> getResources(PackType var1, String var2, String var3, int var4, Predicate<String> var5) {
      Set<ResourceLocation> var6 = Sets.newHashSet();
      if (generatedDir != null) {
         try {
            getResources(var6, var4, var2, generatedDir.resolve(var1.getDirectory()), var3, var5);
         } catch (IOException var15) {
         }

         if (var1 == PackType.CLIENT_RESOURCES) {
            Enumeration<URL> var7 = null;

            try {
               var7 = clientObject.getClassLoader().getResources(var1.getDirectory() + "/");
            } catch (IOException var14) {
            }

            while(var7 != null && var7.hasMoreElements()) {
               try {
                  URI var8 = var7.nextElement().toURI();
                  if ("file".equals(var8.getScheme())) {
                     getResources(var6, var4, var2, Paths.get(var8), var3, var5);
                  }
               } catch (IOException | URISyntaxException var13) {
               }
            }
         }
      }

      try {
         URL var16 = VanillaPackResources.class.getResource("/" + var1.getDirectory() + "/.mcassetsroot");
         if (var16 == null) {
            LOGGER.error("Couldn't find .mcassetsroot, cannot load vanilla resources");
            return var6;
         }

         URI var17 = var16.toURI();
         if ("file".equals(var17.getScheme())) {
            URL var9 = new URL(var16.toString().substring(0, var16.toString().length() - ".mcassetsroot".length()));
            Path var10 = Paths.get(var9.toURI());
            getResources(var6, var4, var2, var10, var3, var5);
         } else if ("jar".equals(var17.getScheme())) {
            Path var18 = JAR_FILESYSTEM_BY_TYPE.get(var1).getPath("/" + var1.getDirectory());
            getResources(var6, var4, "minecraft", var18, var3, var5);
         } else {
            LOGGER.error("Unsupported scheme {} trying to list vanilla resources (NYI?)", var17);
         }
      } catch (NoSuchFileException | FileNotFoundException var11) {
      } catch (IOException | URISyntaxException var7_9) {
         LOGGER.error("Couldn't get a list of all vanilla resources", var7_9);
      }

      return var6;
   }

   private static void getResources(Collection<ResourceLocation> var0, int var1, String var2, Path var3, String var4, Predicate<String> var5) throws IOException {
      Path var6 = var3.resolve(var2);

      try (Stream<Path> var7 = Files.walk(var6.resolve(var4), var1)) {
         var7.filter((var1x) -> !var1x.endsWith(".mcmeta") && Files.isRegularFile(var1x) && var5.test(var1x.getFileName().toString())).map((var2x) -> new ResourceLocation(var2, var6.relativize(var2x).toString().replaceAll("\\\\", "/"))).forEach(var0::add);
      }
   }

   @Nullable
   protected InputStream getResourceAsStream(PackType var1, ResourceLocation var2) {
      String var3 = createPath(var1, var2);
      if (generatedDir != null) {
         Path var4 = generatedDir.resolve(var1.getDirectory() + "/" + var2.getNamespace() + "/" + var2.getPath());
         if (Files.exists(var4)) {
            try {
               return Files.newInputStream(var4);
            } catch (IOException var7) {
            }
         }
      }

      try {
         URL var8 = VanillaPackResources.class.getResource(var3);
         return isResourceUrlValid(var3, var8) ? var8.openStream() : null;
      } catch (IOException var6) {
         return VanillaPackResources.class.getResourceAsStream(var3);
      }
   }

   private static String createPath(PackType var0, ResourceLocation var1) {
      return "/" + var0.getDirectory() + "/" + var1.getNamespace() + "/" + var1.getPath();
   }

   private static boolean isResourceUrlValid(String var0, @Nullable URL var1) throws IOException {
      return var1 != null && (var1.getProtocol().equals("jar") || FolderPackResources.validatePath(new File(var1.getFile()), var0));
   }

   @Nullable
   protected InputStream getResourceAsStream(String var1) {
      return VanillaPackResources.class.getResourceAsStream("/" + var1);
   }

   @Override
   public boolean hasResource(PackType var1, ResourceLocation var2) {
      String var3 = createPath(var1, var2);
      if (generatedDir != null) {
         Path var4 = generatedDir.resolve(var1.getDirectory() + "/" + var2.getNamespace() + "/" + var2.getPath());
         if (Files.exists(var4)) {
            return true;
         }
      }

      try {
         URL var6 = VanillaPackResources.class.getResource(var3);
         return isResourceUrlValid(var3, var6);
      } catch (IOException var5) {
         return false;
      }
   }

   @Override
   public Set<String> getNamespaces(PackType var1) {
      return this.namespaces;
   }

   @Nullable
   @Override
   public <T> T getMetadataSection(MetadataSectionSerializer<T> var1) throws IOException {
      try (InputStream var2 = this.getRootResource("pack.mcmeta")) {
         Object var4 = AbstractPackResources.getMetadataFromStream(var1, var2);
         return (T)var4;
      } catch (FileNotFoundException | RuntimeException var16) {
         return null;
      }
   }

   @Override
   public String getName() {
      return "Default";
   }

   @Override
   public void close() {
   }
}
