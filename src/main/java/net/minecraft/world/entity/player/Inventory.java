package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/player/Inventory.class */
public class Inventory implements Container, Nameable {
    public int selected;
    public final Player player;
    private int timesChanged;
    public final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
    public final NonNullList<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
    public final NonNullList<ItemStack> offhand = NonNullList.withSize(1, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> compartments = ImmutableList.of(this.items, this.armor, this.offhand);
    private ItemStack carried = ItemStack.EMPTY;

    public Inventory(Player player) {
        this.player = player;
    }

    public ItemStack getSelected() {
        if (isHotbarSlot(this.selected)) {
            return this.items.get(this.selected);
        }
        return ItemStack.EMPTY;
    }

    public static int getSelectionSize() {
        return 9;
    }

    private boolean hasRemainingSpaceForItem(ItemStack itemStack, ItemStack itemStack2) {
        return !itemStack.isEmpty() && isSameItem(itemStack, itemStack2) && itemStack.isStackable() && itemStack.getCount() < itemStack.getMaxStackSize() && itemStack.getCount() < getMaxStackSize();
    }

    private boolean isSameItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.getItem() == itemStack2.getItem() && ItemStack.tagMatches(itemStack, itemStack2);
    }

    public int getFreeSlot() {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public void setPickedItem(ItemStack itemStack) {
        int freeSlot;
        int findSlotMatchingItem = findSlotMatchingItem(itemStack);
        if (isHotbarSlot(findSlotMatchingItem)) {
            this.selected = findSlotMatchingItem;
            return;
        }
        if (findSlotMatchingItem == -1) {
            this.selected = getSuitableHotbarSlot();
            if (!this.items.get(this.selected).isEmpty() && (freeSlot = getFreeSlot()) != -1) {
                this.items.set(freeSlot, this.items.get(this.selected));
            }
            this.items.set(this.selected, itemStack);
            return;
        }
        pickSlot(findSlotMatchingItem);
    }

    public void pickSlot(int i) {
        this.selected = getSuitableHotbarSlot();
        ItemStack itemStack = this.items.get(this.selected);
        this.items.set(this.selected, this.items.get(i));
        this.items.set(i, itemStack);
    }

    public static boolean isHotbarSlot(int i) {
        return i >= 0 && i < 9;
    }

    public int findSlotMatchingItem(ItemStack itemStack) {
        for (int i = 0; i < this.items.size(); i++) {
            if (!this.items.get(i).isEmpty() && isSameItem(itemStack, this.items.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public int findSlotMatchingUnusedItem(ItemStack itemStack) {
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemStack2 = this.items.get(i);
            if (!this.items.get(i).isEmpty() && isSameItem(itemStack, this.items.get(i)) && !this.items.get(i).isDamaged() && !itemStack2.isEnchanted() && !itemStack2.hasCustomHoverName()) {
                return i;
            }
        }
        return -1;
    }

    public int getSuitableHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            int i2 = (this.selected + i) % 9;
            if (this.items.get(i2).isEmpty()) {
                return i2;
            }
        }
        for (int i3 = 0; i3 < 9; i3++) {
            int i4 = (this.selected + i3) % 9;
            if (!this.items.get(i4).isEnchanted()) {
                return i4;
            }
        }
        return this.selected;
    }

    public void swapPaint(double d) {
        if (d > 0.0d) {
            d = 1.0d;
        }
        if (d < 0.0d) {
            d = -1.0d;
        }
        this.selected = (int) (this.selected - d);
        while (this.selected < 0) {
            this.selected += 9;
        }
        while (this.selected >= 9) {
            this.selected -= 9;
        }
    }

    public int clearOrCountMatchingItems(Predicate<ItemStack> predicate, int i, Container container) {
        boolean z = i == 0;
        int clearOrCountMatchingItems = 0 + ContainerHelper.clearOrCountMatchingItems(this, predicate, i - 0, z);
        int clearOrCountMatchingItems2 = clearOrCountMatchingItems + ContainerHelper.clearOrCountMatchingItems(container, predicate, i - clearOrCountMatchingItems, z);
        int clearOrCountMatchingItems3 = clearOrCountMatchingItems2 + ContainerHelper.clearOrCountMatchingItems(this.carried, predicate, i - clearOrCountMatchingItems2, z);
        if (this.carried.isEmpty()) {
            this.carried = ItemStack.EMPTY;
        }
        return clearOrCountMatchingItems3;
    }

    private int addResource(ItemStack itemStack) {
        int slotWithRemainingSpace = getSlotWithRemainingSpace(itemStack);
        if (slotWithRemainingSpace == -1) {
            slotWithRemainingSpace = getFreeSlot();
        }
        if (slotWithRemainingSpace == -1) {
            return itemStack.getCount();
        }
        return addResource(slotWithRemainingSpace, itemStack);
    }

    private int addResource(int i, ItemStack itemStack) {
        Item item = itemStack.getItem();
        int count = itemStack.getCount();
        ItemStack item2 = getItem(i);
        if (item2.isEmpty()) {
            item2 = new ItemStack(item, 0);
            if (itemStack.hasTag()) {
                item2.setTag(itemStack.getTag().copy());
            }
            setItem(i, item2);
        }
        int i2 = count;
        if (i2 > item2.getMaxStackSize() - item2.getCount()) {
            i2 = item2.getMaxStackSize() - item2.getCount();
        }
        if (i2 > getMaxStackSize() - item2.getCount()) {
            i2 = getMaxStackSize() - item2.getCount();
        }
        if (i2 == 0) {
            return count;
        }
        int i3 = count - i2;
        item2.grow(i2);
        item2.setPopTime(5);
        return i3;
    }

    public int getSlotWithRemainingSpace(ItemStack itemStack) {
        if (hasRemainingSpaceForItem(getItem(this.selected), itemStack)) {
            return this.selected;
        }
        if (hasRemainingSpaceForItem(getItem(40), itemStack)) {
            return 40;
        }
        for (int i = 0; i < this.items.size(); i++) {
            if (hasRemainingSpaceForItem(this.items.get(i), itemStack)) {
                return i;
            }
        }
        return -1;
    }

    public void tick() {
        for (NonNullList<ItemStack> nonNullList : this.compartments) {
            int i = 0;
            while (i < nonNullList.size()) {
                if (!nonNullList.get(i).isEmpty()) {
                    nonNullList.get(i).inventoryTick(this.player.level, this.player, i, this.selected == i);
                }
                i++;
            }
        }
    }

    public boolean add(ItemStack itemStack) {
        return add(-1, itemStack);
    }

    public boolean add(int i, ItemStack itemStack) {
        int count;
        if (itemStack.isEmpty()) {
            return false;
        }
        try {
            if (!itemStack.isDamaged()) {
                do {
                    count = itemStack.getCount();
                    if (i == -1) {
                        itemStack.setCount(addResource(itemStack));
                    } else {
                        itemStack.setCount(addResource(i, itemStack));
                    }
                    if (itemStack.isEmpty()) {
                        break;
                    }
                } while (itemStack.getCount() < count);
                if (itemStack.getCount() != count || !this.player.abilities.instabuild) {
                    return itemStack.getCount() < count;
                }
                itemStack.setCount(0);
                return true;
            }
            if (i == -1) {
                i = getFreeSlot();
            }
            if (i >= 0) {
                this.items.set(i, itemStack.copy());
                this.items.get(i).setPopTime(5);
                itemStack.setCount(0);
                return true;
            }
            if (this.player.abilities.instabuild) {
                itemStack.setCount(0);
                return true;
            }
            return false;
        } catch (Throwable th) {
            CrashReport forThrowable = CrashReport.forThrowable(th, "Adding item to inventory");
            CrashReportCategory addCategory = forThrowable.addCategory("Item being added");
            addCategory.setDetail("Item ID", Integer.valueOf(Item.getId(itemStack.getItem())));
            addCategory.setDetail("Item data", Integer.valueOf(itemStack.getDamageValue()));
            addCategory.setDetail("Item name", () -> {
                return itemStack.getHoverName().getString();
            });
            throw new ReportedException(forThrowable);
        }
    }

    public void placeItemBackInInventory(Level level, ItemStack itemStack) {
        if (level.isClientSide) {
            return;
        }
        while (!itemStack.isEmpty()) {
            int slotWithRemainingSpace = getSlotWithRemainingSpace(itemStack);
            if (slotWithRemainingSpace == -1) {
                slotWithRemainingSpace = getFreeSlot();
            }
            if (slotWithRemainingSpace == -1) {
                this.player.drop(itemStack, false);
                return;
            } else {
                if (add(slotWithRemainingSpace, itemStack.split(itemStack.getMaxStackSize() - getItem(slotWithRemainingSpace).getCount()))) {
                    ((ServerPlayer) this.player).connection.send(new ClientboundContainerSetSlotPacket(-2, slotWithRemainingSpace, getItem(slotWithRemainingSpace)));
                }
            }
        }
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItem(int i, int i2) {
        List<ItemStack> list = null;
        Iterator<NonNullList<ItemStack>> it = this.compartments.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            NonNullList<ItemStack> next = it.next();
            if (i < next.size()) {
                list = next;
                break;
            }
            i -= next.size();
        }
        if (list != null && !list.get(i).isEmpty()) {
            return ContainerHelper.removeItem(list, i, i2);
        }
        return ItemStack.EMPTY;
    }

    public void removeItem(ItemStack itemStack) {
        for (NonNullList<ItemStack> nonNullList : this.compartments) {
            int i = 0;
            while (true) {
                if (i >= nonNullList.size()) {
                    break;
                }
                if (nonNullList.get(i) != itemStack) {
                    i++;
                } else {
                    nonNullList.set(i, ItemStack.EMPTY);
                    break;
                }
            }
        }
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItemNoUpdate(int i) {
        NonNullList<ItemStack> nonNullList = null;
        Iterator<NonNullList<ItemStack>> it = this.compartments.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            NonNullList<ItemStack> next = it.next();
            if (i < next.size()) {
                nonNullList = next;
                break;
            }
            i -= next.size();
        }
        if (nonNullList != null && !nonNullList.get(i).isEmpty()) {
            ItemStack itemStack = nonNullList.get(i);
            nonNullList.set(i, ItemStack.EMPTY);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.Container
    public void setItem(int i, ItemStack itemStack) {
        NonNullList<ItemStack> nonNullList = null;
        Iterator<NonNullList<ItemStack>> it = this.compartments.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            NonNullList<ItemStack> next = it.next();
            if (i < next.size()) {
                nonNullList = next;
                break;
            }
            i -= next.size();
        }
        if (nonNullList != null) {
            nonNullList.set(i, itemStack);
        }
    }

    public float getDestroySpeed(BlockState blockState) {
        return this.items.get(this.selected).getDestroySpeed(blockState);
    }

    public ListTag save(ListTag listTag) {
        for (int i = 0; i < this.items.size(); i++) {
            if (!this.items.get(i).isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte) i);
                this.items.get(i).save(compoundTag);
                listTag.add(compoundTag);
            }
        }
        for (int i2 = 0; i2 < this.armor.size(); i2++) {
            if (!this.armor.get(i2).isEmpty()) {
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.putByte("Slot", (byte) (i2 + 100));
                this.armor.get(i2).save(compoundTag2);
                listTag.add(compoundTag2);
            }
        }
        for (int i3 = 0; i3 < this.offhand.size(); i3++) {
            if (!this.offhand.get(i3).isEmpty()) {
                CompoundTag compoundTag3 = new CompoundTag();
                compoundTag3.putByte("Slot", (byte) (i3 + 150));
                this.offhand.get(i3).save(compoundTag3);
                listTag.add(compoundTag3);
            }
        }
        return listTag;
    }

    public void load(ListTag listTag) {
        this.items.clear();
        this.armor.clear();
        this.offhand.clear();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compound = listTag.getCompound(i);
            int i2 = compound.getByte("Slot") & 255;
            ItemStack m66of = ItemStack.of(compound);
            if (!m66of.isEmpty()) {
                if (i2 >= 0 && i2 < this.items.size()) {
                    this.items.set(i2, m66of);
                } else if (i2 >= 100 && i2 < this.armor.size() + 100) {
                    this.armor.set(i2 - 100, m66of);
                } else if (i2 >= 150 && i2 < this.offhand.size() + 150) {
                    this.offhand.set(i2 - 150, m66of);
                }
            }
        }
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return this.items.size() + this.armor.size() + this.offhand.size();
    }

