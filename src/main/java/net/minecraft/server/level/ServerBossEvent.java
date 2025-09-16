package net.minecraft.server.level;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class ServerBossEvent extends BossEvent {
   private final Set<ServerPlayer> players = Sets.newHashSet();
   private final Set<ServerPlayer> unmodifiablePlayers = Collections.unmodifiableSet(this.players);
   private boolean visible = true;

   public ServerBossEvent(Component var1, BossEvent.BossBarColor var2, BossEvent.BossBarOverlay var3) {
      super(Mth.createInsecureUUID(), var1, var2, var3);
   }

   @Override
   public void setPercent(float var1) {
      if (var1 != this.percent) {
         super.setPercent(var1);
         this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_PCT);
      }
   }

   @Override
   public void setColor(BossEvent.BossBarColor var1) {
      if (var1 != this.color) {
         super.setColor(var1);
         this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_STYLE);
      }
   }

   @Override
   public void setOverlay(BossEvent.BossBarOverlay var1) {
      if (var1 != this.overlay) {
         super.setOverlay(var1);
         this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_STYLE);
      }
   }

   @Override
   public BossEvent setDarkenScreen(boolean var1) {
      if (var1 != this.darkenScreen) {
         super.setDarkenScreen(var1);
         this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   @Override
   public BossEvent setPlayBossMusic(boolean var1) {
      if (var1 != this.playBossMusic) {
         super.setPlayBossMusic(var1);
         this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   @Override
   public BossEvent setCreateWorldFog(boolean var1) {
      if (var1 != this.createWorldFog) {
         super.setCreateWorldFog(var1);
         this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_PROPERTIES);
      }
      return this;
   }

   @Override
   public void setName(Component var1) {
      if (!Objects.equal(var1, this.name)) {
         super.setName(var1);
         this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_NAME);
      }
   }

   private void broadcast(ClientboundBossEventPacket.Operation var1) {
      if (this.visible) {
         ClientboundBossEventPacket var2 = new ClientboundBossEventPacket(var1, this);

         for(ServerPlayer var4 : this.players) {
            var4.connection.send(var2);
         }
      }
   }

   public void addPlayer(ServerPlayer var1) {
      if (this.players.add(var1) && this.visible) {
         var1.connection.send(new ClientboundBossEventPacket(ClientboundBossEventPacket.Operation.ADD, this));
      }
   }

   public void removePlayer(ServerPlayer var1) {
      if (this.players.remove(var1) && this.visible) {
         var1.connection.send(new ClientboundBossEventPacket(ClientboundBossEventPacket.Operation.REMOVE, this));
      }
   }

   public void removeAllPlayers() {
      if (!this.players.isEmpty()) {
         for(ServerPlayer var2 : Lists.newArrayList(this.players)) {
            this.removePlayer(var2);
         }
      }
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean var1) {
      if (var1 != this.visible) {
         this.visible = var1;

         for(ServerPlayer var3 : this.players) {
            var3.connection.send(new ClientboundBossEventPacket(var1 ? ClientboundBossEventPacket.Operation.ADD : ClientboundBossEventPacket.Operation.REMOVE, this));
         }
      }
   }

   public Collection<ServerPlayer> getPlayers() {
      return this.unmodifiablePlayers;
   }
}
