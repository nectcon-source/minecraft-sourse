//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.bridge.game.GameVersion;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DetectedVersion implements GameVersion {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final GameVersion BUILT_IN = new DetectedVersion();
   private final String id;
   private final String name;
   private final boolean stable;
   private final int worldVersion;
   private final int protocolVersion;
   private final int packVersion;
   private final Date buildTime;
   private final String releaseTarget;

   private DetectedVersion() {
      this.id = UUID.randomUUID().toString().replaceAll("-", "");
      this.name = "1.16.5";
      this.stable = true;
      this.worldVersion = 2586;
      this.protocolVersion = SharedConstants.getProtocolVersion();
      this.packVersion = 6;
      this.buildTime = new Date();
      this.releaseTarget = "1.16.5";
   }

   private DetectedVersion(JsonObject var1) {
      this.id = GsonHelper.getAsString(var1, "id");
      this.name = GsonHelper.getAsString(var1, "name");
      this.releaseTarget = GsonHelper.getAsString(var1, "release_target");
      this.stable = GsonHelper.getAsBoolean(var1, "stable");
      this.worldVersion = GsonHelper.getAsInt(var1, "world_version");
      this.protocolVersion = GsonHelper.getAsInt(var1, "protocol_version");
      this.packVersion = GsonHelper.getAsInt(var1, "pack_version");
      this.buildTime = Date.from(ZonedDateTime.parse(GsonHelper.getAsString(var1, "build_time")).toInstant());
   }

   public static GameVersion tryDetectVersion() {
      try (InputStream var0 = DetectedVersion.class.getResourceAsStream("/version.json")) {
         if (var0 == null) {
            LOGGER.warn("Missing version information!");
            GameVersion var35 = BUILT_IN;
            return var35;
         } else {
            InputStreamReader var2 = new InputStreamReader(var0);
            Throwable var3 = null;

            Object var4;
            try {
               var4 = new DetectedVersion(GsonHelper.parse(var2));
            } catch (Throwable var30) {
               var4 = var30;
               var3 = var30;
               throw var30;
            } finally {
               if (var2 != null) {
                  if (var3 != null) {
                     try {
                        var2.close();
                     } catch (Throwable var29) {
                        var3.addSuppressed(var29);
                     }
                  } else {
                     var2.close();
                  }
               }

            }

            return (GameVersion)var4;
         }
      } catch (JsonParseException | IOException var0_1) {
         throw new IllegalStateException("Game version information is corrupt", var0_1);
      }
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getReleaseTarget() {
      return this.releaseTarget;
   }

   public int getWorldVersion() {
      return this.worldVersion;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public int getPackVersion() {
      return this.packVersion;
   }

   public Date getBuildTime() {
      return this.buildTime;
   }

   public boolean isStable() {
      return this.stable;
   }
}
