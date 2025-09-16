//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBookSettings;

public class ClientboundRecipePacket implements Packet<ClientGamePacketListener> {
   private State state;
   private List<ResourceLocation> recipes;
   private List<ResourceLocation> toHighlight;
   private RecipeBookSettings bookSettings;

   public ClientboundRecipePacket() {
   }

   public ClientboundRecipePacket(State var1, Collection<ResourceLocation> var2, Collection<ResourceLocation> var3, RecipeBookSettings var4) {
      this.state = var1;
      this.recipes = ImmutableList.copyOf(var2);
      this.toHighlight = ImmutableList.copyOf(var3);
      this.bookSettings = var4;
   }

   public void handle(ClientGamePacketListener var1) {
      var1.handleAddOrRemoveRecipes(this);
   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.state = (State)var1.readEnum(State.class);
      this.bookSettings = RecipeBookSettings.read(var1);
      int var2 = var1.readVarInt();
      this.recipes = Lists.newArrayList();

      for(int var3 = 0; var3 < var2; ++var3) {
         this.recipes.add(var1.readResourceLocation());
      }

      if (this.state == ClientboundRecipePacket.State.INIT) {
         var2 = var1.readVarInt();
         this.toHighlight = Lists.newArrayList();

         for(int var5 = 0; var5 < var2; ++var5) {
            this.toHighlight.add(var1.readResourceLocation());
         }
      }

   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeEnum(this.state);
      this.bookSettings.write(var1);
      var1.writeVarInt(this.recipes.size());

      for(ResourceLocation var3 : this.recipes) {
         var1.writeResourceLocation(var3);
      }

      if (this.state == ClientboundRecipePacket.State.INIT) {
         var1.writeVarInt(this.toHighlight.size());

         for(ResourceLocation var5 : this.toHighlight) {
            var1.writeResourceLocation(var5);
         }
      }

   }

   public List<ResourceLocation> getRecipes() {
      return this.recipes;
   }

   public List<ResourceLocation> getHighlights() {
      return this.toHighlight;
   }

   public RecipeBookSettings getBookSettings() {
      return this.bookSettings;
   }

   public State getState() {
      return this.state;
   }

   public static enum State {
      INIT,
      ADD,
      REMOVE;

      private State() {
      }
   }
}
