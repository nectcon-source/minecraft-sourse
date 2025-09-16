package net.minecraft.world.item.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/ShieldDecorationRecipe.class */
public class ShieldDecorationRecipe extends CustomRecipe {
    public ShieldDecorationRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack itemStack2 = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                if (item.getItem() instanceof BannerItem) {
                    if (!itemStack2.isEmpty()) {
                        return false;
                    }
                    itemStack2 = item;
                } else {
                    if (item.getItem() != Items.SHIELD || !itemStack.isEmpty() || item.getTagElement("BlockEntityTag") != null) {
                        return false;
                    }
                    itemStack = item;
                }
            }
        }
        if (itemStack.isEmpty() || itemStack2.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack itemStack2 = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                if (item.getItem() instanceof BannerItem) {
                    itemStack = item;
                } else if (item.getItem() == Items.SHIELD) {
                    itemStack2 = item.copy();
                }
            }
        }
        if (itemStack2.isEmpty()) {
            return itemStack2;
        }
        CompoundTag tagElement = itemStack.getTagElement("BlockEntityTag");
        CompoundTag compoundTag = tagElement == null ? new CompoundTag() : tagElement.copy();
        compoundTag.putInt("Base", ((BannerItem) itemStack.getItem()).getColor().getId());
        itemStack2.addTagElement("BlockEntityTag", compoundTag);
        return itemStack2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i * i2 >= 2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHIELD_DECORATION;
    }
}
