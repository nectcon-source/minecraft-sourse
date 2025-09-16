package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ApplyBonusCount.class */
public class ApplyBonusCount extends LootItemConditionalFunction {
    private static final Map<ResourceLocation, FormulaDeserializer> FORMULAS = Maps.newHashMap();
    private final Enchantment enchantment;
    private final Formula formula;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ApplyBonusCount$Formula.class */
    interface Formula {
        int calculateNewCount(Random random, int i, int i2);

        void serializeParams(JsonObject jsonObject, JsonSerializationContext jsonSerializationContext);

        ResourceLocation getType();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ApplyBonusCount$FormulaDeserializer.class */
    interface FormulaDeserializer {
        Formula deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ApplyBonusCount$BinomialWithBonusCount.class */
    static final class BinomialWithBonusCount implements Formula {
        public static final ResourceLocation TYPE = new ResourceLocation("binomial_with_bonus_count");
        private final int extraRounds;
        private final float probability;

        public BinomialWithBonusCount(int i, float f) {
            this.extraRounds = i;
            this.probability = f;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula
        public int calculateNewCount(Random random, int i, int i2) {
            for (int i3 = 0; i3 < i2 + this.extraRounds; i3++) {
                if (random.nextFloat() < this.probability) {
                    i++;
                }
            }
            return i;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula
        public void serializeParams(JsonObject jsonObject, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("extra", Integer.valueOf(this.extraRounds));
            jsonObject.addProperty("probability", Float.valueOf(this.probability));
        }

        public static Formula deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new BinomialWithBonusCount(GsonHelper.getAsInt(jsonObject, "extra"), GsonHelper.getAsFloat(jsonObject, "probability"));
        }

        @Override // net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula
        public ResourceLocation getType() {
            return TYPE;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ApplyBonusCount$UniformBonusCount.class */
    static final class UniformBonusCount implements Formula {
        public static final ResourceLocation TYPE = new ResourceLocation("uniform_bonus_count");
        private final int bonusMultiplier;

        public UniformBonusCount(int i) {
            this.bonusMultiplier = i;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula
        public int calculateNewCount(Random random, int i, int i2) {
            return i + random.nextInt((this.bonusMultiplier * i2) + 1);
        }

        @Override // net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula
        public void serializeParams(JsonObject jsonObject, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("bonusMultiplier", Integer.valueOf(this.bonusMultiplier));
        }

        public static Formula deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new UniformBonusCount(GsonHelper.getAsInt(jsonObject, "bonusMultiplier"));
        }

        @Override // net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula
        public ResourceLocation getType() {
            return TYPE;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ApplyBonusCount$OreDrops.class */
    static final class OreDrops implements Formula {
        public static final ResourceLocation TYPE = new ResourceLocation("ore_drops");

        private OreDrops() {
        }

        @Override // net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula
        public int calculateNewCount(Random random, int i, int i2) {
            if (i2 > 0) {
                int nextInt = random.nextInt(i2 + 2) - 1;
                if (nextInt < 0) {
                    nextInt = 0;
                }
                return i * (nextInt + 1);
            }
            return i;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula
        public void serializeParams(JsonObject jsonObject, JsonSerializationContext jsonSerializationContext) {
        }

        public static Formula deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new OreDrops();
        }

        @Override // net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula
        public ResourceLocation getType() {
            return TYPE;
        }
    }

    static {
        FORMULAS.put(BinomialWithBonusCount.TYPE, BinomialWithBonusCount::deserialize);
        FORMULAS.put(OreDrops.TYPE, OreDrops::deserialize);
        FORMULAS.put(UniformBonusCount.TYPE, UniformBonusCount::deserialize);
    }

    private ApplyBonusCount(LootItemCondition[] lootItemConditionArr, Enchantment enchantment, Formula formula) {
        super(lootItemConditionArr);
        this.enchantment = enchantment;
        this.formula = formula;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.APPLY_BONUS;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        ItemStack itemStack2 = (ItemStack) lootContext.getParamOrNull(LootContextParams.TOOL);
        if (itemStack2 != null) {
            itemStack.setCount(this.formula.calculateNewCount(lootContext.getRandom(), itemStack.getCount(), EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemStack2)));
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> addBonusBinomialDistributionCount(Enchantment enchantment, float f, int i) {
        return simpleBuilder(lootItemConditionArr -> {
            return new ApplyBonusCount(lootItemConditionArr, enchantment, new BinomialWithBonusCount(i, f));
        });
    }

    public static LootItemConditionalFunction.Builder<?> addOreBonusCount(Enchantment enchantment) {
        return simpleBuilder(lootItemConditionArr -> {
            return new ApplyBonusCount(lootItemConditionArr, enchantment, new OreDrops());
        });
    }

    public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment enchantment) {
        return simpleBuilder(lootItemConditionArr -> {
            return new ApplyBonusCount(lootItemConditionArr, enchantment, new UniformBonusCount(1));
        });
    }

    public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment enchantment, int i) {
        return simpleBuilder(lootItemConditionArr -> {
            return new ApplyBonusCount(lootItemConditionArr, enchantment, new UniformBonusCount(i));
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ApplyBonusCount$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyBonusCount> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, ApplyBonusCount applyBonusCount, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, applyBonusCount, jsonSerializationContext);
            jsonObject.addProperty("enchantment", Registry.ENCHANTMENT.getKey(applyBonusCount.enchantment).toString());
            jsonObject.addProperty("formula", applyBonusCount.formula.getType().toString());
            JsonObject jsonObject2 = new JsonObject();
            applyBonusCount.formula.serializeParams(jsonObject2, jsonSerializationContext);
            if (jsonObject2.size() > 0) {
                jsonObject.add("parameters", jsonObject2);
            }
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public ApplyBonusCount deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            Formula deserialize;
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "enchantment"));
            Enchantment orElseThrow = Registry.ENCHANTMENT.getOptional(resourceLocation).orElseThrow(() -> {
                return new JsonParseException("Invalid enchantment id: " + resourceLocation);
            });
            ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "formula"));
            FormulaDeserializer formulaDeserializer = (FormulaDeserializer) ApplyBonusCount.FORMULAS.get(resourceLocation2);
            if (formulaDeserializer == null) {
                throw new JsonParseException("Invalid formula id: " + resourceLocation2);
            }
            if (jsonObject.has("parameters")) {
                deserialize = formulaDeserializer.deserialize(GsonHelper.getAsJsonObject(jsonObject, "parameters"), jsonDeserializationContext);
            } else {
                deserialize = formulaDeserializer.deserialize(new JsonObject(), jsonDeserializationContext);
            }
            return new ApplyBonusCount(lootItemConditionArr, orElseThrow, deserialize);
        }
    }
}
