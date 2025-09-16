package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ExplorationMapFunction.class */
public class ExplorationMapFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructureFeature<?> DEFAULT_FEATURE = StructureFeature.BURIED_TREASURE;
    public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
    private final StructureFeature<?> destination;
    private final MapDecoration.Type mapDecoration;
    private final byte zoom;
    private final int searchRadius;
    private final boolean skipKnownStructures;

    private ExplorationMapFunction(LootItemCondition[] lootItemConditionArr, StructureFeature<?> structureFeature, MapDecoration.Type type, byte b, int i, boolean z) {
        super(lootItemConditionArr);
        this.destination = structureFeature;
        this.mapDecoration = type;
        this.zoom = b;
        this.searchRadius = i;
        this.skipKnownStructures = z;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.EXPLORATION_MAP;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        ServerLevel level;
        BlockPos findNearestMapFeature;
        if (itemStack.getItem() != Items.MAP) {
            return itemStack;
        }
        Vec3 vec3 = (Vec3) lootContext.getParamOrNull(LootContextParams.ORIGIN);
        if (vec3 != null && (findNearestMapFeature = (level = lootContext.getLevel()).findNearestMapFeature(this.destination, new BlockPos(vec3), this.searchRadius, this.skipKnownStructures)) != null) {
            ItemStack create = MapItem.create(level, findNearestMapFeature.getX(), findNearestMapFeature.getZ(), this.zoom, true, true);
            MapItem.renderBiomePreviewMap(level, create);
            MapItemSavedData.addTargetDecoration(create, findNearestMapFeature, "+", this.mapDecoration);
            create.setHoverName(new TranslatableComponent("filled_map." + this.destination.getFeatureName().toLowerCase(Locale.ROOT)));
            return create;
        }
        return itemStack;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ExplorationMapFunction$Builder.class */
    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private StructureFeature<?> destination = ExplorationMapFunction.DEFAULT_FEATURE;
        private MapDecoration.Type mapDecoration = ExplorationMapFunction.DEFAULT_DECORATION;
        private byte zoom = 2;
        private int searchRadius = 50;
        private boolean skipKnownStructures = true;

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder
        public Builder getThis() {
            return this;
        }

        public Builder setDestination(StructureFeature<?> structureFeature) {
            this.destination = structureFeature;
            return this;
        }

        public Builder setMapDecoration(MapDecoration.Type type) {
            this.mapDecoration = type;
            return this;
        }

        public Builder setZoom(byte b) {
            this.zoom = b;
            return this;
        }

        public Builder setSkipKnownStructures(boolean z) {
            this.skipKnownStructures = z;
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder
        public LootItemFunction build() {
            return new ExplorationMapFunction(getConditions(), this.destination, this.mapDecoration, this.zoom, this.searchRadius, this.skipKnownStructures);
        }
    }

    public static Builder makeExplorationMap() {
        return new Builder();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ExplorationMapFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<ExplorationMapFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, ExplorationMapFunction explorationMapFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  explorationMapFunction, jsonSerializationContext);
            if (!explorationMapFunction.destination.equals(ExplorationMapFunction.DEFAULT_FEATURE)) {
                jsonObject.add("destination", jsonSerializationContext.serialize(explorationMapFunction.destination.getFeatureName()));
            }
            if (explorationMapFunction.mapDecoration != ExplorationMapFunction.DEFAULT_DECORATION) {
                jsonObject.add("decoration", jsonSerializationContext.serialize(explorationMapFunction.mapDecoration.toString().toLowerCase(Locale.ROOT)));
            }
            if (explorationMapFunction.zoom != 2) {
                jsonObject.addProperty("zoom", Byte.valueOf(explorationMapFunction.zoom));
            }
            if (explorationMapFunction.searchRadius != 50) {
                jsonObject.addProperty("search_radius", Integer.valueOf(explorationMapFunction.searchRadius));
            }
            if (!explorationMapFunction.skipKnownStructures) {
                jsonObject.addProperty("skip_existing_chunks", Boolean.valueOf(explorationMapFunction.skipKnownStructures));
            }
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public ExplorationMapFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            StructureFeature<?> readStructure = readStructure(jsonObject);
            String asString = jsonObject.has("decoration") ? GsonHelper.getAsString(jsonObject, "decoration") : "mansion";
            MapDecoration.Type type = ExplorationMapFunction.DEFAULT_DECORATION;
            try {
                type = MapDecoration.Type.valueOf(asString.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                ExplorationMapFunction.LOGGER.error("Error while parsing loot table decoration entry. Found {}. Defaulting to " + ExplorationMapFunction.DEFAULT_DECORATION, asString);
            }
            return new ExplorationMapFunction(lootItemConditionArr, readStructure, type, GsonHelper.getAsByte(jsonObject, "zoom", (byte) 2), GsonHelper.getAsInt(jsonObject, "search_radius", 50), GsonHelper.getAsBoolean(jsonObject, "skip_existing_chunks", true));
        }

        private static StructureFeature<?> readStructure(JsonObject jsonObject) {
            if (jsonObject.has("destination")) {
                StructureFeature<?> structureFeature = (StructureFeature) StructureFeature.STRUCTURES_REGISTRY.get(GsonHelper.getAsString(jsonObject, "destination").toLowerCase(Locale.ROOT));
                if (structureFeature != null) {
                    return structureFeature;
                }
            }
            return ExplorationMapFunction.DEFAULT_FEATURE;
        }
    }
}
