package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LootItemEntityPropertyCondition.class */
public class LootItemEntityPropertyCondition implements LootItemCondition {
    private final EntityPredicate predicate;
    private final LootContext.EntityTarget entityTarget;

    private LootItemEntityPropertyCondition(EntityPredicate entityPredicate, LootContext.EntityTarget entityTarget) {
        this.predicate = entityPredicate;
        this.entityTarget = entityTarget;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_PROPERTIES;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN, this.entityTarget.getParam());
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        Entity entity = (Entity) lootContext.getParamOrNull(this.entityTarget.getParam());
        return this.predicate.matches(lootContext.getLevel(), (Vec3) lootContext.getParamOrNull(LootContextParams.ORIGIN), entity);
    }

    public static LootItemCondition.Builder entityPresent(LootContext.EntityTarget entityTarget) {
        return hasProperties(entityTarget, EntityPredicate.Builder.entity());
    }

    public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget entityTarget, EntityPredicate.Builder builder) {
        return () -> {
            return new LootItemEntityPropertyCondition(builder.build(), entityTarget);
        };
    }

    public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget entityTarget, EntityPredicate entityPredicate) {
        return () -> {
            return new LootItemEntityPropertyCondition(entityPredicate, entityTarget);
        };
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LootItemEntityPropertyCondition$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemEntityPropertyCondition> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, LootItemEntityPropertyCondition lootItemEntityPropertyCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("predicate", lootItemEntityPropertyCondition.predicate.serializeToJson());
            jsonObject.add("entity", jsonSerializationContext.serialize(lootItemEntityPropertyCondition.entityTarget));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public LootItemEntityPropertyCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootItemEntityPropertyCondition(EntityPredicate.fromJson(jsonObject.get("predicate")), (LootContext.EntityTarget) GsonHelper.getAsObject(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class));
        }
    }
}
