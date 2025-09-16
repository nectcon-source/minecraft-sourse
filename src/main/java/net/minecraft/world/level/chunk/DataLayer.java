package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.Util;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/DataLayer.class */
public class DataLayer {

    @Nullable
    protected byte[] data;

    public DataLayer() {
    }

    public DataLayer(byte[] bArr) {
        this.data = bArr;
        if (bArr.length != 2048) {
            throw ((IllegalArgumentException) Util.pauseInIde(new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + bArr.length)));
        }
    }

    protected DataLayer(int i) {
        this.data = new byte[i];
    }

    public int get(int i, int i2, int i3) {
        return get(getIndex(i, i2, i3));
    }

    public void set(int i, int i2, int i3, int i4) {
        set(getIndex(i, i2, i3), i4);
    }

    protected int getIndex(int i, int i2, int i3) {
        return (i2 << 8) | (i3 << 4) | i;
    }

    private int get(int i) {
        if (this.data == null) {
            return 0;
        }
        int position = getPosition(i);
        if (isFirst(i)) {
            return this.data[position] & 15;
        }
        return (this.data[position] >> 4) & 15;
    }

    private void set(int i, int i2) {
        if (this.data == null) {
            this.data = new byte[2048];
        }
        int position = getPosition(i);
        if (isFirst(i)) {
            this.data[position] = (byte) ((this.data[position] & 240) | (i2 & 15));
        } else {
            this.data[position] = (byte) ((this.data[position] & 15) | ((i2 & 15) << 4));
        }
    }

    private boolean isFirst(int i) {
        return (i & 1) == 0;
    }

    private int getPosition(int i) {
        return i >> 1;
    }

    public byte[] getData() {
        if (this.data == null) {
            this.data = new byte[2048];
        }
        return this.data;
    }

    public DataLayer copy() {
        if (this.data == null) {
            return new DataLayer();
        }
        return new DataLayer((byte[]) this.data.clone());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4096; i++) {
            sb.append(Integer.toHexString(get(i)));
            if ((i & 15) == 15) {
                sb.append("\n");
            }
            if ((i & 255) == 255) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public boolean isEmpty() {
        return this.data == null;
    }
}
