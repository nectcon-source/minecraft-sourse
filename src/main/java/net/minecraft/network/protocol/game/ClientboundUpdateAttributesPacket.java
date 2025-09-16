//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class ClientboundUpdateAttributesPacket implements Packet<ClientGamePacketListener> {
   private int entityId;
   private final List<AttributeSnapshot> attributes = Lists.newArrayList();

   public ClientboundUpdateAttributesPacket() {
   }

   public ClientboundUpdateAttributesPacket(int var1, Collection<AttributeInstance> var2) {
      this.entityId = var1;

      for(AttributeInstance var4 : var2) {
         this.attributes.add(new AttributeSnapshot(var4.getAttribute(), var4.getBaseValue(), var4.getModifiers()));
      }

   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.entityId = var1.readVarInt();
      int var2 = var1.readInt();

      for(int var3 = 0; var3 < var2; ++var3) {
         ResourceLocation var4 = var1.readResourceLocation();
         Attribute var5 = (Attribute)Registry.ATTRIBUTE.get(var4);
         double var6 = var1.readDouble();
         List<AttributeModifier> var8 = Lists.newArrayList();
         int var9 = var1.readVarInt();

         for(int var10 = 0; var10 < var9; ++var10) {
            UUID var11 = var1.readUUID();
            var8.add(new AttributeModifier(var11, "Unknown synced attribute modifier", var1.readDouble(), Operation.fromValue(var1.readByte())));
         }

         this.attributes.add(new AttributeSnapshot(var5, var6, var8));
      }

   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeInt(this.attributes.size());

      for(AttributeSnapshot var3 : this.attributes) {
         var1.writeResourceLocation(Registry.ATTRIBUTE.getKey(var3.getAttribute()));
         var1.writeDouble(var3.getBase());
         var1.writeVarInt(var3.getModifiers().size());

         for(AttributeModifier var5 : var3.getModifiers()) {
            var1.writeUUID(var5.getId());
            var1.writeDouble(var5.getAmount());
            var1.writeByte(var5.getOperation().toValue());
         }
      }

   }

   public void handle(ClientGamePacketListener var1) {
      var1.handleUpdateAttributes(this);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public List<AttributeSnapshot> getValues() {
      return this.attributes;
   }

   public class AttributeSnapshot {
      private final Attribute attribute;
      private final double base;
      private final Collection<AttributeModifier> modifiers;

      public AttributeSnapshot(Attribute var2, double var3, Collection<AttributeModifier> var5) {
         this.attribute = var2;
         this.base = var3;
         this.modifiers = var5;
      }

      public Attribute getAttribute() {
         return this.attribute;
      }

      public double getBase() {
         return this.base;
      }

      public Collection<AttributeModifier> getModifiers() {
         return this.modifiers;
      }
   }
}
