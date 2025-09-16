package net.minecraft.network.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Style {
   public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null);
   public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("minecraft", "default");
   @Nullable
   private final TextColor color;
   @Nullable
   private final Boolean bold;
   @Nullable
   private final Boolean italic;
   @Nullable
   private final Boolean underlined;
   @Nullable
   private final Boolean strikethrough;
   @Nullable
   private final Boolean obfuscated;
   @Nullable
   private final ClickEvent clickEvent;
   @Nullable
   private final HoverEvent hoverEvent;
   @Nullable
   private final String insertion;
   @Nullable
   private final ResourceLocation font;

   private Style(
      @Nullable TextColor var1,
      @Nullable Boolean var2,
      @Nullable Boolean var3,
      @Nullable Boolean var4,
      @Nullable Boolean var5,
      @Nullable Boolean var6,
      @Nullable ClickEvent var7,
      @Nullable HoverEvent var8,
      @Nullable String var9,
      @Nullable ResourceLocation var10
   ) {
      this.color = var1;
      this.bold = var2;
      this.italic = var3;
      this.underlined = var4;
      this.strikethrough = var5;
      this.obfuscated = var6;
      this.clickEvent = var7;
      this.hoverEvent = var8;
      this.insertion = var9;
      this.font = var10;
   }

   @Nullable
   public TextColor getColor() {
      return this.color;
   }

   public boolean isBold() {
      return this.bold == Boolean.TRUE;
   }

   public boolean isItalic() {
      return this.italic == Boolean.TRUE;
   }

   public boolean isStrikethrough() {
      return this.strikethrough == Boolean.TRUE;
   }

   public boolean isUnderlined() {
      return this.underlined == Boolean.TRUE;
   }

   public boolean isObfuscated() {
      return this.obfuscated == Boolean.TRUE;
   }

   public boolean isEmpty() {
      return this == EMPTY;
   }

   @Nullable
   public ClickEvent getClickEvent() {
      return this.clickEvent;
   }

   @Nullable
   public HoverEvent getHoverEvent() {
      return this.hoverEvent;
   }

   @Nullable
   public String getInsertion() {
      return this.insertion;
   }

   public ResourceLocation getFont() {
      return this.font != null ? this.font : DEFAULT_FONT;
   }

   public Style withColor(@Nullable TextColor var1) {
      return new Style(
              var1, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
      );
   }

   public Style withColor(@Nullable ChatFormatting var1) {
      return this.withColor(var1 != null ? TextColor.fromLegacyFormat(var1) : null);
   }

   public Style withBold(@Nullable Boolean var1) {
      return new Style(
         this.color, var1, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
      );
   }

   public Style withItalic(@Nullable Boolean var1) {
      return new Style(
         this.color, this.bold, var1, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
      );
   }

   public Style withUnderlined(@Nullable Boolean var1) {
      return new Style(this.color, this.bold, this.italic, var1, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withClickEvent(@Nullable ClickEvent var1) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, var1, this.hoverEvent, this.insertion, this.font);
   }

   public Style withHoverEvent(@Nullable HoverEvent var1) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, var1, this.insertion, this.font);
   }

   public Style withInsertion(@Nullable String var1) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, var1, this.font);
   }

   public Style withFont(@Nullable ResourceLocation var1) {
      return new Style(
         this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, var1
      );
   }

   public Style applyFormat(ChatFormatting var1) {
      TextColor var2 = this.color;
      Boolean var3 = this.bold;
      Boolean var4 = this.italic;
      Boolean var5 = this.strikethrough;
      Boolean var6 = this.underlined;
      Boolean var7 = this.obfuscated;
      switch (var1) {
         case OBFUSCATED:
            var7 = true;
            break;
         case BOLD:
            var3 = true;
            break;
         case STRIKETHROUGH:
            var5 = true;
            break;
         case UNDERLINE:
            var6 = true;
            break;
         case ITALIC:
            var4 = true;
            break;
         case RESET:
            return EMPTY;
         default:
            var2 = TextColor.fromLegacyFormat(var1);
      }

      return new Style(var2, var3, var4, var6, var5, var7, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style applyLegacyFormat(ChatFormatting var1) {
      TextColor var2 = this.color;
      Boolean var3 = this.bold;
      Boolean var4 = this.italic;
      Boolean var5 = this.strikethrough;
      Boolean var6 = this.underlined;
      Boolean var7 = this.obfuscated;
      switch (var1) {
         case OBFUSCATED:
            var7 = true;
            break;
         case BOLD:
            var3 = true;
            break;
         case STRIKETHROUGH:
            var5 = true;
            break;
         case UNDERLINE:
            var6 = true;
            break;
         case ITALIC:
            var4 = true;
            break;
         case RESET:
            return EMPTY;
         default:
            var7 = false;
            var3 = false;
            var5 = false;
            var6 = false;
            var4 = false;
            var2 = TextColor.fromLegacyFormat(var1);
      }

      return new Style(var2, var3, var4, var6, var5, var7, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style applyFormats(ChatFormatting... var1) {
      TextColor var2 = this.color;
      Boolean var3 = this.bold;
      Boolean var4 = this.italic;
      Boolean var5 = this.strikethrough;
      Boolean var6 = this.underlined;
      Boolean var7 = this.obfuscated;

      for(ChatFormatting var11 : var1) {
         switch (var11) {
            case OBFUSCATED:
               var7 = true;
               break;
            case BOLD:
               var3 = true;
               break;
            case STRIKETHROUGH:
               var5 = true;
               break;
            case UNDERLINE:
               var6 = true;
               break;
            case ITALIC:
               var4 = true;
               break;
            case RESET:
               return EMPTY;
            default:
               var2 = TextColor.fromLegacyFormat(var11);
         }
      }

      return new Style(var2, var3, var4, var6, var5, var7, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style applyTo(Style var1) {
      if (this == EMPTY) {
         return var1;
      } else {
         return var1 == EMPTY
            ? this
            : new Style(
               this.color != null ? this.color : var1.color,
               this.bold != null ? this.bold : var1.bold,
               this.italic != null ? this.italic : var1.italic,
               this.underlined != null ? this.underlined :var1.underlined,
               this.strikethrough != null ? this.strikethrough : var1.strikethrough,
               this.obfuscated != null ? this.obfuscated : var1.obfuscated,
               this.clickEvent != null ? this.clickEvent : var1.clickEvent,
               this.hoverEvent != null ? this.hoverEvent : var1.hoverEvent,
               this.insertion != null ? this.insertion : var1.insertion,
               this.font != null ? this.font : var1.font
            );
      }
   }

   @Override
   public String toString() {
      return "Style{ color="
         + this.color
         + ", bold="
         + this.bold
         + ", italic="
         + this.italic
         + ", underlined="
         + this.underlined
         + ", strikethrough="
         + this.strikethrough
         + ", obfuscated="
         + this.obfuscated
         + ", clickEvent="
         + this.getClickEvent()
         + ", hoverEvent="
         + this.getHoverEvent()
         + ", insertion="
         + this.getInsertion()
         + ", font="
         + this.getFont()
         + '}';
   }

   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof Style)) {
         return false;
      } else {
         Style var2 = (Style)var1;
         return this.isBold() == var2.isBold()
            && Objects.equals(this.getColor(), var2.getColor())
            && this.isItalic() == var2.isItalic()
            && this.isObfuscated() == var2.isObfuscated()
            && this.isStrikethrough() == var2.isStrikethrough()
            && this.isUnderlined() == var2.isUnderlined()
            && Objects.equals(this.getClickEvent(), var2.getClickEvent())
            && Objects.equals(this.getHoverEvent(), var2.getHoverEvent())
            && Objects.equals(this.getInsertion(), var2.getInsertion())
            && Objects.equals(this.getFont(), var2.getFont());
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion
      );
   }

   public static class Serializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
      @Nullable
      public Style deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         if (var1.isJsonObject()) {
            JsonObject var4 = var1.getAsJsonObject();
            if (var4 == null) {
               return null;
            } else {
               Boolean var5 = getOptionalFlag(var4, "bold");
               Boolean var6x = getOptionalFlag(var4, "italic");
               Boolean var7xx = getOptionalFlag(var4, "underlined");
               Boolean var8xxx = getOptionalFlag(var4, "strikethrough");
               Boolean var9xxxx = getOptionalFlag(var4, "obfuscated");
               TextColor var10xxxxx = getTextColor(var4);
               String var11xxxxxx = getInsertion(var4);
               ClickEvent var12xxxxxxx = getClickEvent(var4);
               HoverEvent var13xxxxxxxx = getHoverEvent(var4);
               ResourceLocation var14xxxxxxxxx = getFont(var4);
               return new Style(var10xxxxx, var5, var6x, var7xx, var8xxx, var9xxxx, var12xxxxxxx, var13xxxxxxxx, var11xxxxxx, var14xxxxxxxxx);
            }
         } else {
            return null;
         }
      }

      @Nullable
      private static ResourceLocation getFont(JsonObject var0) {
         if (var0.has("font")) {
            String var1 = GsonHelper.getAsString(var0, "font");

            try {
               return new ResourceLocation(var1);
            } catch (ResourceLocationException var3) {
               throw new JsonSyntaxException("Invalid font name: " + var1);
            }
         } else {
            return null;
         }
      }

      @Nullable
      private static HoverEvent getHoverEvent(JsonObject var0) {
         if (var0.has("hoverEvent")) {
            JsonObject var1 = GsonHelper.getAsJsonObject(var0, "hoverEvent");
            HoverEvent var2x = HoverEvent.deserialize(var1);
            if (var2x != null && var2x.getAction().isAllowedFromServer()) {
               return var2x;
            }
         }

         return null;
      }

      @Nullable
      private static ClickEvent getClickEvent(JsonObject var0) {
         if (var0.has("clickEvent")) {
            JsonObject var1 = GsonHelper.getAsJsonObject(var0, "clickEvent");
            String var2x = GsonHelper.getAsString(var1, "action", null);
            ClickEvent.Action var3xx = var2x == null ? null : ClickEvent.Action.getByName(var2x);
            String var4xxx = GsonHelper.getAsString(var1, "value", null);
            if (var3xx != null && var4xxx != null && var3xx.isAllowedFromServer()) {
               return new ClickEvent(var3xx, var4xxx);
            }
         }

         return null;
      }

      @Nullable
      private static String getInsertion(JsonObject var0) {
         return GsonHelper.getAsString(var0, "insertion", null);
      }

      @Nullable
      private static TextColor getTextColor(JsonObject var0) {
         if (var0.has("color")) {
            String var1 = GsonHelper.getAsString(var0, "color");
            return TextColor.parseColor(var1);
         } else {
            return null;
         }
      }

      @Nullable
      private static Boolean getOptionalFlag(JsonObject var0, String var1) {
         return var0.has(var1) ? var0.get(var1).getAsBoolean() : null;
      }

      @Nullable
      public JsonElement serialize(Style var1, Type var2, JsonSerializationContext var3) {
         if (var1.isEmpty()) {
            return null;
         } else {
            JsonObject var4 = new JsonObject();
            if (var1.bold != null) {
               var4.addProperty("bold", var1.bold);
            }

            if (var1.italic != null) {
               var4.addProperty("italic", var1.italic);
            }

            if (var1.underlined != null) {
               var4.addProperty("underlined", var1.underlined);
            }

            if (var1.strikethrough != null) {
               var4.addProperty("strikethrough", var1.strikethrough);
            }

            if (var1.obfuscated != null) {
               var4.addProperty("obfuscated", var1.obfuscated);
            }

            if (var1.color != null) {
               var4.addProperty("color", var1.color.serialize());
            }

            if (var1.insertion != null) {
               var4.add("insertion", var3.serialize(var1.insertion));
            }

            if (var1.clickEvent != null) {
               JsonObject var5 = new JsonObject();
               var5.addProperty("action", var1.clickEvent.getAction().getName());
               var5.addProperty("value", var1.clickEvent.getValue());
               var4.add("clickEvent", var5);
            }

            if (var1.hoverEvent != null) {
               var4.add("hoverEvent", var1.hoverEvent.serialize());
            }

            if (var1.font != null) {
               var4.addProperty("font", var1.font.toString());
            }

            return var4;
         }
      }
   }
}
