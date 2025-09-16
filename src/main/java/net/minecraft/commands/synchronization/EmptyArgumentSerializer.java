package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;

public class EmptyArgumentSerializer<T extends ArgumentType<?>> implements ArgumentSerializer<T> {
   private final Supplier<T> constructor;

   public EmptyArgumentSerializer(Supplier<T> var1) {
      this.constructor = var1;
   }

   @Override
   public void serializeToNetwork(T var1, FriendlyByteBuf var2) {
   }

   @Override
   public T deserializeFromNetwork(FriendlyByteBuf var1) {
      return (T)this.constructor.get();
   }

   @Override
   public void serializeToJson(T var1, JsonObject var2) {
   }
}
