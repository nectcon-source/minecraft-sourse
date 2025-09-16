package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetNameFunction.class */
public class SetNameFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Component name;

    @Nullable
    private final LootContext.EntityTarget resolutionContext;

    private SetNameFunction(LootItemCondition[] lootItemConditionArr, @Nullable Component component, @Nullable LootContext.EntityTarget entityTarget) {
        super(lootItemConditionArr);
        this.name = component;
        this.resolutionContext = entityTarget;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_NAME;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
    }

    public static UnaryOperator<Component> createResolver(LootContext lootContext, @Nullable LootContext.EntityTarget entityTarget) {
        Entity entity;
        if (entityTarget != null && (entity = (Entity) lootContext.getParamOrNull(entityTarget.getParam())) != null) {
            CommandSourceStack withPermission = entity.createCommandSourceStack().withPermission(2);
            return component -> {
                try {
                    return ComponentUtils.updateForEntity(withPermission, component, entity, 0);
                } catch (CommandSyntaxException e) {
                    LOGGER.warn("Failed to resolve text component", e);
                    return component;
                }
            };
        }
        return component2 -> {
            return component2;
        };
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (this.name != null) {
            itemStack.setHoverName((Component) createResolver(lootContext, this.resolutionContext).apply(this.name));
        }
        return itemStack;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetNameFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetNameFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, SetNameFunction setNameFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  setNameFunction, jsonSerializationContext);
            if (setNameFunction.name != null) {
                jsonObject.add("name", Component.Serializer.toJsonTree(setNameFunction.name));
            }
            if (setNameFunction.resolutionContext != null) {
                jsonObject.add("entity", jsonSerializationContext.serialize(setNameFunction.resolutionContext));
            }
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public SetNameFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new SetNameFunction(lootItemConditionArr, Component.Serializer.fromJson(jsonObject.get("name")), (LootContext.EntityTarget) GsonHelper.getAsObject(jsonObject, "entity", null, jsonDeserializationContext, LootContext.EntityTarget.class));
        }
    }
}
