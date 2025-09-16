//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentSerializer implements ArgumentSerializer<DoubleArgumentType> {
   public DoubleArgumentSerializer() {
   }

   public void serializeToNetwork(DoubleArgumentType var1, FriendlyByteBuf var2) {
      boolean var3 = var1.getMinimum() != -Double.MAX_VALUE;
      boolean var4 = var1.getMaximum() != Double.MAX_VALUE;
      var2.writeByte(BrigadierArgumentSerializers.createNumberFlags(var3, var4));
      if (var3) {
         var2.writeDouble(var1.getMinimum());
      }

      if (var4) {
         var2.writeDouble(var1.getMaximum());
      }

   }

   public DoubleArgumentType deserializeFromNetwork(FriendlyByteBuf var1) {
      byte var2 = var1.readByte();
      double var3 = BrigadierArgumentSerializers.numberHasMin(var2) ? var1.readDouble() : -Double.MAX_VALUE;
      double var5 = BrigadierArgumentSerializers.numberHasMax(var2) ? var1.readDouble() : Double.MAX_VALUE;
      return DoubleArgumentType.doubleArg(var3, var5);
   }

   public void serializeToJson(DoubleArgumentType var1, JsonObject var2) {
      if (var1.getMinimum() != -Double.MAX_VALUE) {
         var2.addProperty("min", var1.getMinimum());
      }

      if (var1.getMaximum() != Double.MAX_VALUE) {
         var2.addProperty("max", var1.getMaximum());
      }

   }
}
