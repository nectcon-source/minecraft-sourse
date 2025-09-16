package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LocationCheck.class */
public class LocationCheck implements LootItemCondition {
    private final LocationPredicate predicate;
    private final BlockPos offset;

    private LocationCheck(LocationPredicate locationPredicate, BlockPos blockPos) {
        this.predicate = locationPredicate;
        this.offset = blockPos;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.LOCATION_CHECK;
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        Vec3 vec3 = (Vec3) lootContext.getParamOrNull(LootContextParams.ORIGIN);
        return vec3 != null && this.predicate.matches(lootContext.getLevel(), vec3.x() + ((double) this.offset.getX()), vec3.y() + ((double) this.offset.getY()), vec3.z() + ((double) this.offset.getZ()));
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder) {
        return () -> {
            return new LocationCheck(builder.build(), BlockPos.ZERO);
        };
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder, BlockPos blockPos) {
        return () -> {
            return new LocationCheck(builder.build(), blockPos);
        };
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LocationCheck$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LocationCheck> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, LocationCheck locationCheck, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("predicate", locationCheck.predicate.serializeToJson());
            if (locationCheck.offset.getX() != 0) {
                jsonObject.addProperty("offsetX", Integer.valueOf(locationCheck.offset.getX()));
            }
            if (locationCheck.offset.getY() != 0) {
                jsonObject.addProperty("offsetY", Integer.valueOf(locationCheck.offset.getY()));
            }
            if (locationCheck.offset.getZ() != 0) {
                jsonObject.addProperty("offsetZ", Integer.valueOf(locationCheck.offset.getZ()));
            }
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public LocationCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LocationCheck(LocationPredicate.fromJson(jsonObject.get("predicate")), new BlockPos(GsonHelper.getAsInt(jsonObject, "offsetX", 0), GsonHelper.getAsInt(jsonObject, "offsetY", 0), GsonHelper.getAsInt(jsonObject, "offsetZ", 0)));
        }
    }
}
