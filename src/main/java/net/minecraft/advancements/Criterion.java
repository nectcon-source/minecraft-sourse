

package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Criterion {
   private final CriterionTriggerInstance trigger;

   public Criterion(CriterionTriggerInstance var1) {
      this.trigger = var1;
   }

   public Criterion() {
      this.trigger = null;
   }

   public void serializeToNetwork(FriendlyByteBuf var1) {
   }

   public static Criterion criterionFromJson(JsonObject var0, DeserializationContext var1) {
      ResourceLocation var2 = new ResourceLocation(GsonHelper.getAsString(var0, "trigger"));
      CriterionTrigger<?> var3 = CriteriaTriggers.getCriterion(var2);
      if (var3 == null) {
         throw new JsonSyntaxException("Invalid criterion trigger: " + var2);
      } else {
         CriterionTriggerInstance var4 = var3.createInstance(GsonHelper.getAsJsonObject(var0, "conditions", new JsonObject()), var1);
         return new Criterion(var4);
      }
   }

   public static Criterion criterionFromNetwork(FriendlyByteBuf var0) {
      return new Criterion();
   }

   public static Map<String, Criterion> criteriaFromJson(JsonObject var0, DeserializationContext var1) {
      Map<String, Criterion> var2 = Maps.newHashMap();

      for(Map.Entry<String, JsonElement> var4 : var0.entrySet()) {
         var2.put(var4.getKey(), criterionFromJson(GsonHelper.convertToJsonObject((JsonElement)var4.getValue(), "criterion"), var1));
      }

      return var2;
   }

   public static Map<String, Criterion> criteriaFromNetwork(FriendlyByteBuf var0) {
      Map<String, Criterion> var1 = Maps.newHashMap();
      int var2 = var0.readVarInt();

      for(int var3 = 0; var3 < var2; ++var3) {
         var1.put(var0.readUtf(32767), criterionFromNetwork(var0));
      }

      return var1;
   }

   public static void serializeToNetwork(Map<String, Criterion> var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.size());

      for(Map.Entry<String, Criterion> var3 : var0.entrySet()) {
         var1.writeUtf((String)var3.getKey());
         ((Criterion)var3.getValue()).serializeToNetwork(var1);
      }

   }

   @Nullable
   public CriterionTriggerInstance getTrigger() {
      return this.trigger;
   }

   public JsonElement serializeToJson() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("trigger", this.trigger.getCriterion().toString());
      JsonObject var2 = this.trigger.serializeToJson(SerializationContext.INSTANCE);
      if (var2.size() != 0) {
         var1.add("conditions", var2);
      }

      return var1;
   }
}
