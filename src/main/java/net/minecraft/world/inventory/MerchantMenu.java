package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/MerchantMenu.class */
public class MerchantMenu extends AbstractContainerMenu {
    private final Merchant trader;
    private final MerchantContainer tradeContainer;
    private int merchantLevel;
    private boolean showProgressBar;
    private boolean canRestock;

    public MerchantMenu(int i, Inventory inventory) {
        this(i, inventory, new ClientSideMerchant(inventory.player));
    }

    public MerchantMenu(int i, Inventory inventory, Merchant merchant) {
        super(MenuType.MERCHANT, i);
        this.trader = merchant;
        this.tradeContainer = new MerchantContainer(merchant);
        addSlot(new Slot(this.tradeContainer, 0, 136, 37));
        addSlot(new Slot(this.tradeContainer, 1, 162, 37));
        addSlot(new MerchantResultSlot(inventory.player, merchant, this.tradeContainer, 2, 220, 37));
        for (int i2 = 0; i2 < 3; i2++) {
            for (int i3 = 0; i3 < 9; i3++) {
                addSlot(new Slot(inventory, i3 + (i2 * 9) + 9, 108 + (i3 * 18), 84 + (i2 * 18)));
            }
        }
        for (int i4 = 0; i4 < 9; i4++) {
            addSlot(new Slot(inventory, i4, 108 + (i4 * 18), 142));
        }
    }

    public void setShowProgressBar(boolean z) {
        this.showProgressBar = z;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void slotsChanged(Container container) {
        this.tradeContainer.updateSellItem();
        super.slotsChanged(container);
    }

    public void setSelectionHint(int i) {
        this.tradeContainer.setSelectionHint(i);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return this.trader.getTradingPlayer() == player;
    }

    public int getTraderXp() {
        return this.trader.getVillagerXp();
    }

    public int getFutureTraderXp() {
        return this.tradeContainer.getFutureXp();
    }

    public void setXp(int i) {
        this.trader.overrideXp(i);
    }

    public int getTraderLevel() {
        return this.merchantLevel;
    }

    public void setMerchantLevel(int i) {
        this.merchantLevel = i;
    }

    public void setCanRestock(boolean z) {
        this.canRestock = z;
    }

    public boolean canRestock() {
        return this.canRestock;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return false;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            if (i == 2) {
                if (!moveItemStackTo(item, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(item, itemStack);
                playTradeSound();
            } else if (i == 0 || i == 1) {
                if (!moveItemStackTo(item, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 3 && i < 30) {
                if (!moveItemStackTo(item, 30, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 30 && i < 39 && !moveItemStackTo(item, 3, 30, false)) {
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

    private void playTradeSound() {
        if (!this.trader.getLevel().isClientSide) {
            Entity entity = (Entity) this.trader;
            this.trader.getLevel().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.trader.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0f, 1.0f, false);
        }
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void removed(Player player) {
        super.removed(player);
        this.trader.setTradingPlayer(null);
        if (this.trader.getLevel().isClientSide) {
            return;
        }
        if (!player.isAlive() || ((player instanceof ServerPlayer) && ((ServerPlayer) player).hasDisconnected())) {
            ItemStack removeItemNoUpdate = this.tradeContainer.removeItemNoUpdate(0);
            if (!removeItemNoUpdate.isEmpty()) {
                player.drop(removeItemNoUpdate, false);
            }
            ItemStack removeItemNoUpdate2 = this.tradeContainer.removeItemNoUpdate(1);
            if (!removeItemNoUpdate2.isEmpty()) {
                player.drop(removeItemNoUpdate2, false);
                return;
            }
            return;
        }
        player.inventory.placeItemBackInInventory(player.level, this.tradeContainer.removeItemNoUpdate(0));
        player.inventory.placeItemBackInInventory(player.level, this.tradeContainer.removeItemNoUpdate(1));
    }

    public void tryMoveItems(int i) {
        if (getOffers().size() <= i) {
            return;
        }
        ItemStack item = this.tradeContainer.getItem(0);
        if (!item.isEmpty()) {
            if (!moveItemStackTo(item, 3, 39, true)) {
                return;
            } else {
                this.tradeContainer.setItem(0, item);
            }
        }
        ItemStack item2 = this.tradeContainer.getItem(1);
        if (!item2.isEmpty()) {
            if (!moveItemStackTo(item2, 3, 39, true)) {
                return;
            } else {
                this.tradeContainer.setItem(1, item2);
            }
        }
        if (this.tradeContainer.getItem(0).isEmpty() && this.tradeContainer.getItem(1).isEmpty()) {
            moveFromInventoryToPaymentSlot(0, getOffers().get(i).getCostA());
            moveFromInventoryToPaymentSlot(1, getOffers().get(i).getCostB());
        }
    }

    private void moveFromInventoryToPaymentSlot(int i, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            for (int i2 = 3; i2 < 39; i2++) {
                ItemStack item = this.slots.get(i2).getItem();
                if (!item.isEmpty() && isSameItem(itemStack, item)) {
                    ItemStack item2 = this.tradeContainer.getItem(i);
                    int count = item2.isEmpty() ? 0 : item2.getCount();
                    int min = Math.min(itemStack.getMaxStackSize() - count, item.getCount());
                    ItemStack copy = item.copy();
                    int i3 = count + min;
                    item.shrink(min);
                    copy.setCount(i3);
                    this.tradeContainer.setItem(i, copy);
                    if (i3 >= itemStack.getMaxStackSize()) {
                        return;
                    }
                }
            }
        }
    }

    private boolean isSameItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.getItem() == itemStack2.getItem() && ItemStack.tagMatches(itemStack, itemStack2);
    }

    public void setOffers(MerchantOffers merchantOffers) {
        this.trader.overrideOffers(merchantOffers);
    }

    public MerchantOffers getOffers() {
        return this.trader.getOffers();
    }

    public boolean showProgressBar() {
        return this.showProgressBar;
    }
}
