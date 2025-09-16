package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BackupList extends ValueObject {
   private static final Logger LOGGER = LogManager.getLogger();
   public List<Backup> backups;

   public static BackupList parse(String var0) {
      JsonParser var1 = new JsonParser();
      BackupList var2x = new BackupList();
      var2x.backups = Lists.newArrayList();

      try {
         JsonElement var3xx = var1.parse(var0).getAsJsonObject().get("backups");
         if (var3xx.isJsonArray()) {
            Iterator<JsonElement> var4xxx = var3xx.getAsJsonArray().iterator();

            while(var4xxx.hasNext()) {
               var2x.backups.add(Backup.parse((JsonElement)var4xxx.next()));
            }
         }
      } catch (Exception var5) {
         LOGGER.error("Could not parse BackupList: " + var5.getMessage());
      }

      return var2x;
   }
}
