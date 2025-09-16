package net.minecraft.world.item;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/CreativeModeTab.class */
public abstract class CreativeModeTab {
    public static final CreativeModeTab[] TABS = new CreativeModeTab[12];
    public static final CreativeModeTab TAB_BUILDING_BLOCKS = new CreativeModeTab(0, "buildingBlocks") { // from class: net.minecraft.world.item.CreativeModeTab.1
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.BRICKS);
        }
    }.setRecipeFolderName("building_blocks");
    public static final CreativeModeTab TAB_DECORATIONS = new CreativeModeTab(1, "decorations") { // from class: net.minecraft.world.item.CreativeModeTab.2
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.PEONY);
        }
    };
    public static final CreativeModeTab TAB_REDSTONE = new CreativeModeTab(2, "redstone") { // from class: net.minecraft.world.item.CreativeModeTab.3
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Items.REDSTONE);
        }
    };
    public static final CreativeModeTab TAB_TRANSPORTATION = new CreativeModeTab(3, "transportation") { // from class: net.minecraft.world.item.CreativeModeTab.4
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.POWERED_RAIL);
        }
    };
    public static final CreativeModeTab TAB_MISC = new CreativeModeTab(6, "misc") { // from class: net.minecraft.world.item.CreativeModeTab.5
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Items.LAVA_BUCKET);
        }
    };
    public static final CreativeModeTab TAB_SEARCH = new CreativeModeTab(5, "search") { // from class: net.minecraft.world.item.CreativeModeTab.6
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Items.COMPASS);
        }
    }.setBackgroundSuffix("item_search.png");
    public static final CreativeModeTab TAB_FOOD = new CreativeModeTab(7, "food") { // from class: net.minecraft.world.item.CreativeModeTab.7
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Items.APPLE);
        }
    };
    public static final CreativeModeTab TAB_TOOLS = new CreativeModeTab(8, "tools") { // from class: net.minecraft.world.item.CreativeModeTab.8
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Items.IRON_AXE);
        }
    }.setEnchantmentCategories(EnchantmentCategory.VANISHABLE, EnchantmentCategory.DIGGER, EnchantmentCategory.FISHING_ROD, EnchantmentCategory.BREAKABLE);
    public static final CreativeModeTab TAB_COMBAT = new CreativeModeTab(9, "combat") { // from class: net.minecraft.world.item.CreativeModeTab.9
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Items.GOLDEN_SWORD);
        }
    }.setEnchantmentCategories(EnchantmentCategory.VANISHABLE, EnchantmentCategory.ARMOR, EnchantmentCategory.ARMOR_FEET, EnchantmentCategory.ARMOR_HEAD, EnchantmentCategory.ARMOR_LEGS, EnchantmentCategory.ARMOR_CHEST, EnchantmentCategory.BOW, EnchantmentCategory.WEAPON, EnchantmentCategory.WEARABLE, EnchantmentCategory.BREAKABLE, EnchantmentCategory.TRIDENT, EnchantmentCategory.CROSSBOW);
    public static final CreativeModeTab TAB_BREWING = new CreativeModeTab(10, "brewing") { // from class: net.minecraft.world.item.CreativeModeTab.10
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        }
    };
    public static final CreativeModeTab TAB_MATERIALS = TAB_MISC;
    public static final CreativeModeTab TAB_HOTBAR = new CreativeModeTab(4, "hotbar") { // from class: net.minecraft.world.item.CreativeModeTab.11
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.BOOKSHELF);
        }

        @Override // net.minecraft.world.item.CreativeModeTab
        public void fillItemList(NonNullList<ItemStack> nonNullList) {
            throw new RuntimeException("Implement exception client-side.");
        }

        @Override // net.minecraft.world.item.CreativeModeTab
        public boolean isAlignedRight() {
            return true;
        }
    };
    public static final CreativeModeTab TAB_INVENTORY = new CreativeModeTab(11, "inventory") { // from class: net.minecraft.world.item.CreativeModeTab.12
        @Override // net.minecraft.world.item.CreativeModeTab
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.CHEST);
        }
    }.setBackgroundSuffix("inventory.png").hideScroll().hideTitle();

    /* renamed from: id */
    private final int id;
    private final String langId;
    private final Component displayName;
    private String recipeFolderName;
    private String backgroundSuffix = "items.png";
    private boolean canScroll = true;
    private boolean showTitle = true;
    private EnchantmentCategory[] enchantmentCategories = new EnchantmentCategory[0];
    private ItemStack iconItemStack = ItemStack.EMPTY;

    public abstract ItemStack makeIcon();

    public CreativeModeTab(int i, String str) {
        this.id = i;
        this.langId = str;
        this.displayName = new TranslatableComponent("itemGroup." + str);
        TABS[i] = this;
    }

    public int getId() {
        return this.id;
    }

    public String getRecipeFolderName() {
        return this.recipeFolderName == null ? this.langId : this.recipeFolderName;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIconItem() {
        if (this.iconItemStack.isEmpty()) {
            this.iconItemStack = makeIcon();
        }
        return this.iconItemStack;
    }

    public String getBackgroundSuffix() {
        return this.backgroundSuffix;
    }

    public CreativeModeTab setBackgroundSuffix(String str) {
        this.backgroundSuffix = str;
        return this;
    }

    public CreativeModeTab setRecipeFolderName(String str) {
        this.recipeFolderName = str;
        return this;
    }

    public boolean showTitle() {
        return this.showTitle;
    }

    public CreativeModeTab hideTitle() {
        this.showTitle = false;
        return this;
    }

    public boolean canScroll() {
        return this.canScroll;
    }

    public CreativeModeTab hideScroll() {
        this.canScroll = false;
        return this;
    }

    public int getColumn() {
        return this.id % 6;
    }

    public boolean isTopRow() {
        return this.id < 6;
    }

    public boolean isAlignedRight() {
        return getColumn() == 5;
    }

    public EnchantmentCategory[] getEnchantmentCategories() {
        return this.enchantmentCategories;
    }

    public CreativeModeTab setEnchantmentCategories(EnchantmentCategory... enchantmentCategoryArr) {
        this.enchantmentCategories = enchantmentCategoryArr;
        return this;
    }

    public boolean hasEnchantmentCategory(@Nullable EnchantmentCategory enchantmentCategory) {
        if (enchantmentCategory != null) {
            for (EnchantmentCategory enchantmentCategory2 : this.enchantmentCategories) {
                if (enchantmentCategory2 == enchantmentCategory) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public void fillItemList(NonNullList<ItemStack> nonNullList) {
        Iterator<Item> it = Registry.ITEM.iterator();
        while (it.hasNext()) {
            it.next().fillItemCategory(this, nonNullList);
        }
    }
}
