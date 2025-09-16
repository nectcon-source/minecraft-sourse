package net.minecraft.stats;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerStatsCounter extends StatsCounter {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftServer server;
   private final File file;
   private final Set<Stat<?>> dirty = Sets.newHashSet();
   private int lastStatRequest = -300;

   public ServerStatsCounter(MinecraftServer var1, File var2) {
      this.server = var1;
      this.file = var2;
      if (var2.isFile()) {
         try {
            this.parseLocal(var1.getFixerUpper(), FileUtils.readFileToString(var2));
         } catch (IOException var3_3) {
            LOGGER.error("Couldn't read statistics file {}", var2, var3_3);
         } catch (JsonParseException var3_1) {
            LOGGER.error("Couldn't parse statistics file {}", var2, var3_1);
         }
      }
   }

   public void save() {
      try {
         FileUtils.writeStringToFile(this.file, this.toJson());
      } catch (IOException var2) {
         LOGGER.error("Couldn't save stats", var2);
      }
   }

   @Override
   public void setValue(Player var1, Stat<?> var2, int var3) {
      super.setValue(var1, var2, var3);
      this.dirty.add(var2);
   }

   private Set<Stat<?>> getDirty() {
      Set<Stat<?>> var1 = Sets.newHashSet(this.dirty);
      this.dirty.clear();
      return var1;
   }

   public void parseLocal(DataFixer var1, String var2) {
      try (JsonReader var3 = new JsonReader(new StringReader(var2))) {
         var3.setLenient(false);
         JsonElement var5 = Streams.parse(var3);
         if (var5.isJsonNull()) {
            LOGGER.error("Unable to parse Stat data from {}", this.file);
            return;
         }

         CompoundTag var6 = fromJson(var5.getAsJsonObject());
         if (!var6.contains("DataVersion", 99)) {
            var6.putInt("DataVersion", 1343);
         }

         var6 = NbtUtils.update(var1, DataFixTypes.STATS, var6, var6.getInt("DataVersion"));
         if (var6.contains("stats", 10)) {
            CompoundTag var7 = var6.getCompound("stats");

            for(String var9 : var7.getAllKeys()) {
               if (var7.contains(var9, 10)) {
                  Util.ifElse(Registry.STAT_TYPE.getOptional(new ResourceLocation(var9)), (var3x) -> {
                     CompoundTag var4 = var7.getCompound(var9);

                     for(String var6x : var4.getAllKeys()) {
                        if (var4.contains(var6x, 99)) {
                           Util.ifElse(this.getStat(var3x, var6x), (var3xx) -> this.stats.put(var3xx, var4.getInt(var6x)), () -> LOGGER.warn("Invalid statistic in {}: Don't know what {} is", this.file, var6x));
                        } else {
                           LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.file, var4.get(var6x), var6x);
                        }
                     }

                  }, () -> LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", this.file, var9));
               }
            }
         }
      } catch (IOException | JsonParseException var3_1) {
         LOGGER.error("Unable to parse Stat data from {}", this.file, var3_1);
      }
   }

   private <T> Optional<Stat<T>> getStat(StatType<T> var1, String var2) {
      return Optional.ofNullable(ResourceLocation.tryParse(var2)).flatMap(var1.getRegistry()::getOptional).map(var1::get);
   }

   private static CompoundTag fromJson(JsonObject var0) {
      CompoundTag var1 = new CompoundTag();

      for(Map.Entry<String, JsonElement> var3 : var0.entrySet()) {
         JsonElement var4 = (JsonElement)var3.getValue();
         if (var4.isJsonObject()) {
            var1.put((String)var3.getKey(), fromJson(var4.getAsJsonObject()));
         } else if (var4.isJsonPrimitive()) {
            JsonPrimitive var5 = var4.getAsJsonPrimitive();
            if (var5.isNumber()) {
               var1.putInt((String)var3.getKey(), var5.getAsInt());
            }
         }
      }

      return var1;
   }

   protected String toJson() {
      Map<StatType<?>, JsonObject> var1 = Maps.newHashMap();
      ObjectIterator var2 = this.stats.object2IntEntrySet().iterator();

      while(var2.hasNext()) {
         Object2IntMap.Entry<Stat<?>> var3 = (Object2IntMap.Entry)var2.next();
         Stat<?> var4 = (Stat)var3.getKey();
         ((JsonObject)var1.computeIfAbsent(var4.getType(), (var0) -> new JsonObject())).addProperty(getKey(var4).toString(), var3.getIntValue());
      }

      JsonObject var5 = new JsonObject();

      for(Map.Entry<StatType<?>, JsonObject> var8 : var1.entrySet()) {
         var5.add(Registry.STAT_TYPE.getKey(var8.getKey()).toString(), (JsonElement)var8.getValue());
      }

      JsonObject var7 = new JsonObject();
      var7.add("stats", var5);
      var7.addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
      return var7.toString();
   }

   private static <T> ResourceLocation getKey(Stat<T> var0) {
      return var0.getType().getRegistry().getKey(var0.getValue());
   }

   public void markAllDirty() {
      this.dirty.addAll(this.stats.keySet());
   }

   public void sendStats(ServerPlayer var1) {
      int var2 = this.server.getTickCount();
      Object2IntMap<Stat<?>> var3 = new Object2IntOpenHashMap();
      if (var2 - this.lastStatRequest > 300) {
         this.lastStatRequest = var2;

         for(Stat<?> var5 : this.getDirty()) {
            var3.put(var5, this.getValue(var5));
         }
      }

      var1.connection.send(new ClientboundAwardStatsPacket(var3));
   }
}
