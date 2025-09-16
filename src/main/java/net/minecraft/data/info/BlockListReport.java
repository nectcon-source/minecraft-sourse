package net.minecraft.data.info;

import com.google.common.collect.UnmodifiableIterator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Iterator;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

/* loaded from: client_deobf_norm.jar:net/minecraft/data/info/BlockListReport.class */
public class BlockListReport implements DataProvider {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final DataGenerator generator;

   public BlockListReport(DataGenerator dataGenerator) {
      this.generator = dataGenerator;
   }

   @Override // net.minecraft.data.DataProvider
   public void run(HashCache hashCache) throws IOException {
      JsonObject jsonObject = new JsonObject();
      Iterator<Block> it = Registry.BLOCK.iterator();
      while (it.hasNext()) {
         Block next = it.next();
         ResourceLocation key = Registry.BLOCK.getKey(next);
         JsonObject jsonObject2 = new JsonObject();
         StateDefinition<Block, BlockState> stateDefinition = next.getStateDefinition();
         if (!stateDefinition.getProperties().isEmpty()) {
            JsonObject jsonObject3 = new JsonObject();
            for (Property<?> property : stateDefinition.getProperties()) {
               JsonArray jsonArray = new JsonArray();
               Iterator<?> it2 = property.getPossibleValues().iterator();
               while (it2.hasNext()) {
                  jsonArray.add(Util.getPropertyName(property, (Comparable) it2.next()));
               }
               jsonObject3.add(property.getName(), jsonArray);
            }
            jsonObject2.add("properties", jsonObject3);
         }
         JsonArray jsonArray2 = new JsonArray();
         UnmodifiableIterator it3 = stateDefinition.getPossibleStates().iterator();
         while (it3.hasNext()) {
            BlockState blockState = (BlockState) it3.next();
            JsonObject jsonObject4 = new JsonObject();
            JsonObject jsonObject5 = new JsonObject();
            for (Property<?> property2 : stateDefinition.getProperties()) {
               jsonObject5.addProperty(property2.getName(), Util.getPropertyName(property2, blockState.getValue(property2)));
            }
            if (jsonObject5.size() > 0) {
               jsonObject4.add("properties", jsonObject5);
            }
            jsonObject4.addProperty("id", Integer.valueOf(Block.getId(blockState)));
            if (blockState == next.defaultBlockState()) {
               jsonObject4.addProperty("default", true);
            }
            jsonArray2.add(jsonObject4);
         }
         jsonObject2.add("states", jsonArray2);
         jsonObject.add(key.toString(), jsonObject2);
      }
      DataProvider.save(GSON, hashCache, jsonObject, this.generator.getOutputFolder().resolve("reports/blocks.json"));
   }

   @Override // net.minecraft.data.DataProvider
   public String getName() {
      return "Block List";
   }
}
