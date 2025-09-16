package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionReference;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.EntityHasScoreCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LootItemConditions.class */
public class LootItemConditions {
    public static final LootItemConditionType INVERTED = register("inverted", new InvertedLootItemCondition.Serializer());
    public static final LootItemConditionType ALTERNATIVE = register("alternative", new AlternativeLootItemCondition.Serializer());
    public static final LootItemConditionType RANDOM_CHANCE = register("random_chance", new LootItemRandomChanceCondition.Serializer());
    public static final LootItemConditionType RANDOM_CHANCE_WITH_LOOTING = register("random_chance_with_looting", new LootItemRandomChanceWithLootingCondition.Serializer());
    public static final LootItemConditionType ENTITY_PROPERTIES = register("entity_properties", new LootItemEntityPropertyCondition.Serializer());
    public static final LootItemConditionType KILLED_BY_PLAYER = register("killed_by_player", new LootItemKilledByPlayerCondition.Serializer());
    public static final LootItemConditionType ENTITY_SCORES = register("entity_scores", new EntityHasScoreCondition.Serializer());
    public static final LootItemConditionType BLOCK_STATE_PROPERTY = register("block_state_property", new LootItemBlockStatePropertyCondition.Serializer());
    public static final LootItemConditionType MATCH_TOOL = register("match_tool", new MatchTool.Serializer());
    public static final LootItemConditionType TABLE_BONUS = register("table_bonus", new BonusLevelTableCondition.Serializer());
    public static final LootItemConditionType SURVIVES_EXPLOSION = register("survives_explosion", new ExplosionCondition.Serializer());
    public static final LootItemConditionType DAMAGE_SOURCE_PROPERTIES = register("damage_source_properties", new DamageSourceCondition.Serializer());
    public static final LootItemConditionType LOCATION_CHECK = register("location_check", new LocationCheck.Serializer());
    public static final LootItemConditionType WEATHER_CHECK = register("weather_check", new WeatherCheck.Serializer());
    public static final LootItemConditionType REFERENCE = register("reference", new ConditionReference.Serializer());
    public static final LootItemConditionType TIME_CHECK = register("time_check", new TimeCheck.Serializer());

    private static LootItemConditionType register(String str, Serializer<? extends LootItemCondition> serializer) {
        return (LootItemConditionType) Registry.register(Registry.LOOT_CONDITION_TYPE, new ResourceLocation(str), new LootItemConditionType(serializer));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(Registry.LOOT_CONDITION_TYPE, "condition", "condition", (v0) -> {
            return v0.getType();
        }).build();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static <T> Predicate<T> andConditions(Predicate<T>[] predicateArr) {
        switch (predicateArr.length) {
            case 0:
                return obj -> {
                    return true;
                };
            case 1:
                return (Predicate<T>) predicateArr[0];
            case 2:
                return predicateArr[0].and(predicateArr[1]);
            default:
                return obj2 -> {
                    for (Predicate predicate : predicateArr) {
                        if (!predicate.test(obj2)) {
                            return false;
                        }
                    }
                    return true;
                };
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static <T> Predicate<T> orConditions(Predicate<T>[] predicateArr) {
        switch (predicateArr.length) {
            case 0:
                return obj -> {
                    return false;
                };
            case 1:
                return (Predicate<T>) predicateArr[0];
            case 2:
                return predicateArr[0].or(predicateArr[1]);
            default:
                return obj2 -> {
                    for (Predicate predicate : predicateArr) {
                        if (predicate.test(obj2)) {
                            return true;
                        }
                    }
                    return false;
                };
        }
    }
}
