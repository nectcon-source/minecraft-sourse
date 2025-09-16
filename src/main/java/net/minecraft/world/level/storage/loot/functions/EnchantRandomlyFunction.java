package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/EnchantRandomlyFunction.class */
public class EnchantRandomlyFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<Enchantment> enchantments;

    private EnchantRandomlyFunction(LootItemCondition[] lootItemConditionArr, Collection<Enchantment> collection) {
        super(lootItemConditionArr);
        this.enchantments = ImmutableList.copyOf(collection);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.ENCHANT_RANDOMLY;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Enchantment enchantment;
        Random random = lootContext.getRandom();
        if (this.enchantments.isEmpty()) {
            boolean z = itemStack.getItem() == Items.BOOK;
            List<Enchantment> list =  Registry.ENCHANTMENT.stream().filter((v0) -> {
                return v0.isDiscoverable();
            }).filter(enchantment2 -> {
                return z || enchantment2.canEnchant(itemStack);
            }).collect(Collectors.toList());
            if (list.isEmpty()) {
                LOGGER.warn("Couldn't find a compatible enchantment for {}", itemStack);
                return itemStack;
            }
            enchantment = list.get(random.nextInt(list.size()));
        } else {
            enchantment = this.enchantments.get(random.nextInt(this.enchantments.size()));
        }
        return enchantItem(itemStack, enchantment, random);
    }

    private static ItemStack enchantItem(ItemStack itemStack, Enchantment enchantment, Random random) {
        int nextInt = Mth.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
        if (itemStack.getItem() == Items.BOOK) {
            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(itemStack, new EnchantmentInstance(enchantment, nextInt));
        } else {
            itemStack.enchant(enchantment, nextInt);
        }
        return itemStack;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/EnchantRandomlyFunction$Builder.class */
    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final Set<Enchantment> enchantments = Sets.newHashSet();

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder
        public Builder getThis() {
            return this;
        }

        public Builder withEnchantment(Enchantment enchantment) {
            this.enchantments.add(enchantment);
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder
        public LootItemFunction build() {
            return new EnchantRandomlyFunction(getConditions(), this.enchantments);
        }
    }

    public static LootItemConditionalFunction.Builder<?> randomApplicableEnchantment() {
        return simpleBuilder(lootItemConditionArr -> {
            return new EnchantRandomlyFunction(lootItemConditionArr, ImmutableList.of());
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/EnchantRandomlyFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantRandomlyFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, EnchantRandomlyFunction enchantRandomlyFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  enchantRandomlyFunction, jsonSerializationContext);
            if (!enchantRandomlyFunction.enchantments.isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                for (Enchantment enchantment : enchantRandomlyFunction.enchantments) {
                    ResourceLocation key = Registry.ENCHANTMENT.getKey(enchantment);
                    if (key == null) {
                        throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
                    }
                    jsonArray.add(new JsonPrimitive(key.toString()));
                }
                jsonObject.add("enchantments", jsonArray);
            }
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public EnchantRandomlyFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            List<Enchantment> newArrayList = Lists.newArrayList();
            if (jsonObject.has("enchantments")) {
                Iterator it = GsonHelper.getAsJsonArray(jsonObject, "enchantments").iterator();
                while (it.hasNext()) {
                    String convertToString = GsonHelper.convertToString((JsonElement) it.next(), "enchantment");
                    newArrayList.add(Registry.ENCHANTMENT.getOptional(new ResourceLocation(convertToString)).orElseThrow(() -> {
                        return new JsonSyntaxException("Unknown enchantment '" + convertToString + "'");
                    }));
                }
            }
            return new EnchantRandomlyFunction(lootItemConditionArr, newArrayList);
        }
    }
}
