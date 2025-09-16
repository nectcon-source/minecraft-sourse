//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.tags.TagContainer;

public class ClientboundUpdateTagsPacket implements Packet<ClientGamePacketListener> {
   private TagContainer tags;

   public ClientboundUpdateTagsPacket() {
   }

   public ClientboundUpdateTagsPacket(TagContainer var1) {
      this.tags = var1;
   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.tags = TagContainer.deserializeFromNetwork(var1);
   }

   public void write(FriendlyByteBuf var1) throws IOException {
      this.tags.serializeToNetwork(var1);
   }

   public void handle(ClientGamePacketListener var1) {
      var1.handleUpdateTags(this);
   }

   public TagContainer getTags() {
      return this.tags;
   }
}
