package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetLoreFunction.class */
public class SetLoreFunction extends LootItemConditionalFunction {
    private final boolean replace;
    private final List<Component> lore;

    @Nullable
    private final LootContext.EntityTarget resolutionContext;

    public SetLoreFunction(LootItemCondition[] lootItemConditionArr, boolean z, List<Component> list, @Nullable LootContext.EntityTarget entityTarget) {
        super(lootItemConditionArr);
        this.replace = z;
        this.lore = ImmutableList.copyOf(list);
        this.resolutionContext = entityTarget;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_LORE;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        ListTag var3 = this.getLoreTag(itemStack, !this.lore.isEmpty());
        if (var3 != null) {
            if (this.replace) {
                var3.clear();
            }

            UnaryOperator<Component> var4 = SetNameFunction.createResolver(lootContext, this.resolutionContext);
            this.lore.stream().map(var4).map(Component.Serializer::toJson).map(StringTag::valueOf).forEach(var3::add);
        }

        return itemStack;
    }

    @Nullable
    private ListTag getLoreTag(ItemStack itemStack, boolean z) {
        CompoundTag compoundTag;
        CompoundTag compoundTag2;
        if (itemStack.hasTag()) {
            compoundTag = itemStack.getTag();
        } else if (z) {
            compoundTag = new CompoundTag();
            itemStack.setTag(compoundTag);
        } else {
            return null;
        }
        if (compoundTag.contains("display", 10)) {
            compoundTag2 = compoundTag.getCompound("display");
        } else if (z) {
            compoundTag2 = new CompoundTag();
            compoundTag.put("display", compoundTag2);
        } else {
            return null;
        }
        if (compoundTag2.contains("Lore", 9)) {
            return compoundTag2.getList("Lore", 8);
        }
        if (z) {
            ListTag listTag = new ListTag();
            compoundTag2.put("Lore", listTag);
            return listTag;
        }
        return null;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetLoreFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetLoreFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, SetLoreFunction setLoreFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setLoreFunction, jsonSerializationContext);
            jsonObject.addProperty("replace", Boolean.valueOf(setLoreFunction.replace));
            JsonArray jsonArray = new JsonArray();
            Iterator it = setLoreFunction.lore.iterator();
            while (it.hasNext()) {
                jsonArray.add(Component.Serializer.toJsonTree((Component) it.next()));
            }
            jsonObject.add("lore", jsonArray);
            if (setLoreFunction.resolutionContext != null) {
                jsonObject.add("entity", jsonSerializationContext.serialize(setLoreFunction.resolutionContext));
            }
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public SetLoreFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new SetLoreFunction(lootItemConditionArr, GsonHelper.getAsBoolean(jsonObject, "replace", false),  Streams.stream(GsonHelper.getAsJsonArray(jsonObject, "lore")).map(Component.Serializer::fromJson).collect(ImmutableList.toImmutableList()), GsonHelper.getAsObject(jsonObject, "entity", null, jsonDeserializationContext, LootContext.EntityTarget.class));
        }
    }
}
