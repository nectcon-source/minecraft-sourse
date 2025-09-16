package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/SuspiciousStewRecipe.class */
public class SuspiciousStewRecipe extends CustomRecipe {
    public SuspiciousStewRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                if (item.getItem() == Blocks.BROWN_MUSHROOM.asItem() && !z3) {
                    z3 = true;
                } else if (item.getItem() == Blocks.RED_MUSHROOM.asItem() && !z2) {
                    z2 = true;
                } else if (item.getItem().is(ItemTags.SMALL_FLOWERS) && !z) {
                    z = true;
                } else if (item.getItem() == Items.BOWL && !z4) {
                    z4 = true;
                } else {
                    return false;
                }
            }
        }
        return z && z3 && z2 && z4;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemStack = ItemStack.EMPTY;
        int i = 0;
        while (true) {
            if (i >= craftingContainer.getContainerSize()) {
                break;
            }
            ItemStack item = craftingContainer.getItem(i);
            if (item.isEmpty() || !item.getItem().is(ItemTags.SMALL_FLOWERS)) {
                i++;
            } else {
                itemStack = item;
                break;
            }
        }
        ItemStack itemStack2 = new ItemStack(Items.SUSPICIOUS_STEW, 1);
        if ((itemStack.getItem() instanceof BlockItem) && (((BlockItem) itemStack.getItem()).getBlock() instanceof FlowerBlock)) {
            FlowerBlock flowerBlock = (FlowerBlock) ((BlockItem) itemStack.getItem()).getBlock();
            SuspiciousStewItem.saveMobEffect(itemStack2, flowerBlock.getSuspiciousStewEffect(), flowerBlock.getEffectDuration());
        }
        return itemStack2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i >= 2 && i2 >= 2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SUSPICIOUS_STEW;
    }
}
