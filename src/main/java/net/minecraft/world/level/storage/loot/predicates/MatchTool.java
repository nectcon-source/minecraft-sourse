package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/MatchTool.class */
public class MatchTool implements LootItemCondition {
    private final ItemPredicate predicate;

    public MatchTool(ItemPredicate itemPredicate) {
        this.predicate = itemPredicate;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.MATCH_TOOL;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = (ItemStack) lootContext.getParamOrNull(LootContextParams.TOOL);
        return itemStack != null && this.predicate.matches(itemStack);
    }

    public static LootItemCondition.Builder toolMatches(ItemPredicate.Builder builder) {
        return () -> {
            return new MatchTool(builder.build());
        };
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/MatchTool$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<MatchTool> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, MatchTool matchTool, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("predicate", matchTool.predicate.serializeToJson());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public MatchTool deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new MatchTool(ItemPredicate.fromJson(jsonObject.get("predicate")));
        }
    }
}
