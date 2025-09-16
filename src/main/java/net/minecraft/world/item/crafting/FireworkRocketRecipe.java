package net.minecraft.world.item.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/FireworkRocketRecipe.class */
public class FireworkRocketRecipe extends CustomRecipe {
    private static final Ingredient PAPER_INGREDIENT = Ingredient.of(Items.PAPER);
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);
    private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

    public FireworkRocketRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        boolean z = false;
        int i = 0;
        for (int i2 = 0; i2 < craftingContainer.getContainerSize(); i2++) {
            ItemStack item = craftingContainer.getItem(i2);
            if (!item.isEmpty()) {
                if (PAPER_INGREDIENT.test(item)) {
                    if (z) {
                        return false;
                    }
                    z = true;
                } else if (GUNPOWDER_INGREDIENT.test(item)) {
                    i++;
                    if (i > 3) {
                        return false;
                    }
                } else if (!STAR_INGREDIENT.test(item)) {
                    return false;
                }
            }
        }
        return z && i >= 1;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        CompoundTag tagElement;
        ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET, 3);
        CompoundTag orCreateTagElement = itemStack.getOrCreateTagElement("Fireworks");
        ListTag listTag = new ListTag();
        int i = 0;
        for (int i2 = 0; i2 < craftingContainer.getContainerSize(); i2++) {
            ItemStack item = craftingContainer.getItem(i2);
            if (!item.isEmpty()) {
                if (GUNPOWDER_INGREDIENT.test(item)) {
                    i++;
                } else if (STAR_INGREDIENT.test(item) && (tagElement = item.getTagElement("Explosion")) != null) {
                    listTag.add(tagElement);
                }
            }
        }
        orCreateTagElement.putByte("Flight", (byte) i);
        if (!listTag.isEmpty()) {
            orCreateTagElement.put("Explosions", listTag);
        }
        return itemStack;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i * i2 >= 2;
    }

    @Override // net.minecraft.world.item.crafting.CustomRecipe, net.minecraft.world.item.crafting.Recipe
    public ItemStack getResultItem() {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_ROCKET;
    }
}
