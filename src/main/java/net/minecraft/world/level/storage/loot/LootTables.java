package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootTables.class */
public class LootTables extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = Deserializers.createLootTableSerializer().create();
    private Map<ResourceLocation, LootTable> tables;
    private final PredicateManager predicateManager;

    public LootTables(PredicateManager predicateManager) {
        super(GSON, "loot_tables");
        this.tables = ImmutableMap.of();
        this.predicateManager = predicateManager;
    }

    public LootTable get(ResourceLocation resourceLocation) {
        return this.tables.getOrDefault(resourceLocation, LootTable.EMPTY);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.server.packs.resources.SimplePreparableReloadListener
    public void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, LootTable> var4 = ImmutableMap.builder();
        JsonElement var5 = (JsonElement)map.remove(BuiltInLootTables.EMPTY);
        if (var5 != null) {
            LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", BuiltInLootTables.EMPTY);
        }

        map.forEach((var1x, var2x) -> {
            try {
                LootTable var3 = (LootTable)GSON.fromJson(var2x, LootTable.class);
                var4.put(var1x, var3);
            } catch (Exception var3_1) {
                LOGGER.error("Couldn't parse loot table {}", var1x, var3_1);
            }

        });
        var4.put(BuiltInLootTables.EMPTY, LootTable.EMPTY);
        ImmutableMap<ResourceLocation, LootTable> var6 = var4.build();
        ValidationContext var7 = new ValidationContext(LootContextParamSets.ALL_PARAMS, this.predicateManager::get, var6::get);
        var6.forEach((var1x, var2x) -> validate(var7, var1x, var2x));
        var7.getProblems().forEach((var0, var1x) -> LOGGER.warn("Found validation problem in " + var0 + ": " + var1x));
        this.tables = var6;
    }

    public static void validate(ValidationContext validationContext, ResourceLocation resourceLocation, LootTable lootTable) {
        lootTable.validate(validationContext.setParams(lootTable.getParamSet()).enterTable("{" + resourceLocation + "}", resourceLocation));
    }

    public static JsonElement serialize(LootTable lootTable) {
        return GSON.toJsonTree(lootTable);
    }

    public Set<ResourceLocation> getIds() {
        return this.tables.keySet();
    }
}
