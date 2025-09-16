package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/RandomValueBounds.class */
public class RandomValueBounds implements RandomIntGenerator {
    private final float min;
    private final float max;

    public RandomValueBounds(float f, float f2) {
        this.min = f;
        this.max = f2;
    }

    public RandomValueBounds(float f) {
        this.min = f;
        this.max = f;
    }

    public static RandomValueBounds between(float f, float f2) {
        return new RandomValueBounds(f, f2);
    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }

    @Override // net.minecraft.world.level.storage.loot.RandomIntGenerator
    public int getInt(Random random) {
        return Mth.nextInt(random, Mth.floor(this.min), Mth.floor(this.max));
    }

    public float getFloat(Random random) {
        return Mth.nextFloat(random, this.min, this.max);
    }

    public boolean matchesValue(int i) {
        return ((float) i) <= this.max && ((float) i) >= this.min;
    }

    @Override // net.minecraft.world.level.storage.loot.RandomIntGenerator
    public ResourceLocation getType() {
        return UNIFORM;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/RandomValueBounds$Serializer.class */
    public static class Serializer implements JsonDeserializer<RandomValueBounds>, JsonSerializer<RandomValueBounds> {
        /* renamed from: deserialize, reason: merged with bridge method [inline-methods] */
        public RandomValueBounds deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (GsonHelper.isNumberValue(jsonElement)) {
                return new RandomValueBounds(GsonHelper.convertToFloat(jsonElement, "value"));
            }
            JsonObject convertToJsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
            return new RandomValueBounds(GsonHelper.getAsFloat(convertToJsonObject, "min"), GsonHelper.getAsFloat(convertToJsonObject, "max"));
        }

        public JsonElement serialize(RandomValueBounds randomValueBounds, Type type, JsonSerializationContext jsonSerializationContext) {
            if (randomValueBounds.min == randomValueBounds.max) {
                return new JsonPrimitive(Float.valueOf(randomValueBounds.min));
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("min", Float.valueOf(randomValueBounds.min));
            jsonObject.addProperty("max", Float.valueOf(randomValueBounds.max));
            return jsonObject;
        }
    }
}
