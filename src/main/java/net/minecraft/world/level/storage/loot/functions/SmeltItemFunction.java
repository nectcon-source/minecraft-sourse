package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SmeltItemFunction.class */
public class SmeltItemFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogManager.getLogger();

    private SmeltItemFunction(LootItemCondition[] lootItemConditionArr) {
        super(lootItemConditionArr);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.FURNACE_SMELT;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.isEmpty()) {
            return itemStack;
        }
        Optional<SmeltingRecipe> recipeFor = lootContext.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(itemStack), lootContext.getLevel());
        if (recipeFor.isPresent()) {
            ItemStack resultItem = recipeFor.get().getResultItem();
            if (!resultItem.isEmpty()) {
                ItemStack copy = resultItem.copy();
                copy.setCount(itemStack.getCount());
                return copy;
            }
        }
        LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", itemStack);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> smelted() {
        return simpleBuilder(SmeltItemFunction::new);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SmeltItemFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SmeltItemFunction> {
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public SmeltItemFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new SmeltItemFunction(lootItemConditionArr);
        }
    }
}
