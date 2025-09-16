package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootPoolSingletonContainer.class */
public abstract class LootPoolSingletonContainer extends LootPoolEntryContainer {
    protected final int weight;
    protected final int quality;
    protected final LootItemFunction[] functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final LootPoolEntry entry;

    @FunctionalInterface
    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootPoolSingletonContainer$EntryConstructor.class */
    public interface EntryConstructor {
        LootPoolSingletonContainer build(int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr);
    }

    protected abstract void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext);

    protected LootPoolSingletonContainer(int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
        super(lootItemConditionArr);
        this.entry = new EntryBase() { // from class: net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.1
            @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntry
            public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
                LootPoolSingletonContainer.this.createItemStack(LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, consumer, lootContext), lootContext);
            }
        };
        this.weight = i;
        this.quality = i2;
        this.functions = lootItemFunctionArr;
        this.compositeFunction = LootItemFunctions.compose(lootItemFunctionArr);
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        for (int i = 0; i < this.functions.length; i++) {
            this.functions[i].validate(validationContext.forChild(".functions[" + i + "]"));
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootPoolSingletonContainer$EntryBase.class */
    public abstract class EntryBase implements LootPoolEntry {
        protected EntryBase() {
        }

        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntry
        public int getWeight(float f) {
            return Math.max(Mth.floor(LootPoolSingletonContainer.this.weight + (LootPoolSingletonContainer.this.quality * f)), 0);
        }
    }

    @Override // net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer
    public boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (canRun(lootContext)) {
            consumer.accept(this.entry);
            return true;
        }
        return false;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootPoolSingletonContainer$Builder.class */
    public static abstract class Builder<T extends Builder<T>> extends LootPoolEntryContainer.Builder<T> implements FunctionUserBuilder<T> {
        protected int weight = 1;
        protected int quality = 0;
        private final List<LootItemFunction> functions = Lists.newArrayList();

        @Override // net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder
        public T apply(LootItemFunction.Builder builder) {
            this.functions.add(builder.build());
            return (T) getThis();
        }

        protected LootItemFunction[] getFunctions() {
            return (LootItemFunction[]) this.functions.toArray(new LootItemFunction[0]);
        }

        public T setWeight(int i) {
            this.weight = i;
            return (T) getThis();
        }

        public T setQuality(int i) {
            this.quality = i;
            return (T) getThis();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootPoolSingletonContainer$DummyBuilder.class */
    static class DummyBuilder extends Builder<DummyBuilder> {
        private final EntryConstructor constructor;

        public DummyBuilder(EntryConstructor entryConstructor) {
            this.constructor = entryConstructor;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder
        public DummyBuilder getThis() {
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder
        public LootPoolEntryContainer build() {
            return this.constructor.build(this.weight, this.quality, getConditions(), getFunctions());
        }
    }

    public static Builder<?> simpleBuilder(EntryConstructor entryConstructor) {
        return new DummyBuilder(entryConstructor);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootPoolSingletonContainer$Serializer.class */
    public static abstract class Serializer<T extends LootPoolSingletonContainer> extends LootPoolEntryContainer.Serializer<T> {
        protected abstract T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr);

        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Serializer
        public void serializeCustom(JsonObject jsonObject, T t, JsonSerializationContext jsonSerializationContext) {
            if (t.weight != 1) {
                jsonObject.addProperty("weight", Integer.valueOf(t.weight));
            }
            if (t.quality != 0) {
                jsonObject.addProperty("quality", Integer.valueOf(t.quality));
            }
            if (!ArrayUtils.isEmpty(t.functions)) {
                jsonObject.add("functions", jsonSerializationContext.serialize(t.functions));
            }
        }

        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Serializer
        public final T deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return deserialize(jsonObject, jsonDeserializationContext, GsonHelper.getAsInt(jsonObject, "weight", 1), GsonHelper.getAsInt(jsonObject, "quality", 0), lootItemConditionArr, (LootItemFunction[]) GsonHelper.getAsObject(jsonObject, "functions", new LootItemFunction[0], jsonDeserializationContext, LootItemFunction[].class));
        }
    }
}
