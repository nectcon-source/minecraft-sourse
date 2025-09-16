package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnbtToNbt implements DataProvider {
   @Nullable
   private static final Path dumpSnbtTo = null;
   private static final Logger LOGGER = LogManager.getLogger();
   private final DataGenerator generator;
   private final List<SnbtToNbt.Filter> filters = Lists.newArrayList();

   public SnbtToNbt(DataGenerator var1) {
      this.generator = var1;
   }

   public SnbtToNbt addFilter(SnbtToNbt.Filter var1) {
      this.filters.add(var1);
      return this;
   }

   private CompoundTag applyFilters(String var1, CompoundTag var2) {
      CompoundTag var3 = var2;

      for(Filter var5 : this.filters) {
         var3 = var5.apply(var1, var3);
      }

      return var3;
   }

   @Override
   public void run(HashCache var1) throws IOException {
      Path var2 = this.generator.getOutputFolder();
      List<CompletableFuture<TaskResult>> var3 = Lists.newArrayList();

      for(Path var5 : this.generator.getInputFolders()) {
         Files.walk(var5).filter((var0) -> var0.toString().endsWith(".snbt")).forEach((var3x) -> var3.add(CompletableFuture.supplyAsync(() -> this.readStructure(var3x, this.getName(var5, var3x)), Util.backgroundExecutor())));
      }

      Util.sequence(var3).join().stream().filter(Objects::nonNull).forEach((var3x) -> this.storeStructureIfChanged(var1, var3x, var2));
   }

   @Override
   public String getName() {
      return "SNBT -> NBT";
   }

   private String getName(Path var1, Path var2) {
      String var3 = var1.relativize(var2).toString().replaceAll("\\\\", "/");
      return var3.substring(0, var3.length() - ".snbt".length());
   }

   @Nullable
   private SnbtToNbt.TaskResult readStructure(Path var1, String var2) {
      try (BufferedReader var3 = Files.newBufferedReader(var1)) {
         String var5 = IOUtils.toString(var3);
         CompoundTag var6 = this.applyFilters(var2, TagParser.parseTag(var5));
         ByteArrayOutputStream var7 = new ByteArrayOutputStream();
         NbtIo.writeCompressed(var6, var7);
         byte[] var8 = var7.toByteArray();
         String var9 = SHA1.hashBytes(var8).toString();
         String var10;
         if (dumpSnbtTo != null) {
            var10 = var6.getPrettyDisplay("    ", 0).getString() + "\n";
         } else {
            var10 = null;
         }

         TaskResult var11 = new TaskResult(var2, var8, var10, var9);
         return var11;
      } catch (CommandSyntaxException var3_3) {
         LOGGER.error("Couldn't convert {} from SNBT to NBT at {} as it's invalid SNBT", var2, var1, var3_3);
      } catch (IOException var3_1) {
         LOGGER.error("Couldn't convert {} from SNBT to NBT at {}", var2, var1, var3_1);
      }

      return null;
   }

   private void storeStructureIfChanged(HashCache var1, SnbtToNbt.TaskResult var2, Path var3) {
      if (var2.snbtPayload != null) {
         Path var4 = dumpSnbtTo.resolve(var2.name + ".snbt");

         try {
            FileUtils.write(var4.toFile(), var2.snbtPayload, StandardCharsets.UTF_8);
         } catch (IOException var5_1) {
            LOGGER.error("Couldn't write structure SNBT {} at {}", var2.name, var4, var5_1);
         }
      }

      Path var21 = var3.resolve(var2.name + ".nbt");

      try {
         if (!Objects.equals(var1.getHash(var21), var2.hash) || !Files.exists(var21, new LinkOption[0])) {
            Files.createDirectories(var21.getParent());

            try (OutputStream var5 = Files.newOutputStream(var21)) {
               var5.write(var2.payload);
            }
         }

         var1.putNew(var21, var2.hash);
      } catch (IOException var5_3) {
         LOGGER.error("Couldn't write structure {} at {}", var2.name, var21, var5_3);
      }
   }

   @FunctionalInterface
   public interface Filter {
      CompoundTag apply(String var1, CompoundTag var2);
   }

   static class TaskResult {
      private final String name;
      private final byte[] payload;
      @Nullable
      private final String snbtPayload;
      private final String hash;

      public TaskResult(String var1, byte[] var2, @Nullable String var3, String var4) {
         this.name = var1;
         this.payload = var2;
         this.snbtPayload = var3;
         this.hash = var4;
      }
   }
}
