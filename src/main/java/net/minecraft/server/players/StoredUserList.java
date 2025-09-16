package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StoredUserList<K, V extends StoredUserEntry<K>> {
   protected static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final File file;
   private final Map<String, V> map = Maps.newHashMap();

   public StoredUserList(File var1) {
      this.file = var1;
   }

   public File getFile() {
      return this.file;
   }

   public void add(V var1) {
      this.map.put(this.getKeyForUser(var1.getUser()), var1);

      try {
         this.save();
      } catch (IOException var3) {
         LOGGER.warn("Could not save the list after adding a user.", var3);
      }
   }

   @Nullable
   public V get(K var1) {
      this.removeExpired();
      return this.map.get(this.getKeyForUser(var1));
   }

   public void remove(K var1) {
      this.map.remove(this.getKeyForUser(var1));

      try {
         this.save();
      } catch (IOException var3) {
         LOGGER.warn("Could not save the list after removing a user.", var3);
      }
   }

   public void remove(StoredUserEntry<K> var1) {
      this.remove(var1.getUser());
   }

   public String[] getUserList() {
      return this.map.keySet().toArray(new String[this.map.size()]);
   }

   public boolean isEmpty() {
      return this.map.size() < 1;
   }

   protected String getKeyForUser(K var1) {
      return var1.toString();
   }

   protected boolean contains(K var1) {
      return this.map.containsKey(this.getKeyForUser(var1));
   }

   private void removeExpired() {
      List<K> var1 = Lists.newArrayList();

      for(V var3 : this.map.values()) {
         if (var3.hasExpired()) {
            var1.add(var3.getUser());
         }
      }

      for(K var5 : var1) {
         this.map.remove(this.getKeyForUser(var5));
      }
   }

   protected abstract StoredUserEntry<K> createEntry(JsonObject var1);

   public Collection<V> getEntries() {
      return this.map.values();
   }

   public void save() throws IOException {
      JsonArray var1 = new JsonArray();
      this.map.values().stream().map(var0 -> Util.make(new JsonObject(), var0::serialize)).forEach(var1::add);

      try (BufferedWriter var2x = Files.newWriter(this.file, StandardCharsets.UTF_8)) {
         GSON.toJson(var1, var2x);
      }
   }

   public void load() throws IOException {
      if (this.file.exists()) {
         try (BufferedReader var1 = Files.newReader(this.file, StandardCharsets.UTF_8)) {
            JsonArray var3 = (JsonArray)GSON.fromJson(var1, JsonArray.class);
            this.map.clear();

            for(JsonElement var5 : var3) {
               JsonObject var6 = GsonHelper.convertToJsonObject(var5, "entry");
               StoredUserEntry<K> var7 = this.createEntry(var6);
               if (var7.getUser() != null) {
                  this.map.put(this.getKeyForUser(var7.getUser()), (V) var7);
               }
            }
         }
      }
   }
}
