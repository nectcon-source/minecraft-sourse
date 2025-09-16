package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/AlternativeLootItemCondition.class */
public class AlternativeLootItemCondition implements LootItemCondition {
    private final LootItemCondition[] terms;
    private final Predicate<LootContext> composedPredicate;

    private AlternativeLootItemCondition(LootItemCondition[] lootItemConditionArr) {
        this.terms = lootItemConditionArr;
        this.composedPredicate = LootItemConditions.orConditions(lootItemConditionArr);
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.ALTERNATIVE;
    }

    @Override // java.util.function.Predicate
    public final boolean test(LootContext lootContext) {
        return this.composedPredicate.test(lootContext);
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public void validate(ValidationContext validationContext) {
        LootItemCondition.super.validate(validationContext);
        for (int i = 0; i < this.terms.length; i++) {
            this.terms[i].validate(validationContext.forChild(".term[" + i + "]"));
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/AlternativeLootItemCondition$Builder.class */
    public static class Builder implements LootItemCondition.Builder {
        private final List<LootItemCondition> terms = Lists.newArrayList();

        public Builder(LootItemCondition.Builder... builderArr) {
            for (LootItemCondition.Builder builder : builderArr) {
                this.terms.add(builder.build());
            }
        }

        @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder
        /* renamed from: or */
        public Builder or(LootItemCondition.Builder builder) {
            this.terms.add(builder.build());
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder
        public LootItemCondition build() {
            return new AlternativeLootItemCondition((LootItemCondition[]) this.terms.toArray(new LootItemCondition[0]));
        }
    }

    public static Builder alternative(LootItemCondition.Builder... builderArr) {
        return new Builder(builderArr);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/AlternativeLootItemCondition$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<AlternativeLootItemCondition> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, AlternativeLootItemCondition alternativeLootItemCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("terms", jsonSerializationContext.serialize(alternativeLootItemCondition.terms));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public AlternativeLootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new AlternativeLootItemCondition((LootItemCondition[]) GsonHelper.getAsObject(jsonObject, "terms", jsonDeserializationContext, LootItemCondition[].class));
        }
    }
}
