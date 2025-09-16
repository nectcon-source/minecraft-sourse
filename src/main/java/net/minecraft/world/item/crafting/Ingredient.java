package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/Ingredient.class */
public final class Ingredient implements Predicate<ItemStack> {
    public static final Ingredient EMPTY = new Ingredient(Stream.empty());
    private final Value[] values;
    private ItemStack[] itemStacks;
    private IntList stackingIds;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/Ingredient$Value.class */
    interface Value {
        Collection<ItemStack> getItems();

        JsonObject serialize();
    }

    private Ingredient(Stream<? extends Value> stream) {
        this.values = (Value[]) stream.toArray(i -> {
            return new Value[i];
        });
    }

    public ItemStack[] getItems() {
        dissolve();
        return this.itemStacks;
    }

    private void dissolve() {
        if (this.itemStacks == null) {
            this.itemStacks = (ItemStack[]) Arrays.stream(this.values).flatMap(value -> {
                return value.getItems().stream();
            }).distinct().toArray(i -> {
                return new ItemStack[i];
            });
        }
    }

    @Override // java.util.function.Predicate
    public boolean test(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        dissolve();
        if (this.itemStacks.length == 0) {
            return itemStack.isEmpty();
        }
        for (ItemStack itemStack2 : this.itemStacks) {
            if (itemStack2.getItem() == itemStack.getItem()) {
                return true;
            }
        }
        return false;
    }

    public IntList getStackingIds() {
        if (this.stackingIds == null) {
            dissolve();
            this.stackingIds = new IntArrayList(this.itemStacks.length);
            for (ItemStack itemStack : this.itemStacks) {
                this.stackingIds.add(StackedContents.getStackingIndex(itemStack));
            }
            this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
        }
        return this.stackingIds;
    }

    public void toNetwork(FriendlyByteBuf friendlyByteBuf) {
        dissolve();
        friendlyByteBuf.writeVarInt(this.itemStacks.length);
        for (int i = 0; i < this.itemStacks.length; i++) {
            friendlyByteBuf.writeItem(this.itemStacks[i]);
        }
    }

    public JsonElement toJson() {
        if (this.values.length == 1) {
            return this.values[0].serialize();
        }
        JsonArray jsonArray = new JsonArray();
        for (Value value : this.values) {
            jsonArray.add(value.serialize());
        }
        return jsonArray;
    }

    public boolean isEmpty() {
        return this.values.length == 0 && (this.itemStacks == null || this.itemStacks.length == 0) && (this.stackingIds == null || this.stackingIds.isEmpty());
    }

    private static Ingredient fromValues(Stream<? extends Value> stream) {
        Ingredient ingredient = new Ingredient(stream);
        return ingredient.values.length == 0 ? EMPTY : ingredient;
    }

    /* renamed from: of */
    public static Ingredient of(ItemLike... itemLikeArr) {
        return of((Stream<ItemStack>) Arrays.stream(itemLikeArr).map(ItemStack::new));
    }

    /* renamed from: of */
    public static Ingredient of(ItemStack... itemStackArr) {
        return of((Stream<ItemStack>) Arrays.stream(itemStackArr));
    }

    /* renamed from: of */
    public static Ingredient of(Stream<ItemStack> stream) {
        return fromValues(stream.filter(itemStack -> {
            return !itemStack.isEmpty();
        }).map(itemStack2 -> {
            return new ItemValue(itemStack2);
        }));
    }

    /* renamed from: of */
    public static Ingredient of(Tag<Item> tag) {
        return fromValues(Stream.of(new TagValue(tag)));
    }

    public static Ingredient fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return fromValues(Stream.generate(() -> {
            return new ItemValue(friendlyByteBuf.readItem());
        }).limit(friendlyByteBuf.readVarInt()));
    }

    public static Ingredient fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new JsonSyntaxException("Item cannot be null");
        }
        if (jsonElement.isJsonObject()) {
            return fromValues(Stream.of(valueFromJson(jsonElement.getAsJsonObject())));
        }
        if (jsonElement.isJsonArray()) {
            JsonArray asJsonArray = jsonElement.getAsJsonArray();
            if (asJsonArray.size() == 0) {
                throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
            }
            return fromValues(StreamSupport.stream(asJsonArray.spliterator(), false).map(jsonElement2 -> {
                return valueFromJson(GsonHelper.convertToJsonObject(jsonElement2, "item"));
            }));
        }
        throw new JsonSyntaxException("Expected item to be object or array of objects");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Value valueFromJson(JsonObject jsonObject) {
        if (jsonObject.has("item") && jsonObject.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        }
        if (jsonObject.has("item")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "item"));
            return new ItemValue(new ItemStack(Registry.ITEM.getOptional(resourceLocation).orElseThrow(() -> {
                return new JsonSyntaxException("Unknown item '" + resourceLocation + "'");
            })));
        }
        if (jsonObject.has("tag")) {
            ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
            Tag<Item> tag = SerializationTags.getInstance().getItems().getTag(resourceLocation2);
            if (tag == null) {
                throw new JsonSyntaxException("Unknown item tag '" + resourceLocation2 + "'");
            }
            return new TagValue(tag);
        }
        throw new JsonParseException("An ingredient entry needs either a tag or an item");
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/Ingredient$ItemValue.class */
    static class ItemValue implements Value {
        private final ItemStack item;

        private ItemValue(ItemStack itemStack) {
            this.item = itemStack;
        }

        @Override // net.minecraft.world.item.crafting.Ingredient.Value
        public Collection<ItemStack> getItems() {
            return Collections.singleton(this.item);
        }

        @Override // net.minecraft.world.item.crafting.Ingredient.Value
        public JsonObject serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("item", Registry.ITEM.getKey(this.item.getItem()).toString());
            return jsonObject;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/Ingredient$TagValue.class */
    static class TagValue implements Value {
        private final Tag<Item> tag;

        private TagValue(Tag<Item> tag) {
            this.tag = tag;
        }

        @Override // net.minecraft.world.item.crafting.Ingredient.Value
        public Collection<ItemStack> getItems() {
            List<ItemStack> newArrayList = Lists.newArrayList();
            Iterator<Item> it = this.tag.getValues().iterator();
            while (it.hasNext()) {
                newArrayList.add(new ItemStack(it.next()));
            }
            return newArrayList;
        }

        @Override // net.minecraft.world.item.crafting.Ingredient.Value
        public JsonObject serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("tag", SerializationTags.getInstance().getItems().getIdOrThrow(this.tag).toString());
            return jsonObject;
        }
    }
}
