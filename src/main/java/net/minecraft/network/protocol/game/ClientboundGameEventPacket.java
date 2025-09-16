//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundGameEventPacket implements Packet<ClientGamePacketListener> {
   public static final Type NO_RESPAWN_BLOCK_AVAILABLE = new Type(0);
   public static final Type START_RAINING = new Type(1);
   public static final Type STOP_RAINING = new Type(2);
   public static final Type CHANGE_GAME_MODE = new Type(3);
   public static final Type WIN_GAME = new Type(4);
   public static final Type DEMO_EVENT = new Type(5);
   public static final Type ARROW_HIT_PLAYER = new Type(6);
   public static final Type RAIN_LEVEL_CHANGE = new Type(7);
   public static final Type THUNDER_LEVEL_CHANGE = new Type(8);
   public static final Type PUFFER_FISH_STING = new Type(9);
   public static final Type GUARDIAN_ELDER_EFFECT = new Type(10);
   public static final Type IMMEDIATE_RESPAWN = new Type(11);
   private Type event;
   private float param;

   public ClientboundGameEventPacket() {
   }

   public ClientboundGameEventPacket(Type var1, float var2) {
      this.event = var1;
      this.param = var2;
   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.event = (Type)ClientboundGameEventPacket.Type.TYPES.get(var1.readUnsignedByte());
      this.param = var1.readFloat();
   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeByte(this.event.id);
      var1.writeFloat(this.param);
   }

   public void handle(ClientGamePacketListener var1) {
      var1.handleGameEvent(this);
   }

   public Type getEvent() {
      return this.event;
   }

   public float getParam() {
      return this.param;
   }

   public static class Type {
      private static final Int2ObjectMap<Type> TYPES = new Int2ObjectOpenHashMap();
      private final int id;

      public Type(int var1) {
         this.id = var1;
         TYPES.put(var1, this);
      }
   }
}
