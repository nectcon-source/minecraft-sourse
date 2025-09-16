package net.minecraft.world.inventory;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/DataSlot.class */
public abstract class DataSlot {
    private int prevValue;

    public abstract int get();

    public abstract void set(int i);

    public static DataSlot forContainer(final ContainerData containerData, final int i) {
        return new DataSlot() { // from class: net.minecraft.world.inventory.DataSlot.1
            @Override // net.minecraft.world.inventory.DataSlot
            public int get() {
                return containerData.get(i);
            }

            @Override // net.minecraft.world.inventory.DataSlot
            public void set(int i2) {
                containerData.set(i, i2);
            }
        };
    }

    public static DataSlot shared(final int[] iArr, final int i) {
        return new DataSlot() { // from class: net.minecraft.world.inventory.DataSlot.2
            @Override // net.minecraft.world.inventory.DataSlot
            public int get() {
                return iArr[i];
            }

            @Override // net.minecraft.world.inventory.DataSlot
            public void set(int i2) {
                iArr[i] = i2;
            }
        };
    }

    public static DataSlot standalone() {
        return new DataSlot() { // from class: net.minecraft.world.inventory.DataSlot.3
            private int value;

            @Override // net.minecraft.world.inventory.DataSlot
            public int get() {
                return this.value;
            }

            @Override // net.minecraft.world.inventory.DataSlot
            public void set(int i) {
                this.value = i;
            }
        };
    }

    public boolean checkAndClearUpdateFlag() {
        int i = get();
        boolean z = i != this.prevValue;
        this.prevValue = i;
        return z;
    }
}
