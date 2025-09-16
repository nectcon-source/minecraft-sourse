package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/SingleItemRecipe.class */
public abstract class SingleItemRecipe implements Recipe<Container> {
    protected final Ingredient ingredient;
    protected final ItemStack result;
    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;

    /* renamed from: id */
    protected final ResourceLocation id;
    protected final String group;

    public SingleItemRecipe(RecipeType<?> recipeType, RecipeSerializer<?> recipeSerializer, ResourceLocation resourceLocation, String str, Ingredient ingredient, ItemStack itemStack) {
        this.type = recipeType;
        this.serializer = recipeSerializer;
        this.id = resourceLocation;
        this.group = str;
        this.ingredient = ingredient;
        this.result = itemStack;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeType<?> getType() {
        return this.type;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ResourceLocation getId() {
        return this.id;
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
        NonNullList<Ingredient> create = NonNullList.create();
        create.add(this.ingredient);
        return create;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return true;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(Container container) {
        return this.result.copy();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/SingleItemRecipe$Serializer.class */
    public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
        final SingleItemMaker<T> factory;

        /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/SingleItemRecipe$Serializer$SingleItemMaker.class */
        interface SingleItemMaker<T extends SingleItemRecipe> {
            T create(ResourceLocation resourceLocation, String str, Ingredient ingredient, ItemStack itemStack);
        }

        protected Serializer(SingleItemMaker<T> singleItemMaker) {
            this.factory = singleItemMaker;
        }

        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public T fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            Ingredient fromJson;
            String asString = GsonHelper.getAsString(jsonObject, "group", "");
            if (GsonHelper.isArrayNode(jsonObject, "ingredient")) {
                fromJson = Ingredient.fromJson(GsonHelper.getAsJsonArray(jsonObject, "ingredient"));
            } else {
                fromJson = Ingredient.fromJson(GsonHelper.getAsJsonObject(jsonObject, "ingredient"));
            }
            String asString2 = GsonHelper.getAsString(jsonObject, "result");
            return this.factory.create(resourceLocation, asString, fromJson, new ItemStack(Registry.ITEM.get(new ResourceLocation(asString2)), GsonHelper.getAsInt(jsonObject, "count")));
        }

        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public T fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            return this.factory.create(resourceLocation, friendlyByteBuf.readUtf(32767), Ingredient.fromNetwork(friendlyByteBuf), friendlyByteBuf.readItem());
        }

        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, T t) {
            friendlyByteBuf.writeUtf(t.group);
            t.ingredient.toNetwork(friendlyByteBuf);
            friendlyByteBuf.writeItem(t.result);
        }
    }
}
