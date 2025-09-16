package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/RandomIntGenerators.class */
public class RandomIntGenerators {
    private static final Map<ResourceLocation, Class<? extends RandomIntGenerator>> GENERATORS = Maps.newHashMap();

    static {
        GENERATORS.put(RandomIntGenerator.UNIFORM, RandomValueBounds.class);
        GENERATORS.put(RandomIntGenerator.BINOMIAL, BinomialDistributionGenerator.class);
        GENERATORS.put(RandomIntGenerator.CONSTANT, ConstantIntValue.class);
    }

    public static RandomIntGenerator deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonPrimitive()) {
            return (RandomIntGenerator) jsonDeserializationContext.deserialize(jsonElement, ConstantIntValue.class);
        }
        JsonObject asJsonObject = jsonElement.getAsJsonObject();
        String asString = GsonHelper.getAsString(asJsonObject, "type", RandomIntGenerator.UNIFORM.toString());
        Class<? extends RandomIntGenerator> cls = GENERATORS.get(new ResourceLocation(asString));
        if (cls == null) {
            throw new JsonParseException("Unknown generator: " + asString);
        }
        return (RandomIntGenerator) jsonDeserializationContext.deserialize(asJsonObject, cls);
    }

    public static JsonElement serialize(RandomIntGenerator randomIntGenerator, JsonSerializationContext jsonSerializationContext) {
        JsonElement serialize = jsonSerializationContext.serialize(randomIntGenerator);
        if (serialize.isJsonObject()) {
            serialize.getAsJsonObject().addProperty("type", randomIntGenerator.getType().toString());
        }
        return serialize;
    }
}
