package net.minecraft.util.datafix.fixes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.lang.reflect.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class BlockEntitySignTextStrictJsonFix extends NamedEntityFix {
   public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Component.class, new JsonDeserializer<Component>() {

      public MutableComponent deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         if (var1.isJsonPrimitive()) {
            return new TextComponent(var1.getAsString());
         } else if (var1.isJsonArray()) {
            JsonArray var4 = var1.getAsJsonArray();
            MutableComponent var5x = null;

            for(JsonElement var7 : var4) {
               MutableComponent var8xx = this.deserialize(var7, var7.getClass(), var3);
               if (var5x == null) {
                  var5x = var8xx;
               } else {
                  var5x.append(var8xx);
               }
            }

            return var5x;
         } else {
            throw new JsonParseException("Don't know how to turn " + var1 + " into a Component");
         }
      }
   }).create();

   public BlockEntitySignTextStrictJsonFix(Schema var1, boolean var2) {
      super(var1, var2, "BlockEntitySignTextStrictJsonFix", References.BLOCK_ENTITY, "Sign");
   }

   private Dynamic<?> updateLine(Dynamic<?> var1, String var2) {
      String var3 = var1.get(var2).asString("");
      Component var4x = null;
      if (!"null".equals(var3) && !StringUtils.isEmpty(var3)) {
         if (var3.charAt(0) == '"' && var3.charAt(var3.length() - 1) == '"' || var3.charAt(0) == '{' && var3.charAt(var3.length() - 1) == '}') {
            try {
               var4x = GsonHelper.fromJson(GSON, var3, Component.class, true);
               if (var4x == null) {
                  var4x = TextComponent.EMPTY;
               }
            } catch (JsonParseException var8) {
            }

            if (var4x == null) {
               try {
                  var4x = Component.Serializer.fromJson(var3);
               } catch (JsonParseException var7) {
               }
            }

            if (var4x == null) {
               try {
                  var4x = Component.Serializer.fromJsonLenient(var3);
               } catch (JsonParseException var6) {
               }
            }

            if (var4x == null) {
               var4x = new TextComponent(var3);
            }
         } else {
            var4x = new TextComponent(var3);
         }
      } else {
         var4x = TextComponent.EMPTY;
      }

      return var1.set(var2, var1.createString(Component.Serializer.toJson(var4x)));
   }

   @Override
   protected Typed<?> fix(Typed<?> var1) {
      return var1.update(DSL.remainderFinder(), var1x -> {
         var1x = this.updateLine(var1x, "Text1");
         var1x = this.updateLine(var1x, "Text2");
         var1x = this.updateLine(var1x, "Text3");
         return this.updateLine(var1x, "Text4");
      });
   }
}
