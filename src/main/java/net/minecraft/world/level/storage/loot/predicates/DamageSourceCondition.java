package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/DamageSourceCondition.class */
public class DamageSourceCondition implements LootItemCondition {
    private final DamageSourcePredicate predicate;

    private DamageSourceCondition(DamageSourcePredicate damageSourcePredicate) {
        this.predicate = damageSourcePredicate;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.DAMAGE_SOURCE_PROPERTIES;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN, LootContextParams.DAMAGE_SOURCE);
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        DamageSource damageSource = (DamageSource) lootContext.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
        Vec3 vec3 = (Vec3) lootContext.getParamOrNull(LootContextParams.ORIGIN);
        return (vec3 == null || damageSource == null || !this.predicate.matches(lootContext.getLevel(), vec3, damageSource)) ? false : true;
    }

    public static LootItemCondition.Builder hasDamageSource(DamageSourcePredicate.Builder builder) {
        return () -> {
            return new DamageSourceCondition(builder.build());
        };
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/DamageSourceCondition$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<DamageSourceCondition> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, DamageSourceCondition damageSourceCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("predicate", damageSourceCondition.predicate.serializeToJson());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public DamageSourceCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new DamageSourceCondition(DamageSourcePredicate.fromJson(jsonObject.get("predicate")));
        }
    }
}
