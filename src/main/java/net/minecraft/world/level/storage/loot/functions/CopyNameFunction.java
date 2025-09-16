package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyNameFunction.class */
public class CopyNameFunction extends LootItemConditionalFunction {
    private final NameSource source;

    private CopyNameFunction(LootItemCondition[] lootItemConditionArr, NameSource nameSource) {
        super(lootItemConditionArr);
        this.source = nameSource;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.COPY_NAME;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.source.param);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Object paramOrNull = lootContext.getParamOrNull(this.source.param);
        if (paramOrNull instanceof Nameable) {
            Nameable nameable = (Nameable) paramOrNull;
            if (nameable.hasCustomName()) {
                itemStack.setHoverName(nameable.getDisplayName());
            }
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> copyName(NameSource nameSource) {
        return simpleBuilder(lootItemConditionArr -> {
            return new CopyNameFunction(lootItemConditionArr, nameSource);
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyNameFunction$NameSource.class */
    public enum NameSource {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER),
        BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY);

        public final String name;
        public final LootContextParam<?> param;

        NameSource(String str, LootContextParam lootContextParam) {
            this.name = str;
            this.param = lootContextParam;
        }

        public static NameSource getByName(String str) {
            for (NameSource nameSource : values()) {
                if (nameSource.name.equals(str)) {
                    return nameSource;
                }
            }
            throw new IllegalArgumentException("Invalid name source " + str);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyNameFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNameFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, CopyNameFunction copyNameFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  copyNameFunction, jsonSerializationContext);
            jsonObject.addProperty("source", copyNameFunction.source.name);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public CopyNameFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new CopyNameFunction(lootItemConditionArr, NameSource.getByName(GsonHelper.getAsString(jsonObject, "source")));
        }
    }
}
