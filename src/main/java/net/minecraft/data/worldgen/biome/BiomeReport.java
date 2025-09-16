package net.minecraft.data.worldgen.biome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeReport implements DataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final DataGenerator generator;

   public BiomeReport(DataGenerator var1) {
      this.generator = var1;
   }

   @Override
   public void run(HashCache hashCache) {
      Path outputFolder = this.generator.getOutputFolder();
      for (Map.Entry<ResourceKey<Biome>, Biome> entry : BuiltinRegistries.BIOME.entrySet()) {
         Path createPath = createPath(outputFolder, entry.getKey().location());
         Biome value = entry.getValue();
         try {
            Optional<JsonElement> result = JsonOps.INSTANCE.withEncoder(Biome.CODEC).apply(() -> {
               return value;
            }).result();
            if (result.isPresent()) {
               DataProvider.save(GSON, hashCache, result.get(), createPath);
            } else {
               LOGGER.error("Couldn't serialize biome {}", createPath);
            }
         } catch (IOException e) {
            LOGGER.error("Couldn't save biome {}", createPath, e);
         }
      }
   }

   private static Path createPath(Path path, ResourceLocation resourceLocation) {
      return path.resolve("reports/biomes/" + resourceLocation.getPath() + ".json");
   }

   @Override
   public String getName() {
      return "Biomes";
   }
}
