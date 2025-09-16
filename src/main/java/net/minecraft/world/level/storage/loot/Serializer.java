package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/Serializer.class */
public interface Serializer<T> {
    void serialize(JsonObject jsonObject, T t, JsonSerializationContext jsonSerializationContext);

    T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext);
}
