package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/UpgradeRecipe.class */
public class UpgradeRecipe implements Recipe<Container> {
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack result;

    /* renamed from: id */
    private final ResourceLocation id;

    public UpgradeRecipe(ResourceLocation resourceLocation, Ingredient ingredient, Ingredient ingredient2, ItemStack itemStack) {
        this.id = resourceLocation;
        this.base = ingredient;
        this.addition = ingredient2;
        this.result = itemStack;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(Container container, Level level) {
        return this.base.test(container.getItem(0)) && this.addition.test(container.getItem(1));
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(Container container) {
        ItemStack copy = this.result.copy();
        CompoundTag tag = container.getItem(0).getTag();
        if (tag != null) {
            copy.setTag(tag.copy());
        }
        return copy;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i * i2 >= 2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack getResultItem() {
        return this.result;
    }

    public boolean isAdditionIngredient(ItemStack itemStack) {
        return this.addition.test(itemStack);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ResourceLocation getId() {
        return this.id;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/UpgradeRecipe$Serializer.class */
    public static class Serializer implements RecipeSerializer<UpgradeRecipe> {
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public UpgradeRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            return new UpgradeRecipe(resourceLocation, Ingredient.fromJson(GsonHelper.getAsJsonObject(jsonObject, "base")), Ingredient.fromJson(GsonHelper.getAsJsonObject(jsonObject, "addition")), ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(jsonObject, "result")));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public UpgradeRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            return new UpgradeRecipe(resourceLocation, Ingredient.fromNetwork(friendlyByteBuf), Ingredient.fromNetwork(friendlyByteBuf), friendlyByteBuf.readItem());
        }

        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, UpgradeRecipe upgradeRecipe) {
            upgradeRecipe.base.toNetwork(friendlyByteBuf);
            upgradeRecipe.addition.toNetwork(friendlyByteBuf);
            friendlyByteBuf.writeItem(upgradeRecipe.result);
        }
    }
}
