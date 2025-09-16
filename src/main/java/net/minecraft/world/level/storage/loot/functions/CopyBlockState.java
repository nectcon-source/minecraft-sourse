package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyBlockState.class */
public class CopyBlockState extends LootItemConditionalFunction {
    private final Block block;
    private final Set<Property<?>> properties;

    private CopyBlockState(LootItemCondition[] lootItemConditionArr, Block block, Set<Property<?>> set) {
        super(lootItemConditionArr);
        this.block = block;
        this.properties = set;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.COPY_STATE;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        CompoundTag compoundTag;
        BlockState blockState = (BlockState) lootContext.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (blockState != null) {
            CompoundTag orCreateTag = itemStack.getOrCreateTag();
            if (orCreateTag.contains("BlockStateTag", 10)) {
                compoundTag = orCreateTag.getCompound("BlockStateTag");
            } else {
                compoundTag = new CompoundTag();
                orCreateTag.put("BlockStateTag", compoundTag);
            }
            Stream<Property<?>> stream = this.properties.stream();
            blockState.getClass();
            CompoundTag compoundTag2 = compoundTag;
            stream.filter(blockState::hasProperty).forEach(property -> {
                compoundTag2.putString(property.getName(), serialize(blockState, property));
            });
        }
        return itemStack;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyBlockState$Builder.class */
    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final Block block;
        private final Set<Property<?>> properties;

        private Builder(Block block) {
            this.properties = Sets.newHashSet();
            this.block = block;
        }

        public Builder copy(Property<?> property) {
            if (!this.block.getStateDefinition().getProperties().contains(property)) {
                throw new IllegalStateException("Property " + property + " is not present on block " + this.block);
            }
            this.properties.add(property);
            return this;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder
        public Builder getThis() {
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder
        public LootItemFunction build() {
            return new CopyBlockState(getConditions(), this.block, this.properties);
        }
    }

    public static Builder copyState(Block block) {
        return new Builder(block);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static <T extends Comparable<T>> String serialize(BlockState blockState, Property<T> property) {
        return property.getName(blockState.getValue(property));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyBlockState$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyBlockState> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, CopyBlockState copyBlockState, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, copyBlockState, jsonSerializationContext);
            jsonObject.addProperty("block", Registry.BLOCK.getKey(copyBlockState.block).toString());
            JsonArray jsonArray = new JsonArray();
            copyBlockState.properties.forEach(property -> {
                jsonArray.add(property.getName());
            });
            jsonObject.add("properties", jsonArray);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public CopyBlockState deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
            Block orElseThrow = Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> {
                return new IllegalArgumentException("Can't find block " + resourceLocation);
            });
            StateDefinition<Block, BlockState> stateDefinition = orElseThrow.getStateDefinition();
            Set<Property<?>> newHashSet = Sets.newHashSet();
            JsonArray asJsonArray = GsonHelper.getAsJsonArray(jsonObject, "properties", null);
            if (asJsonArray != null) {
                asJsonArray.forEach(jsonElement -> {
                    newHashSet.add(stateDefinition.getProperty(GsonHelper.convertToString(jsonElement, "property")));
                });
            }
            return new CopyBlockState(lootItemConditionArr, orElseThrow, newHashSet);
        }
    }
}
