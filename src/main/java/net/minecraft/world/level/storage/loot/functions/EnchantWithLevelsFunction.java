package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomIntGenerators;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/EnchantWithLevelsFunction.class */
public class EnchantWithLevelsFunction extends LootItemConditionalFunction {
    private final RandomIntGenerator levels;
    private final boolean treasure;

    private EnchantWithLevelsFunction(LootItemCondition[] lootItemConditionArr, RandomIntGenerator randomIntGenerator, boolean z) {
        super(lootItemConditionArr);
        this.levels = randomIntGenerator;
        this.treasure = z;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.ENCHANT_WITH_LEVELS;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Random random = lootContext.getRandom();
        return EnchantmentHelper.enchantItem(random, itemStack, this.levels.getInt(random), this.treasure);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/EnchantWithLevelsFunction$Builder.class */
    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final RandomIntGenerator levels;
        private boolean treasure;

        public Builder(RandomIntGenerator randomIntGenerator) {
            this.levels = randomIntGenerator;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder
        public Builder getThis() {
            return this;
        }

        public Builder allowTreasure() {
            this.treasure = true;
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder
        public LootItemFunction build() {
            return new EnchantWithLevelsFunction(getConditions(), this.levels, this.treasure);
        }
    }

    public static Builder enchantWithLevels(RandomIntGenerator randomIntGenerator) {
        return new Builder(randomIntGenerator);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/EnchantWithLevelsFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantWithLevelsFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, EnchantWithLevelsFunction enchantWithLevelsFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  enchantWithLevelsFunction, jsonSerializationContext);
            jsonObject.add("levels", RandomIntGenerators.serialize(enchantWithLevelsFunction.levels, jsonSerializationContext));
            jsonObject.addProperty("treasure", Boolean.valueOf(enchantWithLevelsFunction.treasure));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public EnchantWithLevelsFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new EnchantWithLevelsFunction(lootItemConditionArr, RandomIntGenerators.deserialize(jsonObject.get("levels"), jsonDeserializationContext), GsonHelper.getAsBoolean(jsonObject, "treasure", false));
        }
    }
}
