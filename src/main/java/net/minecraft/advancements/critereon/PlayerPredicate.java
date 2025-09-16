package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

public class PlayerPredicate {
   public static final PlayerPredicate ANY = new PlayerPredicate.Builder().build();
   private final MinMaxBounds.Ints level;
   private final GameType gameType;
   private final Map<Stat<?>, MinMaxBounds.Ints> stats;
   private final Object2BooleanMap<ResourceLocation> recipes;
   private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements;

   private static PlayerPredicate.AdvancementPredicate advancementPredicateFromJson(JsonElement var0) {
      if (var0.isJsonPrimitive()) {
         boolean var3 = var0.getAsBoolean();
         return new PlayerPredicate.AdvancementDonePredicate(var3);
      } else {
         Object2BooleanMap<String> var1 = new Object2BooleanOpenHashMap();
         JsonObject var2x = GsonHelper.convertToJsonObject(var0, "criterion data");
         var2x.entrySet().forEach(var1x -> {
            boolean var2x_ = GsonHelper.convertToBoolean((JsonElement)var1x.getValue(), "criterion test");
            var1.put(var1x.getKey(), var2x_);
         });
         return new PlayerPredicate.AdvancementCriterionsPredicate(var1);
      }
   }

   private PlayerPredicate(
      MinMaxBounds.Ints var1,
      GameType var2,
      Map<Stat<?>, MinMaxBounds.Ints> var3,
      Object2BooleanMap<ResourceLocation> var4,
      Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> var5
   ) {
      this.level = var1;
      this.gameType = var2;
      this.stats = var3;
      this.recipes = var4;
      this.advancements = var5;
   }

   public boolean matches(Entity var1) {
      if (this == ANY) {
         return true;
      } else if (!(var1 instanceof ServerPlayer)) {
         return false;
      } else {
         ServerPlayer var2 = (ServerPlayer)var1;
         if (!this.level.matches(var2.experienceLevel)) {
            return false;
         } else if (this.gameType != GameType.NOT_SET && this.gameType != var2.gameMode.getGameModeForPlayer()) {
            return false;
         } else {
            StatsCounter var3 = var2.getStats();

            for(Map.Entry<Stat<?>, MinMaxBounds.Ints> var5 : this.stats.entrySet()) {
               int var6 = var3.getValue((Stat)var5.getKey());
               if (!((MinMaxBounds.Ints)var5.getValue()).matches(var6)) {
                  return false;
               }
            }

            RecipeBook var10 = var2.getRecipeBook();
            ObjectIterator var11 = this.recipes.object2BooleanEntrySet().iterator();

            while(var11.hasNext()) {
               Object2BooleanMap.Entry<ResourceLocation> var13 = (Object2BooleanMap.Entry)var11.next();
               if (var10.contains((ResourceLocation)var13.getKey()) != var13.getBooleanValue()) {
                  return false;
               }
            }

            if (!this.advancements.isEmpty()) {
               PlayerAdvancements var12 = var2.getAdvancements();
               ServerAdvancementManager var14 = var2.getServer().getAdvancements();

               for(Map.Entry<ResourceLocation, AdvancementPredicate> var8 : this.advancements.entrySet()) {
                  Advancement var9 = var14.getAdvancement((ResourceLocation)var8.getKey());
                  if (var9 == null || !((AdvancementPredicate)var8.getValue()).test(var12.getOrStartProgress(var9))) {
                     return false;
                  }
               }
            }

            return true;
         }
      }
   }

   public static PlayerPredicate fromJson(@Nullable JsonElement var0) {
      if (var0 != null && !var0.isJsonNull()) {
         JsonObject var1 = GsonHelper.convertToJsonObject(var0, "player");
         MinMaxBounds.Ints var2 = MinMaxBounds.Ints.fromJson(var1.get("level"));
         String var3 = GsonHelper.getAsString(var1, "gamemode", "");
         GameType var4 = GameType.byName(var3, GameType.NOT_SET);
         Map<Stat<?>, MinMaxBounds.Ints> var5 = Maps.newHashMap();
         JsonArray var6 = GsonHelper.getAsJsonArray(var1, "stats", (JsonArray)null);
         if (var6 != null) {
            for(JsonElement var8 : var6) {
               JsonObject var9 = GsonHelper.convertToJsonObject(var8, "stats entry");
               ResourceLocation var10 = new ResourceLocation(GsonHelper.getAsString(var9, "type"));
               StatType<?> var11 = (StatType)Registry.STAT_TYPE.get(var10);
               if (var11 == null) {
                  throw new JsonParseException("Invalid stat type: " + var10);
               }

               ResourceLocation var12 = new ResourceLocation(GsonHelper.getAsString(var9, "stat"));
               Stat<?> var13 = getStat(var11, var12);
               MinMaxBounds.Ints var14 = MinMaxBounds.Ints.fromJson(var9.get("value"));
               var5.put(var13, var14);
            }
         }

         Object2BooleanMap<ResourceLocation> var15 = new Object2BooleanOpenHashMap();
         JsonObject var16 = GsonHelper.getAsJsonObject(var1, "recipes", new JsonObject());

         for(Map.Entry<String, JsonElement> var19 : var16.entrySet()) {
            ResourceLocation var21 = new ResourceLocation((String)var19.getKey());
            boolean var23 = GsonHelper.convertToBoolean((JsonElement)var19.getValue(), "recipe present");
            var15.put(var21, var23);
         }

         Map<ResourceLocation, AdvancementPredicate> var18 = Maps.newHashMap();
         JsonObject var20 = GsonHelper.getAsJsonObject(var1, "advancements", new JsonObject());

         for(Map.Entry<String, JsonElement> var24 : var20.entrySet()) {
            ResourceLocation var25 = new ResourceLocation((String)var24.getKey());
            AdvancementPredicate var26 = advancementPredicateFromJson((JsonElement)var24.getValue());
            var18.put(var25, var26);
         }

         return new PlayerPredicate(var2, var4, var5, var15, var18);
      } else {
         return ANY;
      }
   }

