package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/gossip/GossipType.class */
public enum GossipType {
    MAJOR_NEGATIVE("major_negative", -5, 100, 10, 10),
    MINOR_NEGATIVE("minor_negative", -1, 200, 20, 20),
    MINOR_POSITIVE("minor_positive", 1, 200, 1, 5),
    MAJOR_POSITIVE("major_positive", 5, 100, 0, 100),
    TRADING("trading", 1, 25, 2, 20);


    /* renamed from: id */
    public final String id;
    public final int weight;
    public final int max;
    public final int decayPerDay;
    public final int decayPerTransfer;
    private static final Map<String, GossipType> BY_ID = Stream.of( values()).collect(ImmutableMap.toImmutableMap(gossipType -> {
        return gossipType.id;
    }, Function.identity()));

    GossipType(String str, int i, int i2, int i3, int i4) {
        this.id = str;
        this.weight = i;
        this.max = i2;
        this.decayPerDay = i3;
        this.decayPerTransfer = i4;
    }

    @Nullable
    public static GossipType byId(String str) {
        return BY_ID.get(str);
    }
}
