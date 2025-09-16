package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/WeatherCheck.class */
public class WeatherCheck implements LootItemCondition {

    @Nullable
    private final Boolean isRaining;

    @Nullable
    private final Boolean isThundering;

    private WeatherCheck(@Nullable Boolean bool, @Nullable Boolean bool2) {
        this.isRaining = bool;
        this.isThundering = bool2;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.WEATHER_CHECK;
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        ServerLevel level = lootContext.getLevel();
        if (this.isRaining != null && this.isRaining.booleanValue() != level.isRaining()) {
            return false;
        }
        if (this.isThundering != null && this.isThundering.booleanValue() != level.isThundering()) {
            return false;
        }
        return true;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/WeatherCheck$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<WeatherCheck> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, WeatherCheck weatherCheck, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("raining", weatherCheck.isRaining);
            jsonObject.addProperty("thundering", weatherCheck.isThundering);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public WeatherCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new WeatherCheck(jsonObject.has("raining") ? Boolean.valueOf(GsonHelper.getAsBoolean(jsonObject, "raining")) : null, jsonObject.has("thundering") ? Boolean.valueOf(GsonHelper.getAsBoolean(jsonObject, "thundering")) : null);
        }
    }
}