    @Override // net.minecraft.world.Container
    public boolean isEmpty() {
        Iterator<ItemStack> it = this.items.iterator();
        while (it.hasNext()) {
            if (!it.next().isEmpty()) {
                return false;
            }
        }
        Iterator<ItemStack> it2 = this.armor.iterator();
        while (it2.hasNext()) {
            if (!it2.next().isEmpty()) {
                return false;
            }
        }
        Iterator<ItemStack> it3 = this.offhand.iterator();
        while (it3.hasNext()) {
            if (!it3.next().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.Container
    public ItemStack getItem(int i) {
        List<ItemStack> list = null;
        Iterator<NonNullList<ItemStack>> it = this.compartments.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            NonNullList<ItemStack> next = it.next();
            if (i < next.size()) {
                list = next;
                break;
            }
            i -= next.size();
        }
        return list == null ? ItemStack.EMPTY : list.get(i);
    }

    @Override // net.minecraft.world.Nameable
    public Component getName() {
        return new TranslatableComponent("container.inventory");
    }

    public ItemStack getArmor(int i) {
        return this.armor.get(i);
    }

    public void hurtArmor(DamageSource damageSource, float f) {
        if (f <= 0.0f) {
            return;
        }
        float f2 = f / 4.0f;
        if (f2 < 1.0f) {
            f2 = 1.0f;
        }
        for (int i = 0; i < this.armor.size(); i++) {
            ItemStack itemStack = this.armor.get(i);
            if ((!damageSource.isFire() || !itemStack.getItem().isFireResistant()) && (itemStack.getItem() instanceof ArmorItem)) {
                int i2 = i;
                itemStack.hurtAndBreak((int) f2, this.player, player -> {
                    player.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i2));
                });
            }
        }
    }

