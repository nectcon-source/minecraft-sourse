package net.minecraft.world.inventory;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/MerchantResultSlot.class */
public class MerchantResultSlot extends Slot {
    private final MerchantContainer slots;
    private final Player player;
    private int removeCount;
    private final Merchant merchant;

    public MerchantResultSlot(Player player, Merchant merchant, MerchantContainer merchantContainer, int i, int i2, int i3) {
        super(merchantContainer, i, i2, i3);
        this.player = player;
        this.merchant = merchant;
        this.slots = merchantContainer;
    }

    @Override // net.minecraft.world.inventory.Slot
    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    @Override // net.minecraft.world.inventory.Slot
    public ItemStack remove(int i) {
        if (hasItem()) {
            this.removeCount += Math.min(i, getItem().getCount());
        }
        return super.remove(i);
    }

    @Override // net.minecraft.world.inventory.Slot
    protected void onQuickCraft(ItemStack itemStack, int i) {
        this.removeCount += i;
        checkTakeAchievements(itemStack);
    }

    @Override // net.minecraft.world.inventory.Slot
    protected void checkTakeAchievements(ItemStack itemStack) {
        itemStack.onCraftedBy(this.player.level, this.player, this.removeCount);
        this.removeCount = 0;
    }

    @Override // net.minecraft.world.inventory.Slot
    public ItemStack onTake(Player player, ItemStack itemStack) {
        checkTakeAchievements(itemStack);
        MerchantOffer activeOffer = this.slots.getActiveOffer();
        if (activeOffer != null) {
            ItemStack item = this.slots.getItem(0);
            ItemStack item2 = this.slots.getItem(1);
            if (activeOffer.take(item, item2) || activeOffer.take(item2, item)) {
                this.merchant.notifyTrade(activeOffer);
                player.awardStat(Stats.TRADED_WITH_VILLAGER);
                this.slots.setItem(0, item);
                this.slots.setItem(1, item2);
            }
            this.merchant.overrideXp(this.merchant.getVillagerXp() + activeOffer.getXp());
        }
        return itemStack;
    }
}
