package com.mojang.realmsclient.dto;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Set;

public class Ops extends ValueObject {
   public Set<String> ops = Sets.newHashSet();

   public static Ops parse(String var0) {
      Ops var1 = new Ops();
      JsonParser var2x = new JsonParser();

      try {
         JsonElement var3xx = var2x.parse(var0);
         JsonObject var4xxx = var3xx.getAsJsonObject();
         JsonElement var5xxxx = var4xxx.get("ops");
         if (var5xxxx.isJsonArray()) {
            for(JsonElement var7 : var5xxxx.getAsJsonArray()) {
               var1.ops.add(var7.getAsString());
            }
         }
      } catch (Exception var8) {
      }

      return var1;
   }
}
