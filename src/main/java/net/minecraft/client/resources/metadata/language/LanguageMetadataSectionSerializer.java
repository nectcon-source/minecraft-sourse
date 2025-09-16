//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.resources.metadata.language;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

public class LanguageMetadataSectionSerializer implements MetadataSectionSerializer<LanguageMetadataSection> {
   public LanguageMetadataSectionSerializer() {
   }

   public LanguageMetadataSection fromJson(JsonObject var1) {
      Set<LanguageInfo> var2 = Sets.newHashSet();

      for(Map.Entry<String, JsonElement> var4 : var1.entrySet()) {
         String var5 = (String)var4.getKey();
         if (var5.length() > 16) {
            throw new JsonParseException("Invalid language->'" + var5 + "': language code must not be more than " + 16 + " characters long");
         }

         JsonObject var6 = GsonHelper.convertToJsonObject((JsonElement)var4.getValue(), "language");
         String var7 = GsonHelper.getAsString(var6, "region");
         String var8 = GsonHelper.getAsString(var6, "name");
         boolean var9 = GsonHelper.getAsBoolean(var6, "bidirectional", false);
         if (var7.isEmpty()) {
            throw new JsonParseException("Invalid language->'" + var5 + "'->region: empty value");
         }

         if (var8.isEmpty()) {
            throw new JsonParseException("Invalid language->'" + var5 + "'->name: empty value");
         }

         if (!var2.add(new LanguageInfo(var5, var7, var8, var9))) {
            throw new JsonParseException("Duplicate language->'" + var5 + "' defined");
         }
      }

      return new LanguageMetadataSection(var2);
   }

   public String getMetadataSectionName() {
      return "language";
   }
}
