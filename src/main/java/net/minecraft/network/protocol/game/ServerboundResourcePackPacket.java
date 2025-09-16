//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundResourcePackPacket implements Packet<ServerGamePacketListener> {
   private Action action;

   public ServerboundResourcePackPacket() {
   }

   public ServerboundResourcePackPacket(Action var1) {
      this.action = var1;
   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.action = (Action)var1.readEnum(Action.class);
   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeEnum(this.action);
   }

   public void handle(ServerGamePacketListener var1) {
      var1.handleResourcePackResponse(this);
   }

   public static enum Action {
      SUCCESSFULLY_LOADED,
      DECLINED,
      FAILED_DOWNLOAD,
      ACCEPTED;

      private Action() {
      }
   }
}
