package net.minecraft.data;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/data/HashCache.class */
public class HashCache {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Path path;
   private final Path cachePath;
   private int hits;
   private final Map<Path, String> oldCache = Maps.newHashMap();
   private final Map<Path, String> newCache = Maps.newHashMap();
   private final Set<Path> keep = Sets.newHashSet();

   public HashCache(Path path, String str) throws IOException {
      this.path = path;
      Path resolve = path.resolve(".cache");
      Files.createDirectories(resolve);
      this.cachePath = resolve.resolve(str);
      walkOutputFiles().forEach(path2 -> {
         this.oldCache.put(path2, "");
      });
      if (Files.isReadable(this.cachePath)) {
         IOUtils.readLines(Files.newInputStream(this.cachePath), Charsets.UTF_8).forEach(str2 -> {
            int indexOf = str2.indexOf(32);
            this.oldCache.put(path.resolve(str2.substring(indexOf + 1)), str2.substring(0, indexOf));
         });
      }
   }

   public void purgeStaleAndWrite() throws IOException {
      removeStale();
      try {
         Writer newBufferedWriter = Files.newBufferedWriter(this.cachePath, new OpenOption[0]);
         IOUtils.writeLines( this.newCache.entrySet().stream().map(entry -> {
            return entry.getValue() + ' ' + this.path.relativize(entry.getKey());
         }).collect(Collectors.toList()), System.lineSeparator(), newBufferedWriter);
         newBufferedWriter.close();
         LOGGER.debug("Caching: cache hits: {}, created: {} removed: {}", Integer.valueOf(this.hits), Integer.valueOf(this.newCache.size() - this.hits), Integer.valueOf(this.oldCache.size()));
      } catch (IOException e) {
         LOGGER.warn("Unable write cachefile {}: {}", this.cachePath, e.toString());
      }
   }

   @Nullable
   public String getHash(Path path) {
      return this.oldCache.get(path);
   }

   public void putNew(Path path, String str) {
      this.newCache.put(path, str);
      if (Objects.equals(this.oldCache.remove(path), str)) {
         this.hits++;
      }
   }

   public boolean had(Path path) {
      return this.oldCache.containsKey(path);
   }

   public void keep(Path path) {
      this.keep.add(path);
   }

   private void removeStale() throws IOException {
      walkOutputFiles().forEach(path -> {
         if (had(path) && !this.keep.contains(path)) {
            try {
               Files.delete(path);
            } catch (IOException e) {
               LOGGER.debug("Unable to delete: {} ({})", path, e.toString());
            }
         }
      });
   }

   private Stream<Path> walkOutputFiles() throws IOException {
      return Files.walk(this.path, new FileVisitOption[0]).filter(path -> {
         return !Objects.equals(this.cachePath, path) && !Files.isDirectory(path);
      });
   }
}
