package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/ShapedRecipe.class */
public class ShapedRecipe implements CraftingRecipe {
    private final int width;
    private final int height;
    private final NonNullList<Ingredient> recipeItems;
    private final ItemStack result;

    /* renamed from: id */
    private final ResourceLocation id;
    private final String group;

    public ShapedRecipe(ResourceLocation resourceLocation, String str, int i, int i2, NonNullList<Ingredient> nonNullList, ItemStack itemStack) {
        this.id = resourceLocation;
        this.group = str;
        this.width = i;
        this.height = i2;
        this.recipeItems = nonNullList;
        this.result = itemStack;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ResourceLocation getId() {
        return this.id;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
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
        return this.recipeItems;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i >= this.width && i2 >= this.height;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        for (int i = 0; i <= craftingContainer.getWidth() - this.width; i++) {
            for (int i2 = 0; i2 <= craftingContainer.getHeight() - this.height; i2++) {
                if (matches(craftingContainer, i, i2, true) || matches(craftingContainer, i, i2, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matches(CraftingContainer craftingContainer, int i, int i2, boolean z) {
        for (int i3 = 0; i3 < craftingContainer.getWidth(); i3++) {
            for (int i4 = 0; i4 < craftingContainer.getHeight(); i4++) {
                int i5 = i3 - i;
                int i6 = i4 - i2;
                Ingredient ingredient = Ingredient.EMPTY;
                if (i5 >= 0 && i6 >= 0 && i5 < this.width && i6 < this.height) {
                    ingredient = z ? this.recipeItems.get(((this.width - i5) - 1) + (i6 * this.width)) : this.recipeItems.get(i5 + (i6 * this.width));
                }
                if (!ingredient.test(craftingContainer.getItem(i3 + (i4 * craftingContainer.getWidth())))) {
                    return false;
                }
            }
        }
        return true;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        return getResultItem().copy();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static NonNullList<Ingredient> dissolvePattern(String[] strArr, Map<String, Ingredient> map, int i, int i2) {
        NonNullList<Ingredient> withSize = NonNullList.withSize(i * i2, Ingredient.EMPTY);
        Set<String> newHashSet = Sets.newHashSet(map.keySet());
        newHashSet.remove(" ");
        for (int i3 = 0; i3 < strArr.length; i3++) {
            for (int i4 = 0; i4 < strArr[i3].length(); i4++) {
                String substring = strArr[i3].substring(i4, i4 + 1);
                Ingredient ingredient = map.get(substring);
                if (ingredient == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + substring + "' but it's not defined in the key");
                }
                newHashSet.remove(substring);
                withSize.set(i4 + (i * i3), ingredient);
            }
        }
        if (!newHashSet.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + newHashSet);
        }
        return withSize;
    }

    @VisibleForTesting
    static String[] shrink(String... strArr) {
        int i = Integer.MAX_VALUE;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        for (int i5 = 0; i5 < strArr.length; i5++) {
            String str = strArr[i5];
            i = Math.min(i, firstNonSpace(str));
            int lastNonSpace = lastNonSpace(str);
            i2 = Math.max(i2, lastNonSpace);
            if (lastNonSpace < 0) {
                if (i3 == i5) {
                    i3++;
                }
                i4++;
            } else {
                i4 = 0;
            }
        }
        if (strArr.length == i4) {
            return new String[0];
        }
        String[] strArr2 = new String[(strArr.length - i4) - i3];
        for (int i6 = 0; i6 < strArr2.length; i6++) {
            strArr2[i6] = strArr[i6 + i3].substring(i, i2 + 1);
        }
        return strArr2;
    }

    private static int firstNonSpace(String str) {
        int i = 0;
        while (i < str.length() && str.charAt(i) == ' ') {
            i++;
        }
        return i;
    }

    private static int lastNonSpace(String str) {
        int length = str.length() - 1;
        while (length >= 0 && str.charAt(length) == ' ') {
            length--;
        }
        return length;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String[] patternFromJson(JsonArray jsonArray) {
        String[] strArr = new String[jsonArray.size()];
        if (strArr.length > 3) {
            throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
        }
        if (strArr.length == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        }
        for (int i = 0; i < strArr.length; i++) {
            String convertToString = GsonHelper.convertToString(jsonArray.get(i), "pattern[" + i + "]");
            if (convertToString.length() > 3) {
                throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
            }
            if (i > 0 && strArr[0].length() != convertToString.length()) {
                throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            }
            strArr[i] = convertToString;
        }
        return strArr;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Map<String, Ingredient> keyFromJson(JsonObject jsonObject) {
        HashMap newHashMap = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getKey().length() != 1) {
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(entry.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }
            newHashMap.put(entry.getKey(), Ingredient.fromJson(entry.getValue()));
        }
        newHashMap.put(" ", Ingredient.EMPTY);
        return newHashMap;
    }

    public static ItemStack itemFromJson(JsonObject jsonObject) {
        String asString = GsonHelper.getAsString(jsonObject, "item");
        Item orElseThrow = Registry.ITEM.getOptional(new ResourceLocation(asString)).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown item '" + asString + "'");
        });
        if (jsonObject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
        }
        return new ItemStack(orElseThrow, GsonHelper.getAsInt(jsonObject, "count", 1));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/ShapedRecipe$Serializer.class */
    public static class Serializer implements RecipeSerializer<ShapedRecipe> {
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public ShapedRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            String asString = GsonHelper.getAsString(jsonObject, "group", "");
            Map<String, Ingredient> keyFromJson = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(jsonObject, "key"));
            String[] shrink = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(jsonObject, "pattern")));
            int length = shrink[0].length();
            int length2 = shrink.length;
            return new ShapedRecipe(resourceLocation, asString, length, length2, ShapedRecipe.dissolvePattern(shrink, keyFromJson, length, length2), ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(jsonObject, "result")));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public ShapedRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            int readVarInt = friendlyByteBuf.readVarInt();
            int readVarInt2 = friendlyByteBuf.readVarInt();
            String readUtf = friendlyByteBuf.readUtf(32767);
            NonNullList<Ingredient> withSize = NonNullList.withSize(readVarInt * readVarInt2, Ingredient.EMPTY);
            for (int i = 0; i < withSize.size(); i++) {
                withSize.set(i, Ingredient.fromNetwork(friendlyByteBuf));
            }
            return new ShapedRecipe(resourceLocation, readUtf, readVarInt, readVarInt2, withSize, friendlyByteBuf.readItem());
        }

        @Override // net.minecraft.world.item.crafting.RecipeSerializer
        public void toNetwork(FriendlyByteBuf var1, ShapedRecipe var2) {
            var1.writeVarInt(var2.width);
            var1.writeVarInt(var2.height);
            var1.writeUtf(var2.group);

            for(Ingredient var4 : var2.recipeItems) {
                var4.toNetwork(var1);
            }

            var1.writeItem(var2.result);
        }
    }
}
