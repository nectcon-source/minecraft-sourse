package net.minecraft.world;

import javax.annotation.concurrent.Immutable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Immutable
/* loaded from: client_deobf_norm.jar:net/minecraft/world/LockCode.class */
public class LockCode {
    public static final LockCode NO_LOCK = new LockCode("");
    private final String key;

    public LockCode(String str) {
        this.key = str;
    }

    public boolean unlocksWith(ItemStack itemStack) {
        return this.key.isEmpty() || (!itemStack.isEmpty() && itemStack.hasCustomHoverName() && this.key.equals(itemStack.getHoverName().getString()));
    }

    public void addToTag(CompoundTag compoundTag) {
        if (!this.key.isEmpty()) {
            compoundTag.putString("Lock", this.key);
        }
    }

    public static LockCode fromTag(CompoundTag compoundTag) {
        if (compoundTag.contains("Lock", 8)) {
            return new LockCode(compoundTag.getString("Lock"));
        }
        return NO_LOCK;
    }
}
