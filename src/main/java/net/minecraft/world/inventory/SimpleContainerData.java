package net.minecraft.world.inventory;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/SimpleContainerData.class */
public class SimpleContainerData implements ContainerData {
    private final int[] ints;

    public SimpleContainerData(int i) {
        this.ints = new int[i];
    }

    @Override // net.minecraft.world.inventory.ContainerData
    public int get(int i) {
        return this.ints[i];
    }

    @Override // net.minecraft.world.inventory.ContainerData
    public void set(int i, int i2) {
        this.ints[i] = i2;
    }

    @Override // net.minecraft.world.inventory.ContainerData
    public int getCount() {
        return this.ints.length;
    }
}
