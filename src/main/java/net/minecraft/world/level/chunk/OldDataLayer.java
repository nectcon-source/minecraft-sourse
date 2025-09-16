package net.minecraft.world.level.chunk;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/OldDataLayer.class */
public class OldDataLayer {
    public final byte[] data;
    private final int depthBits;
    private final int depthBitsPlusFour;

    public OldDataLayer(byte[] bArr, int i) {
        this.data = bArr;
        this.depthBits = i;
        this.depthBitsPlusFour = i + 4;
    }

    public int get(int i, int i2, int i3) {
        int i4 = (i << this.depthBitsPlusFour) | (i3 << this.depthBits) | i2;
        int i5 = i4 >> 1;
        if ((i4 & 1) == 0) {
            return this.data[i5] & 15;
        }
        return (this.data[i5] >> 4) & 15;
    }
}
