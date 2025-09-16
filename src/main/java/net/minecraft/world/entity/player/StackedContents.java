package net.minecraft.world.entity.player;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/player/StackedContents.class */
public class StackedContents {
    public final Int2IntMap contents = new Int2IntOpenHashMap();

    public void accountSimpleStack(ItemStack itemStack) {
        if (!itemStack.isDamaged() && !itemStack.isEnchanted() && !itemStack.hasCustomHoverName()) {
            accountStack(itemStack);
        }
    }

    public void accountStack(ItemStack itemStack) {
        accountStack(itemStack, 64);
    }

    public void accountStack(ItemStack itemStack, int i) {
        if (!itemStack.isEmpty()) {
            put(getStackingIndex(itemStack), Math.min(i, itemStack.getCount()));
        }
    }

    public static int getStackingIndex(ItemStack itemStack) {
        return Registry.ITEM.getId(itemStack.getItem());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean has(int i) {
        return this.contents.get(i) > 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int take(int i, int i2) {
        int i3 = this.contents.get(i);
        if (i3 >= i2) {
            this.contents.put(i, i3 - i2);
            return i;
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void put(int i, int i2) {
        this.contents.put(i, this.contents.get(i) + i2);
    }

    public boolean canCraft(Recipe<?> recipe, @Nullable IntList intList) {
        return canCraft(recipe, intList, 1);
    }

    public boolean canCraft(Recipe<?> recipe, @Nullable IntList intList, int i) {
        return new RecipePicker(recipe).tryPick(i, intList);
    }

    public int getBiggestCraftableStack(Recipe<?> recipe, @Nullable IntList intList) {
        return getBiggestCraftableStack(recipe, Integer.MAX_VALUE, intList);
    }

    public int getBiggestCraftableStack(Recipe<?> recipe, int i, @Nullable IntList intList) {
        return new RecipePicker(recipe).tryPickAll(i, intList);
    }

    public static ItemStack fromStackingIndex(int i) {
        if (i == 0) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(Item.byId(i));
    }

    public void clear() {
        this.contents.clear();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/player/StackedContents$RecipePicker.class */
    class RecipePicker {
        private final Recipe<?> recipe;
        private final int ingredientCount;
        private final int[] items;
        private final int itemCount;
        private final BitSet data;
        private final List<Ingredient> ingredients = Lists.newArrayList();
        private final IntList path = new IntArrayList();

        public RecipePicker(Recipe<?> recipe) {
            this.recipe = recipe;
            this.ingredients.addAll(recipe.getIngredients());
            this.ingredients.removeIf((v0) -> {
                return v0.isEmpty();
            });
            this.ingredientCount = this.ingredients.size();
            this.items = getUniqueAvailableIngredientItems();
            this.itemCount = this.items.length;
            this.data = new BitSet(this.ingredientCount + this.itemCount + this.ingredientCount + (this.ingredientCount * this.itemCount));
            for (int i = 0; i < this.ingredients.size(); i++) {
                IntList stackingIds = this.ingredients.get(i).getStackingIds();
                for (int i2 = 0; i2 < this.itemCount; i2++) {
                    if (stackingIds.contains(this.items[i2])) {
                        this.data.set(getIndex(true, i2, i));
                    }
                }
            }
        }

        public boolean tryPick(int i, @Nullable IntList intList) {
            if (i <= 0) {
                return true;
            }
            int i2 = 0;
            while (dfs(i)) {
                StackedContents.this.take(this.items[this.path.getInt(0)], i);
                int size = this.path.size() - 1;
                setSatisfied(this.path.getInt(size));
                for (int i3 = 0; i3 < size; i3++) {
                    toggleResidual((i3 & 1) == 0, this.path.get(i3).intValue(), this.path.get(i3 + 1).intValue());
                }
                this.path.clear();
                this.data.clear(0, this.ingredientCount + this.itemCount);
                i2++;
            }
            boolean z = i2 == this.ingredientCount;
            boolean z2 = z && intList != null;
            if (z2) {
                intList.clear();
            }
            this.data.clear(0, this.ingredientCount + this.itemCount + this.ingredientCount);
            int i4 = 0;
            List<Ingredient> ingredients = this.recipe.getIngredients();
            for (int i5 = 0; i5 < ingredients.size(); i5++) {
                if (z2 && ingredients.get(i5).isEmpty()) {
                    intList.add(0);
                } else {
                    for (int i6 = 0; i6 < this.itemCount; i6++) {
                        if (hasResidual(false, i4, i6)) {
                            toggleResidual(true, i6, i4);
                            StackedContents.this.put(this.items[i6], i);
                            if (z2) {
                                intList.add(this.items[i6]);
                            }
                        }
                    }
                    i4++;
                }
            }
            return z;
        }

        private int[] getUniqueAvailableIngredientItems() {
            IntAVLTreeSet intAVLTreeSet = new IntAVLTreeSet();
            Iterator<Ingredient> it = this.ingredients.iterator();
            while (it.hasNext()) {
                intAVLTreeSet.addAll(it.next().getStackingIds());
            }
            IntIterator it2 = intAVLTreeSet.iterator();
            while (it2.hasNext()) {
                if (!StackedContents.this.has(it2.nextInt())) {
                    it2.remove();
                }
            }
            return intAVLTreeSet.toIntArray();
        }

        private boolean dfs(int i) {
            int i2 = this.itemCount;
            for (int i3 = 0; i3 < i2; i3++) {
                if (StackedContents.this.contents.get(this.items[i3]) >= i) {
                    visit(false, i3);
                    while (!this.path.isEmpty()) {
                        int size = this.path.size();
                        boolean z = (size & 1) == 1;
                        int i4 = this.path.getInt(size - 1);
                        if (!z && !isSatisfied(i4)) {
                            break;
                        }
                        int i5 = z ? this.ingredientCount : i2;
                        int i6 = 0;
                        while (true) {
                            if (i6 >= i5) {
                                break;
                            }
                            if (hasVisited(z, i6) || !hasConnection(z, i4, i6) || !hasResidual(z, i4, i6)) {
                                i6++;
                            } else {
                                visit(z, i6);
                                break;
                            }
                        }
                        int size2 = this.path.size();
                        if (size2 == size) {
                            this.path.removeInt(size2 - 1);
                        }
                    }
                    if (!this.path.isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isSatisfied(int i) {
            return this.data.get(getSatisfiedIndex(i));
        }

        private void setSatisfied(int i) {
            this.data.set(getSatisfiedIndex(i));
        }

        private int getSatisfiedIndex(int i) {
            return this.ingredientCount + this.itemCount + i;
        }

        private boolean hasConnection(boolean z, int i, int i2) {
            return this.data.get(getIndex(z, i, i2));
        }

        private boolean hasResidual(boolean z, int i, int i2) {
            return z != this.data.get(1 + getIndex(z, i, i2));
        }

        private void toggleResidual(boolean z, int i, int i2) {
            this.data.flip(1 + getIndex(z, i, i2));
        }

        private int getIndex(boolean z, int i, int i2) {
            return this.ingredientCount + this.itemCount + this.ingredientCount + (2 * (z ? (i * this.ingredientCount) + i2 : (i2 * this.ingredientCount) + i));
        }

        private void visit(boolean z, int i) {
            this.data.set(getVisitedIndex(z, i));
            this.path.add(i);
        }

        private boolean hasVisited(boolean z, int i) {
            return this.data.get(getVisitedIndex(z, i));
        }

        private int getVisitedIndex(boolean z, int i) {
            return (z ? 0 : this.ingredientCount) + i;
        }

        public int tryPickAll(int i, @Nullable IntList intList) {
            int i2;
            int i3 = 0;
            int min = Math.min(i, getMinIngredientCount()) + 1;
            while (true) {
                i2 = (i3 + min) / 2;
                if (tryPick(i2, null)) {
                    if (min - i3 <= 1) {
                        break;
                    }
                    i3 = i2;
                } else {
                    min = i2;
                }
            }
            if (i2 > 0) {
                tryPick(i2, intList);
            }
            return i2;
        }

        private int getMinIngredientCount() {
            int i = Integer.MAX_VALUE;
            Iterator<Ingredient> it = this.ingredients.iterator();
            while (it.hasNext()) {
                int i2 = 0;
                IntListIterator it2 = it.next().getStackingIds().iterator();
                while (it2.hasNext()) {
                    i2 = Math.max(i2, StackedContents.this.contents.get(((Integer) it2.next()).intValue()));
                }
                if (i > 0) {
                    i = Math.min(i, i2);
                }
            }
            return i;
        }
    }
}
