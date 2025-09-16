package net.minecraft.world.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/PlayerEnderChestContainer.class */
public class PlayerEnderChestContainer extends SimpleContainer {
    private EnderChestBlockEntity activeChest;

    public PlayerEnderChestContainer() {
        super(27);
    }

    public void setActiveChest(EnderChestBlockEntity enderChestBlockEntity) {
        this.activeChest = enderChestBlockEntity;
    }

    @Override // net.minecraft.world.SimpleContainer
    public void fromTag(ListTag listTag) {
        for (int i = 0; i < getContainerSize(); i++) {
            setItem(i, ItemStack.EMPTY);
        }
        for (int i2 = 0; i2 < listTag.size(); i2++) {
            CompoundTag compound = listTag.getCompound(i2);
            int i3 = compound.getByte("Slot") & 255;
            if (i3 >= 0 && i3 < getContainerSize()) {
                setItem(i3, ItemStack.of(compound));
            }
        }
    }

    @Override // net.minecraft.world.SimpleContainer
    public ListTag createTag() {
        ListTag listTag = new ListTag();
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack item = getItem(i);
            if (!item.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte) i);
                item.save(compoundTag);
                listTag.add(compoundTag);
            }
        }
        return listTag;
    }

    @Override // net.minecraft.world.SimpleContainer, net.minecraft.world.Container
    public boolean stillValid(Player player) {
        if (this.activeChest != null && !this.activeChest.stillValid(player)) {
            return false;
        }
        return super.stillValid(player);
    }

    @Override // net.minecraft.world.Container
    public void startOpen(Player player) {
        if (this.activeChest != null) {
            this.activeChest.startOpen();
        }
        super.startOpen(player);
    }

    @Override // net.minecraft.world.Container
    public void stopOpen(Player player) {
        if (this.activeChest != null) {
            this.activeChest.stopOpen();
        }
        super.stopOpen(player);
        this.activeChest = null;
    }
}
