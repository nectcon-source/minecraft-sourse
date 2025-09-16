package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ClientboundUpdateRecipesPacket implements Packet<ClientGamePacketListener> {
   private List<Recipe<?>> recipes;

   public ClientboundUpdateRecipesPacket() {
   }

   public ClientboundUpdateRecipesPacket(Collection<Recipe<?>> var1) {
      this.recipes = Lists.newArrayList(var1);
   }

   public void handle(ClientGamePacketListener var1) {
      var1.handleUpdateRecipes(this);
   }

   @Override
   public void read(FriendlyByteBuf var1) throws IOException {
      this.recipes = Lists.newArrayList();
      int var2 = var1.readVarInt();

      for(int var3x = 0; var3x < var2; ++var3x) {
         this.recipes.add(fromNetwork(var1));
      }
   }

   @Override
   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeVarInt(this.recipes.size());

      for(Recipe<?> var3 : this.recipes) {
         toNetwork(var3, var1);
      }
   }

   public List<Recipe<?>> getRecipes() {
      return this.recipes;
   }

   public static Recipe<?> fromNetwork(FriendlyByteBuf var0) {
      ResourceLocation var1 = var0.readResourceLocation();
      ResourceLocation var2x =var0.readResourceLocation();
      return Registry.RECIPE_SERIALIZER.getOptional(var1).orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + var1)).fromNetwork(var2x, var0);
   }

//   public static <T extends Recipe<?>> void toNetwork(T recipe, FriendlyByteBuf buf) {
//      buf.writeResourceLocation(Registry.RECIPE_SERIALIZER.getKey(recipe.getSerializer()));
//      buf.writeResourceLocation(recipe.getId());
//      recipe.getSerializer().toNetwork(buf, recipe);
//   }
public static <T extends Recipe<?>> void toNetwork(T recipe, FriendlyByteBuf buf) throws IOException {
   try {
      Objects.requireNonNull(recipe, "Recipe cannot be null");
      Objects.requireNonNull(buf, "Buffer cannot be null");

      // Явное приведение типа к Recipe<?>
      RecipeSerializer<Recipe<?>> serializer =
              (RecipeSerializer<Recipe<?>>) recipe.getSerializer();

      if (serializer == null) {
         throw new IOException("Recipe serializer is null for recipe " + recipe.getId());
      }

      ResourceLocation serializerKey = Registry.RECIPE_SERIALIZER.getKey(serializer);
      if (serializerKey == null) {
         throw new IOException("Unregistered recipe serializer for recipe " + recipe.getId());
      }

      ResourceLocation recipeId = recipe.getId();
      if (recipeId == null) {
         throw new IOException("Recipe ID is null");
      }

      buf.writeResourceLocation(serializerKey);
      buf.writeResourceLocation(recipeId);

      // Явное приведение типа перед вызовом
      serializer.toNetwork(buf, (Recipe<?>)recipe);

   } catch (ClassCastException e) {
      throw new IOException("Type mismatch in recipe serialization", e);
   } catch (Exception e) {
      throw new IOException("Failed to serialize recipe", e);
   }
}

}
