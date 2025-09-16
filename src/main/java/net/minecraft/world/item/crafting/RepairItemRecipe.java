package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/RepairItemRecipe.class */
public class RepairItemRecipe extends CustomRecipe {
    public RepairItemRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        List<ItemStack> newArrayList = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                newArrayList.add(item);
                if (newArrayList.size() > 1) {
                    ItemStack itemStack = newArrayList.get(0);
                    if (item.getItem() != itemStack.getItem() || itemStack.getCount() != 1 || item.getCount() != 1 || !itemStack.getItem().canBeDepleted()) {
                        return false;
                    }
                } else {
                    continue;
                }
            }
        }
        return newArrayList.size() == 2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        List<ItemStack> newArrayList = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                newArrayList.add(item);
                if (newArrayList.size() > 1) {
                    ItemStack itemStack = newArrayList.get(0);
                    if (item.getItem() != itemStack.getItem() || itemStack.getCount() != 1 || item.getCount() != 1 || !itemStack.getItem().canBeDepleted()) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    continue;
                }
            }
        }
        if (newArrayList.size() == 2) {
            ItemStack itemStack2 = newArrayList.get(0);
            ItemStack itemStack3 = newArrayList.get(1);
            if (itemStack2.getItem() == itemStack3.getItem() && itemStack2.getCount() == 1 && itemStack3.getCount() == 1 && itemStack2.getItem().canBeDepleted()) {
                Item item2 = itemStack2.getItem();
                int maxDamage = item2.getMaxDamage() - (((item2.getMaxDamage() - itemStack2.getDamageValue()) + (item2.getMaxDamage() - itemStack3.getDamageValue())) + ((item2.getMaxDamage() * 5) / 100));
                if (maxDamage < 0) {
                    maxDamage = 0;
                }
                ItemStack itemStack4 = new ItemStack(itemStack2.getItem());
                itemStack4.setDamageValue(maxDamage);
                Map<Enchantment, Integer> newHashMap = Maps.newHashMap();
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack2);
                Map<Enchantment, Integer> enchantments2 = EnchantmentHelper.getEnchantments(itemStack3);
                Registry.ENCHANTMENT.stream().filter((v0) -> {
                    return v0.isCurse();
                }).forEach(enchantment -> {
                    int max = Math.max(((Integer) enchantments.getOrDefault(enchantment, 0)).intValue(), ((Integer) enchantments2.getOrDefault(enchantment, 0)).intValue());
                    if (max > 0) {
                        newHashMap.put(enchantment, Integer.valueOf(max));
                    }
                });
                if (!newHashMap.isEmpty()) {
                    EnchantmentHelper.setEnchantments(newHashMap, itemStack4);
                }
                return itemStack4;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i * i2 >= 2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.REPAIR_ITEM;
    }
}
