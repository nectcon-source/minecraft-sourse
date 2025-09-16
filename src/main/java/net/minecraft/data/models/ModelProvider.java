package net.minecraft.data.models;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModelProvider implements DataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private final DataGenerator generator;

   public ModelProvider(DataGenerator var1) {
      this.generator = var1;
   }

   @Override
   public void run(HashCache var1) {
      Path var2 = this.generator.getOutputFolder();
      Map<Block, BlockStateGenerator> var3 = Maps.newHashMap();
      Consumer<BlockStateGenerator> var4 = (var1x) -> {
         Block var2_ = var1x.getBlock();
         BlockStateGenerator var3x = var3.put(var2_, var1x);
         if (var3x != null) {
            throw new IllegalStateException("Duplicate blockstate definition for " + var2_);
         }
      };
      Map<ResourceLocation, Supplier<JsonElement>> var5 = Maps.newHashMap();
      Set<Item> var6 = Sets.newHashSet();
      BiConsumer<ResourceLocation, Supplier<JsonElement>> var7 = (var1x, var2x) -> {
         Supplier<JsonElement> var3_ = var5.put(var1x, var2x);
         if (var3_ != null) {
            throw new IllegalStateException("Duplicate model definition for " + var1x);
         }
      };
      Consumer<Item> var8 = var6::add;
      (new BlockModelGenerators(var4, var7, var8)).run();
      (new ItemModelGenerators(var7)).run();
      List<Block> var9 = Registry.BLOCK.stream().filter((var1x) -> !var3.containsKey(var1x)).collect(Collectors.toList());
      if (!var9.isEmpty()) {
         throw new IllegalStateException("Missing blockstate definitions for: " + var9);
      } else {
         Registry.BLOCK.forEach((var2x) -> {
            Item var3__ = (Item)Item.BY_BLOCK.get(var2x);
            if (var3__ != null) {
               if (var6.contains(var3__)) {
                  return;
               }

               ResourceLocation var4_ = ModelLocationUtils.getModelLocation(var3__);
               if (!var5.containsKey(var4_)) {
                  var5.put(var4_, new DelegatedModel(ModelLocationUtils.getModelLocation(var2x)));
               }
            }

         });
         this.saveCollection(var1, var2, var3, ModelProvider::createBlockStatePath);
         this.saveCollection(var1, var2, var5, ModelProvider::createModelPath);
      }
   }

   private <T> void saveCollection(HashCache var1, Path var2, Map<T, ? extends Supplier<JsonElement>> var3, BiFunction<Path, T, Path> var4) {
      var3.forEach((var3x, var4x) -> {
         Path var5 = var4.apply(var2, var3x);

         try {
            DataProvider.save(GSON, var1, var4x.get(), var5);
         } catch (Exception var6_1) {
            LOGGER.error("Couldn't save {}", var5, var6_1);
         }

      });
   }

   private static Path createBlockStatePath(Path var0, Block var1) {
      ResourceLocation var2 = Registry.BLOCK.getKey(var1);
      return var0.resolve("assets/" + var2.getNamespace() + "/blockstates/" + var2.getPath() + ".json");
   }

   private static Path createModelPath(Path var0, ResourceLocation var1) {
      return var0.resolve("assets/" + var1.getNamespace() + "/models/" + var1.getPath() + ".json");
   }

   @Override
   public String getName() {
      return "Block State Definitions";
   }
}
