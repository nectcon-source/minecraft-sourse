package net.minecraft.data.structures;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NbtToSnbt implements DataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private final DataGenerator generator;

   public NbtToSnbt(DataGenerator var1) {
      this.generator = var1;
   }

   @Override
   public void run(HashCache var1) throws IOException {
      Path var2 = this.generator.getOutputFolder();

      for(Path var4 : this.generator.getInputFolders()) {
         Files.walk(var4).filter((var0) -> var0.toString().endsWith(".nbt")).forEach((var3) -> convertStructure(var3, this.getName(var4, var3), var2));
      }
   }

   @Override
   public String getName() {
      return "NBT to SNBT";
   }

   private String getName(Path var1, Path var2) {
      String var3 = var1.relativize(var2).toString().replaceAll("\\\\", "/");
      return var3.substring(0, var3.length() - ".nbt".length());
   }

   @Nullable
   public static Path convertStructure(Path var0, String var1, Path var2) {
      try {
         CompoundTag var3 = NbtIo.readCompressed(Files.newInputStream(var0));
         Component var4 = var3.getPrettyDisplay("    ", 0);
         String var5 = var4.getString() + "\n";
         Path var6 = var2.resolve(var1 + ".snbt");
         Files.createDirectories(var6.getParent());

         try (BufferedWriter var7 = Files.newBufferedWriter(var6)) {
            var7.write(var5);
         }

         LOGGER.info("Converted {} from NBT to SNBT", var1);
         return var6;
      } catch (IOException var3_1) {
         LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", var1, var0, var3_1);
         return null;
      }
   }
}
