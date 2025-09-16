package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;

/* loaded from: client_deobf_norm.jar:net/minecraft/data/info/RegistryDumpReport.class */
public class RegistryDumpReport implements DataProvider {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final DataGenerator generator;

   public RegistryDumpReport(DataGenerator dataGenerator) {
      this.generator = dataGenerator;
   }

   @Override // net.minecraft.data.DataProvider
   public void run(HashCache hashCache) throws IOException {
      JsonObject jsonObject = new JsonObject();
      Registry.REGISTRY.keySet().forEach(resourceLocation -> {
         jsonObject.add(resourceLocation.toString(), dumpRegistry(Registry.REGISTRY.get(resourceLocation)));
      });
      DataProvider.save(GSON, hashCache, jsonObject, this.generator.getOutputFolder().resolve("reports/registries.json"));
   }

   private static <T> JsonElement dumpRegistry(Registry<T> registry2) {
//      JsonObject var1 = new JsonObject();
//      if (registry2 instanceof DefaultedRegistry) {
//         ResourceLocation var2 = ((DefaultedRegistry)registry2).getDefaultKey();
//         var1.addProperty("default", var2.toString());
//      }
//
//      int var9 = (Registry)Registry.REGISTRY.getId(registry2);
//      var1.addProperty("protocol_id", var9);
//      JsonObject var3 = new JsonObject();
//
//      for(ResourceLocation var5 : registry2.keySet()) {
//         T var6 = (T)registry2.get(var5);
//         int var7 = registry2.getId(var6);
//         JsonObject var8 = new JsonObject();
//         var8.addProperty("protocol_id", var7);
//         var3.add(var5.toString(), var8);
//      }
//
//      var1.add("entries", var3);
//      return var1;
//   }
      JsonObject jsonobject = new JsonObject();
      if (registry2 instanceof DefaultedRegistry) {
         ResourceLocation resourcelocation = ((DefaultedRegistry)registry2).getDefaultKey();
         jsonobject.addProperty("default", resourcelocation.toString());
      }

      int j = ((Registry)Registry.REGISTRY).getId(registry2);
      jsonobject.addProperty("protocol_id", j);
      JsonObject jsonobject1 = new JsonObject();

      for(ResourceLocation resourcelocation1 : registry2.keySet()) {
         T t = registry2.get(resourcelocation1);
         int i = registry2.getId(t);
         JsonObject jsonobject2 = new JsonObject();
         jsonobject2.addProperty("protocol_id", i);
         jsonobject1.add(resourcelocation1.toString(), jsonobject2);
      }

      jsonobject.add("entries", jsonobject1);
      return jsonobject;
   }

   @Override // net.minecraft.data.DataProvider
   public String getName() {
      return "Registry Dump";
   }
}
