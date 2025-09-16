package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerEntity {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ServerLevel level;
   private final Entity entity;
   private final int updateInterval;
   private final boolean trackDelta;
   private final Consumer<Packet<?>> broadcast;
   private long xp;
   private long yp;
   private long zp;
   private int yRotp;
   private int xRotp;
   private int yHeadRotp;
   private Vec3 ap = Vec3.ZERO;
   private int tickCount;
   private int teleportDelay;
   private List<Entity> lastPassengers = Collections.emptyList();
   private boolean wasRiding;
   private boolean wasOnGround;

   public ServerEntity(ServerLevel var1, Entity var2, int var3, boolean var4, Consumer<Packet<?>> var5) {
      this.level = var1;
      this.broadcast = var5;
      this.entity = var2;
      this.updateInterval = var3;
      this.trackDelta = var4;
      this.updateSentPos();
      this.yRotp = Mth.floor(var2.yRot * 256.0F / 360.0F);
      this.xRotp = Mth.floor(var2.xRot * 256.0F / 360.0F);
      this.yHeadRotp = Mth.floor(var2.getYHeadRot() * 256.0F / 360.0F);
      this.wasOnGround = var2.isOnGround();
   }

   public void sendChanges() {
      List<Entity> var1 = this.entity.getPassengers();
      if (!var1.equals(this.lastPassengers)) {
         this.lastPassengers = var1;
         this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
      }

      if (this.entity instanceof ItemFrame && this.tickCount % 10 == 0) {
         ItemFrame var2 = (ItemFrame)this.entity;
         ItemStack var3 = var2.getItem();
         if (var3.getItem() instanceof MapItem) {
            MapItemSavedData var4 = MapItem.getOrCreateSavedData(var3, this.level);

            for(ServerPlayer var6 : this.level.players()) {
               var4.tickCarriedBy(var6, var3);
               Packet<?> var7 = ((MapItem)var3.getItem()).getUpdatePacket(var3, this.level, var6);
               if (var7 != null) {
                  var6.connection.send(var7);
               }
            }
         }

         this.sendDirtyEntityData();
      }

      if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
         if (this.entity.isPassenger()) {
            int var17 = Mth.floor(this.entity.yRot * 256.0F / 360.0F);
            int var20 = Mth.floor(this.entity.xRot * 256.0F / 360.0F);
            boolean var22 = Math.abs(var17 - this.yRotp) >= 1 || Math.abs(var20 - this.xRotp) >= 1;
            if (var22) {
               this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)var17, (byte)var20, this.entity.isOnGround()));
               this.yRotp = var17;
               this.xRotp = var20;
            }

            this.updateSentPos();
            this.sendDirtyEntityData();
            this.wasRiding = true;
         } else {
            ++this.teleportDelay;
            int var16 = Mth.floor(this.entity.yRot * 256.0F / 360.0F);
            int var19 = Mth.floor(this.entity.xRot * 256.0F / 360.0F);
            Vec3 var21 = this.entity.position().subtract(ClientboundMoveEntityPacket.packetToEntity(this.xp, this.yp, this.zp));
            boolean var23 = var21.lengthSqr() >= (double)7.6293945E-6F;
            Packet<?> var24 = null;
            boolean var25 = var23 || this.tickCount % 60 == 0;
            boolean var8 = Math.abs(var16 - this.yRotp) >= 1 || Math.abs(var19 - this.xRotp) >= 1;
            if (this.tickCount > 0 || this.entity instanceof AbstractArrow) {
               long var9 = ClientboundMoveEntityPacket.entityToPacket(var21.x);
               long var11 = ClientboundMoveEntityPacket.entityToPacket(var21.y);
               long var13 = ClientboundMoveEntityPacket.entityToPacket(var21.z);
               boolean var15 = var9 < -32768L || var9 > 32767L || var11 < -32768L || var11 > 32767L || var13 < -32768L || var13 > 32767L;
               if (!var15 && this.teleportDelay <= 400 && !this.wasRiding && this.wasOnGround == this.entity.isOnGround()) {
                  if ((!var25 || !var8) && !(this.entity instanceof AbstractArrow)) {
                     if (var25) {
                        var24 = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)((int)var9), (short)((int)var11), (short)((int)var13), this.entity.isOnGround());
                     } else if (var8) {
                        var24 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)var16, (byte)var19, this.entity.isOnGround());
                     }
                  } else {
                     var24 = new ClientboundMoveEntityPacket.PosRot(this.entity.getId(), (short)((int)var9), (short)((int)var11), (short)((int)var13), (byte)var16, (byte)var19, this.entity.isOnGround());
                  }
               } else {
                  this.wasOnGround = this.entity.isOnGround();
                  this.teleportDelay = 0;
                  var24 = new ClientboundTeleportEntityPacket(this.entity);
               }
            }

            if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.tickCount > 0) {
               Vec3 var26 = this.entity.getDeltaMovement();
               double var10 = var26.distanceToSqr(this.ap);
               if (var10 > 1.0E-7 || var10 > (double)0.0F && var26.lengthSqr() == (double)0.0F) {
                  this.ap = var26;
                  this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
               }
            }

            if (var24 != null) {
               this.broadcast.accept(var24);
            }

            this.sendDirtyEntityData();
            if (var25) {
               this.updateSentPos();
            }

            if (var8) {
               this.yRotp = var16;
               this.xRotp = var19;
            }

            this.wasRiding = false;
         }

         int var18 = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
         if (Math.abs(var18 - this.yHeadRotp) >= 1) {
            this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte)var18));
            this.yHeadRotp = var18;
         }

         this.entity.hasImpulse = false;
      }

      ++this.tickCount;
      if (this.entity.hurtMarked) {
         this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
         this.entity.hurtMarked = false;
      }
   }

   public void removePairing(ServerPlayer var1) {
      this.entity.stopSeenByPlayer(var1);
      var1.sendRemoveEntity(this.entity);
   }

   public void addPairing(ServerPlayer var1) {
      this.sendPairingData(var1.connection::send);
      this.entity.startSeenByPlayer(var1);
      var1.cancelRemoveEntity(this.entity);
   }

   public void sendPairingData(Consumer<Packet<?>> var1) {
      if (this.entity.removed) {
         LOGGER.warn("Fetching packet for removed entity " + this.entity);
      }

      Packet<?> var2 = this.entity.getAddEntityPacket();
      this.yHeadRotp = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
      var1.accept(var2);
      if (!this.entity.getEntityData().isEmpty()) {
         var1.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.entity.getEntityData(), true));
      }

      boolean var3 = this.trackDelta;
      if (this.entity instanceof LivingEntity) {
         Collection<AttributeInstance> var4 = ((LivingEntity)this.entity).getAttributes().getSyncableAttributes();
         if (!var4.isEmpty()) {
            var1.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), var4));
         }

         if (((LivingEntity)this.entity).isFallFlying()) {
            var3 = true;
         }
      }

      this.ap = this.entity.getDeltaMovement();
      if (var3 && !(var2 instanceof ClientboundAddMobPacket)) {
         var1.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
      }

      if (this.entity instanceof LivingEntity) {
         List<Pair<EquipmentSlot, ItemStack>> var10 = Lists.newArrayList();

         for(EquipmentSlot var8 : EquipmentSlot.values()) {
            ItemStack var9 = ((LivingEntity)this.entity).getItemBySlot(var8);
            if (!var9.isEmpty()) {
               var10.add(Pair.of(var8, var9.copy()));
            }
         }

         if (!var10.isEmpty()) {
            var1.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), var10));
         }
      }

      if (this.entity instanceof LivingEntity) {
         LivingEntity var11 = (LivingEntity)this.entity;

         for(MobEffectInstance var14 : var11.getActiveEffects()) {
            var1.accept(new ClientboundUpdateMobEffectPacket(this.entity.getId(), var14));
         }
      }

      if (!this.entity.getPassengers().isEmpty()) {
         var1.accept(new ClientboundSetPassengersPacket(this.entity));
      }

      if (this.entity.isPassenger()) {
         var1.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
      }

      if (this.entity instanceof Mob) {
         Mob var12 = (Mob)this.entity;
         if (var12.isLeashed()) {
            var1.accept(new ClientboundSetEntityLinkPacket(var12, var12.getLeashHolder()));
         }
      }
   }

   private void sendDirtyEntityData() {
      SynchedEntityData var1 = this.entity.getEntityData();
      if (var1.isDirty()) {
         this.broadcastAndSend(new ClientboundSetEntityDataPacket(this.entity.getId(), var1, false));
      }

      if (this.entity instanceof LivingEntity) {
         Set<AttributeInstance> var2 = ((LivingEntity)this.entity).getAttributes().getDirtyAttributes();
         if (!var2.isEmpty()) {
            this.broadcastAndSend(new ClientboundUpdateAttributesPacket(this.entity.getId(), var2));
         }

         var2.clear();
      }
   }

   private void updateSentPos() {
      this.xp = ClientboundMoveEntityPacket.entityToPacket(this.entity.getX());
      this.yp = ClientboundMoveEntityPacket.entityToPacket(this.entity.getY());
      this.zp = ClientboundMoveEntityPacket.entityToPacket(this.entity.getZ());
   }

   public Vec3 sentPos() {
      return ClientboundMoveEntityPacket.packetToEntity(this.xp, this.yp, this.zp);
   }

   private void broadcastAndSend(Packet<?> var1) {
      this.broadcast.accept(var1);
      if (this.entity instanceof ServerPlayer) {
         ((ServerPlayer)this.entity).connection.send(var1);
      }
   }
}
