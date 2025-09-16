package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/BinomialDistributionGenerator.class */
public final class BinomialDistributionGenerator implements RandomIntGenerator {

    /* renamed from: n */
    private final int n;

    /* renamed from: p */
    private final float p;

    public BinomialDistributionGenerator(int i, float f) {
        this.n = i;
        this.p = f;
    }

    @Override // net.minecraft.world.level.storage.loot.RandomIntGenerator
    public int getInt(Random random) {
        int i = 0;
        for (int i2 = 0; i2 < this.n; i2++) {
            if (random.nextFloat() < this.p) {
                i++;
            }
        }
        return i;
    }

    public static BinomialDistributionGenerator binomial(int i, float f) {
        return new BinomialDistributionGenerator(i, f);
    }

    @Override // net.minecraft.world.level.storage.loot.RandomIntGenerator
    public ResourceLocation getType() {
        return BINOMIAL;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/BinomialDistributionGenerator$Serializer.class */
    public static class Serializer implements JsonDeserializer<BinomialDistributionGenerator>, JsonSerializer<BinomialDistributionGenerator> {
        /* renamed from: deserialize, reason: merged with bridge method [inline-methods] */
        public BinomialDistributionGenerator deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject convertToJsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
            return new BinomialDistributionGenerator(GsonHelper.getAsInt(convertToJsonObject, "n"), GsonHelper.getAsFloat(convertToJsonObject, "p"));
        }

        public JsonElement serialize(BinomialDistributionGenerator binomialDistributionGenerator, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("n", Integer.valueOf(binomialDistributionGenerator.n));
            jsonObject.addProperty("p", Float.valueOf(binomialDistributionGenerator.p));
            return jsonObject;
        }
    }
}
