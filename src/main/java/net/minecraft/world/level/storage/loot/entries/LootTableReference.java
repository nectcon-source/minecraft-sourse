package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootTableReference.class */
public class LootTableReference extends LootPoolSingletonContainer {
    private final ResourceLocation name;

    private LootTableReference(ResourceLocation resourceLocation, int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
        super(i, i2, lootItemConditionArr, lootItemFunctionArr);
        this.name = resourceLocation;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public LootPoolEntryType getType() {
        return LootPoolEntries.REFERENCE;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        lootContext.getLootTable(this.name).getRandomItemsRaw(lootContext, consumer);
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer, net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
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

    public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation resourceLocation) {
        return simpleBuilder((i, i2, lootItemConditionArr, lootItemFunctionArr) -> {
            return new LootTableReference(resourceLocation, i, i2, lootItemConditionArr, lootItemFunctionArr);
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootTableReference$Serializer.class */
    public static class Serializer extends LootPoolSingletonContainer.Serializer<LootTableReference> {
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Serializer, net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Serializer
        public void serializeCustom(JsonObject jsonObject, LootTableReference lootTableReference, JsonSerializationContext jsonSerializationContext) {
            super.serializeCustom(jsonObject,  lootTableReference, jsonSerializationContext);
            jsonObject.addProperty("name", lootTableReference.name.toString());
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Serializer
        public LootTableReference deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
            return new LootTableReference(new ResourceLocation(GsonHelper.getAsString(jsonObject, "name")), i, i2, lootItemConditionArr, lootItemFunctionArr);
        }
    }
}