    public void dropAll() {
        for (List<ItemStack> list : this.compartments) {
            for (int i = 0; i < list.size(); i++) {
                ItemStack itemStack = list.get(i);
                if (!itemStack.isEmpty()) {
                    this.player.drop(itemStack, true, false);
                    list.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override // net.minecraft.world.Container
    public void setChanged() {
        this.timesChanged++;
    }

    public int getTimesChanged() {
        return this.timesChanged;
    }

    public void setCarried(ItemStack itemStack) {
        this.carried = itemStack;
    }

    public ItemStack getCarried() {
        return this.carried;
    }

    @Override // net.minecraft.world.Container
    public boolean stillValid(Player player) {
        if (this.player.removed || player.distanceToSqr(this.player) > 64.0d) {
            return false;
        }
        return true;
    }

    public boolean contains(ItemStack itemStack) {
        Iterator<NonNullList<ItemStack>> it = this.compartments.iterator();
        while (it.hasNext()) {
            for (ItemStack itemStack2 : it.next()) {
                if (!itemStack2.isEmpty() && itemStack2.sameItem(itemStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean contains(Tag<Item> tag) {
        Iterator<NonNullList<ItemStack>> it = this.compartments.iterator();
        while (it.hasNext()) {
            for (ItemStack itemStack : it.next()) {
                if (!itemStack.isEmpty() && tag.contains(itemStack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void replaceWith(Inventory inventory) {
        for (int i = 0; i < getContainerSize(); i++) {
            setItem(i, inventory.getItem(i));
        }
        this.selected = inventory.selected;
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        Iterator<NonNullList<ItemStack>> it = this.compartments.iterator();
        while (it.hasNext()) {
            it.next().clear();
        }
    }

    public void fillStackedContents(StackedContents stackedContents) {
        Iterator<ItemStack> it = this.items.iterator();
        while (it.hasNext()) {
            stackedContents.accountSimpleStack(it.next());
        }
    }
}
