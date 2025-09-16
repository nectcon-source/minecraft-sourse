//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;

public class DirectAssetIndex extends AssetIndex {
   private final File assetsDirectory;

   public DirectAssetIndex(File var1) {
      this.assetsDirectory = var1;
   }

   public File getFile(ResourceLocation var1) {
      return new File(this.assetsDirectory, var1.toString().replace(':', '/'));
   }

   public File getRootFile(String var1) {
      return new File(this.assetsDirectory, var1);
   }

   public Collection<ResourceLocation> getFiles(String var1, String var2, int var3, Predicate<String> var4) {
      Path var5 = this.assetsDirectory.toPath().resolve(var2);

      try (Stream<Path> var6 = Files.walk(var5.resolve(var1), var3)) {
         Collection var8 = var6.filter((var0) -> Files.isRegularFile(var0)).filter((var0) -> !var0.endsWith(".mcmeta")).filter((var1x) -> var4.test(var1x.getFileName().toString())).map((var2x) -> new ResourceLocation(var2, var5.relativize(var2x).toString().replaceAll("\\\\", "/"))).collect(Collectors.toList());
         return var8;
      } catch (NoSuchFileException var21) {
      } catch (IOException var6_1) {
         LOGGER.warn("Unable to getFiles on {}", var1, var6_1);
      }

      return Collections.emptyList();
   }
}
