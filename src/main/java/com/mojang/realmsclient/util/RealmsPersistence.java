//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.realmsclient.util;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;

public class RealmsPersistence {
   private static final GuardedSerializer GSON = new GuardedSerializer();

   public static RealmsPersistenceData readFile() {
      File var0 = getPathToData();

      try {
         return (RealmsPersistenceData)GSON.fromJson(FileUtils.readFileToString(var0, StandardCharsets.UTF_8), RealmsPersistenceData.class);
      } catch (IOException var2) {
         return new RealmsPersistenceData();
      }
   }

   public static void writeFile(RealmsPersistenceData var0) {
      File var1 = getPathToData();

      try {
         FileUtils.writeStringToFile(var1, GSON.toJson(var0), StandardCharsets.UTF_8);
      } catch (IOException var3) {
      }

   }

   private static File getPathToData() {
      return new File(Minecraft.getInstance().gameDirectory, "realms_persistence.json");
   }

   public static class RealmsPersistenceData implements ReflectionBasedSerialization {
      @SerializedName("newsLink")
      public String newsLink;
      @SerializedName("hasUnreadNews")
      public boolean hasUnreadNews;

      public RealmsPersistenceData() {
      }
   }
}
