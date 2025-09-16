package net.minecraft.network.protocol.status;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPongResponsePacket implements Packet<ClientStatusPacketListener> {
   private long time;

   public ClientboundPongResponsePacket() {
   }

   public ClientboundPongResponsePacket(long var1) {
      this.time = var1;
   }

   @Override
   public void read(FriendlyByteBuf var1) throws IOException {
      this.time = var1.readLong();
   }

   @Override
   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeLong(this.time);
   }

   public void handle(ClientStatusPacketListener var1) {
      var1.handlePongResponse(this);
   }
}