   private static <T> Stat<T> getStat(StatType<T> var0, ResourceLocation var1) {
      Registry<T> var2 = var0.getRegistry();
      T var3 = (T)var2.get(var1);
      if (var3 == null) {
         throw new JsonParseException("Unknown object " + var1 + " for stat type " + Registry.STAT_TYPE.getKey(var0));
      } else {
         return var0.get(var3);
      }
   }

   private static <T> ResourceLocation getStatValueId(Stat<T> var0) {
      return var0.getType().getRegistry().getKey(var0.getValue());
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject var1 = new JsonObject();
         var1.add("level", this.level.serializeToJson());
         if (this.gameType != GameType.NOT_SET) {
            var1.addProperty("gamemode", this.gameType.getName());
         }

         if (!this.stats.isEmpty()) {
            JsonArray var2 = new JsonArray();
            this.stats.forEach((var1x, var2x) -> {
               JsonObject var3 = new JsonObject();
               var3.addProperty("type", Registry.STAT_TYPE.getKey(var1x.getType()).toString());
               var3.addProperty("stat", getStatValueId(var1x).toString());
               var3.add("value", var2x.serializeToJson());
               var2.add(var3);
            });
            var1.add("stats", var2);
         }

         if (!this.recipes.isEmpty()) {
            JsonObject var3 = new JsonObject();
            this.recipes.forEach((var1x, var2x) -> var3.addProperty(var1x.toString(), var2x));
            var1.add("recipes", var3);
         }

         if (!this.advancements.isEmpty()) {
            JsonObject var4 = new JsonObject();
            this.advancements.forEach((var1x, var2x) -> var4.add(var1x.toString(), var2x.toJson()));
            var1.add("advancements", var4);
         }

         return var1;
      }
   }

   static class AdvancementCriterionsPredicate implements PlayerPredicate.AdvancementPredicate {
      private final Object2BooleanMap<String> criterions;

      public AdvancementCriterionsPredicate(Object2BooleanMap<String> var1) {
         this.criterions = var1;
      }

      @Override
      public JsonElement toJson() {
         JsonObject var1 = new JsonObject();
         this.criterions.forEach(var1::addProperty);
         return var1;
      }

      public boolean test(AdvancementProgress var1) {
         ObjectIterator var2 = this.criterions.object2BooleanEntrySet().iterator();

         while(var2.hasNext()) {
            Object2BooleanMap.Entry<String> var3 = (Object2BooleanMap.Entry)var2.next();
            CriterionProgress var4 = var1.getCriterion((String)var3.getKey());
            if (var4 == null || var4.isDone() != var3.getBooleanValue()) {
               return false;
            }
         }

         return true;
      }
   }

   static class AdvancementDonePredicate implements PlayerPredicate.AdvancementPredicate {
      private final boolean state;

      public AdvancementDonePredicate(boolean var1) {
         this.state = var1;
      }

      @Override
      public JsonElement toJson() {
         return new JsonPrimitive(this.state);
      }

      public boolean test(AdvancementProgress var1) {
         return var1.isDone() == this.state;
      }
   }

   interface AdvancementPredicate extends Predicate<AdvancementProgress> {
      JsonElement toJson();
   }

   public static class Builder {
      private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
      private GameType gameType = GameType.NOT_SET;
      private final Map<Stat<?>, MinMaxBounds.Ints> stats = Maps.newHashMap();
      private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap();
      private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements = Maps.newHashMap();

      public PlayerPredicate build() {
         return new PlayerPredicate(this.level, this.gameType, this.stats, this.recipes, this.advancements);
      }
   }
}
