package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/PredicateManager.class */
public class PredicateManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = Deserializers.createConditionSerializer().create();
    private Map<ResourceLocation, LootItemCondition> conditions;

    public PredicateManager() {
        super(GSON, "predicates");
        this.conditions = ImmutableMap.of();
    }

    @Nullable
    public LootItemCondition get(ResourceLocation resourceLocation) {
        return this.conditions.get(resourceLocation);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.server.packs.resources.SimplePreparableReloadListener
    public void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, LootItemCondition> var4 = ImmutableMap.builder();
        map.forEach((var1x, var2x) -> {
            try {
                if (var2x.isJsonArray()) {
                    LootItemCondition[] var3 = (LootItemCondition[])GSON.fromJson(var2x, LootItemCondition[].class);
                    var4.put(var1x, new CompositePredicate(var3));
                } else {
                    LootItemCondition var5 = (LootItemCondition)GSON.fromJson(var2x, LootItemCondition.class);
                    var4.put(var1x, var5);
                }
            } catch (Exception var3_1) {
                LOGGER.error("Couldn't parse loot table {}", var1x, var3_1);
            }

        });
        Map<ResourceLocation, LootItemCondition> var5 = var4.build();
        ValidationContext var6 = new ValidationContext(LootContextParamSets.ALL_PARAMS, var5::get, (var0) -> null);
        var5.forEach((var1x, var2x) -> var2x.validate(var6.enterCondition("{" + var1x + "}", var1x)));
        var6.getProblems().forEach((var0, var1x) -> LOGGER.warn("Found validation problem in " + var0 + ": " + var1x));
        this.conditions = var5;
    }

    public Set<ResourceLocation> getKeys() {
        return Collections.unmodifiableSet(this.conditions.keySet());
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/PredicateManager$CompositePredicate.class */
    static class CompositePredicate implements LootItemCondition {
        private final LootItemCondition[] terms;
        private final Predicate<LootContext> composedPredicate;

        private CompositePredicate(LootItemCondition[] lootItemConditionArr) {
            this.terms = lootItemConditionArr;
            this.composedPredicate = LootItemConditions.andConditions(lootItemConditionArr);
        }

        @Override // java.util.function.Predicate
        public final boolean test(LootContext lootContext) {
            return this.composedPredicate.test(lootContext);
        }

        @Override // net.minecraft.world.level.storage.loot.LootContextUser
        public void validate(ValidationContext validationContext) {
            LootItemCondition.super.validate(validationContext);
            for (int i = 0; i < this.terms.length; i++) {
                this.terms[i].validate(validationContext.forChild(".term[" + i + "]"));
            }
        }

        @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
        public LootItemConditionType getType() {
            throw new UnsupportedOperationException();
        }
    }
}
