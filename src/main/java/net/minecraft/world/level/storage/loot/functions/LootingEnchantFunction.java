package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LootingEnchantFunction.class */
public class LootingEnchantFunction extends LootItemConditionalFunction {
    private final RandomValueBounds value;
    private final int limit;

    private LootingEnchantFunction(LootItemCondition[] lootItemConditionArr, RandomValueBounds randomValueBounds, int i) {
        super(lootItemConditionArr);
        this.value = randomValueBounds;
        this.limit = i;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.LOOTING_ENCHANT;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean hasLimit() {
        return this.limit > 0;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Entity entity = (Entity) lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
        if (entity instanceof LivingEntity) {
            int mobLooting = EnchantmentHelper.getMobLooting((LivingEntity) entity);
            if (mobLooting == 0) {
                return itemStack;
            }
            itemStack.grow(Math.round(mobLooting * this.value.getFloat(lootContext.getRandom())));
            if (hasLimit() && itemStack.getCount() > this.limit) {
                itemStack.setCount(this.limit);
            }
        }
        return itemStack;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LootingEnchantFunction$Builder.class */
    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final RandomValueBounds count;
        private int limit = 0;

        public Builder(RandomValueBounds randomValueBounds) {
            this.count = randomValueBounds;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder
        public Builder getThis() {
            return this;
        }

        public Builder setLimit(int i) {
            this.limit = i;
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder
        public LootItemFunction build() {
            return new LootingEnchantFunction(getConditions(), this.count, this.limit);
        }
    }

    public static Builder lootingMultiplier(RandomValueBounds randomValueBounds) {
        return new Builder(randomValueBounds);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LootingEnchantFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<LootingEnchantFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, LootingEnchantFunction lootingEnchantFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  lootingEnchantFunction, jsonSerializationContext);
            jsonObject.add("count", jsonSerializationContext.serialize(lootingEnchantFunction.value));
            if (lootingEnchantFunction.hasLimit()) {
                jsonObject.add("limit", jsonSerializationContext.serialize(Integer.valueOf(lootingEnchantFunction.limit)));
            }
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public LootingEnchantFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new LootingEnchantFunction(lootItemConditionArr, (RandomValueBounds) GsonHelper.getAsObject(jsonObject, "count", jsonDeserializationContext, RandomValueBounds.class), GsonHelper.getAsInt(jsonObject, "limit", 0));
        }
    }
}
