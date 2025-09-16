package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetItemDamageFunction.class */
public class SetItemDamageFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RandomValueBounds damage;

    private SetItemDamageFunction(LootItemCondition[] lootItemConditionArr, RandomValueBounds randomValueBounds) {
        super(lootItemConditionArr);
        this.damage = randomValueBounds;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_DAMAGE;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.isDamageableItem()) {
            itemStack.setDamageValue(Mth.floor((1.0f - this.damage.getFloat(lootContext.getRandom())) * itemStack.getMaxDamage()));
        } else {
            LOGGER.warn("Couldn't set damage of loot item {}", itemStack);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(RandomValueBounds randomValueBounds) {
        return simpleBuilder(lootItemConditionArr -> {
            return new SetItemDamageFunction(lootItemConditionArr, randomValueBounds);
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetItemDamageFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemDamageFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, SetItemDamageFunction setItemDamageFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  setItemDamageFunction, jsonSerializationContext);
            jsonObject.add("damage", jsonSerializationContext.serialize(setItemDamageFunction.damage));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public SetItemDamageFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new SetItemDamageFunction(lootItemConditionArr, (RandomValueBounds) GsonHelper.getAsObject(jsonObject, "damage", jsonDeserializationContext, RandomValueBounds.class));
        }
    }
}
