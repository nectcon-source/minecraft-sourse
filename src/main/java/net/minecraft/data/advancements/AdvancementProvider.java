package net.minecraft.data.advancements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancementProvider implements DataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final DataGenerator generator;
   private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(
      new TheEndAdvancements(), new HusbandryAdvancements(), new AdventureAdvancements(), new NetherAdvancements(), new StoryAdvancements()
   );

   public AdvancementProvider(DataGenerator var1) {
      this.generator = var1;
   }

   @Override
   public void run(HashCache var1) throws IOException {
      Path var2 = this.generator.getOutputFolder();
      Set<ResourceLocation> var3 = Sets.newHashSet();
      Consumer<Advancement> var4xx = var3x -> {
         if (!var3.add(var3x.getId())) {
            throw new IllegalStateException("Duplicate advancement " + var3x.getId());
         } else {
            Path var4x = createPath(var2, var3x);

            try {
               DataProvider.save(GSON, var1, var3x.deconstruct().serializeToJson(), var4x);
            } catch (IOException var6x) {
               LOGGER.error("Couldn't save advancement {}", var4x, var6x);
            }
         }
      };

      for(Consumer<Consumer<Advancement>> var6 : this.tabs) {
         var6.accept(var4xx);
      }
   }

   private static Path createPath(Path var0, Advancement var1) {
      return var0.resolve("data/" + var1.getId().getNamespace() + "/advancements/" + var1.getId().getPath() + ".json");
   }

   @Override
   public String getName() {
      return "Advancements";
   }
}
