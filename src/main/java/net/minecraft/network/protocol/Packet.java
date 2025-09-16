package net.minecraft.network.protocol;

import java.io.IOException;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public interface Packet<T extends PacketListener> {
   void read(FriendlyByteBuf var1) throws IOException;

   void write(FriendlyByteBuf var1) throws IOException;

   void handle(T var1) throws CommandSyntaxException;

   default boolean isSkippable() {
      return false;
   }
}
