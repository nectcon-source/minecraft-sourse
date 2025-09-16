package net.minecraft.world.inventory;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/MerchantContainer.class */
public class MerchantContainer implements Container {
    private final Merchant merchant;
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(3, ItemStack.EMPTY);

    @Nullable
    private MerchantOffer activeOffer;
    private int selectionHint;
    private int futureXp;

    public MerchantContainer(Merchant merchant) {
        this.merchant = merchant;
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override // net.minecraft.world.Container
    public boolean isEmpty() {
        Iterator<ItemStack> it = this.itemStacks.iterator();
        while (it.hasNext()) {
            if (!it.next().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.Container
    public ItemStack getItem(int i) {
        return this.itemStacks.get(i);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItem(int i, int i2) {
        ItemStack itemStack = this.itemStacks.get(i);
        if (i == 2 && !itemStack.isEmpty()) {
            return ContainerHelper.removeItem(this.itemStacks, i, itemStack.getCount());
        }
        ItemStack removeItem = ContainerHelper.removeItem(this.itemStacks, i, i2);
        if (!removeItem.isEmpty() && isPaymentSlot(i)) {
            updateSellItem();
        }
        return removeItem;
    }

    private boolean isPaymentSlot(int i) {
        return i == 0 || i == 1;
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(this.itemStacks, i);
    }

    @Override // net.minecraft.world.Container
    public void setItem(int i, ItemStack itemStack) {
        this.itemStacks.set(i, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }
        if (isPaymentSlot(i)) {
            updateSellItem();
        }
    }

    @Override // net.minecraft.world.Container
    public boolean stillValid(Player player) {
        return this.merchant.getTradingPlayer() == player;
    }

    @Override // net.minecraft.world.Container
    public void setChanged() {
        updateSellItem();
    }

    public void updateSellItem() {
        ItemStack itemStack;
        ItemStack itemStack2;
        this.activeOffer = null;
        if (this.itemStacks.get(0).isEmpty()) {
            itemStack = this.itemStacks.get(1);
            itemStack2 = ItemStack.EMPTY;
        } else {
            itemStack = this.itemStacks.get(0);
            itemStack2 = this.itemStacks.get(1);
        }
        if (itemStack.isEmpty()) {
            setItem(2, ItemStack.EMPTY);
            this.futureXp = 0;
            return;
        }
        MerchantOffers offers = this.merchant.getOffers();
        if (!offers.isEmpty()) {
            MerchantOffer recipeFor = offers.getRecipeFor(itemStack, itemStack2, this.selectionHint);
            if (recipeFor == null || recipeFor.isOutOfStock()) {
                this.activeOffer = recipeFor;
                recipeFor = offers.getRecipeFor(itemStack2, itemStack, this.selectionHint);
            }
            if (recipeFor != null && !recipeFor.isOutOfStock()) {
                this.activeOffer = recipeFor;
                setItem(2, recipeFor.assemble());
                this.futureXp = recipeFor.getXp();
            } else {
                setItem(2, ItemStack.EMPTY);
                this.futureXp = 0;
            }
        }
        this.merchant.notifyTradeUpdated(getItem(2));
    }

    @Nullable
    public MerchantOffer getActiveOffer() {
        return this.activeOffer;
    }

    public void setSelectionHint(int i) {
        this.selectionHint = i;
        updateSellItem();
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        this.itemStacks.clear();
    }

    public int getFutureXp() {
        return this.futureXp;
    }
}
