package net.minecraft.world.level.storage.loot;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/SerializerType.class */
public class SerializerType<T> {
    private final Serializer<? extends T> serializer;

    public SerializerType(Serializer<? extends T> serializer) {
        this.serializer = serializer;
    }

    public Serializer<? extends T> getSerializer() {
        return this.serializer;
    }
}
