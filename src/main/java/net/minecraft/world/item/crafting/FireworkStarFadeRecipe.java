package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/FireworkStarFadeRecipe.class */
public class FireworkStarFadeRecipe extends CustomRecipe {
    private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

    public FireworkStarFadeRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        boolean z = false;
        boolean z2 = false;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                if (item.getItem() instanceof DyeItem) {
                    z = true;
                } else {
                    if (!STAR_INGREDIENT.test(item) || z2) {
                        return false;
                    }
                    z2 = true;
                }
            }
        }
        return z2 && z;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        List<Integer> newArrayList = Lists.newArrayList();
        ItemStack itemStack = null;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            Item item2 = item.getItem();
            if (item2 instanceof DyeItem) {
                newArrayList.add(Integer.valueOf(((DyeItem) item2).getDyeColor().getFireworkColor()));
            } else if (STAR_INGREDIENT.test(item)) {
                itemStack = item.copy();
                itemStack.setCount(1);
            }
        }
        if (itemStack == null || newArrayList.isEmpty()) {
            return ItemStack.EMPTY;
        }
        itemStack.getOrCreateTagElement("Explosion").putIntArray("FadeColors", newArrayList);
        return itemStack;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i * i2 >= 2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR_FADE;
    }
}
