//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundClientCommandPacket implements Packet<ServerGamePacketListener> {
   private Action action;

   public ServerboundClientCommandPacket() {
   }

   public ServerboundClientCommandPacket(Action var1) {
      this.action = var1;
   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.action = (Action)var1.readEnum(Action.class);
   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeEnum(this.action);
   }

   public void handle(ServerGamePacketListener var1) {
      var1.handleClientCommand(this);
   }

   public Action getAction() {
      return this.action;
   }

   public static enum Action {
      PERFORM_RESPAWN,
      REQUEST_STATS;

      private Action() {
      }
   }
}
