package net.minecraft.server.packs;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FolderPackResources extends AbstractPackResources {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final boolean ON_WINDOWS = Util.getPlatform() == Util.OS.WINDOWS;
   private static final CharMatcher BACKSLASH_MATCHER = CharMatcher.is('\\');

   public FolderPackResources(File var1) {
      super(var1);
   }

   public static boolean validatePath(File var0, String var1) throws IOException {
      String var2 = var0.getCanonicalPath();
      if (ON_WINDOWS) {
         var2 = BACKSLASH_MATCHER.replaceFrom(var2, '/');
      }

      return var2.endsWith(var1);
   }

   @Override
   protected InputStream getResource(String var1) throws IOException {
      File var2 = this.getFile(var1);
      if (var2 == null) {
         throw new ResourcePackFileNotFoundException(this.file, var1);
      } else {
         return new FileInputStream(var2);
      }
   }

   @Override
   protected boolean hasResource(String var1) {
      return this.getFile(var1) != null;
   }

   @Nullable
   private File getFile(String var1) {
      try {
         File var2 = new File(this.file, var1);
         if (var2.isFile() && validatePath(var2, var1)) {
            return var2;
         }
      } catch (IOException var3) {
      }

      return null;
   }

   @Override
   public Set<String> getNamespaces(PackType var1) {
      Set<String> var2 = Sets.newHashSet();
      File var3x = new File(this.file, var1.getDirectory());
      File[] var4xx = var3x.listFiles((FilenameFilter) DirectoryFileFilter.DIRECTORY);
      if (var4xx != null) {
         for(File var8 : var4xx) {
            String var9 = getRelativePath(var3x, var8);
            if (var9.equals(var9.toLowerCase(Locale.ROOT))) {
               var2.add(var9.substring(0, var9.length() - 1));
            } else {
               this.logWarning(var9);
            }
         }
      }

      return var2;
   }

   @Override
   public void close() {
   }

   @Override
   public Collection<ResourceLocation> getResources(PackType var1, String var2, String var3, int var4, Predicate<String> var5) {
      File var6 = new File(this.file, var1.getDirectory());
      List<ResourceLocation> var7 = Lists.newArrayList();
      this.listResources(new File(new File(var6, var2), var3), var4, var2, var7, var3 + "/", var5);
      return var7;
   }

   private void listResources(File var1, int var2, String var3, List<ResourceLocation> var4, String var5, Predicate<String> var6) {
      File[] var7 = var1.listFiles();
      if (var7 != null) {
         for(File var11 : var7) {
            if (var11.isDirectory()) {
               if (var2 > 0) {
                  this.listResources(var11, var2 - 1, var3, var4, var5 + var11.getName() + "/", var6);
               }
            } else if (!var11.getName().endsWith(".mcmeta") && var6.test(var11.getName())) {
               try {
                  var4.add(new ResourceLocation(var3, var5 + var11.getName()));
               } catch (ResourceLocationException var12_1) {
                  LOGGER.error(var12_1.getMessage());
               }
            }
         }
      }
   }
}
