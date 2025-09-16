package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/IndexMerger.class */
interface IndexMerger {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/IndexMerger$IndexConsumer.class */
    public interface IndexConsumer {
        boolean merge(int i, int i2, int i3);
    }

    DoubleList getList();

    boolean forMergedIndexes(IndexConsumer indexConsumer);
}
