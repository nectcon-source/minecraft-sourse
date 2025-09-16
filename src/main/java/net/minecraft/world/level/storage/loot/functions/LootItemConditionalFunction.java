package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LootItemConditionalFunction.class */
public abstract class LootItemConditionalFunction implements LootItemFunction {
    protected final LootItemCondition[] predicates;
    private final Predicate<LootContext> compositePredicates;

    protected abstract ItemStack run(ItemStack itemStack, LootContext lootContext);

    protected LootItemConditionalFunction(LootItemCondition[] lootItemConditionArr) {
        this.predicates = lootItemConditionArr;
        this.compositePredicates = LootItemConditions.andConditions(lootItemConditionArr);
    }

    @Override // java.util.function.BiFunction
    public final ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        return this.compositePredicates.test(lootContext) ? run(itemStack, lootContext) : itemStack;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public void validate(ValidationContext validationContext) {
        LootItemFunction.super.validate(validationContext);
        for (int i = 0; i < this.predicates.length; i++) {
            this.predicates[i].validate(validationContext.forChild(".conditions[" + i + "]"));
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LootItemConditionalFunction$Builder.class */
    public static abstract class Builder<T extends Builder<T>> implements LootItemFunction.Builder, ConditionUserBuilder<T> {
        private final List<LootItemCondition> conditions = Lists.newArrayList();

        protected abstract T getThis();

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
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LootItemConditionalFunction$DummyBuilder.class */
    static final class DummyBuilder extends Builder<DummyBuilder> {
        private final Function<LootItemCondition[], LootItemFunction> constructor;

        public DummyBuilder(Function<LootItemCondition[], LootItemFunction> function) {
            this.constructor = function;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder
        public DummyBuilder getThis() {
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder
        public LootItemFunction build() {
            return this.constructor.apply(getConditions());
        }
    }

    protected static Builder<?> simpleBuilder(Function<LootItemCondition[], LootItemFunction> function) {
        return new DummyBuilder(function);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LootItemConditionalFunction$Serializer.class */
    public static abstract class Serializer<T extends LootItemConditionalFunction> implements net.minecraft.world.level.storage.loot.Serializer<T> {
        public abstract T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr);

        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, T t, JsonSerializationContext jsonSerializationContext) {
            if (!ArrayUtils.isEmpty(t.predicates)) {
                jsonObject.add("conditions", jsonSerializationContext.serialize(t.predicates));
            }
        }

        @Override // net.minecraft.world.level.storage.loot.Serializer
        public final T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return deserialize(jsonObject, jsonDeserializationContext, (LootItemCondition[]) GsonHelper.getAsObject(jsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class));
        }
    }
}
