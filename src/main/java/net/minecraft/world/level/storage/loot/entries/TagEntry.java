package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/TagEntry.class */
public class TagEntry extends LootPoolSingletonContainer {
    private final Tag<Item> tag;
    private final boolean expand;

    private TagEntry(Tag<Item> tag, boolean z, int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
        super(i, i2, lootItemConditionArr, lootItemFunctionArr);
        this.tag = tag;
        this.expand = z;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public LootPoolEntryType getType() {
        return LootPoolEntries.TAG;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        this.tag.getValues().forEach(item -> {
            consumer.accept(new ItemStack(item));
        });
    }

    private boolean expandTag(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (!this.canRun(lootContext)) {
            return false;
        } else {
            for(final Item var4 : this.tag.getValues()) {
                consumer.accept(new LootPoolSingletonContainer.EntryBase() {
                    public void createItemStack(Consumer<ItemStack> var1, LootContext var2) {
                        var1.accept(new ItemStack(var4));
                    }
                });
            }

            return true;
        }
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer, net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer
    public boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (this.expand) {
            return expandTag(lootContext, consumer);
        }
        return super.expand(lootContext, consumer);
    }

    public static LootPoolSingletonContainer.Builder<?> expandTag(Tag<Item> tag) {
        return simpleBuilder((i, i2, lootItemConditionArr, lootItemFunctionArr) -> {
            return new TagEntry(tag, true, i, i2, lootItemConditionArr, lootItemFunctionArr);
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/TagEntry$Serializer.class */
    public static class Serializer extends LootPoolSingletonContainer.Serializer<TagEntry> {
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Serializer, net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Serializer
        public void serializeCustom(JsonObject jsonObject, TagEntry tagEntry, JsonSerializationContext jsonSerializationContext) {
            super.serializeCustom(jsonObject,  tagEntry, jsonSerializationContext);
            jsonObject.addProperty("name", SerializationTags.getInstance().getItems().getIdOrThrow(tagEntry.tag).toString());
            jsonObject.addProperty("expand", Boolean.valueOf(tagEntry.expand));
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Serializer
        public TagEntry deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
            Tag<Item> tag = SerializationTags.getInstance().getItems().getTag(resourceLocation);
            if (tag == null) {
                throw new JsonParseException("Can't find tag: " + resourceLocation);
            }
            return new TagEntry(tag, GsonHelper.getAsBoolean(jsonObject, "expand"), i, i2, lootItemConditionArr, lootItemFunctionArr);
        }
    }
}
