package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LootItemBlockStatePropertyCondition.class */
public class LootItemBlockStatePropertyCondition implements LootItemCondition {
    private final Block block;
    private final StatePropertiesPredicate properties;

    private LootItemBlockStatePropertyCondition(Block block, StatePropertiesPredicate statePropertiesPredicate) {
        this.block = block;
        this.properties = statePropertiesPredicate;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.BLOCK_STATE_PROPERTY;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        BlockState blockState = (BlockState) lootContext.getParamOrNull(LootContextParams.BLOCK_STATE);
        return blockState != null && this.block == blockState.getBlock() && this.properties.matches(blockState);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LootItemBlockStatePropertyCondition$Builder.class */
    public static class Builder implements LootItemCondition.Builder {
        private final Block block;
        private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;

        public Builder(Block block) {
            this.block = block;
        }

        public Builder setProperties(StatePropertiesPredicate.Builder builder) {
            this.properties = builder.build();
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder
        public LootItemCondition build() {
            return new LootItemBlockStatePropertyCondition(this.block, this.properties);
        }
    }

    public static Builder hasBlockStateProperties(Block block) {
        return new Builder(block);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LootItemBlockStatePropertyCondition$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemBlockStatePropertyCondition> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, LootItemBlockStatePropertyCondition lootItemBlockStatePropertyCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("block", Registry.BLOCK.getKey(lootItemBlockStatePropertyCondition.block).toString());
            jsonObject.add("properties", lootItemBlockStatePropertyCondition.properties.serializeToJson());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public LootItemBlockStatePropertyCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
            Block orElseThrow = Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> {
                return new IllegalArgumentException("Can't find block " + resourceLocation);
            });
            StatePropertiesPredicate fromJson = StatePropertiesPredicate.fromJson(jsonObject.get("properties"));
            fromJson.checkState(orElseThrow.getStateDefinition(), str -> {
                throw new JsonSyntaxException("Block " + orElseThrow + " has no property " + str);
            });
            return new LootItemBlockStatePropertyCondition(orElseThrow, fromJson);
        }
    }
}
