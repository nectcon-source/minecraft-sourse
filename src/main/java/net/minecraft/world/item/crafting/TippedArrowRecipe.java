package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/TippedArrowRecipe.class */
public class TippedArrowRecipe extends CustomRecipe {
    public TippedArrowRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        if (craftingContainer.getWidth() != 3 || craftingContainer.getHeight() != 3) {
            return false;
        }
        for (int i = 0; i < craftingContainer.getWidth(); i++) {
            for (int i2 = 0; i2 < craftingContainer.getHeight(); i2++) {
                ItemStack item = craftingContainer.getItem(i + (i2 * craftingContainer.getWidth()));
                if (item.isEmpty()) {
                    return false;
                }
                Item item2 = item.getItem();
                if (i == 1 && i2 == 1) {
                    if (item2 != Items.LINGERING_POTION) {
                        return false;
                    }
                } else if (item2 != Items.ARROW) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack item = craftingContainer.getItem(1 + craftingContainer.getWidth());
        if (item.getItem() != Items.LINGERING_POTION) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = new ItemStack(Items.TIPPED_ARROW, 8);
        PotionUtils.setPotion(itemStack, PotionUtils.getPotion(item));
        PotionUtils.setCustomEffects(itemStack, PotionUtils.getCustomEffects(item));
        return itemStack;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i >= 2 && i2 >= 2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.TIPPED_ARROW;
    }
}
