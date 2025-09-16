package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetAttributesFunction.class */
public class SetAttributesFunction extends LootItemConditionalFunction {
    private final List<Modifier> modifiers;

    private SetAttributesFunction(LootItemCondition[] lootItemConditionArr, List<Modifier> list) {
        super(lootItemConditionArr);
        this.modifiers = ImmutableList.copyOf(list);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_ATTRIBUTES;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Random random = lootContext.getRandom();
        for (Modifier modifier : this.modifiers) {
            UUID uuid = modifier.id;
            if (uuid == null) {
                uuid = UUID.randomUUID();
            }
            itemStack.addAttributeModifier(modifier.attribute, new AttributeModifier(uuid, modifier.name, modifier.amount.getFloat(random), modifier.operation), (EquipmentSlot) Util.getRandom(modifier.slots, random));
        }
        return itemStack;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetAttributesFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetAttributesFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, SetAttributesFunction setAttributesFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  setAttributesFunction, jsonSerializationContext);
            JsonArray jsonArray = new JsonArray();
            Iterator it = setAttributesFunction.modifiers.iterator();
            while (it.hasNext()) {
                jsonArray.add(((Modifier) it.next()).serialize(jsonSerializationContext));
            }
            jsonObject.add("modifiers", jsonArray);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public SetAttributesFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            JsonArray asJsonArray = GsonHelper.getAsJsonArray(jsonObject, "modifiers");
            List<Modifier> newArrayListWithExpectedSize = Lists.newArrayListWithExpectedSize(asJsonArray.size());
            Iterator it = asJsonArray.iterator();
            while (it.hasNext()) {
                newArrayListWithExpectedSize.add(Modifier.deserialize(GsonHelper.convertToJsonObject((JsonElement) it.next(), "modifier"), jsonDeserializationContext));
            }
            if (newArrayListWithExpectedSize.isEmpty()) {
                throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
            }
            return new SetAttributesFunction(lootItemConditionArr, newArrayListWithExpectedSize);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetAttributesFunction$Modifier.class */
    static class Modifier {
        private final String name;
        private final Attribute attribute;
        private final AttributeModifier.Operation operation;
        private final RandomValueBounds amount;

        /* renamed from: id */
        @Nullable
        private final UUID id;
        private final EquipmentSlot[] slots;

        private Modifier(String str, Attribute attribute, AttributeModifier.Operation operation, RandomValueBounds randomValueBounds, EquipmentSlot[] equipmentSlotArr, @Nullable UUID uuid) {
            this.name = str;
            this.attribute = attribute;
            this.operation = operation;
            this.amount = randomValueBounds;
            this.id = uuid;
            this.slots = equipmentSlotArr;
        }

        public JsonObject serialize(JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", this.name);
            jsonObject.addProperty("attribute", Registry.ATTRIBUTE.getKey(this.attribute).toString());
            jsonObject.addProperty("operation", operationToString(this.operation));
            jsonObject.add("amount", jsonSerializationContext.serialize(this.amount));
            if (this.id != null) {
                jsonObject.addProperty("id", this.id.toString());
            }
            if (this.slots.length == 1) {
                jsonObject.addProperty("slot", this.slots[0].getName());
            } else {
                JsonArray jsonArray = new JsonArray();
                for (EquipmentSlot equipmentSlot : this.slots) {
                    jsonArray.add(new JsonPrimitive(equipmentSlot.getName()));
                }
                jsonObject.add("slot", jsonArray);
            }
            return jsonObject;
        }

        public static Modifier deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            EquipmentSlot[] equipmentSlotArr;
            String asString = GsonHelper.getAsString(jsonObject, "name");
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "attribute"));
            Attribute attribute = Registry.ATTRIBUTE.get(resourceLocation);
            if (attribute == null) {
                throw new JsonSyntaxException("Unknown attribute: " + resourceLocation);
            }
            AttributeModifier.Operation operationFromString = operationFromString(GsonHelper.getAsString(jsonObject, "operation"));
            RandomValueBounds randomValueBounds = (RandomValueBounds) GsonHelper.getAsObject(jsonObject, "amount", jsonDeserializationContext, RandomValueBounds.class);
            UUID uuid = null;
            if (GsonHelper.isStringValue(jsonObject, "slot")) {
                equipmentSlotArr = new EquipmentSlot[]{EquipmentSlot.byName(GsonHelper.getAsString(jsonObject, "slot"))};
            } else if (GsonHelper.isArrayNode(jsonObject, "slot")) {
                JsonArray asJsonArray = GsonHelper.getAsJsonArray(jsonObject, "slot");
                equipmentSlotArr = new EquipmentSlot[asJsonArray.size()];
                int i = 0;
                Iterator it = asJsonArray.iterator();
                while (it.hasNext()) {
                    int i2 = i;
                    i++;
                    equipmentSlotArr[i2] = EquipmentSlot.byName(GsonHelper.convertToString((JsonElement) it.next(), "slot"));
                }
                if (equipmentSlotArr.length == 0) {
                    throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
                }
            } else {
                throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
            }
            if (jsonObject.has("id")) {
                String asString2 = GsonHelper.getAsString(jsonObject, "id");
                try {
                    uuid = UUID.fromString(asString2);
                } catch (IllegalArgumentException e) {
                    throw new JsonSyntaxException("Invalid attribute modifier id '" + asString2 + "' (must be UUID format, with dashes)");
                }
            }
            return new Modifier(asString, attribute, operationFromString, randomValueBounds, equipmentSlotArr, uuid);
        }

        private static String operationToString(AttributeModifier.Operation operation) {
            switch (operation) {
                case ADDITION:
                    return "addition";
                case MULTIPLY_BASE:
                    return "multiply_base";
                case MULTIPLY_TOTAL:
                    return "multiply_total";
                default:
                    throw new IllegalArgumentException("Unknown operation " + operation);
            }
        }

        private static AttributeModifier.Operation operationFromString(String str) {
            switch (str) {
                case "addition":
                    return AttributeModifier.Operation.ADDITION;
                case "multiply_base":
                    return AttributeModifier.Operation.MULTIPLY_BASE;
                case "multiply_total":
                    return AttributeModifier.Operation.MULTIPLY_TOTAL;
                default:
                    throw new JsonSyntaxException("Unknown attribute modifier operation " + str);
            }
        }
    }
}
