package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/InvertedLootItemCondition.class */
public class InvertedLootItemCondition implements LootItemCondition {
    private final LootItemCondition term;

    private InvertedLootItemCondition(LootItemCondition lootItemCondition) {
        this.term = lootItemCondition;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.INVERTED;
    }

    @Override // java.util.function.Predicate
    public final boolean test(LootContext lootContext) {
        return !this.term.test(lootContext);
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.term.getReferencedContextParams();
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public void validate(ValidationContext validationContext) {
        LootItemCondition.super.validate(validationContext);
        this.term.validate(validationContext);
    }

    public static LootItemCondition.Builder invert(LootItemCondition.Builder builder) {
        InvertedLootItemCondition invertedLootItemCondition = new InvertedLootItemCondition(builder.build());
        return () -> {
            return invertedLootItemCondition;
        };
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/InvertedLootItemCondition$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<InvertedLootItemCondition> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, InvertedLootItemCondition invertedLootItemCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("term", jsonSerializationContext.serialize(invertedLootItemCondition.term));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public InvertedLootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new InvertedLootItemCondition((LootItemCondition) GsonHelper.getAsObject(jsonObject, "term", jsonDeserializationContext, LootItemCondition.class));
        }
    }
}
