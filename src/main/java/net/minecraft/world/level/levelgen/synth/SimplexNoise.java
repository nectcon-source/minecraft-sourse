package net.minecraft.world.level.levelgen.synth;

import java.util.Random;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/synth/SimplexNoise.class */
public class SimplexNoise {
    protected static final int[][] GRADIENT = {new int[]{1, 1, 0}, new int[]{-1, 1, 0}, new int[]{1, -1, 0}, new int[]{-1, -1, 0}, new int[]{1, 0, 1}, new int[]{-1, 0, 1}, new int[]{1, 0, -1}, new int[]{-1, 0, -1}, new int[]{0, 1, 1}, new int[]{0, -1, 1}, new int[]{0, 1, -1}, new int[]{0, -1, -1}, new int[]{1, 1, 0}, new int[]{0, -1, 1}, new int[]{-1, 1, 0}, new int[]{0, -1, -1}};
    private static final double SQRT_3 = Math.sqrt(3.0d);

    /* renamed from: F2 */
    private static final double F2 = 0.5d * (SQRT_3 - 1.0d);

    /* renamed from: G2 */
    private static final double G2 = (3.0d - SQRT_3) / 6.0d;

    /* renamed from: p */
    private final int[] p = new int[512];

    /* renamed from: xo */
    public final double xo;

    /* renamed from: yo */
    public final double yo;

    /* renamed from: zo */
    public final double zo;

    public SimplexNoise(Random random) {
        this.xo = random.nextDouble() * 256.0d;
        this.yo = random.nextDouble() * 256.0d;
        this.zo = random.nextDouble() * 256.0d;
        for (int i = 0; i < 256; i++) {
            this.p[i] = i;
        }
        for (int i2 = 0; i2 < 256; i2++) {
            int nextInt = random.nextInt(256 - i2);
            int i3 = this.p[i2];
            this.p[i2] = this.p[nextInt + i2];
            this.p[nextInt + i2] = i3;
        }
    }

    /* renamed from: p */
    private int p(int i) {
        return this.p[i & 255];
    }

    protected static double dot(int[] iArr, double d, double d2, double d3) {
        return (iArr[0] * d) + (iArr[1] * d2) + (iArr[2] * d3);
    }

    private double getCornerNoise3D(int i, double d, double d2, double d3, double d4) {
        double dot;
        double d5 = ((d4 - (d * d)) - (d2 * d2)) - (d3 * d3);
        if (d5 < 0.0d) {
            dot = 0.0d;
        } else {
            double d6 = d5 * d5;
            dot = d6 * d6 * dot(GRADIENT[i], d, d2, d3);
        }
        return dot;
    }

    public double getValue(double d, double d2) {
        int i;
        int i2;
        double d3 = (d + d2) * F2;
        int floor = Mth.floor(d + d3);
        int floor2 = Mth.floor(d2 + d3);
        double d4 = (floor + floor2) * G2;
        double d5 = floor - d4;
        double d6 = floor2 - d4;
        double d7 = d - d5;
        double d8 = d2 - d6;
        if (d7 > d8) {
            i = 1;
            i2 = 0;
        } else {
            i = 0;
            i2 = 1;
        }
        double d9 = (d7 - i) + G2;
        double d10 = (d8 - i2) + G2;
        double d11 = (d7 - 1.0d) + (2.0d * G2);
        double d12 = (d8 - 1.0d) + (2.0d * G2);
        int i3 = floor & 255;
        int i4 = floor2 & 255;
        return 70.0d * (getCornerNoise3D(p(i3 + p(i4)) % 12, d7, d8, 0.0d, 0.5d) + getCornerNoise3D(p((i3 + i) + p(i4 + i2)) % 12, d9, d10, 0.0d, 0.5d) + getCornerNoise3D(p((i3 + 1) + p(i4 + 1)) % 12, d11, d12, 0.0d, 0.5d));
    }

    public double getValue(double d, double d2, double d3) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        double d4 = (d + d2 + d3) * 0.3333333333333333d;
        int floor = Mth.floor(d + d4);
        int floor2 = Mth.floor(d2 + d4);
        int floor3 = Mth.floor(d3 + d4);
        double d5 = (floor + floor2 + floor3) * 0.16666666666666666d;
        double d6 = floor - d5;
        double d7 = floor2 - d5;
        double d8 = floor3 - d5;
        double d9 = d - d6;
        double d10 = d2 - d7;
        double d11 = d3 - d8;
        if (d9 >= d10) {
            if (d10 >= d11) {
                i = 1;
                i2 = 0;
                i3 = 0;
                i4 = 1;
                i5 = 1;
                i6 = 0;
            } else if (d9 >= d11) {
                i = 1;
                i2 = 0;
                i3 = 0;
                i4 = 1;
                i5 = 0;
                i6 = 1;
            } else {
                i = 0;
                i2 = 0;
                i3 = 1;
                i4 = 1;
                i5 = 0;
                i6 = 1;
            }
        } else if (d10 < d11) {
            i = 0;
            i2 = 0;
            i3 = 1;
            i4 = 0;
            i5 = 1;
            i6 = 1;
        } else if (d9 < d11) {
            i = 0;
            i2 = 1;
            i3 = 0;
            i4 = 0;
            i5 = 1;
            i6 = 1;
        } else {
            i = 0;
            i2 = 1;
            i3 = 0;
            i4 = 1;
            i5 = 1;
            i6 = 0;
        }
        double d12 = (d9 - i) + 0.16666666666666666d;
        double d13 = (d10 - i2) + 0.16666666666666666d;
        double d14 = (d11 - i3) + 0.16666666666666666d;
        double d15 = (d9 - i4) + 0.3333333333333333d;
        double d16 = (d10 - i5) + 0.3333333333333333d;
        double d17 = (d11 - i6) + 0.3333333333333333d;
        double d18 = (d9 - 1.0d) + 0.5d;
        double d19 = (d10 - 1.0d) + 0.5d;
        double d20 = (d11 - 1.0d) + 0.5d;
        int i7 = floor & 255;
        int i8 = floor2 & 255;
        int i9 = floor3 & 255;
        int m83p = p(i7 + p(i8 + p(i9))) % 12;
        int m83p2 = p((i7 + i) + p((i8 + i2) + p(i9 + i3))) % 12;
        int m83p3 = p((i7 + i4) + p((i8 + i5) + p(i9 + i6))) % 12;
        int m83p4 = p((i7 + 1) + p((i8 + 1) + p(i9 + 1))) % 12;
        return 32.0d * (getCornerNoise3D(m83p, d9, d10, d11, 0.6d) + getCornerNoise3D(m83p2, d12, d13, d14, 0.6d) + getCornerNoise3D(m83p3, d15, d16, d17, 0.6d) + getCornerNoise3D(m83p4, d18, d19, d20, 0.6d));
    }
}
