package net.minecraft.world.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Iterator;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/ShapelessRecipe.class */
public class ShapelessRecipe implements CraftingRecipe {

    /* renamed from: id */
    private final ResourceLocation id;
    private final String group;
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients;

    public ShapelessRecipe(ResourceLocation resourceLocation, String str, ItemStack itemStack, NonNullList<Ingredient> nonNullList) {
        this.id = resourceLocation;
        this.group = str;
        this.result = itemStack;
        this.ingredients = nonNullList;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ResourceLocation getId() {
        return this.id;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public String getGroup() {
        return this.group;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack getResultItem() {
        return this.result;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        StackedContents stackedContents = new StackedContents();
        int i = 0;
        for (int i2 = 0; i2 < craftingContainer.getContainerSize(); i2++) {
            ItemStack item = craftingContainer.getItem(i2);
            if (!item.isEmpty()) {
                i++;
                stackedContents.accountStack(item, 1);
            }
        }
        return i == this.ingredients.size() && stackedContents.canCraft(this, null);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        return this.result.copy();
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i * i2 >= this.ingredients.size();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/ShapelessRecipe$Serializer.class */
    public static class Serializer implements RecipeSerializer<ShapelessRecipe> {
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public ShapelessRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            String asString = GsonHelper.getAsString(jsonObject, "group", "");
            NonNullList<Ingredient> itemsFromJson = itemsFromJson(GsonHelper.getAsJsonArray(jsonObject, "ingredients"));
            if (itemsFromJson.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            }
            if (itemsFromJson.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            }
            return new ShapelessRecipe(resourceLocation, asString, ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(jsonObject, "result")), itemsFromJson);
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray jsonArray) {
            NonNullList<Ingredient> create = NonNullList.create();
            for (int i = 0; i < jsonArray.size(); i++) {
                Ingredient fromJson = Ingredient.fromJson(jsonArray.get(i));
                if (!fromJson.isEmpty()) {
                    create.add(fromJson);
                }
            }
            return create;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public ShapelessRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            String readUtf = friendlyByteBuf.readUtf(32767);
            NonNullList<Ingredient> withSize = NonNullList.withSize(friendlyByteBuf.readVarInt(), Ingredient.EMPTY);
            for (int i = 0; i < withSize.size(); i++) {
                withSize.set(i, Ingredient.fromNetwork(friendlyByteBuf));
            }
            return new ShapelessRecipe(resourceLocation, readUtf, friendlyByteBuf.readItem(), withSize);
        }

        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, ShapelessRecipe shapelessRecipe) {
            friendlyByteBuf.writeUtf(shapelessRecipe.group);
            friendlyByteBuf.writeVarInt(shapelessRecipe.ingredients.size());

            for(Ingredient var4 : shapelessRecipe.ingredients) {
                var4.toNetwork(friendlyByteBuf);
            }

            friendlyByteBuf.writeItem(shapelessRecipe.result);
        }
    }
}
