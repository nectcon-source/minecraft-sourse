package net.minecraft.world.item.trading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/trading/MerchantOffer.class */
public class MerchantOffer {
    private final ItemStack baseCostA;
    private final ItemStack costB;
    private final ItemStack result;
    private int uses;
    private final int maxUses;
    private boolean rewardExp;
    private int specialPriceDiff;
    private int demand;
    private float priceMultiplier;

    /* renamed from: xp */
    private int xp;

    public MerchantOffer(CompoundTag compoundTag) {
        this.rewardExp = true;
        this.xp = 1;
        this.baseCostA = ItemStack.of(compoundTag.getCompound("buy"));
        this.costB = ItemStack.of(compoundTag.getCompound("buyB"));
        this.result = ItemStack.of(compoundTag.getCompound("sell"));
        this.uses = compoundTag.getInt("uses");
        if (compoundTag.contains("maxUses", 99)) {
            this.maxUses = compoundTag.getInt("maxUses");
        } else {
            this.maxUses = 4;
        }
        if (compoundTag.contains("rewardExp", 1)) {
            this.rewardExp = compoundTag.getBoolean("rewardExp");
        }
        if (compoundTag.contains("xp", 3)) {
            this.xp = compoundTag.getInt("xp");
        }
        if (compoundTag.contains("priceMultiplier", 5)) {
            this.priceMultiplier = compoundTag.getFloat("priceMultiplier");
        }
        this.specialPriceDiff = compoundTag.getInt("specialPrice");
        this.demand = compoundTag.getInt("demand");
    }

    public MerchantOffer(ItemStack itemStack, ItemStack itemStack2, int i, int i2, float f) {
        this(itemStack, ItemStack.EMPTY, itemStack2, i, i2, f);
    }

    public MerchantOffer(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, int i, int i2, float f) {
        this(itemStack, itemStack2, itemStack3, 0, i, i2, f);
    }

    public MerchantOffer(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, int i, int i2, int i3, float f) {
        this(itemStack, itemStack2, itemStack3, i, i2, i3, f, 0);
    }

    public MerchantOffer(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, int i, int i2, int i3, float f, int i4) {
        this.rewardExp = true;
        this.xp = 1;
        this.baseCostA = itemStack;
        this.costB = itemStack2;
        this.result = itemStack3;
        this.uses = i;
        this.maxUses = i2;
        this.xp = i3;
        this.priceMultiplier = f;
        this.demand = i4;
    }

    public ItemStack getBaseCostA() {
        return this.baseCostA;
    }

    public ItemStack getCostA() {
        int count = this.baseCostA.getCount();
        ItemStack copy = this.baseCostA.copy();
        copy.setCount(Mth.clamp(count + Math.max(0, Mth.floor(count * this.demand * this.priceMultiplier)) + this.specialPriceDiff, 1, this.baseCostA.getItem().getMaxStackSize()));
        return copy;
    }

    public ItemStack getCostB() {
        return this.costB;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public void updateDemand() {
        this.demand = (this.demand + this.uses) - (this.maxUses - this.uses);
    }

    public ItemStack assemble() {
        return this.result.copy();
    }

    public int getUses() {
        return this.uses;
    }

    public void resetUses() {
        this.uses = 0;
    }

    public int getMaxUses() {
        return this.maxUses;
    }

    public void increaseUses() {
        this.uses++;
    }

    public int getDemand() {
        return this.demand;
    }

    public void addToSpecialPriceDiff(int i) {
        this.specialPriceDiff += i;
    }

    public void resetSpecialPriceDiff() {
        this.specialPriceDiff = 0;
    }

    public int getSpecialPriceDiff() {
        return this.specialPriceDiff;
    }

    public void setSpecialPriceDiff(int i) {
        this.specialPriceDiff = i;
    }

    public float getPriceMultiplier() {
        return this.priceMultiplier;
    }

    public int getXp() {
        return this.xp;
    }

    public boolean isOutOfStock() {
        return this.uses >= this.maxUses;
    }

    public void setToOutOfStock() {
        this.uses = this.maxUses;
    }

    public boolean needsRestock() {
        return this.uses > 0;
    }

    public boolean shouldRewardExp() {
        return this.rewardExp;
    }

    public CompoundTag createTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("buy", this.baseCostA.save(new CompoundTag()));
        compoundTag.put("sell", this.result.save(new CompoundTag()));
        compoundTag.put("buyB", this.costB.save(new CompoundTag()));
        compoundTag.putInt("uses", this.uses);
        compoundTag.putInt("maxUses", this.maxUses);
        compoundTag.putBoolean("rewardExp", this.rewardExp);
        compoundTag.putInt("xp", this.xp);
        compoundTag.putFloat("priceMultiplier", this.priceMultiplier);
        compoundTag.putInt("specialPrice", this.specialPriceDiff);
        compoundTag.putInt("demand", this.demand);
        return compoundTag;
    }

    public boolean satisfiedBy(ItemStack itemStack, ItemStack itemStack2) {
        return isRequiredItem(itemStack, getCostA()) && itemStack.getCount() >= getCostA().getCount() && isRequiredItem(itemStack2, this.costB) && itemStack2.getCount() >= this.costB.getCount();
    }

    private boolean isRequiredItem(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.isEmpty() && itemStack.isEmpty()) {
            return true;
        }
        ItemStack copy = itemStack.copy();
        if (copy.getItem().canBeDepleted()) {
            copy.setDamageValue(copy.getDamageValue());
        }
        return ItemStack.isSame(copy, itemStack2) && (!itemStack2.hasTag() || (copy.hasTag() && NbtUtils.compareNbt(itemStack2.getTag(), copy.getTag(), false)));
    }

    public boolean take(ItemStack itemStack, ItemStack itemStack2) {
        if (!satisfiedBy(itemStack, itemStack2)) {
            return false;
        }
        itemStack.shrink(getCostA().getCount());
        if (!getCostB().isEmpty()) {
            itemStack2.shrink(getCostB().getCount());
            return true;
        }
        return true;
    }
}
