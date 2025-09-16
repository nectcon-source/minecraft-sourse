package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/GsonAdapterFactory.class */
public class GsonAdapterFactory {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/GsonAdapterFactory$DefaultSerializer.class */
    public interface DefaultSerializer<T> {
        JsonElement serialize(T t, JsonSerializationContext jsonSerializationContext);

        T deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/GsonAdapterFactory$Builder.class */
    public static class Builder<E, T extends SerializerType<E>> {
        private final Registry<T> registry;
        private final String elementName;
        private final String typeKey;
        private final Function<E, T> typeGetter;

        @Nullable
        private Pair<T, DefaultSerializer<? extends E>> defaultType;

        private Builder(Registry<T> registry, String str, String str2, Function<E, T> function) {
            this.registry = registry;
            this.elementName = str;
            this.typeKey = str2;
            this.typeGetter = function;
        }

        public Object build() {
            return new JsonAdapter(this.registry, this.elementName, this.typeKey, this.typeGetter, this.defaultType);
        }
    }

    public static <E, T extends SerializerType<E>> Builder<E, T> builder(Registry<T> registry, String str, String str2, Function<E, T> function) {
        return new Builder<>(registry, str, str2, function);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/GsonAdapterFactory$JsonAdapter.class */
    static class JsonAdapter<E, T extends SerializerType<E>> implements JsonDeserializer<E>, JsonSerializer<E> {
        private final Registry<T> registry;
        private final String elementName;
        private final String typeKey;
        private final Function<E, T> typeGetter;

        @Nullable
        private final Pair<T, DefaultSerializer<? extends E>> defaultType;

        private JsonAdapter(Registry<T> registry, String str, String str2, Function<E, T> function, @Nullable Pair<T, DefaultSerializer<? extends E>> pair) {
            this.registry = registry;
            this.elementName = str;
            this.typeKey = str2;
            this.typeGetter = function;
            this.defaultType = pair;
        }

        public E deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                JsonObject convertToJsonObject = GsonHelper.convertToJsonObject(jsonElement, this.elementName);
                ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(convertToJsonObject, this.typeKey));
                T t = this.registry.get(resourceLocation);
                if (t == null) {
                    throw new JsonSyntaxException("Unknown type '" + resourceLocation + "'");
                }
                return (E) t.getSerializer().deserialize(convertToJsonObject, jsonDeserializationContext);
            }
            if (this.defaultType == null) {
                throw new UnsupportedOperationException("Object " + jsonElement + " can't be deserialized");
            }
            return (E) ((DefaultSerializer) this.defaultType.getSecond()).deserialize(jsonElement, jsonDeserializationContext);
        }

//        public JsonElement serialize(E e, Type type, JsonSerializationContext jsonSerializationContext) {
//            T apply = this.typeGetter.apply(e);
//            if (this.defaultType != null && this.defaultType.getFirst() == apply) {
//                return ((DefaultSerializer) this.defaultType.getSecond()).serialize(e, jsonSerializationContext);
//            }
//            if (apply == null) {
//                throw new JsonSyntaxException("Unknown type: " + e);
//            }
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty(this.typeKey, this.registry.getKey(apply).toString());
//            apply.getSerializer().serialize(jsonObject, e, jsonSerializationContext);
//            return jsonObject;
//        }
public JsonElement serialize(E e, Type type, JsonSerializationContext jsonSerializationContext) {
    T apply = this.typeGetter.apply(e);
    if (this.defaultType != null && this.defaultType.getFirst() == apply) {
        return ((DefaultSerializer<E>) this.defaultType.getSecond()).serialize(e, jsonSerializationContext);
    }
    if (apply == null) {
        throw new JsonSyntaxException("Unknown type: " + e);
    }
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(this.typeKey, this.registry.getKey(apply).toString());
    ((Serializer<E>) apply.getSerializer()).serialize(jsonObject, e, jsonSerializationContext);
    return jsonObject;
}
    }
}
