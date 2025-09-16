package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.SerializableUUID;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/gossip/GossipContainer.class */
public class GossipContainer {
    private final Map<UUID, EntityGossips> gossips = Maps.newHashMap();

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/gossip/GossipContainer$GossipEntry.class */
    static class GossipEntry {
        public final UUID target;
        public final GossipType type;
        public final int value;

        public GossipEntry(UUID uuid, GossipType gossipType, int i) {
            this.target = uuid;
            this.type = gossipType;
            this.value = i;
        }

        public int weightedValue() {
            return this.value * this.type.weight;
        }

        public String toString() {
            return "GossipEntry{target=" + this.target + ", type=" + this.type + ", value=" + this.value + '}';
        }

        public <T> Dynamic<T> store(DynamicOps<T> dynamicOps) {
            return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("Target"), SerializableUUID.CODEC.encodeStart(dynamicOps, this.target).result().orElseThrow(RuntimeException::new), dynamicOps.createString("Type"), dynamicOps.createString(this.type.id), dynamicOps.createString("Value"), dynamicOps.createInt(this.value))));
        }

        public static DataResult<GossipEntry> load(Dynamic<?> dynamic) {
            return DataResult.unbox(DataResult.instance().group(dynamic.get("Target").read(SerializableUUID.CODEC), dynamic.get("Type").asString().map(GossipType::byId), dynamic.get("Value").asNumber().map((v0) -> {
                return v0.intValue();
            })).apply(DataResult.instance(), (v1, v2, v3) -> {
                return new GossipEntry(v1, v2, v3);
            }));
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/gossip/GossipContainer$EntityGossips.class */
    static class EntityGossips {
        private final Object2IntMap<GossipType> entries;

        private EntityGossips() {
            this.entries = new Object2IntOpenHashMap();
        }

        public int weightedValue(Predicate<GossipType> predicate) {
            return this.entries.object2IntEntrySet().stream().filter(entry -> {
                return predicate.test(entry.getKey());
            }).mapToInt(entry2 -> {
                return entry2.getIntValue() * ((GossipType) entry2.getKey()).weight;
            }).sum();
        }

        public Stream<GossipEntry> unpack(UUID uuid) {
            return this.entries.object2IntEntrySet().stream().map(entry -> {
                return new GossipEntry(uuid, (GossipType) entry.getKey(), entry.getIntValue());
            });
        }

        public void decay() {
            ObjectIterator<Object2IntMap.Entry<GossipType>> it = this.entries.object2IntEntrySet().iterator();
            while (it.hasNext()) {
                Object2IntMap.Entry<GossipType> entry = (Object2IntMap.Entry) it.next();
                int intValue = entry.getIntValue() - ((GossipType) entry.getKey()).decayPerDay;
                if (intValue < 2) {
                    it.remove();
                } else {
                    entry.setValue(intValue);
                }
            }
        }

        public boolean isEmpty() {
            return this.entries.isEmpty();
        }

        public void makeSureValueIsntTooLowOrTooHigh(GossipType gossipType) {
            int i = this.entries.getInt(gossipType);
            if (i > gossipType.max) {
                this.entries.put(gossipType, gossipType.max);
            }
            if (i < 2) {
                remove(gossipType);
            }
        }

        public void remove(GossipType gossipType) {
            this.entries.removeInt(gossipType);
        }
    }

    public void decay() {
        Iterator<EntityGossips> it = this.gossips.values().iterator();
        while (it.hasNext()) {
            EntityGossips next = it.next();
            next.decay();
            if (next.isEmpty()) {
                it.remove();
            }
        }
    }

    private Stream<GossipEntry> unpack() {
        return this.gossips.entrySet().stream().flatMap(entry -> {
            return ((EntityGossips) entry.getValue()).unpack((UUID) entry.getKey());
        });
    }

    private Collection<GossipEntry> selectGossipsForTransfer(Random random, int i) {
        List<GossipEntry> list = (List) unpack().collect(Collectors.toList());
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        int[] iArr = new int[list.size()];
        int i2 = 0;
        for (int i3 = 0; i3 < list.size(); i3++) {
            i2 += Math.abs(list.get(i3).weightedValue());
            iArr[i3] = i2 - 1;
        }
        Set<GossipEntry> newIdentityHashSet = Sets.newIdentityHashSet();
        for (int i4 = 0; i4 < i; i4++) {
            int binarySearch = Arrays.binarySearch(iArr, random.nextInt(i2));
            newIdentityHashSet.add(list.get(binarySearch < 0 ? (-binarySearch) - 1 : binarySearch));
        }
        return newIdentityHashSet;
    }

    private EntityGossips getOrCreate(UUID uuid) {
        return this.gossips.computeIfAbsent(uuid, uuid2 -> {
            return new EntityGossips();
        });
    }

    public void transferFrom(GossipContainer gossipContainer, Random random, int i) {
        gossipContainer.selectGossipsForTransfer(random, i).forEach(gossipEntry -> {
            int i2 = gossipEntry.value - gossipEntry.type.decayPerTransfer;
            if (i2 >= 2) {
                getOrCreate(gossipEntry.target).entries.mergeInt(gossipEntry.type, i2, (v0, v1) -> {
                    return mergeValuesForTransfer(v0, v1);
                });
            }
        });
    }

    public int getReputation(UUID uuid, Predicate<GossipType> predicate) {
        EntityGossips entityGossips = this.gossips.get(uuid);
        if (entityGossips != null) {
            return entityGossips.weightedValue(predicate);
        }
        return 0;
    }

    public void add(UUID uuid, GossipType gossipType, int i) {
        EntityGossips orCreate = getOrCreate(uuid);
        orCreate.entries.mergeInt(gossipType, i, (num, num2) -> {
            return Integer.valueOf(mergeValuesForAddition(gossipType, num.intValue(), num2.intValue()));
        });
        orCreate.makeSureValueIsntTooLowOrTooHigh(gossipType);
        if (orCreate.isEmpty()) {
            this.gossips.remove(uuid);
        }
    }

    public <T> Dynamic<T> store(DynamicOps<T> dynamicOps) {
        return new Dynamic<>(dynamicOps, dynamicOps.createList(unpack().map(gossipEntry -> {
            return gossipEntry.store(dynamicOps);
        }).map((v0) -> {
            return v0.getValue();
        })));
    }

    public void update(Dynamic<?> dynamic) {
        dynamic.asStream().map(GossipEntry::load).flatMap(dataResult -> {
            return Util.toStream(dataResult.result());
        }).forEach(gossipEntry -> {
            getOrCreate(gossipEntry.target).entries.put(gossipEntry.type, gossipEntry.value);
        });
    }

    private static int mergeValuesForTransfer(int i, int i2) {
        return Math.max(i, i2);
    }

    private int mergeValuesForAddition(GossipType gossipType, int i, int i2) {
        int i3 = i + i2;
        return i3 > gossipType.max ? Math.max(gossipType.max, i) : i3;
    }
}
