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

public class RealmsNews extends ValueObject {
   private static final Logger LOGGER = LogManager.getLogger();
   public String newsLink;

   public RealmsNews() {
   }

   public static RealmsNews parse(String var0) {
      RealmsNews var1 = new RealmsNews();

      try {
         JsonParser var2 = new JsonParser();
         JsonObject var3 = var2.parse(var0).getAsJsonObject();
         var1.newsLink = JsonUtils.getStringOr("newsLink", var3, null);
      } catch (Exception var2_1) {
         LOGGER.error("Could not parse RealmsNews: " + var2_1.getMessage());
      }

      return var1;
   }
}
