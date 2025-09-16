package net.minecraft.world.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/AbstractContainerMenu.class */
public abstract class AbstractContainerMenu {

    @Nullable
    private final MenuType<?> menuType;
    public final int containerId;
    private short changeUid;
    private int quickcraftStatus;
    private final NonNullList<ItemStack> lastSlots = NonNullList.create();
    public final List<Slot> slots = Lists.newArrayList();
    private final List<DataSlot> dataSlots = Lists.newArrayList();
    private int quickcraftType = -1;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();
    private final List<ContainerListener> containerListeners = Lists.newArrayList();
    private final Set<Player> unSynchedPlayers = Sets.newHashSet();

    public abstract boolean stillValid(Player player);

    protected AbstractContainerMenu(@Nullable MenuType<?> menuType, int i) {
        this.menuType = menuType;
        this.containerId = i;
    }

    protected static boolean stillValid(ContainerLevelAccess containerLevelAccess, Player player, Block block) {
        return ((Boolean) containerLevelAccess.evaluate((level, blockPos) -> {
            if (level.getBlockState(blockPos).is(block)) {
                return Boolean.valueOf(player.distanceToSqr(((double) blockPos.getX()) + 0.5d, ((double) blockPos.getY()) + 0.5d, ((double) blockPos.getZ()) + 0.5d) <= 64.0d);
            }
            return false;
        }, true)).booleanValue();
    }

    public MenuType<?> getType() {
        if (this.menuType == null) {
            throw new UnsupportedOperationException("Unable to construct this menu by type");
        }
        return this.menuType;
    }

    protected static void checkContainerSize(Container container, int i) {
        int containerSize = container.getContainerSize();
        if (containerSize < i) {
            throw new IllegalArgumentException("Container size " + containerSize + " is smaller than expected " + i);
        }
    }

    protected static void checkContainerDataCount(ContainerData containerData, int i) {
        int count = containerData.getCount();
        if (count < i) {
            throw new IllegalArgumentException("Container data count " + count + " is smaller than expected " + i);
        }
    }

    protected Slot addSlot(Slot slot) {
        slot.index = this.slots.size();
        this.slots.add(slot);
        this.lastSlots.add(ItemStack.EMPTY);
        return slot;
    }

    protected DataSlot addDataSlot(DataSlot dataSlot) {
        this.dataSlots.add(dataSlot);
        return dataSlot;
    }

    protected void addDataSlots(ContainerData containerData) {
        for (int i = 0; i < containerData.getCount(); i++) {
            addDataSlot(DataSlot.forContainer(containerData, i));
        }
    }

    public void addSlotListener(ContainerListener containerListener) {
        if (this.containerListeners.contains(containerListener)) {
            return;
        }
        this.containerListeners.add(containerListener);
        containerListener.refreshContainer(this, getItems());
        broadcastChanges();
    }

