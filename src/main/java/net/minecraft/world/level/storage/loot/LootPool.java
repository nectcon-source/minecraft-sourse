package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootPool.class */
public class LootPool {
    private final LootPoolEntryContainer[] entries;
    private final LootItemCondition[] conditions;
    private final Predicate<LootContext> compositeCondition;
    private final LootItemFunction[] functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final RandomIntGenerator rolls;
    private final RandomValueBounds bonusRolls;

    private LootPool(LootPoolEntryContainer[] lootPoolEntryContainerArr, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr, RandomIntGenerator randomIntGenerator, RandomValueBounds randomValueBounds) {
        this.entries = lootPoolEntryContainerArr;
        this.conditions = lootItemConditionArr;
        this.compositeCondition = LootItemConditions.andConditions(lootItemConditionArr);
        this.functions = lootItemFunctionArr;
        this.compositeFunction = LootItemFunctions.compose(lootItemFunctionArr);
        this.rolls = randomIntGenerator;
        this.bonusRolls = randomValueBounds;
    }

    private void addRandomItem(Consumer<ItemStack> consumer, LootContext lootContext) {
        Random random = lootContext.getRandom();
        List<LootPoolEntry> newArrayList = Lists.newArrayList();
        MutableInt mutableInt = new MutableInt();
        for (LootPoolEntryContainer lootPoolEntryContainer : this.entries) {
            lootPoolEntryContainer.expand(lootContext, lootPoolEntry -> {
                int weight = lootPoolEntry.getWeight(lootContext.getLuck());
                if (weight > 0) {
                    newArrayList.add(lootPoolEntry);
                    mutableInt.add(weight);
                }
            });
        }
        int size = newArrayList.size();
        if (mutableInt.intValue() == 0 || size == 0) {
            return;
        }
        if (size == 1) {
            newArrayList.get(0).createItemStack(consumer, lootContext);
            return;
        }
        int nextInt = random.nextInt(mutableInt.intValue());
        for (LootPoolEntry lootPoolEntry2 : newArrayList) {
            nextInt -= lootPoolEntry2.getWeight(lootContext.getLuck());
            if (nextInt < 0) {
                lootPoolEntry2.createItemStack(consumer, lootContext);
                return;
            }
        }
    }

    public void addRandomItems(Consumer<ItemStack> consumer, LootContext lootContext) {
        if (!this.compositeCondition.test(lootContext)) {
            return;
        }
        Consumer<ItemStack> decorate = LootItemFunction.decorate(this.compositeFunction, consumer, lootContext);
        Random random = lootContext.getRandom();
        int i = this.rolls.getInt(random) + Mth.floor(this.bonusRolls.getFloat(random) * lootContext.getLuck());
        for (int i2 = 0; i2 < i; i2++) {
            addRandomItem(decorate, lootContext);
        }
    }

    public void validate(ValidationContext validationContext) {
        for (int i = 0; i < this.conditions.length; i++) {
            this.conditions[i].validate(validationContext.forChild(".condition[" + i + "]"));
        }
        for (int i2 = 0; i2 < this.functions.length; i2++) {
            this.functions[i2].validate(validationContext.forChild(".functions[" + i2 + "]"));
        }
        for (int i3 = 0; i3 < this.entries.length; i3++) {
            this.entries[i3].validate(validationContext.forChild(".entries[" + i3 + "]"));
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootPool$Builder.class */
    public static class Builder implements FunctionUserBuilder<Builder>, ConditionUserBuilder<Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();
        private final List<LootItemCondition> conditions = Lists.newArrayList();
        private final List<LootItemFunction> functions = Lists.newArrayList();
        private RandomIntGenerator rolls = new RandomValueBounds(1.0f);
        private RandomValueBounds bonusRolls = new RandomValueBounds(0.0f, 0.0f);

        public Builder setRolls(RandomIntGenerator randomIntGenerator) {
            this.rolls = randomIntGenerator;
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder, net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder
        public Builder unwrap() {
            return this;
        }

        public Builder add(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add(builder.build());
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder
        public Builder when(LootItemCondition.Builder builder) {
            this.conditions.add(builder.build());
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder
        public Builder apply(LootItemFunction.Builder builder) {
            this.functions.add(builder.build());
            return this;
        }

        public LootPool build() {
            if (this.rolls == null) {
                throw new IllegalArgumentException("Rolls not set");
            }
            return new LootPool((LootPoolEntryContainer[]) this.entries.toArray(new LootPoolEntryContainer[0]), (LootItemCondition[]) this.conditions.toArray(new LootItemCondition[0]), (LootItemFunction[]) this.functions.toArray(new LootItemFunction[0]), this.rolls, this.bonusRolls);
        }
    }

    public static Builder lootPool() {
        return new Builder();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootPool$Serializer.class */
    public static class Serializer implements JsonDeserializer<LootPool>, JsonSerializer<LootPool> {
        /* renamed from: deserialize, reason: merged with bridge method [inline-methods] */
        public LootPool deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject convertToJsonObject = GsonHelper.convertToJsonObject(jsonElement, "loot pool");
            return new LootPool((LootPoolEntryContainer[]) GsonHelper.getAsObject(convertToJsonObject, "entries", jsonDeserializationContext, LootPoolEntryContainer[].class), (LootItemCondition[]) GsonHelper.getAsObject(convertToJsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class), (LootItemFunction[]) GsonHelper.getAsObject(convertToJsonObject, "functions", new LootItemFunction[0], jsonDeserializationContext, LootItemFunction[].class), RandomIntGenerators.deserialize(convertToJsonObject.get("rolls"), jsonDeserializationContext), (RandomValueBounds) GsonHelper.getAsObject(convertToJsonObject, "bonus_rolls", new RandomValueBounds(0.0f, 0.0f), jsonDeserializationContext, RandomValueBounds.class));
        }

        public JsonElement serialize(LootPool lootPool, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("rolls", RandomIntGenerators.serialize(lootPool.rolls, jsonSerializationContext));
            jsonObject.add("entries", jsonSerializationContext.serialize(lootPool.entries));
            if (lootPool.bonusRolls.getMin() != 0.0f && lootPool.bonusRolls.getMax() != 0.0f) {
                jsonObject.add("bonus_rolls", jsonSerializationContext.serialize(lootPool.bonusRolls));
            }
            if (!ArrayUtils.isEmpty(lootPool.conditions)) {
                jsonObject.add("conditions", jsonSerializationContext.serialize(lootPool.conditions));
            }
            if (!ArrayUtils.isEmpty(lootPool.functions)) {
                jsonObject.add("functions", jsonSerializationContext.serialize(lootPool.functions));
            }
            return jsonObject;
        }
    }
}
