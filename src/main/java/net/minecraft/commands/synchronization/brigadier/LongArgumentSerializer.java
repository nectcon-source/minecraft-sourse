//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentSerializer implements ArgumentSerializer<LongArgumentType> {
   public LongArgumentSerializer() {
   }

   public void serializeToNetwork(LongArgumentType var1, FriendlyByteBuf var2) {
      boolean var3 = var1.getMinimum() != Long.MIN_VALUE;
      boolean var4 = var1.getMaximum() != Long.MAX_VALUE;
      var2.writeByte(BrigadierArgumentSerializers.createNumberFlags(var3, var4));
      if (var3) {
         var2.writeLong(var1.getMinimum());
      }

      if (var4) {
         var2.writeLong(var1.getMaximum());
      }

   }

   public LongArgumentType deserializeFromNetwork(FriendlyByteBuf var1) {
      byte var2 = var1.readByte();
      long var3 = BrigadierArgumentSerializers.numberHasMin(var2) ? var1.readLong() : Long.MIN_VALUE;
      long var5 = BrigadierArgumentSerializers.numberHasMax(var2) ? var1.readLong() : Long.MAX_VALUE;
      return LongArgumentType.longArg(var3, var5);
   }

   public void serializeToJson(LongArgumentType var1, JsonObject var2) {
      if (var1.getMinimum() != Long.MIN_VALUE) {
         var2.addProperty("min", var1.getMinimum());
      }

      if (var1.getMaximum() != Long.MAX_VALUE) {
         var2.addProperty("max", var1.getMaximum());
      }

   }
}
