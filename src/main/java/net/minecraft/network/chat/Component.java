package net.minecraft.network.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public interface Component extends Message, FormattedText {
   Style getStyle();

   String getContents();

   @Override
   default String getString() {
      return FormattedText.super.getString();
   }

   default String getString(int var1) {
      StringBuilder var2 = new StringBuilder();
      this.visit(var2x -> {
         int var3 = var1 - var2.length();
         if (var3 <= 0) {
            return STOP_ITERATION;
         } else {
            var2.append(var2x.length() <= var3 ? var2x : var2x.substring(0, var3));
            return Optional.empty();
         }
      });
      return var2.toString();
   }

   List<Component> getSiblings();

   MutableComponent plainCopy();

   MutableComponent copy();

   FormattedCharSequence getVisualOrderText();

   @Override
   default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> var1, Style var2) {
      Style var3 = this.getStyle().applyTo(var2);
      Optional<T> var4x = this.visitSelf(var1, var3);
      if (var4x.isPresent()) {
         return var4x;
      } else {
         for(Component var6 : this.getSiblings()) {
            Optional<T> var7 = var6.visit(var1, var3);
            if (var7.isPresent()) {
               return var7;
            }
         }

         return Optional.empty();
      }
   }

   @Override
   default <T> Optional<T> visit(FormattedText.ContentConsumer<T> var1) {
      Optional<T> var2 = this.visitSelf(var1);
      if (var2.isPresent()) {
         return var2;
      } else {
         for(Component var4 : this.getSiblings()) {
            Optional<T> var5 = var4.visit(var1);
            if (var5.isPresent()) {
               return var5;
            }
         }

         return Optional.empty();
      }
   }

   default <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> var1, Style var2) {
      return var1.accept(var2, this.getContents());
   }

   default <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> var1) {
      return var1.accept(this.getContents());
   }

   static Component nullToEmpty(@Nullable String var0) {
      return (Component)(var0 != null ? new TextComponent(var0) : TextComponent.EMPTY);
   }

   public static class Serializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
      private static final Gson GSON = Util.make(() -> {
         GsonBuilder var0 = new GsonBuilder();
         var0.disableHtmlEscaping();
         var0.registerTypeHierarchyAdapter(Component.class, new Component.Serializer());
         var0.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
         var0.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
         return var0.create();
      });
      private static final Field JSON_READER_POS = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field var0 = JsonReader.class.getDeclaredField("pos");
            var0.setAccessible(true);
            return var0;
         } catch (NoSuchFieldException var1) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", var1);
         }
      });
      private static final Field JSON_READER_LINESTART = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field var0 = JsonReader.class.getDeclaredField("lineStart");
            var0.setAccessible(true);
            return var0;
         } catch (NoSuchFieldException var1) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", var1);
         }
      });

      public MutableComponent deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         if (var1.isJsonPrimitive()) {
            return new TextComponent(var1.getAsString());
         } else if (!var1.isJsonObject()) {
            if (var1.isJsonArray()) {
               JsonArray var11 = var1.getAsJsonArray();
               MutableComponent var12x = null;

               for(JsonElement var19 : var11) {
                  MutableComponent var20xx = this.deserialize(var19, var19.getClass(), var3);
                  if (var12x == null) {
                     var12x = var20xx;
                  } else {
                     var12x.append(var20xx);
                  }
               }

               return var12x;
            } else {
               throw new JsonParseException("Don't know how to turn " + var1 + " into a Component");
            }
         } else {
            JsonObject var4x = var1.getAsJsonObject();
            MutableComponent var5;
            if (var4x.has("text")) {
               var5 = new TextComponent(GsonHelper.getAsString(var4x, "text"));
            } else if (var4x.has("translate")) {
               String var6 = GsonHelper.getAsString(var4x, "translate");
               if (var4x.has("with")) {
                  JsonArray var7x = GsonHelper.getAsJsonArray(var4x, "with");
                  Object[] var8xx = new Object[var7x.size()];

                  for(int var9xxx = 0; var9xxx < var8xx.length; ++var9xxx) {
                     var8xx[var9xxx] = this.deserialize(var7x.get(var9xxx), var2, var3);
                     if (var8xx[var9xxx] instanceof TextComponent) {
                        TextComponent var10xxxx = (TextComponent)var8xx[var9xxx];
                        if (var10xxxx.getStyle().isEmpty() && var10xxxx.getSiblings().isEmpty()) {
                           var8xx[var9xxx] = var10xxxx.getText();
                        }
                     }
                  }

                  var5 = new TranslatableComponent(var6, var8xx);
               } else {
                  var5 = new TranslatableComponent(var6);
               }
            } else if (var4x.has("score")) {
               JsonObject var13 = GsonHelper.getAsJsonObject(var4x, "score");
               if (!var13.has("name") || !var13.has("objective")) {
                  throw new JsonParseException("A score component needs a least a name and an objective");
               }

               var5 = new ScoreComponent(GsonHelper.getAsString(var13, "name"), GsonHelper.getAsString(var13, "objective"));
            } else if (var4x.has("selector")) {
               var5 = new SelectorComponent(GsonHelper.getAsString(var4x, "selector"));
            } else if (var4x.has("keybind")) {
               var5 = new KeybindComponent(GsonHelper.getAsString(var4x, "keybind"));
            } else {
               if (!var4x.has("nbt")) {
                  throw new JsonParseException("Don't know how to turn " + var1 + " into a Component");
               }

               String var14 = GsonHelper.getAsString(var4x, "nbt");
               boolean var17x = GsonHelper.getAsBoolean(var4x, "interpret", false);
               if (var4x.has("block")) {
                  var5 = new NbtComponent.BlockNbtComponent(var14, var17x, GsonHelper.getAsString(var4x, "block"));
               } else if (var4x.has("entity")) {
                  var5 = new NbtComponent.EntityNbtComponent(var14, var17x, GsonHelper.getAsString(var4x, "entity"));
               } else {
                  if (!var4x.has("storage")) {
                     throw new JsonParseException("Don't know how to turn " + var1 + " into a Component");
                  }

                  var5 = new NbtComponent.StorageNbtComponent(var14, var17x, new ResourceLocation(GsonHelper.getAsString(var4x, "storage")));
               }
            }

            if (var4x.has("extra")) {
               JsonArray var15 = GsonHelper.getAsJsonArray(var4x, "extra");
               if (var15.size() <= 0) {
                  throw new JsonParseException("Unexpected empty array of components");
               }

               for(int var18 = 0; var18 < var15.size(); ++var18) {
                  var5.append(this.deserialize(var15.get(var18), var2, var3));
               }
            }

            var5.setStyle((Style)var3.deserialize(var1, Style.class));
            return var5;
         }
      }

      private void serializeStyle(Style var1, JsonObject var2, JsonSerializationContext var3) {
         JsonElement var4 = var3.serialize(var1);
         if (var4.isJsonObject()) {
            JsonObject var5x = (JsonObject)var4;

            for(Entry<String, JsonElement> var7 : var5x.entrySet()) {
               var2.add(var7.getKey(), (JsonElement)var7.getValue());
            }
         }
      }

      public JsonElement serialize(Component var1, Type var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();
         if (!var1.getStyle().isEmpty()) {
            this.serializeStyle(var1.getStyle(), var4, var3);
         }

         if (!var1.getSiblings().isEmpty()) {
            JsonArray var5 = new JsonArray();

            for(Component var7 : var1.getSiblings()) {
               var5.add(this.serialize(var7, var7.getClass(), var3));
            }

            var4.add("extra", var5);
         }

         if (var1 instanceof TextComponent) {
            var4.addProperty("text", ((TextComponent)var1).getText());
         } else if (var1 instanceof TranslatableComponent) {
            TranslatableComponent var11 = (TranslatableComponent)var1;
            var4.addProperty("translate", var11.getKey());
            if (var11.getArgs() != null && var11.getArgs().length > 0) {
               JsonArray var16x = new JsonArray();

               for(Object var10 : var11.getArgs()) {
                  if (var10 instanceof Component) {
                     var16x.add(this.serialize((Component)var10, var10.getClass(), var3));
                  } else {
                     var16x.add(new JsonPrimitive(String.valueOf(var10)));
                  }
               }

               var4.add("with", var16x);
            }
         } else if (var1 instanceof ScoreComponent) {
            ScoreComponent var12 = (ScoreComponent)var1;
            JsonObject var17x = new JsonObject();
            var17x.addProperty("name", var12.getName());
            var17x.addProperty("objective", var12.getObjective());
            var4.add("score", var17x);
         } else if (var1 instanceof SelectorComponent) {
            SelectorComponent var13 = (SelectorComponent)var1;
            var4.addProperty("selector", var13.getPattern());
         } else if (var1 instanceof KeybindComponent) {
            KeybindComponent var14 = (KeybindComponent)var1;
            var4.addProperty("keybind", var14.getName());
         } else {
            if (!(var1 instanceof NbtComponent)) {
               throw new IllegalArgumentException("Don't know how to serialize " + var1 + " as a Component");
            }

            NbtComponent var15 = (NbtComponent)var1;
            var4.addProperty("nbt", var15.getNbtPath());
            var4.addProperty("interpret", var15.isInterpreting());
            if (var1 instanceof NbtComponent.BlockNbtComponent) {
               NbtComponent.BlockNbtComponent var18x = (NbtComponent.BlockNbtComponent)var1;
               var4.addProperty("block", var18x.getPos());
            } else if (var1 instanceof NbtComponent.EntityNbtComponent) {
               NbtComponent.EntityNbtComponent var19 = (NbtComponent.EntityNbtComponent)var1;
               var4.addProperty("entity", var19.getSelector());
            } else {
               if (!(var1 instanceof NbtComponent.StorageNbtComponent)) {
                  throw new IllegalArgumentException("Don't know how to serialize " + var1 + " as a Component");
               }

               NbtComponent.StorageNbtComponent var20 = (NbtComponent.StorageNbtComponent)var1;

               var4.addProperty("storage", var20.getId().toString());
            }
         }

         return var4;
      }

      public static String toJson(Component var0) {
         return GSON.toJson(var0);
      }

      public static JsonElement toJsonTree(Component var0) {
         return GSON.toJsonTree(var0);
      }

      @Nullable
      public static MutableComponent fromJson(String var0) {
         return GsonHelper.fromJson(GSON, var0, MutableComponent.class, false);
      }

      @Nullable
      public static MutableComponent fromJson(JsonElement var0) {
         return (MutableComponent)GSON.fromJson(var0, MutableComponent.class);
      }

      @Nullable
      public static MutableComponent fromJsonLenient(String var0) {
         return GsonHelper.fromJson(GSON, var0, MutableComponent.class, true);
      }

      public static MutableComponent fromJson(com.mojang.brigadier.StringReader var0) {
         try {
            JsonReader var1 = new JsonReader(new StringReader(var0.getRemaining()));
            var1.setLenient(false);
            MutableComponent var2x = (MutableComponent)GSON.getAdapter(MutableComponent.class).read(var1);
            var0.setCursor(var0.getCursor() + getPos(var1));
            return var2x;
         } catch (StackOverflowError | IOException var3) {
            throw new JsonParseException(var3);
         }
      }

      private static int getPos(JsonReader var0) {
         try {
            return JSON_READER_POS.getInt(var0) - JSON_READER_LINESTART.getInt(var0) + 1;
         } catch (IllegalAccessException var2) {
            throw new IllegalStateException("Couldn't read position of JsonReader", var2);
         }
      }
   }
}
