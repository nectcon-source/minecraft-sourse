//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer implements ArgumentSerializer<StringArgumentType> {
   public StringArgumentSerializer() {
   }

   public void serializeToNetwork(StringArgumentType var1, FriendlyByteBuf var2) {
      var2.writeEnum(var1.getType());
   }

   public StringArgumentType deserializeFromNetwork(FriendlyByteBuf var1) {
      StringArgumentType.StringType var2 = (StringArgumentType.StringType)var1.readEnum(StringArgumentType.StringType.class);
      switch (var2) {
         case SINGLE_WORD:
            return StringArgumentType.word();
         case QUOTABLE_PHRASE:
            return StringArgumentType.string();
         case GREEDY_PHRASE:
         default:
            return StringArgumentType.greedyString();
      }
   }

   public void serializeToJson(StringArgumentType var1, JsonObject var2) {
      switch (var1.getType()) {
         case SINGLE_WORD:
            var2.addProperty("type", "word");
            break;
         case QUOTABLE_PHRASE:
            var2.addProperty("type", "phrase");
            break;
         case GREEDY_PHRASE:
         default:
            var2.addProperty("type", "greedy");
      }

   }
}
