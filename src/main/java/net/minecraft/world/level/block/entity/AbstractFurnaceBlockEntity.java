package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity.class */
public abstract class AbstractFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible, TickableBlockEntity {
    private static final int[] SLOTS_FOR_UP = {0};
    private static final int[] SLOTS_FOR_DOWN = {2, 1};
    private static final int[] SLOTS_FOR_SIDES = {1};
    protected NonNullList<ItemStack> items;
    private int litTime;
    private int litDuration;
    private int cookingProgress;
    private int cookingTotalTime;
    protected final ContainerData dataAccess;
    private final Object2IntOpenHashMap<ResourceLocation> recipesUsed;
    protected final RecipeType<? extends AbstractCookingRecipe> recipeType;

    protected AbstractFurnaceBlockEntity(BlockEntityType<?> blockEntityType, RecipeType<? extends AbstractCookingRecipe> recipeType) {
        super(blockEntityType);
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
        this.dataAccess = new ContainerData() { // from class: net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.1
            @Override // net.minecraft.world.inventory.ContainerData
            public int get(int i) {
                switch (i) {
                    case 0:
                        return AbstractFurnaceBlockEntity.this.litTime;
                    case 1:
                        return AbstractFurnaceBlockEntity.this.litDuration;
                    case 2:
                        return AbstractFurnaceBlockEntity.this.cookingProgress;
                    case 3:
                        return AbstractFurnaceBlockEntity.this.cookingTotalTime;
                    default:
                        return 0;
                }
            }

            @Override // net.minecraft.world.inventory.ContainerData
            public void set(int i, int i2) {
                switch (i) {
                    case 0:
                        AbstractFurnaceBlockEntity.this.litTime = i2;
                        break;
                    case 1:
                        AbstractFurnaceBlockEntity.this.litDuration = i2;
                        break;
                    case 2:
                        AbstractFurnaceBlockEntity.this.cookingProgress = i2;
                        break;
                    case 3:
                        AbstractFurnaceBlockEntity.this.cookingTotalTime = i2;
                        break;
                }
            }

            @Override // net.minecraft.world.inventory.ContainerData
            public int getCount() {
                return 4;
            }
        };
        this.recipesUsed = new Object2IntOpenHashMap<>();
        this.recipeType = recipeType;
    }

