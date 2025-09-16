package net.minecraft.world.level.lighting;

import net.minecraft.world.level.chunk.DataLayer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/FlatDataLayer.class */
public class FlatDataLayer extends DataLayer {
    public FlatDataLayer() {
        super(128);
    }

    public FlatDataLayer(DataLayer dataLayer, int i) {
        super(128);
        System.arraycopy(dataLayer.getData(), i * 128, this.data, 0, 128);
    }

    @Override // net.minecraft.world.level.chunk.DataLayer
    protected int getIndex(int i, int i2, int i3) {
        return (i3 << 4) | i;
    }

    @Override // net.minecraft.world.level.chunk.DataLayer
    public byte[] getData() {
        byte[] bArr = new byte[2048];
        for (int i = 0; i < 16; i++) {
            System.arraycopy(this.data, 0, bArr, i * 128, 128);
        }
        return bArr;
    }
}
