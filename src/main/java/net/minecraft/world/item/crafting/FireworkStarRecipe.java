package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/FireworkStarRecipe.class */
public class FireworkStarRecipe extends CustomRecipe {
    private static final Ingredient SHAPE_INGREDIENT = Ingredient.of(Items.FIRE_CHARGE, Items.FEATHER, Items.GOLD_NUGGET, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.CREEPER_HEAD, Items.PLAYER_HEAD, Items.DRAGON_HEAD, Items.ZOMBIE_HEAD);
    private static final Ingredient TRAIL_INGREDIENT = Ingredient.of(Items.DIAMOND);
    private static final Ingredient FLICKER_INGREDIENT = Ingredient.of(Items.GLOWSTONE_DUST);
    private static final Map<Item, FireworkRocketItem.Shape> SHAPE_BY_ITEM =  Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(Items.FIRE_CHARGE, FireworkRocketItem.Shape.LARGE_BALL);
        hashMap.put(Items.FEATHER, FireworkRocketItem.Shape.BURST);
        hashMap.put(Items.GOLD_NUGGET, FireworkRocketItem.Shape.STAR);
        hashMap.put(Items.SKELETON_SKULL, FireworkRocketItem.Shape.CREEPER);
        hashMap.put(Items.WITHER_SKELETON_SKULL, FireworkRocketItem.Shape.CREEPER);
        hashMap.put(Items.CREEPER_HEAD, FireworkRocketItem.Shape.CREEPER);
        hashMap.put(Items.PLAYER_HEAD, FireworkRocketItem.Shape.CREEPER);
        hashMap.put(Items.DRAGON_HEAD, FireworkRocketItem.Shape.CREEPER);
        hashMap.put(Items.ZOMBIE_HEAD, FireworkRocketItem.Shape.CREEPER);
    });
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);

    public FireworkStarRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        boolean z5 = false;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                if (SHAPE_INGREDIENT.test(item)) {
                    if (z3) {
                        return false;
                    }
                    z3 = true;
                } else if (FLICKER_INGREDIENT.test(item)) {
                    if (z5) {
                        return false;
                    }
                    z5 = true;
                } else if (TRAIL_INGREDIENT.test(item)) {
                    if (z4) {
                        return false;
                    }
                    z4 = true;
                } else if (GUNPOWDER_INGREDIENT.test(item)) {
                    if (z) {
                        return false;
                    }
                    z = true;
                } else if (item.getItem() instanceof DyeItem) {
                    z2 = true;
                } else {
                    return false;
                }
            }
        }
        return z && z2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemStack = new ItemStack(Items.FIREWORK_STAR);
        CompoundTag orCreateTagElement = itemStack.getOrCreateTagElement("Explosion");
        FireworkRocketItem.Shape shape = FireworkRocketItem.Shape.SMALL_BALL;
        List<Integer> newArrayList = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                if (SHAPE_INGREDIENT.test(item)) {
                    shape = SHAPE_BY_ITEM.get(item.getItem());
                } else if (FLICKER_INGREDIENT.test(item)) {
                    orCreateTagElement.putBoolean("Flicker", true);
                } else if (TRAIL_INGREDIENT.test(item)) {
                    orCreateTagElement.putBoolean("Trail", true);
                } else if (item.getItem() instanceof DyeItem) {
                    newArrayList.add(Integer.valueOf(((DyeItem) item.getItem()).getDyeColor().getFireworkColor()));
                }
            }
        }
        orCreateTagElement.putIntArray("Colors", newArrayList);
        orCreateTagElement.putByte("Type", (byte) shape.getId());
        return itemStack;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i * i2 >= 2;
    }

    @Override // net.minecraft.world.item.crafting.CustomRecipe, net.minecraft.world.item.crafting.Recipe
    public ItemStack getResultItem() {
        return new ItemStack(Items.FIREWORK_STAR);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR;
    }
}
