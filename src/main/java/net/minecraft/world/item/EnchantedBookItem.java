package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/EnchantedBookItem.class */
public class EnchantedBookItem extends Item {
    public EnchantedBookItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }

    @Override // net.minecraft.world.item.Item
    public boolean isEnchantable(ItemStack itemStack) {
        return false;
    }

    public static ListTag getEnchantments(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            return tag.getList("StoredEnchantments", 10);
        }
        return new ListTag();
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        ItemStack.appendEnchantmentNames(list, getEnchantments(itemStack));
    }

    public static void addEnchantment(ItemStack itemStack, EnchantmentInstance enchantmentInstance) {
        ListTag enchantments = getEnchantments(itemStack);
        boolean z = true;
        ResourceLocation key = Registry.ENCHANTMENT.getKey(enchantmentInstance.enchantment);
        int i = 0;
        while (true) {
            if (i >= enchantments.size()) {
                break;
            }
            CompoundTag compound = enchantments.getCompound(i);
            ResourceLocation tryParse = ResourceLocation.tryParse(compound.getString("id"));
            if (tryParse == null || !tryParse.equals(key)) {
                i++;
            } else {
                if (compound.getInt("lvl") < enchantmentInstance.level) {
                    compound.putShort("lvl", (short) enchantmentInstance.level);
                }
                z = false;
            }
        }
        if (z) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("id", String.valueOf(key));
            compoundTag.putShort("lvl", (short) enchantmentInstance.level);
            enchantments.add(compoundTag);
        }
        itemStack.getOrCreateTag().put("StoredEnchantments", enchantments);
    }

    public static ItemStack createForEnchantment(EnchantmentInstance enchantmentInstance) {
        ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        addEnchantment(itemStack, enchantmentInstance);
        return itemStack;
    }

    @Override // net.minecraft.world.item.Item
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        if (creativeModeTab == CreativeModeTab.TAB_SEARCH) {
            Iterator it = Registry.ENCHANTMENT.iterator();
            while (it.hasNext()) {
                Enchantment enchantment = (Enchantment) it.next();
                if (enchantment.category != null) {
                    for (int minLevel = enchantment.getMinLevel(); minLevel <= enchantment.getMaxLevel(); minLevel++) {
                        nonNullList.add(createForEnchantment(new EnchantmentInstance(enchantment, minLevel)));
                    }
                }
            }
            return;
        }
        if (creativeModeTab.getEnchantmentCategories().length != 0) {
            Iterator it2 = Registry.ENCHANTMENT.iterator();
            while (it2.hasNext()) {
                Enchantment enchantment2 = (Enchantment) it2.next();
                if (creativeModeTab.hasEnchantmentCategory(enchantment2.category)) {
                    nonNullList.add(createForEnchantment(new EnchantmentInstance(enchantment2, enchantment2.getMaxLevel())));
                }
            }
        }
    }
}
