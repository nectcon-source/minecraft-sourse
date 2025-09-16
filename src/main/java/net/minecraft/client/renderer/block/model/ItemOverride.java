package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemOverride {
   private final ResourceLocation model;
   private final Map<ResourceLocation, Float> predicates;

   public ItemOverride(ResourceLocation var1, Map<ResourceLocation, Float> var2) {
      this.model = var1;
      this.predicates = var2;
   }

   public ResourceLocation getModel() {
      return this.model;
   }

   boolean test(ItemStack var1, @Nullable ClientLevel var2, @Nullable LivingEntity var3) {
      Item var4 = var1.getItem();

      for(Map.Entry<ResourceLocation, Float> var6 : this.predicates.entrySet()) {
         ItemPropertyFunction var7 = ItemProperties.getProperty(var4, var6.getKey());
         if (var7 == null || var7.call(var1, var2, var3) < var6.getValue()) {
            return false;
         }
      }

      return true;
   }

   public static class Deserializer implements JsonDeserializer<ItemOverride> {
      protected Deserializer() {
      }

      public ItemOverride deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = var1.getAsJsonObject();
         ResourceLocation var5 = new ResourceLocation(GsonHelper.getAsString(var4, "model"));
         Map<ResourceLocation, Float> var6 = this.getPredicates(var4);
         return new ItemOverride(var5, var6);
      }

      protected Map<ResourceLocation, Float> getPredicates(JsonObject var1) {
         Map<ResourceLocation, Float> var2 = Maps.newLinkedHashMap();
         JsonObject var3 = GsonHelper.getAsJsonObject(var1, "predicate");

         for(Map.Entry<String, JsonElement> var5 : var3.entrySet()) {
            var2.put(new ResourceLocation(var5.getKey()), GsonHelper.convertToFloat(var5.getValue(), var5.getKey()));
         }

         return var2;
      }
   }
}
