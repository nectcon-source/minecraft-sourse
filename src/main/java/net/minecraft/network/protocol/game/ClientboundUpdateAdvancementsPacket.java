//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.network.protocol.game;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundUpdateAdvancementsPacket implements Packet<ClientGamePacketListener> {
   private boolean reset;
   private Map<ResourceLocation, Advancement.Builder> added;
   private Set<ResourceLocation> removed;
   private Map<ResourceLocation, AdvancementProgress> progress;

   public ClientboundUpdateAdvancementsPacket() {
   }

   public ClientboundUpdateAdvancementsPacket(boolean var1, Collection<Advancement> var2, Set<ResourceLocation> var3, Map<ResourceLocation, AdvancementProgress> var4) {
      this.reset = var1;
      this.added = Maps.newHashMap();

      for(Advancement var6 : var2) {
         this.added.put(var6.getId(), var6.deconstruct());
      }

      this.removed = var3;
      this.progress = Maps.newHashMap(var4);
   }

   public void handle(ClientGamePacketListener var1) {
      var1.handleUpdateAdvancementsPacket(this);
   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.reset = var1.readBoolean();
      this.added = Maps.newHashMap();
      this.removed = Sets.newLinkedHashSet();
      this.progress = Maps.newHashMap();
      int var2 = var1.readVarInt();

      for(int var3 = 0; var3 < var2; ++var3) {
         ResourceLocation var4 = var1.readResourceLocation();
         Advancement.Builder var5 = Builder.fromNetwork(var1);
         this.added.put(var4, var5);
      }

      var2 = var1.readVarInt();

      for(int var8 = 0; var8 < var2; ++var8) {
         ResourceLocation var10 = var1.readResourceLocation();
         this.removed.add(var10);
      }

      var2 = var1.readVarInt();

      for(int var9 = 0; var9 < var2; ++var9) {
         ResourceLocation var11 = var1.readResourceLocation();
         this.progress.put(var11, AdvancementProgress.fromNetwork(var1));
      }

   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeBoolean(this.reset);
      var1.writeVarInt(this.added.size());

      for(Map.Entry<ResourceLocation, Advancement.Builder> var3 : this.added.entrySet()) {
         ResourceLocation var4 = (ResourceLocation)var3.getKey();
         Advancement.Builder var5 = (Advancement.Builder)var3.getValue();
         var1.writeResourceLocation(var4);
         var5.serializeToNetwork(var1);
      }

      var1.writeVarInt(this.removed.size());

      for(ResourceLocation var8 : this.removed) {
         var1.writeResourceLocation(var8);
      }

      var1.writeVarInt(this.progress.size());

      for(Map.Entry<ResourceLocation, AdvancementProgress> var9 : this.progress.entrySet()) {
         var1.writeResourceLocation((ResourceLocation)var9.getKey());
         ((AdvancementProgress)var9.getValue()).serializeToNetwork(var1);
      }

   }

   public Map<ResourceLocation, Advancement.Builder> getAdded() {
      return this.added;
   }

   public Set<ResourceLocation> getRemoved() {
      return this.removed;
   }

   public Map<ResourceLocation, AdvancementProgress> getProgress() {
      return this.progress;
   }

   public boolean shouldReset() {
      return this.reset;
   }
}
