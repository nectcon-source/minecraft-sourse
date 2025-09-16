package net.minecraft.world.entity.ai.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/memory/ExpirableValue.class */
public class ExpirableValue<T> {
    private final T value;
    private long timeToLive;

    public ExpirableValue(T t, long j) {
        this.value = t;
        this.timeToLive = j;
    }

    public void tick() {
        if (canExpire()) {
            this.timeToLive--;
        }
    }

    /* renamed from: of */
    public static <T> ExpirableValue<T> of(T t) {
        return new ExpirableValue<>(t, Long.MAX_VALUE);
    }

    /* renamed from: of */
    public static <T> ExpirableValue<T> of(T t, long j) {
        return new ExpirableValue<>(t, j);
    }

    public T getValue() {
        return this.value;
    }

    public boolean hasExpired() {
        return this.timeToLive <= 0;
    }

    public String toString() {
        return this.value.toString() + (canExpire() ? " (ttl: " + this.timeToLive + ")" : "");
    }

    public boolean canExpire() {
        return this.timeToLive != Long.MAX_VALUE;
    }

    public static <T> Codec<ExpirableValue<T>> codec(Codec<T> codec) {
        return RecordCodecBuilder.create(instance -> {
            return instance.group(codec.fieldOf("value").forGetter(expirableValue -> {
                return expirableValue.value;
            }), Codec.LONG.optionalFieldOf("ttl").forGetter(expirableValue2 -> {
                return expirableValue2.canExpire() ? Optional.of(Long.valueOf(expirableValue2.timeToLive)) : Optional.empty();
            })).apply(instance, (obj, optional) -> {
                return new ExpirableValue(obj, ((Long) optional.orElse(Long.MAX_VALUE)).longValue());
            });
        });
    }
}
