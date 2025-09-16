package net.minecraft.world.level.storage.loot;

import com.google.gson.GsonBuilder;
import net.minecraft.world.level.storage.loot.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.IntLimiter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/Deserializers.class */
public class Deserializers {
    public static GsonBuilder createConditionSerializer() {
        return new GsonBuilder().registerTypeAdapter(RandomValueBounds.class, new RandomValueBounds.Serializer()).registerTypeAdapter(BinomialDistributionGenerator.class, new BinomialDistributionGenerator.Serializer()).registerTypeAdapter(ConstantIntValue.class, new ConstantIntValue.Serializer()).registerTypeHierarchyAdapter(LootItemCondition.class, LootItemConditions.createGsonAdapter()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer());
    }

    public static GsonBuilder createFunctionSerializer() {
        return createConditionSerializer().registerTypeAdapter(IntLimiter.class, new IntLimiter.Serializer()).registerTypeHierarchyAdapter(LootPoolEntryContainer.class, LootPoolEntries.createGsonAdapter()).registerTypeHierarchyAdapter(LootItemFunction.class, LootItemFunctions.createGsonAdapter());
    }

    public static GsonBuilder createLootTableSerializer() {
        return createFunctionSerializer().registerTypeAdapter(LootPool.class, new LootPool.Serializer()).registerTypeAdapter(LootTable.class, new LootTable.Serializer());
    }
}
