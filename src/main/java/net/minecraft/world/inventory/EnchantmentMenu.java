package net.minecraft.world.inventory;

import java.util.List;
import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/EnchantmentMenu.class */
public class EnchantmentMenu extends AbstractContainerMenu {
    private final Container enchantSlots;
    private final ContainerLevelAccess access;
    private final Random random;
    private final DataSlot enchantmentSeed;
    public final int[] costs;
    public final int[] enchantClue;
    public final int[] levelClue;

    public EnchantmentMenu(int i, Inventory inventory) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }

    public EnchantmentMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.ENCHANTMENT, i);
        this.enchantSlots = new SimpleContainer(2) { // from class: net.minecraft.world.inventory.EnchantmentMenu.1
            @Override // net.minecraft.world.SimpleContainer, net.minecraft.world.Container
            public void setChanged() {
                super.setChanged();
                EnchantmentMenu.this.slotsChanged(this);
            }
        };
        this.random = new Random();
        this.enchantmentSeed = DataSlot.standalone();
        this.costs = new int[3];
        this.enchantClue = new int[]{-1, -1, -1};
        this.levelClue = new int[]{-1, -1, -1};
        this.access = containerLevelAccess;
        addSlot(new Slot(this.enchantSlots, 0, 15, 47) { // from class: net.minecraft.world.inventory.EnchantmentMenu.2
            @Override // net.minecraft.world.inventory.Slot
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }

            @Override // net.minecraft.world.inventory.Slot
            public int getMaxStackSize() {
                return 1;
            }
        });
        addSlot(new Slot(this.enchantSlots, 1, 35, 47) { // from class: net.minecraft.world.inventory.EnchantmentMenu.3
            @Override // net.minecraft.world.inventory.Slot
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.getItem() == Items.LAPIS_LAZULI;
            }
        });
        for (int i2 = 0; i2 < 3; i2++) {
            for (int i3 = 0; i3 < 9; i3++) {
                addSlot(new Slot(inventory, i3 + (i2 * 9) + 9, 8 + (i3 * 18), 84 + (i2 * 18)));
            }
        }
        for (int i4 = 0; i4 < 9; i4++) {
            addSlot(new Slot(inventory, i4, 8 + (i4 * 18), 142));
        }
        addDataSlot(DataSlot.shared(this.costs, 0));
        addDataSlot(DataSlot.shared(this.costs, 1));
        addDataSlot(DataSlot.shared(this.costs, 2));
        addDataSlot(this.enchantmentSeed).set(inventory.player.getEnchantmentSeed());
        addDataSlot(DataSlot.shared(this.enchantClue, 0));
        addDataSlot(DataSlot.shared(this.enchantClue, 1));
        addDataSlot(DataSlot.shared(this.enchantClue, 2));
        addDataSlot(DataSlot.shared(this.levelClue, 0));
        addDataSlot(DataSlot.shared(this.levelClue, 1));
        addDataSlot(DataSlot.shared(this.levelClue, 2));
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void slotsChanged(Container container) {
        if (container == this.enchantSlots) {
            ItemStack item = container.getItem(0);
            if (item.isEmpty() || !item.isEnchantable()) {
                for (int i = 0; i < 3; i++) {
                    this.costs[i] = 0;
                    this.enchantClue[i] = -1;
                    this.levelClue[i] = -1;
                }
                return;
            }
            this.access.execute((level, blockPos) -> {
                List<EnchantmentInstance> enchantmentList;
                int i2 = 0;
                for (int i3 = -1; i3 <= 1; i3++) {
                    for (int i4 = -1; i4 <= 1; i4++) {
                        if ((i3 != 0 || i4 != 0) && level.isEmptyBlock(blockPos.offset(i4, 0, i3)) && level.isEmptyBlock(blockPos.offset(i4, 1, i3))) {
                            if (level.getBlockState(blockPos.offset(i4 * 2, 0, i3 * 2)).is(Blocks.BOOKSHELF)) {
                                i2++;
                            }
                            if (level.getBlockState(blockPos.offset(i4 * 2, 1, i3 * 2)).is(Blocks.BOOKSHELF)) {
                                i2++;
                            }
                            if (i4 != 0 && i3 != 0) {
                                if (level.getBlockState(blockPos.offset(i4 * 2, 0, i3)).is(Blocks.BOOKSHELF)) {
                                    i2++;
                                }
                                if (level.getBlockState(blockPos.offset(i4 * 2, 1, i3)).is(Blocks.BOOKSHELF)) {
                                    i2++;
                                }
                                if (level.getBlockState(blockPos.offset(i4, 0, i3 * 2)).is(Blocks.BOOKSHELF)) {
                                    i2++;
                                }
                                if (level.getBlockState(blockPos.offset(i4, 1, i3 * 2)).is(Blocks.BOOKSHELF)) {
                                    i2++;
                                }
                            }
                        }
                    }
                }
                this.random.setSeed(this.enchantmentSeed.get());
                for (int i5 = 0; i5 < 3; i5++) {
                    this.costs[i5] = EnchantmentHelper.getEnchantmentCost(this.random, i5, i2, item);
                    this.enchantClue[i5] = -1;
                    this.levelClue[i5] = -1;
                    if (this.costs[i5] < i5 + 1) {
                        this.costs[i5] = 0;
                    }
                }
                for (int i6 = 0; i6 < 3; i6++) {
                    if (this.costs[i6] > 0 && (enchantmentList = getEnchantmentList(item, i6, this.costs[i6])) != null && !enchantmentList.isEmpty()) {
                        EnchantmentInstance enchantmentInstance = enchantmentList.get(this.random.nextInt(enchantmentList.size()));
                        this.enchantClue[i6] = Registry.ENCHANTMENT.getId(enchantmentInstance.enchantment);
                        this.levelClue[i6] = enchantmentInstance.level;
                    }
                }
                broadcastChanges();
            });
        }
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean clickMenuButton(Player player, int i) {
        ItemStack item = this.enchantSlots.getItem(0);
        ItemStack item2 = this.enchantSlots.getItem(1);
        int i2 = i + 1;
        if (((item2.isEmpty() || item2.getCount() < i2) && !player.abilities.instabuild) || this.costs[i] <= 0 || item.isEmpty()) {
            return false;
        }
        if ((player.experienceLevel >= i2 && player.experienceLevel >= this.costs[i]) || player.abilities.instabuild) {
            this.access.execute((level, blockPos) -> {
                ItemStack itemStack = item;
                List<EnchantmentInstance> enchantmentList = getEnchantmentList(itemStack, i, this.costs[i]);
                if (!enchantmentList.isEmpty()) {
                    player.onEnchantmentPerformed(itemStack, i2);
                    boolean z = itemStack.getItem() == Items.BOOK;
                    if (z) {
                        itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                        CompoundTag tag = item.getTag();
                        if (tag != null) {
                            itemStack.setTag(tag.copy());
                        }
                        this.enchantSlots.setItem(0, itemStack);
                    }
                    for (int i3 = 0; i3 < enchantmentList.size(); i3++) {
                        EnchantmentInstance enchantmentInstance = enchantmentList.get(i3);
                        if (z) {
                            EnchantedBookItem.addEnchantment(itemStack, enchantmentInstance);
                        } else {
                            itemStack.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
                        }
                    }
                    if (!player.abilities.instabuild) {
                        item2.shrink(i2);
                        if (item2.isEmpty()) {
                            this.enchantSlots.setItem(1, ItemStack.EMPTY);
                        }
                    }
                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer) player, itemStack, i2);
                    }
                    this.enchantSlots.setChanged();
                    this.enchantmentSeed.set(player.getEnchantmentSeed());
                    slotsChanged(this.enchantSlots);
                    level.playSound((Player) null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, (level.random.nextFloat() * 0.1f) + 0.9f);
                }
            });
            return true;
        }
        return false;
    }

    private List<EnchantmentInstance> getEnchantmentList(ItemStack itemStack, int i, int i2) {
        this.random.setSeed(this.enchantmentSeed.get() + i);
        List<EnchantmentInstance> selectEnchantment = EnchantmentHelper.selectEnchantment(this.random, itemStack, i2, false);
        if (itemStack.getItem() == Items.BOOK && selectEnchantment.size() > 1) {
            selectEnchantment.remove(this.random.nextInt(selectEnchantment.size()));
        }
        return selectEnchantment;
    }

    public int getGoldCount() {
        ItemStack item = this.enchantSlots.getItem(1);
        if (item.isEmpty()) {
            return 0;
        }
        return item.getCount();
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed.get();
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> {
            clearContainer(player, player.level, this.enchantSlots);
        });
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            if (i == 0) {
                if (!moveItemStackTo(item, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (i == 1) {
                if (!moveItemStackTo(item, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (item.getItem() == Items.LAPIS_LAZULI) {
                if (!moveItemStackTo(item, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.slots.get(0).hasItem() && this.slots.get(0).mayPlace(item)) {
                ItemStack copy = item.copy();
                copy.setCount(1);
                item.shrink(1);
                this.slots.get(0).set(copy);
            } else {
                return ItemStack.EMPTY;
            }
            if (item.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (item.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, item);
        }
        return itemStack;
    }
}
