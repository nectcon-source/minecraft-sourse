package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootContext.class */
public class LootContext {
    private final Random random;
    private final float luck;
    private final ServerLevel level;
    private final Function<ResourceLocation, LootTable> lootTables;
    private final Set<LootTable> visitedTables;
    private final Function<ResourceLocation, LootItemCondition> conditions;
    private final Set<LootItemCondition> visitedConditions;
    private final Map<LootContextParam<?>, Object> params;
    private final Map<ResourceLocation, DynamicDrop> dynamicDrops;

    @FunctionalInterface
    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootContext$DynamicDrop.class */
    public interface DynamicDrop {
        void add(LootContext lootContext, Consumer<ItemStack> consumer);
    }

    private LootContext(Random random, float f, ServerLevel serverLevel, Function<ResourceLocation, LootTable> function, Function<ResourceLocation, LootItemCondition> function2, Map<LootContextParam<?>, Object> map, Map<ResourceLocation, DynamicDrop> map2) {
        this.visitedTables = Sets.newLinkedHashSet();
        this.visitedConditions = Sets.newLinkedHashSet();
        this.random = random;
        this.luck = f;
        this.level = serverLevel;
        this.lootTables = function;
        this.conditions = function2;
        this.params = ImmutableMap.copyOf(map);
        this.dynamicDrops = ImmutableMap.copyOf(map2);
    }

    public boolean hasParam(LootContextParam<?> lootContextParam) {
        return this.params.containsKey(lootContextParam);
    }

    public void addDynamicDrops(ResourceLocation resourceLocation, Consumer<ItemStack> consumer) {
        DynamicDrop dynamicDrop = this.dynamicDrops.get(resourceLocation);
        if (dynamicDrop != null) {
            dynamicDrop.add(this, consumer);
        }
    }

    @Nullable
    public <T> T getParamOrNull(LootContextParam<T> lootContextParam) {
        return (T) this.params.get(lootContextParam);
    }

    public boolean addVisitedTable(LootTable lootTable) {
        return this.visitedTables.add(lootTable);
    }

    public void removeVisitedTable(LootTable lootTable) {
        this.visitedTables.remove(lootTable);
    }

    public boolean addVisitedCondition(LootItemCondition lootItemCondition) {
        return this.visitedConditions.add(lootItemCondition);
    }

    public void removeVisitedCondition(LootItemCondition lootItemCondition) {
        this.visitedConditions.remove(lootItemCondition);
    }

    public LootTable getLootTable(ResourceLocation resourceLocation) {
        return this.lootTables.apply(resourceLocation);
    }

    public LootItemCondition getCondition(ResourceLocation resourceLocation) {
        return this.conditions.apply(resourceLocation);
    }

    public Random getRandom() {
        return this.random;
    }

    public float getLuck() {
        return this.luck;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootContext$Builder.class */
    public static class Builder {
        private final ServerLevel level;
        private final Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
        private final Map<ResourceLocation, DynamicDrop> dynamicDrops = Maps.newHashMap();
        private Random random;
        private float luck;

        public Builder(ServerLevel serverLevel) {
            this.level = serverLevel;
        }

        public Builder withRandom(Random random) {
            this.random = random;
            return this;
        }

        public Builder withOptionalRandomSeed(long j) {
            if (j != 0) {
                this.random = new Random(j);
            }
            return this;
        }

        public Builder withOptionalRandomSeed(long j, Random random) {
            if (j == 0) {
                this.random = random;
            } else {
                this.random = new Random(j);
            }
            return this;
        }

        public Builder withLuck(float f) {
            this.luck = f;
            return this;
        }

        public <T> Builder withParameter(LootContextParam<T> lootContextParam, T t) {
            this.params.put(lootContextParam, t);
            return this;
        }

        public <T> Builder withOptionalParameter(LootContextParam<T> lootContextParam, @Nullable T t) {
            if (t == null) {
                this.params.remove(lootContextParam);
            } else {
                this.params.put(lootContextParam, t);
            }
            return this;
        }

        public Builder withDynamicDrop(ResourceLocation resourceLocation, DynamicDrop dynamicDrop) {
            if (this.dynamicDrops.put(resourceLocation, dynamicDrop) != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
            }
            return this;
        }

        public ServerLevel getLevel() {
            return this.level;
        }

        public <T> T getParameter(LootContextParam<T> lootContextParam) {
            T t = (T) this.params.get(lootContextParam);
            if (t == null) {
                throw new IllegalArgumentException("No parameter " + lootContextParam);
            }
            return t;
        }

        @Nullable
        public <T> T getOptionalParameter(LootContextParam<T> lootContextParam) {
            return (T) this.params.get(lootContextParam);
        }

        public LootContext create(LootContextParamSet lootContextParamSet) {
            Sets.SetView difference = Sets.difference(this.params.keySet(), lootContextParamSet.getAllowed());
            if (!difference.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + difference);
            }
            Sets.SetView difference2 = Sets.difference(lootContextParamSet.getRequired(), this.params.keySet());
            if (!difference2.isEmpty()) {
                throw new IllegalArgumentException("Missing required parameters: " + difference2);
            }
            Random random = this.random;
            if (random == null) {
                random = new Random();
            }
            MinecraftServer server = this.level.getServer();
            float f = this.luck;
            ServerLevel serverLevel = this.level;
            PredicateManager predicateManager = server.getPredicateManager();
            predicateManager.getClass();
            return new LootContext(random, f, serverLevel, server.getLootTables()::get, predicateManager::get, this.params, this.dynamicDrops);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootContext$EntityTarget.class */
    public enum EntityTarget {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        DIRECT_KILLER("direct_killer", LootContextParams.DIRECT_KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER);

        private final String name;
        private final LootContextParam<? extends Entity> param;

        EntityTarget(String str, LootContextParam lootContextParam) {
            this.name = str;
            this.param = lootContextParam;
        }

        public LootContextParam<? extends Entity> getParam() {
            return this.param;
        }

        public static EntityTarget getByName(String str) {
            for (EntityTarget entityTarget : values()) {
                if (entityTarget.name.equals(str)) {
                    return entityTarget;
                }
            }
            throw new IllegalArgumentException("Invalid entity target " + str);
        }

        /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootContext$EntityTarget$Serializer.class */
        public static class Serializer extends TypeAdapter<EntityTarget> {
            public void write(JsonWriter jsonWriter, EntityTarget entityTarget) throws IOException {
                jsonWriter.value(entityTarget.name);
            }

            /* renamed from: read, reason: merged with bridge method [inline-methods] */
            public EntityTarget read(JsonReader jsonReader) throws IOException {
                return EntityTarget.getByName(jsonReader.nextString());
            }
        }
    }
}
