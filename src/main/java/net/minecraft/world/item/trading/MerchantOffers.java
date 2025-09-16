package net.minecraft.world.item.trading;

import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/trading/MerchantOffers.class */
public class MerchantOffers extends ArrayList<MerchantOffer> {
    public MerchantOffers() {
    }

    public MerchantOffers(CompoundTag compoundTag) {
        ListTag list = compoundTag.getList("Recipes", 10);
        for (int i = 0; i < list.size(); i++) {
            add(new MerchantOffer(list.getCompound(i)));
        }
    }

    @Nullable
    public MerchantOffer getRecipeFor(ItemStack itemStack, ItemStack itemStack2, int i) {
        if (i > 0 && i < size()) {
            MerchantOffer merchantOffer = get(i);
            if (merchantOffer.satisfiedBy(itemStack, itemStack2)) {
                return merchantOffer;
            }
            return null;
        }
        for (int i2 = 0; i2 < size(); i2++) {
            MerchantOffer merchantOffer2 = get(i2);
            if (merchantOffer2.satisfiedBy(itemStack, itemStack2)) {
                return merchantOffer2;
            }
        }
        return null;
    }

    public void writeToStream(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte((byte) (size() & 255));
        for (int i = 0; i < size(); i++) {
            MerchantOffer merchantOffer = get(i);
            friendlyByteBuf.writeItem(merchantOffer.getBaseCostA());
            friendlyByteBuf.writeItem(merchantOffer.getResult());
            ItemStack costB = merchantOffer.getCostB();
            friendlyByteBuf.writeBoolean(!costB.isEmpty());
            if (!costB.isEmpty()) {
                friendlyByteBuf.writeItem(costB);
            }
            friendlyByteBuf.writeBoolean(merchantOffer.isOutOfStock());
            friendlyByteBuf.writeInt(merchantOffer.getUses());
            friendlyByteBuf.writeInt(merchantOffer.getMaxUses());
            friendlyByteBuf.writeInt(merchantOffer.getXp());
            friendlyByteBuf.writeInt(merchantOffer.getSpecialPriceDiff());
            friendlyByteBuf.writeFloat(merchantOffer.getPriceMultiplier());
            friendlyByteBuf.writeInt(merchantOffer.getDemand());
        }
    }

    public static MerchantOffers createFromStream(FriendlyByteBuf friendlyByteBuf) {
        MerchantOffers merchantOffers = new MerchantOffers();
        int readByte = friendlyByteBuf.readByte() & 255;
        for (int i = 0; i < readByte; i++) {
            ItemStack readItem = friendlyByteBuf.readItem();
            ItemStack readItem2 = friendlyByteBuf.readItem();
            ItemStack itemStack = ItemStack.EMPTY;
            if (friendlyByteBuf.readBoolean()) {
                itemStack = friendlyByteBuf.readItem();
            }
            boolean readBoolean = friendlyByteBuf.readBoolean();
            int readInt = friendlyByteBuf.readInt();
            int readInt2 = friendlyByteBuf.readInt();
            int readInt3 = friendlyByteBuf.readInt();
            int readInt4 = friendlyByteBuf.readInt();
            MerchantOffer merchantOffer = new MerchantOffer(readItem, itemStack, readItem2, readInt, readInt2, readInt3, friendlyByteBuf.readFloat(), friendlyByteBuf.readInt());
            if (readBoolean) {
                merchantOffer.setToOutOfStock();
            }
            merchantOffer.setSpecialPriceDiff(readInt4);
            merchantOffers.add(merchantOffer);
        }
        return merchantOffers;
    }

    public CompoundTag createTag() {
        CompoundTag compoundTag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (int i = 0; i < size(); i++) {
            listTag.add(get(i).createTag());
        }
        compoundTag.put("Recipes", listTag);
        return compoundTag;
    }
}
