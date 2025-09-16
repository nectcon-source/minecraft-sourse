package net.minecraft.world.level.block.entity;

import java.util.Random;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/DispenserBlockEntity.class */
public class DispenserBlockEntity extends RandomizableContainerBlockEntity {
    private static final Random RANDOM = new Random();
    private NonNullList<ItemStack> items;

    protected DispenserBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
        this.items = NonNullList.withSize(9, ItemStack.EMPTY);
    }

    public DispenserBlockEntity() {
        this(BlockEntityType.DISPENSER);
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return 9;
    }

    public int getRandomSlot() {
        unpackLootTable(null);
        int i = -1;
        int i2 = 1;
        for (int i3 = 0; i3 < this.items.size(); i3++) {
            if (!this.items.get(i3).isEmpty()) {
                int i4 = i2;
                i2++;
                if (RANDOM.nextInt(i4) == 0) {
                    i = i3;
                }
            }
        }
        return i;
    }

    public int addItem(ItemStack itemStack) {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty()) {
                setItem(i, itemStack);
                return i;
            }
        }
        return -1;
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected Component getDefaultName() {
        return new TranslatableComponent("container.dispenser");
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.items);
        }
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (!trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.items);
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new DispenserMenu(i, inventory, this);
    }
}
