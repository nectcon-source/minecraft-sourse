package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/BonusLevelTableCondition.class */
public class BonusLevelTableCondition implements LootItemCondition {
    private final Enchantment enchantment;
    private final float[] values;

    private BonusLevelTableCondition(Enchantment enchantment, float[] fArr) {
        this.enchantment = enchantment;
        this.values = fArr;
    }

    @Override // net.minecraft.world.level.storage.loot.predicates.LootItemCondition
    public LootItemConditionType getType() {
        return LootItemConditions.TABLE_BONUS;
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override // java.util.function.Predicate
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = (ItemStack) lootContext.getParamOrNull(LootContextParams.TOOL);
        return lootContext.getRandom().nextFloat() < this.values[Math.min(itemStack != null ? EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemStack) : 0, this.values.length - 1)];
    }

    public static LootItemCondition.Builder bonusLevelFlatChance(Enchantment enchantment, float... fArr) {
        return () -> {
            return new BonusLevelTableCondition(enchantment, fArr);
        };
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/BonusLevelTableCondition$Serializer.class */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BonusLevelTableCondition> {
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, BonusLevelTableCondition bonusLevelTableCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("enchantment", Registry.ENCHANTMENT.getKey(bonusLevelTableCondition.enchantment).toString());
            jsonObject.add("chances", jsonSerializationContext.serialize(bonusLevelTableCondition.values));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.Serializer
        public BonusLevelTableCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "enchantment"));
            return new BonusLevelTableCondition(Registry.ENCHANTMENT.getOptional(resourceLocation).orElseThrow(() -> {
                return new JsonParseException("Invalid enchantment id: " + resourceLocation);
            }), (float[]) GsonHelper.getAsObject(jsonObject, "chances", jsonDeserializationContext, float[].class));
        }
    }
}
