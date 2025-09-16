package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/ShulkerBoxColoring.class */
public class ShulkerBoxColoring extends CustomRecipe {
    public ShulkerBoxColoring(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < craftingContainer.getContainerSize(); i3++) {
            ItemStack item = craftingContainer.getItem(i3);
            if (!item.isEmpty()) {
                if (Block.byItem(item.getItem()) instanceof ShulkerBoxBlock) {
                    i++;
                } else if (item.getItem() instanceof DyeItem) {
                    i2++;
                } else {
                    return false;
                }
                if (i2 > 1 || i > 1) {
                    return false;
                }
            }
        }
        return i == 1 && i2 == 1;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemStack = ItemStack.EMPTY;
        DyeItem dyeItem = (DyeItem) Items.WHITE_DYE;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                Item item2 = item.getItem();
                if (Block.byItem(item2) instanceof ShulkerBoxBlock) {
                    itemStack = item;
                } else if (item2 instanceof DyeItem) {
                    dyeItem = (DyeItem) item2;
                }
            }
        }
        ItemStack coloredItemStack = ShulkerBoxBlock.getColoredItemStack(dyeItem.getDyeColor());
        if (itemStack.hasTag()) {
            coloredItemStack.setTag(itemStack.getTag().copy());
        }
        return coloredItemStack;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i * i2 >= 2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHULKER_BOX_COLORING;
    }
}