    public static Map<Item, Integer> getFuel() {
        Map<Item, Integer> newLinkedHashMap = Maps.newLinkedHashMap();
        add(newLinkedHashMap, Items.LAVA_BUCKET, 20000);
        add(newLinkedHashMap, Blocks.COAL_BLOCK, 16000);
        add(newLinkedHashMap, Items.BLAZE_ROD, 2400);
        add(newLinkedHashMap, Items.COAL, 1600);
        add(newLinkedHashMap, Items.CHARCOAL, 1600);
        add(newLinkedHashMap, ItemTags.LOGS, 300);
        add(newLinkedHashMap, ItemTags.PLANKS, 300);
        add(newLinkedHashMap, ItemTags.WOODEN_STAIRS, 300);
        add(newLinkedHashMap, ItemTags.WOODEN_SLABS, 150);
        add(newLinkedHashMap, ItemTags.WOODEN_TRAPDOORS, 300);
        add(newLinkedHashMap, ItemTags.WOODEN_PRESSURE_PLATES, 300);
        add(newLinkedHashMap, Blocks.OAK_FENCE, 300);
        add(newLinkedHashMap, Blocks.BIRCH_FENCE, 300);
        add(newLinkedHashMap, Blocks.SPRUCE_FENCE, 300);
        add(newLinkedHashMap, Blocks.JUNGLE_FENCE, 300);
        add(newLinkedHashMap, Blocks.DARK_OAK_FENCE, 300);
        add(newLinkedHashMap, Blocks.ACACIA_FENCE, 300);
        add(newLinkedHashMap, Blocks.OAK_FENCE_GATE, 300);
        add(newLinkedHashMap, Blocks.BIRCH_FENCE_GATE, 300);
        add(newLinkedHashMap, Blocks.SPRUCE_FENCE_GATE, 300);
        add(newLinkedHashMap, Blocks.JUNGLE_FENCE_GATE, 300);
        add(newLinkedHashMap, Blocks.DARK_OAK_FENCE_GATE, 300);
        add(newLinkedHashMap, Blocks.ACACIA_FENCE_GATE, 300);
        add(newLinkedHashMap, Blocks.NOTE_BLOCK, 300);
        add(newLinkedHashMap, Blocks.BOOKSHELF, 300);
        add(newLinkedHashMap, Blocks.LECTERN, 300);
        add(newLinkedHashMap, Blocks.JUKEBOX, 300);
        add(newLinkedHashMap, Blocks.CHEST, 300);
        add(newLinkedHashMap, Blocks.TRAPPED_CHEST, 300);
        add(newLinkedHashMap, Blocks.CRAFTING_TABLE, 300);
        add(newLinkedHashMap, Blocks.DAYLIGHT_DETECTOR, 300);
        add(newLinkedHashMap, ItemTags.BANNERS, 300);
        add(newLinkedHashMap, Items.BOW, 300);
        add(newLinkedHashMap, Items.FISHING_ROD, 300);
        add(newLinkedHashMap, Blocks.LADDER, 300);
        add(newLinkedHashMap, ItemTags.SIGNS, 200);
        add(newLinkedHashMap, Items.WOODEN_SHOVEL, 200);
        add(newLinkedHashMap, Items.WOODEN_SWORD, 200);
        add(newLinkedHashMap, Items.WOODEN_HOE, 200);
        add(newLinkedHashMap, Items.WOODEN_AXE, 200);
        add(newLinkedHashMap, Items.WOODEN_PICKAXE, 200);
        add(newLinkedHashMap, ItemTags.WOODEN_DOORS, 200);
        add(newLinkedHashMap, ItemTags.BOATS, 1200);
        add(newLinkedHashMap, ItemTags.WOOL, 100);
        add(newLinkedHashMap, ItemTags.WOODEN_BUTTONS, 100);
        add(newLinkedHashMap, Items.STICK, 100);
        add(newLinkedHashMap, ItemTags.SAPLINGS, 100);
        add(newLinkedHashMap, Items.BOWL, 100);
        add(newLinkedHashMap, ItemTags.CARPETS, 67);
        add(newLinkedHashMap, Blocks.DRIED_KELP_BLOCK, 4001);
        add(newLinkedHashMap, Items.CROSSBOW, 300);
        add(newLinkedHashMap, Blocks.BAMBOO, 50);
        add(newLinkedHashMap, Blocks.DEAD_BUSH, 100);
        add(newLinkedHashMap, Blocks.SCAFFOLDING, 400);
        add(newLinkedHashMap, Blocks.LOOM, 300);
        add(newLinkedHashMap, Blocks.BARREL, 300);
        add(newLinkedHashMap, Blocks.CARTOGRAPHY_TABLE, 300);
        add(newLinkedHashMap, Blocks.FLETCHING_TABLE, 300);
        add(newLinkedHashMap, Blocks.SMITHING_TABLE, 300);
        add(newLinkedHashMap, Blocks.COMPOSTER, 300);
        return newLinkedHashMap;
    }

    private static boolean isNeverAFurnaceFuel(Item item) {
        return ItemTags.NON_FLAMMABLE_WOOD.contains(item);
    }

    private static void add(Map<Item, Integer> map, Tag<Item> tag, int i) {
        for (Item item : tag.getValues()) {
            if (!isNeverAFurnaceFuel(item)) {
                map.put(item, Integer.valueOf(i));
            }
        }
    }

