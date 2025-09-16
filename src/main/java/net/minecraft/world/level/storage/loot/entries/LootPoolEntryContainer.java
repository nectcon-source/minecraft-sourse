package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootPoolEntryContainer.class */
public abstract class LootPoolEntryContainer implements ComposableEntryContainer {
    protected final LootItemCondition[] conditions;
    private final Predicate<LootContext> compositeCondition;

    public abstract LootPoolEntryType getType();

    protected LootPoolEntryContainer(LootItemCondition[] lootItemConditionArr) {
        this.conditions = lootItemConditionArr;
        this.compositeCondition = LootItemConditions.andConditions(lootItemConditionArr);
    }

    public void validate(ValidationContext validationContext) {
        for (int i = 0; i < this.conditions.length; i++) {
            this.conditions[i].validate(validationContext.forChild(".condition[" + i + "]"));
        }
    }

    protected final boolean canRun(LootContext lootContext) {
        return this.compositeCondition.test(lootContext);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootPoolEntryContainer$Builder.class */
    public static abstract class Builder<T extends Builder<T>> implements ConditionUserBuilder<T> {
        private final List<LootItemCondition> conditions = Lists.newArrayList();

        protected abstract T getThis();

        public abstract LootPoolEntryContainer build();

        @Override // net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder
        public T when(LootItemCondition.Builder builder) {
            this.conditions.add(builder.build());
            return getThis();
        }

        @Override // net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder
        public final T unwrap() {
            return getThis();
        }

        protected LootItemCondition[] getConditions() {
            return (LootItemCondition[]) this.conditions.toArray(new LootItemCondition[0]);
        }

        public AlternativesEntry.Builder otherwise(Builder<?> builder) {
            return new AlternativesEntry.Builder(this, builder);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootPoolEntryContainer$Serializer.class */
    public static abstract class Serializer<T extends LootPoolEntryContainer> implements net.minecraft.world.level.storage.loot.Serializer<T> {
        public abstract void serializeCustom(JsonObject jsonObject, T t, JsonSerializationContext jsonSerializationContext);

        public abstract T deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr);

        @Override // net.minecraft.world.level.storage.loot.Serializer
        public final void serialize(JsonObject jsonObject, T t, JsonSerializationContext jsonSerializationContext) {
            if (!ArrayUtils.isEmpty(t.conditions)) {
                jsonObject.add("conditions", jsonSerializationContext.serialize(t.conditions));
            }
            serializeCustom(jsonObject, t, jsonSerializationContext);
        }

        @Override // net.minecraft.world.level.storage.loot.Serializer
        public final T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return deserializeCustom(jsonObject, jsonDeserializationContext, (LootItemCondition[]) GsonHelper.getAsObject(jsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class));
        }
    }
}
