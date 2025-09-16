package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/ArmorDyeRecipe.class */
public class ArmorDyeRecipe extends CustomRecipe {
    public ArmorDyeRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        ItemStack itemStack = ItemStack.EMPTY;
        List<ItemStack> newArrayList = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                if (item.getItem() instanceof DyeableLeatherItem) {
                    if (!itemStack.isEmpty()) {
                        return false;
                    }
                    itemStack = item;
                } else if (item.getItem() instanceof DyeItem) {
                    newArrayList.add(item);
                } else {
                    return false;
                }
            }
        }
        return (itemStack.isEmpty() || newArrayList.isEmpty()) ? false : true;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        List<DyeItem> newArrayList = Lists.newArrayList();
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                Item item2 = item.getItem();
                if (item2 instanceof DyeableLeatherItem) {
                    if (!itemStack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    itemStack = item.copy();
                } else if (item2 instanceof DyeItem) {
                    newArrayList.add((DyeItem) item2);
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (itemStack.isEmpty() || newArrayList.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return DyeableLeatherItem.dyeArmor(itemStack, newArrayList);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i * i2 >= 2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.ARMOR_DYE;
    }
}
