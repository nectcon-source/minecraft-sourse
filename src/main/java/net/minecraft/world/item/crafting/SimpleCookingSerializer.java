package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/SimpleCookingSerializer.class */
public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
    private final int defaultCookingTime;
    private final CookieBaker<T> factory;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/SimpleCookingSerializer$CookieBaker.class */
    interface CookieBaker<T extends AbstractCookingRecipe> {
        T create(ResourceLocation resourceLocation, String str, Ingredient ingredient, ItemStack itemStack, float f, int i);
    }

    public SimpleCookingSerializer(CookieBaker<T> cookieBaker, int i) {
        this.defaultCookingTime = i;
        this.factory = cookieBaker;
    }

    @Override // net.minecraft.world.item.crafting.RecipeSerializer
    public T fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        String asString = GsonHelper.getAsString(jsonObject, "group", "");
        Ingredient fromJson = Ingredient.fromJson(GsonHelper.isArrayNode(jsonObject, "ingredient") ? GsonHelper.getAsJsonArray(jsonObject, "ingredient") : GsonHelper.getAsJsonObject(jsonObject, "ingredient"));
        String asString2 = GsonHelper.getAsString(jsonObject, "result");
        return this.factory.create(resourceLocation, asString, fromJson, new ItemStack(Registry.ITEM.getOptional(new ResourceLocation(asString2)).orElseThrow(() -> {
            return new IllegalStateException("Item: " + asString2 + " does not exist");
        })), GsonHelper.getAsFloat(jsonObject, "experience", 0.0f), GsonHelper.getAsInt(jsonObject, "cookingtime", this.defaultCookingTime));
    }

    @Override // net.minecraft.world.item.crafting.RecipeSerializer
    public T fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        return this.factory.create(resourceLocation, friendlyByteBuf.readUtf(32767), Ingredient.fromNetwork(friendlyByteBuf), friendlyByteBuf.readItem(), friendlyByteBuf.readFloat(), friendlyByteBuf.readVarInt());
    }

    @Override // net.minecraft.world.item.crafting.RecipeSerializer
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, T t) {
        friendlyByteBuf.writeUtf(t.group);
        t.ingredient.toNetwork(friendlyByteBuf);
        friendlyByteBuf.writeItem(t.result);
        friendlyByteBuf.writeFloat(t.experience);
        friendlyByteBuf.writeVarInt(t.cookingTime);
    }
}
