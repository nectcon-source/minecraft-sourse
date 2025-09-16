//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundRemoveEntitiesPacket implements Packet<ClientGamePacketListener> {
   private int[] entityIds;

   public ClientboundRemoveEntitiesPacket() {
   }

   public ClientboundRemoveEntitiesPacket(int... var1) {
      this.entityIds = var1;
   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.entityIds = new int[var1.readVarInt()];

      for(int var2 = 0; var2 < this.entityIds.length; ++var2) {
         this.entityIds[var2] = var1.readVarInt();
      }

   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeVarInt(this.entityIds.length);

      for(int var5 : this.entityIds) {
         var1.writeVarInt(var5);
      }

   }

   public void handle(ClientGamePacketListener var1) {
      var1.handleRemoveEntity(this);
   }

   public int[] getEntityIds() {
      return this.entityIds;
   }
}
