package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetNbtFunction.class */
public class SetNbtFunction extends LootItemConditionalFunction {
    private final CompoundTag tag;

    private SetNbtFunction(LootItemCondition[] lootItemConditionArr, CompoundTag compoundTag) {
        super(lootItemConditionArr);
        this.tag = compoundTag;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_NBT;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.getOrCreateTag().merge(this.tag);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setTag(CompoundTag compoundTag) {
        return simpleBuilder(lootItemConditionArr -> {
            return new SetNbtFunction(lootItemConditionArr, compoundTag);
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetNbtFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetNbtFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, SetNbtFunction setNbtFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  setNbtFunction, jsonSerializationContext);
            jsonObject.addProperty("tag", setNbtFunction.tag.toString());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public SetNbtFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            try {
                return new SetNbtFunction(lootItemConditionArr, TagParser.parseTag(GsonHelper.getAsString(jsonObject, "tag")));
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException(e.getMessage());
            }
        }
    }
}
