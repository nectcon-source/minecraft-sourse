

package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public class AdvancementProgress implements Comparable<AdvancementProgress> {
   private final Map<String, CriterionProgress> criteria = Maps.newHashMap();
   private String[][] requirements = new String[0][];

   public AdvancementProgress() {
   }

   public void update(Map<String, Criterion> var1, String[][] var2) {
      Set<String> var3 = var1.keySet();
      this.criteria.entrySet().removeIf((var1x) -> !var3.contains(var1x.getKey()));

      for(String var5 : var3) {
         if (!this.criteria.containsKey(var5)) {
            this.criteria.put(var5, new CriterionProgress());
         }
      }

      this.requirements = var2;
   }

   public boolean isDone() {
      if (this.requirements.length == 0) {
         return false;
      } else {
         for(String[] var4 : this.requirements) {
            boolean var5 = false;

            for(String var9 : var4) {
               CriterionProgress var10 = this.getCriterion(var9);
               if (var10 != null && var10.isDone()) {
                  var5 = true;
                  break;
               }
            }

            if (!var5) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean hasProgress() {
      for(CriterionProgress var2 : this.criteria.values()) {
         if (var2.isDone()) {
            return true;
         }
      }

      return false;
   }

   public boolean grantProgress(String var1) {
      CriterionProgress var2 = (CriterionProgress)this.criteria.get(var1);
      if (var2 != null && !var2.isDone()) {
         var2.grant();
         return true;
      } else {
         return false;
      }
   }

   public boolean revokeProgress(String var1) {
      CriterionProgress var2 = (CriterionProgress)this.criteria.get(var1);
      if (var2 != null && var2.isDone()) {
         var2.revoke();
         return true;
      } else {
         return false;
      }
   }

   public String toString() {
      return "AdvancementProgress{criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
   }

   public void serializeToNetwork(FriendlyByteBuf var1) {
      var1.writeVarInt(this.criteria.size());

      for(Map.Entry<String, CriterionProgress> var3 : this.criteria.entrySet()) {
         var1.writeUtf((String)var3.getKey());
         ((CriterionProgress)var3.getValue()).serializeToNetwork(var1);
      }

   }

   public static AdvancementProgress fromNetwork(FriendlyByteBuf var0) {
      AdvancementProgress var1 = new AdvancementProgress();
      int var2 = var0.readVarInt();

      for(int var3 = 0; var3 < var2; ++var3) {
         var1.criteria.put(var0.readUtf(32767), CriterionProgress.fromNetwork(var0));
      }

      return var1;
   }

   @Nullable
   public CriterionProgress getCriterion(String var1) {
      return (CriterionProgress)this.criteria.get(var1);
   }

   public float getPercent() {
      if (this.criteria.isEmpty()) {
         return 0.0F;
      } else {
         float var1 = (float)this.requirements.length;
         float var2 = (float)this.countCompletedRequirements();
         return var2 / var1;
      }
   }

   @Nullable
   public String getProgressText() {
      if (this.criteria.isEmpty()) {
         return null;
      } else {
         int var1 = this.requirements.length;
         if (var1 <= 1) {
            return null;
         } else {
            int var2 = this.countCompletedRequirements();
            return var2 + "/" + var1;
         }
      }
   }

   private int countCompletedRequirements() {
      int var1 = 0;

      for(String[] var5 : this.requirements) {
         boolean var6 = false;

         for(String var10 : var5) {
            CriterionProgress var11 = this.getCriterion(var10);
            if (var11 != null && var11.isDone()) {
               var6 = true;
               break;
            }
         }

         if (var6) {
            ++var1;
         }
      }

      return var1;
   }

   public Iterable<String> getRemainingCriteria() {
      List<String> var1 = Lists.newArrayList();

      for(Map.Entry<String, CriterionProgress> var3 : this.criteria.entrySet()) {
         if (!((CriterionProgress)var3.getValue()).isDone()) {
            var1.add(var3.getKey());
         }
      }

      return var1;
   }

   public Iterable<String> getCompletedCriteria() {
      List<String> var1 = Lists.newArrayList();

      for(Map.Entry<String, CriterionProgress> var3 : this.criteria.entrySet()) {
         if (((CriterionProgress)var3.getValue()).isDone()) {
            var1.add(var3.getKey());
         }
      }

      return var1;
   }

   @Nullable
   public Date getFirstProgressDate() {
      Date var1 = null;

      for(CriterionProgress var3 : this.criteria.values()) {
         if (var3.isDone() && (var1 == null || var3.getObtained().before(var1))) {
            var1 = var3.getObtained();
         }
      }

      return var1;
   }

   public int compareTo(AdvancementProgress var1) {
      Date var2 = this.getFirstProgressDate();
      Date var3 = var1.getFirstProgressDate();
      if (var2 == null && var3 != null) {
         return 1;
      } else if (var2 != null && var3 == null) {
         return -1;
      } else {
         return var2 == null && var3 == null ? 0 : var2.compareTo(var3);
      }
   }

   public static class Serializer implements JsonDeserializer<AdvancementProgress>, JsonSerializer<AdvancementProgress> {
      public Serializer() {
      }

      public JsonElement serialize(AdvancementProgress var1, Type var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();
         JsonObject var5 = new JsonObject();

         for(Map.Entry<String, CriterionProgress> var7 : var1.criteria.entrySet()) {
            CriterionProgress var8 = (CriterionProgress)var7.getValue();
            if (var8.isDone()) {
               var5.add((String)var7.getKey(), var8.serializeToJson());
            }
         }

         if (!var5.entrySet().isEmpty()) {
            var4.add("criteria", var5);
         }

         var4.addProperty("done", var1.isDone());
         return var4;
      }

      public AdvancementProgress deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = GsonHelper.convertToJsonObject(var1, "advancement");
         JsonObject var5 = GsonHelper.getAsJsonObject(var4, "criteria", new JsonObject());
         AdvancementProgress var6 = new AdvancementProgress();

         for(Map.Entry<String, JsonElement> var8 : var5.entrySet()) {
            String var9 = (String)var8.getKey();
            var6.criteria.put(var9, CriterionProgress.fromJson(GsonHelper.convertToString((JsonElement)var8.getValue(), var9)));
         }

         return var6;
      }
   }
}
