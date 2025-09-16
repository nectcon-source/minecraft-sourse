package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/SimpleRecipeSerializer.class */
public class SimpleRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final Function<ResourceLocation, T> constructor;

    public SimpleRecipeSerializer(Function<ResourceLocation, T> function) {
        this.constructor = function;
    }

    @Override // net.minecraft.world.item.crafting.RecipeSerializer
    public T fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        return this.constructor.apply(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.RecipeSerializer
    public T fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        return this.constructor.apply(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.RecipeSerializer
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, T t) {
    }
}
