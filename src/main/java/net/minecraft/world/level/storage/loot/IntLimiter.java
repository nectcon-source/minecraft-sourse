package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/IntLimiter.class */
public class IntLimiter implements IntUnaryOperator {
    private final Integer min;
    private final Integer max;

    /* renamed from: op */
    private final IntUnaryOperator op;

    private IntLimiter(@Nullable Integer num, @Nullable Integer num2) {
        this.min = num;
        this.max = num2;
        if (num == null) {
            if (num2 == null) {
                this.op = i -> {
                    return i;
                };
                return;
            } else {
                int intValue = num2.intValue();
                this.op = i2 -> {
                    return Math.min(intValue, i2);
                };
                return;
            }
        }
        int intValue2 = num.intValue();
        if (num2 == null) {
            this.op = i3 -> {
                return Math.max(intValue2, i3);
            };
        } else {
            int intValue3 = num2.intValue();
            this.op = i4 -> {
                return Mth.clamp(i4, intValue2, intValue3);
            };
        }
    }

    public static IntLimiter clamp(int i, int i2) {
        return new IntLimiter(Integer.valueOf(i), Integer.valueOf(i2));
    }

    public static IntLimiter lowerBound(int i) {
        return new IntLimiter(Integer.valueOf(i), null);
    }

    public static IntLimiter upperBound(int i) {
        return new IntLimiter(null, Integer.valueOf(i));
    }

    @Override // java.util.function.IntUnaryOperator
    public int applyAsInt(int i) {
        return this.op.applyAsInt(i);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/IntLimiter$Serializer.class */
    public static class Serializer implements JsonDeserializer<IntLimiter>, JsonSerializer<IntLimiter> {
        /* renamed from: deserialize, reason: merged with bridge method [inline-methods] */
        public IntLimiter deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject convertToJsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
            return new IntLimiter(convertToJsonObject.has("min") ? Integer.valueOf(GsonHelper.getAsInt(convertToJsonObject, "min")) : null, convertToJsonObject.has("max") ? Integer.valueOf(GsonHelper.getAsInt(convertToJsonObject, "max")) : null);
        }

        public JsonElement serialize(IntLimiter intLimiter, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (intLimiter.max != null) {
                jsonObject.addProperty("max", intLimiter.max);
            }
            if (intLimiter.min != null) {
                jsonObject.addProperty("min", intLimiter.min);
            }
            return jsonObject;
        }
    }
}
