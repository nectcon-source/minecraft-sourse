//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.network.protocol.game;

import java.io.IOException;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPlayerActionPacket implements Packet<ServerGamePacketListener> {
   private BlockPos pos;
   private Direction direction;
   private Action action;

   public ServerboundPlayerActionPacket() {
   }

   public ServerboundPlayerActionPacket(Action var1, BlockPos var2, Direction var3) {
      this.action = var1;
      this.pos = var2.immutable();
      this.direction = var3;
   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.action = (Action)var1.readEnum(Action.class);
      this.pos = var1.readBlockPos();
      this.direction = Direction.from3DDataValue(var1.readUnsignedByte());
   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeEnum(this.action);
      var1.writeBlockPos(this.pos);
      var1.writeByte(this.direction.get3DDataValue());
   }

   public void handle(ServerGamePacketListener var1) throws CommandSyntaxException {
      var1.handlePlayerAction(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public Action getAction() {
      return this.action;
   }

   public static enum Action {
      START_DESTROY_BLOCK,
      ABORT_DESTROY_BLOCK,
      STOP_DESTROY_BLOCK,
      DROP_ALL_ITEMS,
      DROP_ITEM,
      RELEASE_USE_ITEM,
      SWAP_ITEM_WITH_OFFHAND;

      private Action() {
      }
   }
}
