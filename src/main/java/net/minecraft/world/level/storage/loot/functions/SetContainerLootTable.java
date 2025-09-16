package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetContainerLootTable.class */
public class SetContainerLootTable extends LootItemConditionalFunction {
    private final ResourceLocation name;
    private final long seed;

    private SetContainerLootTable(LootItemCondition[] lootItemConditionArr, ResourceLocation resourceLocation, long j) {
        super(lootItemConditionArr);
        this.name = resourceLocation;
        this.seed = j;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_LOOT_TABLE;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.isEmpty()) {
            return itemStack;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("LootTable", this.name.toString());
        if (this.seed != 0) {
            compoundTag.putLong("LootTableSeed", this.seed);
        }
        itemStack.getOrCreateTag().put("BlockEntityTag", compoundTag);
        return itemStack;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction, net.minecraft.world.level.storage.loot.LootContextUser
    public void validate(ValidationContext validationContext) {
        if (validationContext.hasVisitedTable(this.name)) {
            validationContext.reportProblem("Table " + this.name + " is recursively called");
            return;
        }
        super.validate(validationContext);
        LootTable resolveLootTable = validationContext.resolveLootTable(this.name);
        if (resolveLootTable == null) {
            validationContext.reportProblem("Unknown loot table called " + this.name);
        } else {
            resolveLootTable.validate(validationContext.enterTable("->{" + this.name + "}", this.name));
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetContainerLootTable$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerLootTable> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, SetContainerLootTable setContainerLootTable, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  setContainerLootTable, jsonSerializationContext);
            jsonObject.addProperty("name", setContainerLootTable.name.toString());
            if (setContainerLootTable.seed != 0) {
                jsonObject.addProperty("seed", Long.valueOf(setContainerLootTable.seed));
            }
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public SetContainerLootTable deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new SetContainerLootTable(lootItemConditionArr, new ResourceLocation(GsonHelper.getAsString(jsonObject, "name")), GsonHelper.getAsLong(jsonObject, "seed", 0L));
        }
    }
}
