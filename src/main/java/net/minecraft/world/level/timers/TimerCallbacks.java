package net.minecraft.world.level.timers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;
import net.minecraft.world.level.timers.TimerCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/timers/TimerCallbacks.class */
public class TimerCallbacks<C> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final TimerCallbacks<MinecraftServer> SERVER_CALLBACKS = new TimerCallbacks().register(new FunctionCallback.Serializer()).register(new FunctionTagCallback.Serializer());
    private final Map<ResourceLocation, TimerCallback.Serializer<C, ?>> idToSerializer = Maps.newHashMap();
    private final Map<Class<?>, TimerCallback.Serializer<C, ?>> classToSerializer = Maps.newHashMap();

    @VisibleForTesting
    public TimerCallbacks() {
    }

    public TimerCallbacks<C> register(TimerCallback.Serializer<C, ?> serializer) {
        this.idToSerializer.put(serializer.getId(), serializer);
        this.classToSerializer.put(serializer.getCls(), serializer);
        return this;
    }

    private <T extends TimerCallback<C>> TimerCallback.Serializer<C, T> getSerializer(Class<?> cls) {
        return (TimerCallback.Serializer<C, T>)this.classToSerializer.get(cls);
    }

    public <T extends TimerCallback<C>> CompoundTag serialize(T t) {
        TimerCallback.Serializer<C, T> serializer = getSerializer(t.getClass());
        CompoundTag compoundTag = new CompoundTag();
        serializer.serialize(compoundTag, t);
        compoundTag.putString("Type", serializer.getId().toString());
        return compoundTag;
    }

    @Nullable
    public TimerCallback<C> deserialize(CompoundTag compoundTag) {
        TimerCallback.Serializer<C, ?> serializer = this.idToSerializer.get(ResourceLocation.tryParse(compoundTag.getString("Type")));
        if (serializer == null) {
            LOGGER.error("Failed to deserialize timer callback: " + compoundTag);
            return null;
        }
        try {
            return (TimerCallback<C>) serializer.deserialize(compoundTag);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize timer callback: " + compoundTag, e);
            return null;
        }
    }
}
