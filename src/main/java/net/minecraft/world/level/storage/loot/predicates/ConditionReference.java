package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/ConditionReference.class */
public class ConditionReference implements LootItemCondition {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceLocation name;

    private ConditionReference(ResourceLocation resourceLocation) {
        this.name = resourceLocation;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.REFERENCE;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public void validate(ValidationContext validationContext) {
        if (validationContext.hasVisitedCondition(this.name)) {
            validationContext.reportProblem("Condition " + this.name + " is recursively called");
            return;
        }
        LootItemCondition.super.validate(validationContext);
        LootItemCondition resolveCondition = validationContext.resolveCondition(this.name);
        if (resolveCondition == null) {
            validationContext.reportProblem("Unknown condition table called " + this.name);
        } else {
            resolveCondition.validate(validationContext.enterTable(".{" + this.name + "}", this.name));
        }
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        LootItemCondition condition = lootContext.getCondition(this.name);
        if (lootContext.addVisitedCondition(condition)) {
            try {
                boolean test = condition.test(lootContext);
                lootContext.removeVisitedCondition(condition);
                return test;
            } catch (Throwable th) {
                lootContext.removeVisitedCondition(condition);
                throw th;
            }
        }
        LOGGER.warn("Detected infinite loop in loot tables");
        return false;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/ConditionReference$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConditionReference> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, ConditionReference conditionReference, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("name", conditionReference.name.toString());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public ConditionReference deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new ConditionReference(new ResourceLocation(GsonHelper.getAsString(jsonObject, "name")));
        }
    }
}
