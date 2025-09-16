//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsServerAddress extends ValueObject {
   private static final Logger LOGGER = LogManager.getLogger();
   public String address;
   public String resourcePackUrl;
   public String resourcePackHash;

   public RealmsServerAddress() {
   }

   public static RealmsServerAddress parse(String var0) {
      JsonParser var1 = new JsonParser();
      RealmsServerAddress var2 = new RealmsServerAddress();

      try {
         JsonObject var3 = var1.parse(var0).getAsJsonObject();
         var2.address = JsonUtils.getStringOr("address", var3, null);
         var2.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", var3, null);
         var2.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", var3, null);
      } catch (Exception var3_1) {
         LOGGER.error("Could not parse RealmsServerAddress: " + var3_1.getMessage());
      }

      return var2;
   }
}
