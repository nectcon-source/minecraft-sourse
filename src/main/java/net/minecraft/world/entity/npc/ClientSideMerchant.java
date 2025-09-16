package net.minecraft.world.entity.npc;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/npc/ClientSideMerchant.class */
public class ClientSideMerchant implements Merchant {
    private final Player source;

    /* renamed from: xp */
    private int f450xp;
    private MerchantOffers offers = new MerchantOffers();
    private final MerchantContainer container = new MerchantContainer(this);

    public ClientSideMerchant(Player player) {
        this.source = player;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    @Nullable
    public Player getTradingPlayer() {
        return this.source;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public void setTradingPlayer(@Nullable Player player) {
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public MerchantOffers getOffers() {
        return this.offers;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public void overrideOffers(@Nullable MerchantOffers merchantOffers) {
        this.offers = merchantOffers;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public void notifyTrade(MerchantOffer merchantOffer) {
        merchantOffer.increaseUses();
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public void notifyTradeUpdated(ItemStack itemStack) {
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public Level getLevel() {
        return this.source.level;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public int getVillagerXp() {
        return this.f450xp;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public void overrideXp(int i) {
        this.f450xp = i;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public boolean showProgressBar() {
        return true;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }
}
