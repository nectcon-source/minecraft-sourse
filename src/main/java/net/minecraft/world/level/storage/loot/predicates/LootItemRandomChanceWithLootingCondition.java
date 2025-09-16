package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LootItemRandomChanceWithLootingCondition.class */
public class LootItemRandomChanceWithLootingCondition implements LootItemCondition {
    private final float percent;
    private final float lootingMultiplier;

    private LootItemRandomChanceWithLootingCondition(float f, float f2) {
        this.percent = f;
        this.lootingMultiplier = f2;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE_WITH_LOOTING;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        Entity entity = (Entity) lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
        int i = 0;
        if (entity instanceof LivingEntity) {
            i = EnchantmentHelper.getMobLooting((LivingEntity) entity);
        }
        return lootContext.getRandom().nextFloat() < this.percent + (((float) i) * this.lootingMultiplier);
    }

    public static LootItemCondition.Builder randomChanceAndLootingBoost(float f, float f2) {
        return () -> {
            return new LootItemRandomChanceWithLootingCondition(f, f2);
        };
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LootItemRandomChanceWithLootingCondition$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemRandomChanceWithLootingCondition> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, LootItemRandomChanceWithLootingCondition lootItemRandomChanceWithLootingCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("chance", Float.valueOf(lootItemRandomChanceWithLootingCondition.percent));
            jsonObject.addProperty("looting_multiplier", Float.valueOf(lootItemRandomChanceWithLootingCondition.lootingMultiplier));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public LootItemRandomChanceWithLootingCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootItemRandomChanceWithLootingCondition(GsonHelper.getAsFloat(jsonObject, "chance"), GsonHelper.getAsFloat(jsonObject, "looting_multiplier"));
        }
    }
}
