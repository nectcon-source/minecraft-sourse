package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringDecomposer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Language {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new Gson();
   private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
   private static volatile Language instance = loadDefault();

   private static Language loadDefault() {
      Builder<String, String> var0 = ImmutableMap.builder();
      BiConsumer<String, String> var1x = var0::put;

      try (InputStream var2xx = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json")) {
         loadFromJson(var2xx, var1x);
      } catch (JsonParseException | IOException var15) {
         LOGGER.error("Couldn't read strings from /assets/minecraft/lang/en_us.json", var15);
      }

      final Map<String, String> var16xx = var0.build();
      return new Language() {
         @Override
         public String getOrDefault(String var1) {
            return var16xx.getOrDefault(var1, var1);
         }

         @Override
         public boolean has(String var1) {
            return var16xx.containsKey(var1);
         }

         @Override
         public boolean isDefaultRightToLeft() {
            return false;
         }

         @Override
         public FormattedCharSequence getVisualOrder(FormattedText var1) {
            return var1x -> var1.visit(
                     (var1xx, var2) -> StringDecomposer.iterateFormatted(var2, var1xx, var1x) ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY
                  )
                  .isPresent();
         }
      };
   }

   public static void loadFromJson(InputStream var0, BiConsumer<String, String> var1) {
      JsonObject var2 = (JsonObject)GSON.fromJson(new InputStreamReader(var0, StandardCharsets.UTF_8), JsonObject.class);

      for(Entry<String, JsonElement> var4 : var2.entrySet()) {
         String var5x = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString((JsonElement)var4.getValue(), var4.getKey())).replaceAll("%$1s");
         var1.accept(var4.getKey(), var5x);
      }
   }

   public static Language getInstance() {
      return instance;
   }

   public static void inject(Language var0) {
      instance = var0;
   }

   public abstract String getOrDefault(String var1);

   public abstract boolean has(String var1);

   public abstract boolean isDefaultRightToLeft();

   public abstract FormattedCharSequence getVisualOrder(FormattedText var1);

   public List<FormattedCharSequence> getVisualOrder(List<FormattedText> var1) {
      return var1.stream().map(getInstance()::getVisualOrder).collect(ImmutableList.toImmutableList());
   }
}
