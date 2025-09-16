package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/LecternMenu.class */
public class LecternMenu extends AbstractContainerMenu {
    private final Container lectern;
    private final ContainerData lecternData;

    public LecternMenu(int i) {
        this(i, new SimpleContainer(1), new SimpleContainerData(1));
    }

    public LecternMenu(int i, Container container, ContainerData containerData) {
        super(MenuType.LECTERN, i);
        checkContainerSize(container, 1);
        checkContainerDataCount(containerData, 1);
        this.lectern = container;
        this.lecternData = containerData;
        addSlot(new Slot(container, 0, 0, 0) { // from class: net.minecraft.world.inventory.LecternMenu.1
            @Override // net.minecraft.world.inventory.Slot
            public void setChanged() {
                super.setChanged();
                LecternMenu.this.slotsChanged(this.container);
            }
        });
        addDataSlots(containerData);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean clickMenuButton(Player player, int i) {
        if (i >= 100) {
            setData(0, i - 100);
            return true;
        }
        switch (i) {
            case 1:
                setData(0, this.lecternData.get(0) - 1);
                break;
            case 2:
                setData(0, this.lecternData.get(0) + 1);
                break;
            case 3:
                if (player.mayBuild()) {
                    ItemStack removeItemNoUpdate = this.lectern.removeItemNoUpdate(0);
                    this.lectern.setChanged();
                    if (!player.inventory.add(removeItemNoUpdate)) {
                        player.drop(removeItemNoUpdate, false);
                        break;
                    }
                }
                break;
        }
        return true;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void setData(int i, int i2) {
        super.setData(i, i2);
        broadcastChanges();
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return this.lectern.stillValid(player);
    }

    public ItemStack getBook() {
        return this.lectern.getItem(0);
    }

    public int getPage() {
        return this.lecternData.get(0);
    }
}
