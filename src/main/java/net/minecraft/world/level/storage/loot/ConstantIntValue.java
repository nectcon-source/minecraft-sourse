package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/ConstantIntValue.class */
public final class ConstantIntValue implements RandomIntGenerator {
    private final int value;

    public ConstantIntValue(int i) {
        this.value = i;
    }

    @Override // net.minecraft.world.level.storage.loot.RandomIntGenerator
    public int getInt(Random random) {
        return this.value;
    }

    @Override // net.minecraft.world.level.storage.loot.RandomIntGenerator
    public ResourceLocation getType() {
        return CONSTANT;
    }

    public static ConstantIntValue exactly(int i) {
        return new ConstantIntValue(i);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/ConstantIntValue$Serializer.class */
    public static class Serializer implements JsonDeserializer<ConstantIntValue>, JsonSerializer<ConstantIntValue> {
        /* renamed from: deserialize, reason: merged with bridge method [inline-methods] */
        public ConstantIntValue deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return new ConstantIntValue(GsonHelper.convertToInt(jsonElement, "value"));
        }

        public JsonElement serialize(ConstantIntValue constantIntValue, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(Integer.valueOf(constantIntValue.value));
        }
    }
}
