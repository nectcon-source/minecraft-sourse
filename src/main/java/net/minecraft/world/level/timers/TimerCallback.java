package net.minecraft.world.level.timers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/timers/TimerCallback.class */
public interface TimerCallback<T> {
    void handle(T t, TimerQueue<T> timerQueue, long j);

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/timers/TimerCallback$Serializer.class */
    public static abstract class Serializer<T, C extends TimerCallback<T>> {

        /* renamed from: id */
        private final ResourceLocation id;
        private final Class<?> cls;

        public abstract void serialize(CompoundTag compoundTag, C c);

        public abstract C deserialize(CompoundTag compoundTag);

        public Serializer(ResourceLocation resourceLocation, Class<?> cls) {
            this.id = resourceLocation;
            this.cls = cls;
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public Class<?> getCls() {
            return this.cls;
        }
    }
}
