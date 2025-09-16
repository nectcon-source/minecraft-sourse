//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoPacket implements Packet<ClientGamePacketListener> {
   private Action action;
   private final List<PlayerUpdate> entries = Lists.newArrayList();

   public ClientboundPlayerInfoPacket() {
   }

   public ClientboundPlayerInfoPacket(Action var1, ServerPlayer... var2) {
      this.action = var1;

      for(ServerPlayer var6 : var2) {
         this.entries.add(new PlayerUpdate(var6.getGameProfile(), var6.latency, var6.gameMode.getGameModeForPlayer(), var6.getTabListDisplayName()));
      }

   }

   public ClientboundPlayerInfoPacket(Action var1, Iterable<ServerPlayer> var2) {
      this.action = var1;

      for(ServerPlayer var4 : var2) {
         this.entries.add(new PlayerUpdate(var4.getGameProfile(), var4.latency, var4.gameMode.getGameModeForPlayer(), var4.getTabListDisplayName()));
      }

   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.action = (Action)var1.readEnum(Action.class);
      int var2 = var1.readVarInt();

      for(int var3 = 0; var3 < var2; ++var3) {
         GameProfile var4 = null;
         int var5 = 0;
         GameType var6 = null;
         Component var7 = null;
         switch (this.action) {
            case ADD_PLAYER:
               var4 = new GameProfile(var1.readUUID(), var1.readUtf(16));
               int var8 = var1.readVarInt();
               int var9 = 0;

               for(; var9 < var8; ++var9) {
                  String var10 = var1.readUtf(32767);
                  String var11 = var1.readUtf(32767);
                  if (var1.readBoolean()) {
                     var4.getProperties().put(var10, new Property(var10, var11, var1.readUtf(32767)));
                  } else {
                     var4.getProperties().put(var10, new Property(var10, var11));
                  }
               }

               var6 = GameType.byId(var1.readVarInt());
               var5 = var1.readVarInt();
               if (var1.readBoolean()) {
                  var7 = var1.readComponent();
               }
               break;
            case UPDATE_GAME_MODE:
               var4 = new GameProfile(var1.readUUID(), (String)null);
               var6 = GameType.byId(var1.readVarInt());
               break;
            case UPDATE_LATENCY:
               var4 = new GameProfile(var1.readUUID(), (String)null);
               var5 = var1.readVarInt();
               break;
            case UPDATE_DISPLAY_NAME:
               var4 = new GameProfile(var1.readUUID(), (String)null);
               if (var1.readBoolean()) {
                  var7 = var1.readComponent();
               }
               break;
            case REMOVE_PLAYER:
               var4 = new GameProfile(var1.readUUID(), (String)null);
         }

         this.entries.add(new PlayerUpdate(var4, var5, var6, var7));
      }

   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeEnum(this.action);
      var1.writeVarInt(this.entries.size());

      for(PlayerUpdate var3 : this.entries) {
         switch (this.action) {
            case ADD_PLAYER:
               var1.writeUUID(var3.getProfile().getId());
               var1.writeUtf(var3.getProfile().getName());
               var1.writeVarInt(var3.getProfile().getProperties().size());

               for(Property var5 : var3.getProfile().getProperties().values()) {
                  var1.writeUtf(var5.getName());
                  var1.writeUtf(var5.getValue());
                  if (var5.hasSignature()) {
                     var1.writeBoolean(true);
                     var1.writeUtf(var5.getSignature());
                  } else {
                     var1.writeBoolean(false);
                  }
               }

               var1.writeVarInt(var3.getGameMode().getId());
               var1.writeVarInt(var3.getLatency());
               if (var3.getDisplayName() == null) {
                  var1.writeBoolean(false);
               } else {
                  var1.writeBoolean(true);
                  var1.writeComponent(var3.getDisplayName());
               }
               break;
            case UPDATE_GAME_MODE:
               var1.writeUUID(var3.getProfile().getId());
               var1.writeVarInt(var3.getGameMode().getId());
               break;
            case UPDATE_LATENCY:
               var1.writeUUID(var3.getProfile().getId());
               var1.writeVarInt(var3.getLatency());
               break;
            case UPDATE_DISPLAY_NAME:
               var1.writeUUID(var3.getProfile().getId());
               if (var3.getDisplayName() == null) {
                  var1.writeBoolean(false);
               } else {
                  var1.writeBoolean(true);
                  var1.writeComponent(var3.getDisplayName());
               }
               break;
            case REMOVE_PLAYER:
               var1.writeUUID(var3.getProfile().getId());
         }
      }

   }

   public void handle(ClientGamePacketListener var1) {
      var1.handlePlayerInfo(this);
   }

   public List<PlayerUpdate> getEntries() {
      return this.entries;
   }

   public Action getAction() {
      return this.action;
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("action", this.action).add("entries", this.entries).toString();
   }

   public static enum Action {
      ADD_PLAYER,
      UPDATE_GAME_MODE,
      UPDATE_LATENCY,
      UPDATE_DISPLAY_NAME,
      REMOVE_PLAYER;

      private Action() {
      }
   }

   public class PlayerUpdate {
      private final int latency;
      private final GameType gameMode;
      private final GameProfile profile;
      private final Component displayName;

      public PlayerUpdate(GameProfile var2, int var3, GameType var4, @Nullable Component var5) {
         this.profile = var2;
         this.latency = var3;
         this.gameMode = var4;
         this.displayName = var5;
      }

      public GameProfile getProfile() {
         return this.profile;
      }

      public int getLatency() {
         return this.latency;
      }

      public GameType getGameMode() {
         return this.gameMode;
      }

      @Nullable
      public Component getDisplayName() {
         return this.displayName;
      }

      public String toString() {
         return MoreObjects.toStringHelper(this).add("latency", this.latency).add("gameMode", this.gameMode).add("profile", this.profile).add("displayName", this.displayName == null ? null : Serializer.toJson(this.displayName)).toString();
      }
   }
}
