package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/MapExtendingRecipe.class */
public class MapExtendingRecipe extends ShapedRecipe {
    public MapExtendingRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation, "", 3, 3, NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.FILLED_MAP), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER)), new ItemStack(Items.MAP));
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // net.minecraft.world.item.crafting.ShapedRecipe, net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        MapItemSavedData orCreateSavedData;
        if (!super.matches(craftingContainer, level)) {
            return false;
        }
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize() && itemStack.isEmpty(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (item.getItem() == Items.FILLED_MAP) {
                itemStack = item;
            }
        }
        return (itemStack.isEmpty() || (orCreateSavedData = MapItem.getOrCreateSavedData(itemStack, level)) == null || isExplorationMap(orCreateSavedData) || orCreateSavedData.scale >= 4) ? false : true;
    }

    private boolean isExplorationMap(MapItemSavedData mapItemSavedData) {
        if (mapItemSavedData.decorations != null) {
            for (MapDecoration mapDecoration : mapItemSavedData.decorations.values()) {
                if (mapDecoration.getType() == MapDecoration.Type.MANSION || mapDecoration.getType() == MapDecoration.Type.MONUMENT) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // net.minecraft.world.item.crafting.ShapedRecipe, net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize() && itemStack.isEmpty(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (item.getItem() == Items.FILLED_MAP) {
                itemStack = item;
            }
        }
        ItemStack copy = itemStack.copy();
        copy.setCount(1);
        copy.getOrCreateTag().putInt("map_scale_direction", 1);
        return copy;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean isSpecial() {
        return true;
    }

    @Override // net.minecraft.world.item.crafting.ShapedRecipe, net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_EXTENDING;
    }
}