    private static void add(Map<Item, Integer> map, ItemLike itemLike, int i) {
        Item asItem = itemLike.asItem();
        if (isNeverAFurnaceFuel(asItem)) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw ((IllegalStateException) Util.pauseInIde(new IllegalStateException("A developer tried to explicitly make fire resistant item " + asItem.getName(null).getString() + " a furnace fuel. That will not work!")));
            }
        } else {
            map.put(asItem, Integer.valueOf(i));
        }
    }

    private boolean isLit() {
        return this.litTime > 0;
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.items);
        this.litTime = compoundTag.getShort("BurnTime");
        this.cookingProgress = compoundTag.getShort("CookTime");
        this.cookingTotalTime = compoundTag.getShort("CookTimeTotal");
        this.litDuration = getBurnDuration(this.items.get(1));
        CompoundTag compound = compoundTag.getCompound("RecipesUsed");
        for (String str : compound.getAllKeys()) {
            this.recipesUsed.put(new ResourceLocation(str), compound.getInt(str));
        }
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.putShort("BurnTime", (short) this.litTime);
        compoundTag.putShort("CookTime", (short) this.cookingProgress);
        compoundTag.putShort("CookTimeTotal", (short) this.cookingTotalTime);
        ContainerHelper.saveAllItems(compoundTag, this.items);
        CompoundTag compoundTag2 = new CompoundTag();
        this.recipesUsed.forEach((resourceLocation, num) -> {
            compoundTag2.putInt(resourceLocation.toString(), num.intValue());
        });
        compoundTag.put("RecipesUsed", compoundTag2);
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        boolean isLit = isLit();
        boolean z = false;
        if (isLit()) {
            this.litTime--;
        }
        if (!this.level.isClientSide) {
            ItemStack itemStack = this.items.get(1);
            if (isLit() || !(itemStack.isEmpty() || this.items.get(0).isEmpty())) {
                Recipe<?> recipe = (Recipe) this.level.getRecipeManager().getRecipeFor(this.recipeType, this, this.level).orElse(null);
                if (!isLit() && canBurn(recipe)) {
                    this.litTime = getBurnDuration(itemStack);
                    this.litDuration = this.litTime;
                    if (isLit()) {
                        z = true;
                        if (!itemStack.isEmpty()) {
                            Item item = itemStack.getItem();
                            itemStack.shrink(1);
                            if (itemStack.isEmpty()) {
                                Item craftingRemainingItem = item.getCraftingRemainingItem();
                                this.items.set(1, craftingRemainingItem == null ? ItemStack.EMPTY : new ItemStack(craftingRemainingItem));
                            }
                        }
                    }
                }
                if (isLit() && canBurn(recipe)) {
                    this.cookingProgress++;
                    if (this.cookingProgress == this.cookingTotalTime) {
                        this.cookingProgress = 0;
                        this.cookingTotalTime = getTotalCookTime();
                        burn(recipe);
                        z = true;
                    }
                } else {
                    this.cookingProgress = 0;
                }
            } else if (!isLit() && this.cookingProgress > 0) {
                this.cookingProgress = Mth.clamp(this.cookingProgress - 2, 0, this.cookingTotalTime);
            }
            if (isLit != isLit()) {
                z = true;
                this.level.setBlock(this.worldPosition, (BlockState) this.level.getBlockState(this.worldPosition).setValue(AbstractFurnaceBlock.LIT, Boolean.valueOf(isLit())), 3);
            }
        }
        if (z) {
            setChanged();
        }
    }

    protected boolean canBurn(@Nullable Recipe<?> recipe) {
        if (this.items.get(0).isEmpty() || recipe == null) {
            return false;
        }
        ItemStack resultItem = recipe.getResultItem();
        if (resultItem.isEmpty()) {
            return false;
        }
        ItemStack itemStack = this.items.get(2);
        if (itemStack.isEmpty()) {
            return true;
        }
        if (itemStack.sameItem(resultItem)) {
            return (itemStack.getCount() < getMaxStackSize() && itemStack.getCount() < itemStack.getMaxStackSize()) || itemStack.getCount() < resultItem.getMaxStackSize();
        }
        return false;
    }

    private void burn(@Nullable Recipe<?> recipe) {
        if (recipe == null || !canBurn(recipe)) {
            return;
        }
        ItemStack itemStack = this.items.get(0);
        ItemStack resultItem = recipe.getResultItem();
        ItemStack itemStack2 = this.items.get(2);
        if (itemStack2.isEmpty()) {
            this.items.set(2, resultItem.copy());
        } else if (itemStack2.getItem() == resultItem.getItem()) {
            itemStack2.grow(1);
        }
        if (!this.level.isClientSide) {
            setRecipeUsed(recipe);
        }
        if (itemStack.getItem() == Blocks.WET_SPONGE.asItem() && !this.items.get(1).isEmpty() && this.items.get(1).getItem() == Items.BUCKET) {
            this.items.set(1, new ItemStack(Items.WATER_BUCKET));
        }
        itemStack.shrink(1);
    }

    protected int getBurnDuration(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        return getFuel().getOrDefault(itemStack.getItem(), 0).intValue();
    }

    protected int getTotalCookTime() {
        return ((Integer) this.level.getRecipeManager().getRecipeFor(this.recipeType, this, this.level).map((v0) -> {
            return v0.getCookingTime();
        }).orElse(200)).intValue();
    }

    public static boolean isFuel(ItemStack itemStack) {
        return getFuel().containsKey(itemStack.getItem());
    }

    @Override // net.minecraft.world.WorldlyContainer
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        }
        if (direction == Direction.UP) {
            return SLOTS_FOR_UP;
        }
        return SLOTS_FOR_SIDES;
    }

    @Override // net.minecraft.world.WorldlyContainer
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(i, itemStack);
    }

    @Override // net.minecraft.world.WorldlyContainer
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        Item item;
        if (direction == Direction.DOWN && i == 1 && (item = itemStack.getItem()) != Items.WATER_BUCKET && item != Items.BUCKET) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return this.items.size();
    }

    @Override // net.minecraft.world.Container
    public boolean isEmpty() {
        Iterator<ItemStack> it = this.items.iterator();
        while (it.hasNext()) {
            if (!it.next().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.Container
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItem(int i, int i2) {
        return ContainerHelper.removeItem(this.items, i, i2);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(this.items, i);
    }

    @Override // net.minecraft.world.Container
    public void setItem(int i, ItemStack itemStack) {
        ItemStack itemStack2 = this.items.get(i);
        boolean z = !itemStack.isEmpty() && itemStack.sameItem(itemStack2) && ItemStack.tagMatches(itemStack, itemStack2);
        this.items.set(i, itemStack);
        if (itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }
        if (i == 0 && !z) {
            this.cookingTotalTime = getTotalCookTime();
            this.cookingProgress = 0;
            setChanged();
        }
    }

    @Override // net.minecraft.world.Container
    public boolean stillValid(Player player) {
        return this.level.getBlockEntity(this.worldPosition) == this && player.distanceToSqr(((double) this.worldPosition.getX()) + 0.5d, ((double) this.worldPosition.getY()) + 0.5d, ((double) this.worldPosition.getZ()) + 0.5d) <= 64.0d;
    }

    @Override // net.minecraft.world.Container
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        if (i == 2) {
            return false;
        }
        if (i == 1) {
            return isFuel(itemStack) || (itemStack.getItem() == Items.BUCKET && this.items.get(1).getItem() != Items.BUCKET);
        }
        return true;
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        this.items.clear();
    }

    @Override // net.minecraft.world.inventory.RecipeHolder
    public void setRecipeUsed(@Nullable Recipe<?> recipe) {
        if (recipe != null) {
            this.recipesUsed.addTo(recipe.getId(), 1);
        }
    }

    @Override // net.minecraft.world.inventory.RecipeHolder
    @Nullable
    public Recipe<?> getRecipeUsed() {
        return null;
    }

    @Override // net.minecraft.world.inventory.RecipeHolder
    public void awardUsedRecipes(Player player) {
    }

    public void awardUsedRecipesAndPopExperience(Player player) {
        player.awardRecipes(getRecipesToAwardAndPopExperience(player.level, player.position()));
        this.recipesUsed.clear();
    }

    public List<Recipe<?>> getRecipesToAwardAndPopExperience(Level level, Vec3 vec3) {
        List<Recipe<?>> newArrayList = Lists.newArrayList();
        ObjectIterator it = this.recipesUsed.object2IntEntrySet().iterator();
        while (it.hasNext()) {
            Object2IntMap.Entry<ResourceLocation> entry = (Object2IntMap.Entry) it.next();
            level.getRecipeManager().byKey((ResourceLocation) entry.getKey()).ifPresent(recipe -> {
                newArrayList.add(recipe);
                createExperience(level, vec3, entry.getIntValue(), ((AbstractCookingRecipe) recipe).getExperience());
            });
        }
        return newArrayList;
    }

    private static void createExperience(Level level, Vec3 vec3, int i, float f) {
        int floor = Mth.floor(i * f);
        float frac = Mth.frac(i * f);
        if (frac != 0.0f && Math.random() < frac) {
            floor++;
        }
        while (floor > 0) {
            int experienceValue = ExperienceOrb.getExperienceValue(floor);
            floor -= experienceValue;
            level.addFreshEntity(new ExperienceOrb(level, vec3.x, vec3.y, vec3.z, experienceValue));
        }
    }

    @Override // net.minecraft.world.inventory.StackedContentsCompatible
    public void fillStackedContents(StackedContents stackedContents) {
        Iterator<ItemStack> it = this.items.iterator();
        while (it.hasNext()) {
            stackedContents.accountStack(it.next());
        }
    }
}