    public void removeSlotListener(ContainerListener containerListener) {
        this.containerListeners.remove(containerListener);
    }

    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> create = NonNullList.create();
        for (int i = 0; i < this.slots.size(); i++) {
            create.add(this.slots.get(i).getItem());
        }
        return create;
    }

    public void broadcastChanges() {
        for (int i = 0; i < this.slots.size(); i++) {
            ItemStack item = this.slots.get(i).getItem();
            if (!ItemStack.matches(this.lastSlots.get(i), item)) {
                ItemStack copy = item.copy();
                this.lastSlots.set(i, copy);
                Iterator<ContainerListener> it = this.containerListeners.iterator();
                while (it.hasNext()) {
                    it.next().slotChanged(this, i, copy);
                }
            }
        }
        for (int i2 = 0; i2 < this.dataSlots.size(); i2++) {
            DataSlot dataSlot = this.dataSlots.get(i2);
            if (dataSlot.checkAndClearUpdateFlag()) {
                Iterator<ContainerListener> it2 = this.containerListeners.iterator();
                while (it2.hasNext()) {
                    it2.next().setContainerData(this, i2, dataSlot.get());
                }
            }
        }
    }

    public boolean clickMenuButton(Player player, int i) {
        return false;
    }

    public Slot getSlot(int i) {
        return this.slots.get(i);
    }

    public ItemStack quickMoveStack(Player player, int i) {
        Slot slot = this.slots.get(i);
        if (slot != null) {
            return slot.getItem();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack clicked(int i, int i2, ClickType clickType, Player player) {
        try {
            return doClick(i, i2, clickType, player);
        } catch (Exception e) {
            CrashReport forThrowable = CrashReport.forThrowable(e, "Container click");
            CrashReportCategory addCategory = forThrowable.addCategory("Click info");
            addCategory.setDetail("Menu Type", () -> {
                return this.menuType != null ? Registry.MENU.getKey(this.menuType).toString() : "<no type>";
            });
            addCategory.setDetail("Menu Class", () -> {
                return getClass().getCanonicalName();
            });
            addCategory.setDetail("Slot Count", Integer.valueOf(this.slots.size()));
            addCategory.setDetail("Slot", Integer.valueOf(i));
            addCategory.setDetail("Button", Integer.valueOf(i2));
            addCategory.setDetail("Type", clickType);
            throw new ReportedException(forThrowable);
        }
    }

    private ItemStack doClick(int i, int i2, ClickType clickType, Player player) {
        ItemStack itemStack = ItemStack.EMPTY;
        Inventory inventory = player.inventory;
        if (clickType == ClickType.QUICK_CRAFT) {
            int i3 = this.quickcraftStatus;
            this.quickcraftStatus = getQuickcraftHeader(i2);
            if ((i3 != 1 || this.quickcraftStatus != 2) && i3 != this.quickcraftStatus) {
                resetQuickCraft();
            } else if (inventory.getCarried().isEmpty()) {
                resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = getQuickcraftType(i2);
                if (isValidQuickcraftType(this.quickcraftType, player)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                Slot slot = this.slots.get(i);
                ItemStack carried = inventory.getCarried();
                if (slot != null && canItemQuickReplace(slot, carried, true) && slot.mayPlace(carried) && ((this.quickcraftType == 2 || carried.getCount() > this.quickcraftSlots.size()) && canDragTo(slot))) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    ItemStack copy = inventory.getCarried().copy();
                    int count = inventory.getCarried().getCount();
                    for (Slot slot2 : this.quickcraftSlots) {
                        ItemStack carried2 = inventory.getCarried();
                        if (slot2 != null && canItemQuickReplace(slot2, carried2, true) && slot2.mayPlace(carried2) && (this.quickcraftType == 2 || carried2.getCount() >= this.quickcraftSlots.size())) {
                            if (canDragTo(slot2)) {
                                ItemStack copy2 = copy.copy();
                                int count2 = slot2.hasItem() ? slot2.getItem().getCount() : 0;
                                getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, copy2, count2);
                                int min = Math.min(copy2.getMaxStackSize(), slot2.getMaxStackSize(copy2));
                                if (copy2.getCount() > min) {
                                    copy2.setCount(min);
                                }
                                count -= copy2.getCount() - count2;
                                slot2.set(copy2);
                            }
                        }
                    }
                    copy.setCount(count);
                    inventory.setCarried(copy);
                }
                resetQuickCraft();
            } else {
                resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            resetQuickCraft();
        } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (i2 == 0 || i2 == 1)) {
            if (i == -999) {
                if (!inventory.getCarried().isEmpty()) {
                    if (i2 == 0) {
                        player.drop(inventory.getCarried(), true);
                        inventory.setCarried(ItemStack.EMPTY);
                    }
                    if (i2 == 1) {
                        player.drop(inventory.getCarried().split(1), true);
                    }
                }
            } else if (clickType == ClickType.QUICK_MOVE) {
                if (i < 0) {
                    return ItemStack.EMPTY;
                }
                Slot slot3 = this.slots.get(i);
                if (slot3 == null || !slot3.mayPickup(player)) {
                    return ItemStack.EMPTY;
                }
                ItemStack quickMoveStack = quickMoveStack(player, i);
                while (true) {
                    ItemStack itemStack2 = quickMoveStack;
                    if (itemStack2.isEmpty() || !ItemStack.isSame(slot3.getItem(), itemStack2)) {
                        break;
                    }
                    itemStack = itemStack2.copy();
                    quickMoveStack = quickMoveStack(player, i);
                }
            } else {
                if (i < 0) {
                    return ItemStack.EMPTY;
                }
                Slot slot4 = this.slots.get(i);
                if (slot4 != null) {
                    ItemStack item = slot4.getItem();
                    ItemStack carried3 = inventory.getCarried();
                    if (!item.isEmpty()) {
                        itemStack = item.copy();
                    }
                    if (item.isEmpty()) {
                        if (!carried3.isEmpty() && slot4.mayPlace(carried3)) {
                            int count3 = i2 == 0 ? carried3.getCount() : 1;
                            if (count3 > slot4.getMaxStackSize(carried3)) {
                                count3 = slot4.getMaxStackSize(carried3);
                            }
                            slot4.set(carried3.split(count3));
                        }
                    } else if (slot4.mayPickup(player)) {
                        if (carried3.isEmpty()) {
                            if (item.isEmpty()) {
                                slot4.set(ItemStack.EMPTY);
                                inventory.setCarried(ItemStack.EMPTY);
                            } else {
                                inventory.setCarried(slot4.remove(i2 == 0 ? item.getCount() : (item.getCount() + 1) / 2));
                                if (item.isEmpty()) {
                                    slot4.set(ItemStack.EMPTY);
                                }
                                slot4.onTake(player, inventory.getCarried());
                            }
                        } else if (slot4.mayPlace(carried3)) {
                            if (consideredTheSameItem(item, carried3)) {
                                int count4 = i2 == 0 ? carried3.getCount() : 1;
                                if (count4 > slot4.getMaxStackSize(carried3) - item.getCount()) {
                                    count4 = slot4.getMaxStackSize(carried3) - item.getCount();
                                }
                                if (count4 > carried3.getMaxStackSize() - item.getCount()) {
                                    count4 = carried3.getMaxStackSize() - item.getCount();
                                }
                                carried3.shrink(count4);
                                item.grow(count4);
                            } else if (carried3.getCount() <= slot4.getMaxStackSize(carried3)) {
                                slot4.set(carried3);
                                inventory.setCarried(item);
                            }
                        } else if (carried3.getMaxStackSize() > 1 && consideredTheSameItem(item, carried3) && !item.isEmpty()) {
                            int count5 = item.getCount();
                            if (count5 + carried3.getCount() <= carried3.getMaxStackSize()) {
                                carried3.grow(count5);
                                if (slot4.remove(count5).isEmpty()) {
                                    slot4.set(ItemStack.EMPTY);
                                }
                                slot4.onTake(player, inventory.getCarried());
                            }
                        }
                    }
                    slot4.setChanged();
                }
            }
        } else if (clickType == ClickType.SWAP) {
            Slot slot5 = this.slots.get(i);
            ItemStack item2 = inventory.getItem(i2);
            ItemStack item3 = slot5.getItem();
            if (!item2.isEmpty() || !item3.isEmpty()) {
                if (item2.isEmpty()) {
                    if (slot5.mayPickup(player)) {
                        inventory.setItem(i2, item3);
                        slot5.onSwapCraft(item3.getCount());
                        slot5.set(ItemStack.EMPTY);
                        slot5.onTake(player, item3);
                    }
                } else if (item3.isEmpty()) {
                    if (slot5.mayPlace(item2)) {
                        int maxStackSize = slot5.getMaxStackSize(item2);
                        if (item2.getCount() > maxStackSize) {
                            slot5.set(item2.split(maxStackSize));
                        } else {
                            slot5.set(item2);
                            inventory.setItem(i2, ItemStack.EMPTY);
                        }
                    }
                } else if (slot5.mayPickup(player) && slot5.mayPlace(item2)) {
                    int maxStackSize2 = slot5.getMaxStackSize(item2);
                    if (item2.getCount() > maxStackSize2) {
                        slot5.set(item2.split(maxStackSize2));
                        slot5.onTake(player, item3);
                        if (!inventory.add(item3)) {
                            player.drop(item3, true);
                        }
                    } else {
                        slot5.set(item2);
                        inventory.setItem(i2, item3);
                        slot5.onTake(player, item3);
                    }
                }
            }
        } else if (clickType == ClickType.CLONE && player.abilities.instabuild && inventory.getCarried().isEmpty() && i >= 0) {
            Slot slot6 = this.slots.get(i);
            if (slot6 != null && slot6.hasItem()) {
                ItemStack copy3 = slot6.getItem().copy();
                copy3.setCount(copy3.getMaxStackSize());
                inventory.setCarried(copy3);
            }
        } else if (clickType == ClickType.THROW && inventory.getCarried().isEmpty() && i >= 0) {
            Slot slot7 = this.slots.get(i);
            if (slot7 != null && slot7.hasItem() && slot7.mayPickup(player)) {
                ItemStack remove = slot7.remove(i2 == 0 ? 1 : slot7.getItem().getCount());
                slot7.onTake(player, remove);
                player.drop(remove, true);
            }
        } else if (clickType == ClickType.PICKUP_ALL && i >= 0) {
            Slot slot8 = this.slots.get(i);
            ItemStack carried4 = inventory.getCarried();
            if (!carried4.isEmpty() && (slot8 == null || !slot8.hasItem() || !slot8.mayPickup(player))) {
                int size = i2 == 0 ? 0 : this.slots.size() - 1;
                int i4 = i2 == 0 ? 1 : -1;
                for (int i5 = 0; i5 < 2; i5++) {
                    int i6 = size;
                    while (true) {
                        int i7 = i6;
                        if (i7 >= 0 && i7 < this.slots.size() && carried4.getCount() < carried4.getMaxStackSize()) {
                            Slot slot9 = this.slots.get(i7);
                            if (slot9.hasItem() && canItemQuickReplace(slot9, carried4, true) && slot9.mayPickup(player) && canTakeItemForPickAll(carried4, slot9)) {
                                ItemStack item4 = slot9.getItem();
                                if (i5 != 0 || item4.getCount() != item4.getMaxStackSize()) {
                                    int min2 = Math.min(carried4.getMaxStackSize() - carried4.getCount(), item4.getCount());
                                    ItemStack remove2 = slot9.remove(min2);
                                    carried4.grow(min2);
                                    if (remove2.isEmpty()) {
                                        slot9.set(ItemStack.EMPTY);
                                    }
                                    slot9.onTake(player, remove2);
                                }
                            }
                            i6 = i7 + i4;
                        }
                    }
                }
            }
            broadcastChanges();
        }
        return itemStack;
    }

    public static boolean consideredTheSameItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.getItem() == itemStack2.getItem() && ItemStack.tagMatches(itemStack, itemStack2);
    }

    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return true;
    }

    public void removed(Player player) {
        Inventory inventory = player.inventory;
        if (!inventory.getCarried().isEmpty()) {
            player.drop(inventory.getCarried(), false);
            inventory.setCarried(ItemStack.EMPTY);
        }
    }

    protected void clearContainer(Player player, Level level, Container container) {
        if (!player.isAlive() || ((player instanceof ServerPlayer) && ((ServerPlayer) player).hasDisconnected())) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                player.drop(container.removeItemNoUpdate(i), false);
            }
            return;
        }
        for (int i2 = 0; i2 < container.getContainerSize(); i2++) {
            player.inventory.placeItemBackInInventory(level, container.removeItemNoUpdate(i2));
        }
    }

    public void slotsChanged(Container container) {
        broadcastChanges();
    }

    public void setItem(int i, ItemStack itemStack) {
        getSlot(i).set(itemStack);
    }

    public void setAll(List<ItemStack> list) {
        for (int i = 0; i < list.size(); i++) {
            getSlot(i).set(list.get(i));
        }
    }

    public void setData(int i, int i2) {
        this.dataSlots.get(i).set(i2);
    }

    public short backup(Inventory inventory) {
        this.changeUid = (short) (this.changeUid + 1);
        return this.changeUid;
    }

    public boolean isSynched(Player player) {
        return !this.unSynchedPlayers.contains(player);
    }

    public void setSynched(Player player, boolean z) {
        if (z) {
            this.unSynchedPlayers.remove(player);
        } else {
            this.unSynchedPlayers.add(player);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x006f  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0086  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x00bc A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:28:0x00b6 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:58:0x0155 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:62:0x014f A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected boolean moveItemStackTo(net.minecraft.world.item.ItemStack var1, int var2, int var3, boolean var4) {
        boolean var5 = false;
        int var6x = var2;
        if (var4) {
            var6x = var3 - 1;
        }

        if (var1.isStackable()) {
            while(!var1.isEmpty() && (var4 ? var6x >= var3 : var6x < var2)) {
                Slot var7 = this.slots.get(var6x);
                ItemStack var8x = var7.getItem();
                if (!var8x.isEmpty() && consideredTheSameItem(var1, var8x)) {
                    int var9xx = var8x.getCount() + var1.getCount();
                    if (var9xx <= var1.getMaxStackSize()) {
                        var1.setCount(0);
                        var8x.setCount(var9xx);
                        var7.setChanged();
                        var5 = true;
                    } else if (var8x.getCount() < var1.getMaxStackSize()) {
                        var1.shrink(var1.getMaxStackSize() - var8x.getCount());
                        var8x.setCount(var1.getMaxStackSize());
                  var7.setChanged();
                        var5 = true;
                    }
                }

                if (var4) {
                    --var6x;
                } else {
                    ++var6x;
                }
            }
        }

        if (!var1.isEmpty()) {
            if (var4) {
                var6x = var3 - 1;
            } else {
                var6x = var2;
            }

            while(var4 ? var6x >= var3 : var6x < var2) {
                Slot var11 = this.slots.get(var6x);
                ItemStack var12x = var11.getItem();
                if (var12x.isEmpty() && var11.mayPlace(var1)) {
                    if (var1.getCount() > var11.getMaxStackSize()) {
                        var11.set(var1.split(var11.getMaxStackSize()));
                    } else {
                        var11.set(var1.split(var1.getCount()));
                    }

                    var11.setChanged();
                    var5 = true;
                    break;
                }

                if (var4) {
                    --var6x;
                } else {
                    ++var6x;
                }
            }
        }

        return var5;
    }

    public static int getQuickcraftType(int i) {
        return (i >> 2) & 3;
    }

    public static int getQuickcraftHeader(int i) {
        return i & 3;
    }

    public static int getQuickcraftMask(int i, int i2) {
        return (i & 3) | ((i2 & 3) << 2);
    }

    public static boolean isValidQuickcraftType(int i, Player player) {
        if (i == 0 || i == 1) {
            return true;
        }
        if (i == 2 && player.abilities.instabuild) {
            return true;
        }
        return false;
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack itemStack, boolean z) {
        boolean z2 = slot == null || !slot.hasItem();
        if (!z2 && itemStack.sameItem(slot.getItem()) && ItemStack.tagMatches(slot.getItem(), itemStack)) {
            return slot.getItem().getCount() + (z ? 0 : itemStack.getCount()) <= itemStack.getMaxStackSize();
        }
        return z2;
    }

    public static void getQuickCraftSlotCount(Set<Slot> set, int i, ItemStack itemStack, int i2) {
        switch (i) {
            case 0:
                itemStack.setCount(Mth.floor(itemStack.getCount() / set.size()));
                break;
            case 1:
                itemStack.setCount(1);
                break;
            case 2:
                itemStack.setCount(itemStack.getItem().getMaxStackSize());
                break;
        }
        itemStack.grow(i2);
    }

    public boolean canDragTo(Slot slot) {
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static int getRedstoneSignalFromBlockEntity(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof Container) {
            return getRedstoneSignalFromContainer((Container) blockEntity);
        }
        return 0;
    }

    public static int getRedstoneSignalFromContainer(@Nullable Container container) {
        if (container == null) {
            return 0;
        } else {
            int var1 = 0;
            float var2 = 0.0F;

            for(int var3 = 0; var3 < container.getContainerSize(); ++var3) {
                ItemStack var4 = container.getItem(var3);
                if (!var4.isEmpty()) {
                    var2 += (float)var4.getCount() / (float)Math.min(container.getMaxStackSize(), var4.getMaxStackSize());
                    ++var1;
                }
            }

            var2 /= (float)container.getContainerSize();
            return Mth.floor(var2 * 14.0F) + (var1 > 0 ? 1 : 0);
        }
    }
}
