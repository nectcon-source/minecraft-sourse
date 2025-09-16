package net.minecraft.world.level.chunk.storage;

import java.util.BitSet;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/RegionBitmap.class */
public class RegionBitmap {
    private final BitSet used = new BitSet();

    public void force(int i, int i2) {
        this.used.set(i, i + i2);
    }

    public void free(int i, int i2) {
        this.used.clear(i, i + i2);
    }

    public int allocate(int i) {
        int nextClearBit;
        int i2 = 0;
        while (true) {
            nextClearBit = this.used.nextClearBit(i2);
            int nextSetBit = this.used.nextSetBit(nextClearBit);
            if (nextSetBit == -1 || nextSetBit - nextClearBit >= i) {
                break;
            }
            i2 = nextSetBit;
        }
        force(nextClearBit, i);
        return nextClearBit;
    }
}
