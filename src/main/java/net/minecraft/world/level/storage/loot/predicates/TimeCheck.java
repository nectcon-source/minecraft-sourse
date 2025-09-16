package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/TimeCheck.class */
public class TimeCheck implements LootItemCondition {

    @Nullable
    private final Long period;
    private final RandomValueBounds value;

    private TimeCheck(@Nullable Long l, RandomValueBounds randomValueBounds) {
        this.period = l;
        this.value = randomValueBounds;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.TIME_CHECK;
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        long dayTime = lootContext.getLevel().getDayTime();
        if (this.period != null) {
            dayTime %= this.period.longValue();
        }
        return this.value.matchesValue((int) dayTime);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/TimeCheck$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<TimeCheck> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, TimeCheck timeCheck, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("period", timeCheck.period);
            jsonObject.add("value", jsonSerializationContext.serialize(timeCheck.value));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public TimeCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new TimeCheck(jsonObject.has("period") ? Long.valueOf(GsonHelper.getAsLong(jsonObject, "period")) : null, (RandomValueBounds) GsonHelper.getAsObject(jsonObject, "value", jsonDeserializationContext, RandomValueBounds.class));
        }
    }
}
