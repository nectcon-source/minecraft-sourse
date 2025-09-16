package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetContainerContents.class */
public class SetContainerContents extends LootItemConditionalFunction {
    private final List<LootPoolEntryContainer> entries;

    private SetContainerContents(LootItemCondition[] lootItemConditionArr, List<LootPoolEntryContainer> list) {
        super(lootItemConditionArr);
        this.entries = ImmutableList.copyOf(list);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_CONTENTS;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.isEmpty()) {
            return itemStack;
        } else {
            NonNullList<ItemStack> var3 = NonNullList.create();
            this.entries.forEach((var2x) -> var2x.expand(lootContext, (var2xx) -> {
                var3.getClass();
                var2xx.createItemStack(LootTable.createStackSplitter(var3::add), lootContext);
            }));
            CompoundTag var4 = new CompoundTag();
            ContainerHelper.saveAllItems(var4, var3);
            CompoundTag var5 = itemStack.getOrCreateTag();
            var5.put("BlockEntityTag", var4.merge(var5.getCompound("BlockEntityTag")));
            return itemStack;
        }
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction, net.minecraft.world.level.storage.loot.LootContextUser
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        for (int i = 0; i < this.entries.size(); i++) {
            this.entries.get(i).validate(validationContext.forChild(".entry[" + i + "]"));
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetContainerContents$Builder.class */
    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder
        public Builder getThis() {
            return this;
        }

        public Builder withEntry(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add(builder.build());
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder
        public LootItemFunction build() {
            return new SetContainerContents(getConditions(), this.entries);
        }
    }

    public static Builder setContents() {
        return new Builder();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetContainerContents$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerContents> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, SetContainerContents setContainerContents, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  setContainerContents, jsonSerializationContext);
            jsonObject.add("entries", jsonSerializationContext.serialize(setContainerContents.entries));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public SetContainerContents deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new SetContainerContents(lootItemConditionArr, Arrays.asList((LootPoolEntryContainer[]) GsonHelper.getAsObject(jsonObject, "entries", jsonDeserializationContext, LootPoolEntryContainer[].class)));
        }
    }
}
