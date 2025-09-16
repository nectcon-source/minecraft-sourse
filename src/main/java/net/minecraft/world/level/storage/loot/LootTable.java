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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootTable.class */
public class LootTable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, new LootPool[0], new LootItemFunction[0]);
    public static final LootContextParamSet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
    private final LootContextParamSet paramSet;
    private final LootPool[] pools;
    private final LootItemFunction[] functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

    private LootTable(LootContextParamSet lootContextParamSet, LootPool[] lootPoolArr, LootItemFunction[] lootItemFunctionArr) {
        this.paramSet = lootContextParamSet;
        this.pools = lootPoolArr;
        this.functions = lootItemFunctionArr;
        this.compositeFunction = LootItemFunctions.compose(lootItemFunctionArr);
    }

    public static Consumer<ItemStack> createStackSplitter(Consumer<ItemStack> consumer) {
        return itemStack -> {
            if (itemStack.getCount() < itemStack.getMaxStackSize()) {
                consumer.accept(itemStack);
                return;
            }
            int count = itemStack.getCount();
            while (count > 0) {
                ItemStack copy = itemStack.copy();
                copy.setCount(Math.min(itemStack.getMaxStackSize(), count));
                count -= copy.getCount();
                consumer.accept(copy);
            }
        };
    }

    public void getRandomItemsRaw(LootContext lootContext, Consumer<ItemStack> consumer) {
        if (lootContext.addVisitedTable(this)) {
            Consumer<ItemStack> decorate = LootItemFunction.decorate(this.compositeFunction, consumer, lootContext);
            for (LootPool lootPool : this.pools) {
                lootPool.addRandomItems(decorate, lootContext);
            }
            lootContext.removeVisitedTable(this);
            return;
        }
        LOGGER.warn("Detected infinite loop in loot tables");
    }

    public void getRandomItems(LootContext lootContext, Consumer<ItemStack> consumer) {
        getRandomItemsRaw(lootContext, createStackSplitter(consumer));
    }

    public List<ItemStack> getRandomItems(LootContext lootContext) {
        List<ItemStack> var2 = Lists.newArrayList();
        this.getRandomItems(lootContext, var2::add);
        return var2;
    }

    public LootContextParamSet getParamSet() {
        return this.paramSet;
    }

    public void validate(ValidationContext validationContext) {
        for (int i = 0; i < this.pools.length; i++) {
            this.pools[i].validate(validationContext.forChild(".pools[" + i + "]"));
        }
        for (int i2 = 0; i2 < this.functions.length; i2++) {
            this.functions[i2].validate(validationContext.forChild(".functions[" + i2 + "]"));
        }
    }

    public void fill(Container container, LootContext lootContext) {
        List<ItemStack> randomItems = getRandomItems(lootContext);
        Random random = lootContext.getRandom();
        List<Integer> availableSlots = getAvailableSlots(container, random);
        shuffleAndSplitItems(randomItems, availableSlots.size(), random);
        for (ItemStack itemStack : randomItems) {
            if (availableSlots.isEmpty()) {
                LOGGER.warn("Tried to over-fill a container");
                return;
            } else if (itemStack.isEmpty()) {
                container.setItem(availableSlots.remove(availableSlots.size() - 1).intValue(), ItemStack.EMPTY);
            } else {
                container.setItem(availableSlots.remove(availableSlots.size() - 1).intValue(), itemStack);
            }
        }
    }

    private void shuffleAndSplitItems(List<ItemStack> list, int i, Random random) {
        List<ItemStack> newArrayList = Lists.newArrayList();
        Iterator<ItemStack> it = list.iterator();
        while (it.hasNext()) {
            ItemStack next = it.next();
            if (next.isEmpty()) {
                it.remove();
            } else if (next.getCount() > 1) {
                newArrayList.add(next);
                it.remove();
            }
        }
        while ((i - list.size()) - newArrayList.size() > 0 && !newArrayList.isEmpty()) {
            ItemStack remove = newArrayList.remove(Mth.nextInt(random, 0, newArrayList.size() - 1));
            ItemStack split = remove.split(Mth.nextInt(random, 1, remove.getCount() / 2));
            if (remove.getCount() > 1 && random.nextBoolean()) {
                newArrayList.add(remove);
            } else {
                list.add(remove);
            }
            if (split.getCount() > 1 && random.nextBoolean()) {
                newArrayList.add(split);
            } else {
                list.add(split);
            }
        }
        list.addAll(newArrayList);
        Collections.shuffle(list, random);
    }

    private List<Integer> getAvailableSlots(Container container, Random random) {
        List<Integer> newArrayList = Lists.newArrayList();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).isEmpty()) {
                newArrayList.add(Integer.valueOf(i));
            }
        }
        Collections.shuffle(newArrayList, random);
        return newArrayList;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootTable$Builder.class */
    public static class Builder implements FunctionUserBuilder<Builder> {
        private final List<LootPool> pools = Lists.newArrayList();
        private final List<LootItemFunction> functions = Lists.newArrayList();
        private LootContextParamSet paramSet = LootTable.DEFAULT_PARAM_SET;

        public Builder withPool(LootPool.Builder builder) {
            this.pools.add(builder.build());
            return this;
        }

        public Builder setParamSet(LootContextParamSet lootContextParamSet) {
            this.paramSet = lootContextParamSet;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder
        public Builder apply(LootItemFunction.Builder builder) {
            this.functions.add(builder.build());
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder, net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder
        public Builder unwrap() {
            return this;
        }

        public LootTable build() {
            return new LootTable(this.paramSet, (LootPool[]) this.pools.toArray(new LootPool[0]), (LootItemFunction[]) this.functions.toArray(new LootItemFunction[0]));
        }
    }

    public static Builder lootTable() {
        return new Builder();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootTable$Serializer.class */
    public static class Serializer implements JsonDeserializer<LootTable>, JsonSerializer<LootTable> {
        /* renamed from: deserialize, reason: merged with bridge method [inline-methods] */
        public LootTable deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject convertToJsonObject = GsonHelper.convertToJsonObject(jsonElement, "loot table");
            LootPool[] lootPoolArr = (LootPool[]) GsonHelper.getAsObject(convertToJsonObject, "pools", new LootPool[0], jsonDeserializationContext, LootPool[].class);
            LootContextParamSet lootContextParamSet = null;
            if (convertToJsonObject.has("type")) {
                lootContextParamSet = LootContextParamSets.get(new ResourceLocation(GsonHelper.getAsString(convertToJsonObject, "type")));
            }
            return new LootTable(lootContextParamSet != null ? lootContextParamSet : LootContextParamSets.ALL_PARAMS, lootPoolArr, (LootItemFunction[]) GsonHelper.getAsObject(convertToJsonObject, "functions", new LootItemFunction[0], jsonDeserializationContext, LootItemFunction[].class));
        }

        public JsonElement serialize(LootTable lootTable, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (lootTable.paramSet != LootTable.DEFAULT_PARAM_SET) {
                ResourceLocation key = LootContextParamSets.getKey(lootTable.paramSet);
                if (key == null) {
                    LootTable.LOGGER.warn("Failed to find id for param set " + lootTable.paramSet);
                } else {
                    jsonObject.addProperty("type", key.toString());
                }
            }
            if (lootTable.pools.length > 0) {
                jsonObject.add("pools", jsonSerializationContext.serialize(lootTable.pools));
            }
            if (!ArrayUtils.isEmpty(lootTable.functions)) {
                jsonObject.add("functions", jsonSerializationContext.serialize(lootTable.functions));
            }
            return jsonObject;
        }
    }
}
