package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/EntityHasScoreCondition.class */
public class EntityHasScoreCondition implements LootItemCondition {
    private final Map<String, RandomValueBounds> scores;
    private final LootContext.EntityTarget entityTarget;

    private EntityHasScoreCondition(Map<String, RandomValueBounds> map, LootContext.EntityTarget entityTarget) {
        this.scores = ImmutableMap.copyOf(map);
        this.entityTarget = entityTarget;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_SCORES;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        Entity entity = (Entity) lootContext.getParamOrNull(this.entityTarget.getParam());
        if (entity == null) {
            return false;
        }
        Scoreboard scoreboard = entity.level.getScoreboard();
        for (Map.Entry<String, RandomValueBounds> entry : this.scores.entrySet()) {
            if (!hasScore(entity, scoreboard, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    protected boolean hasScore(Entity entity, Scoreboard scoreboard, String str, RandomValueBounds randomValueBounds) {
        Objective objective = scoreboard.getObjective(str);
        if (objective == null) {
            return false;
        }
        String scoreboardName = entity.getScoreboardName();
        if (!scoreboard.hasPlayerScore(scoreboardName, objective)) {
            return false;
        }
        return randomValueBounds.matchesValue(scoreboard.getOrCreatePlayerScore(scoreboardName, objective).getScore());
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/EntityHasScoreCondition$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityHasScoreCondition> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, EntityHasScoreCondition entityHasScoreCondition, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject2 = new JsonObject();
            for (Map.Entry<String, RandomValueBounds> entry : entityHasScoreCondition.scores.entrySet()) {
                jsonObject2.add(entry.getKey(), jsonSerializationContext.serialize(entry.getValue()));
            }
            jsonObject.add("scores", jsonObject2);
            jsonObject.add("entity", jsonSerializationContext.serialize(entityHasScoreCondition.entityTarget));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public EntityHasScoreCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            Set<Map.Entry<String, JsonElement>> entrySet = GsonHelper.getAsJsonObject(jsonObject, "scores").entrySet();
            LinkedHashMap newLinkedHashMap = Maps.newLinkedHashMap();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                newLinkedHashMap.put(entry.getKey(), GsonHelper.convertToObject(entry.getValue(), "score", jsonDeserializationContext, RandomValueBounds.class));
            }
            return new EntityHasScoreCondition(newLinkedHashMap, (LootContext.EntityTarget) GsonHelper.getAsObject(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class));
        }
    }
}
