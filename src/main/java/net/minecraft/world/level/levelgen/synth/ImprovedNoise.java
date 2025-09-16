package net.minecraft.world.level.levelgen.synth;

import java.util.Random;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/synth/ImprovedNoise.class */
public final class ImprovedNoise {

    /* renamed from: p */
    private final byte[] p = new byte[256];

    /* renamed from: xo */
    public final double xo;

    /* renamed from: yo */
    public final double yo;

    /* renamed from: zo */
    public final double zo;

    public ImprovedNoise(Random random) {
        this.xo = random.nextDouble() * 256.0d;
        this.yo = random.nextDouble() * 256.0d;
        this.zo = random.nextDouble() * 256.0d;
        for (int i = 0; i < 256; i++) {
            this.p[i] = (byte) i;
        }
        for (int i2 = 0; i2 < 256; i2++) {
            int nextInt = random.nextInt(256 - i2);
            byte b = this.p[i2];
            this.p[i2] = this.p[i2 + nextInt];
            this.p[i2 + nextInt] = b;
        }
    }

    public double noise(double d, double d2, double d3, double d4, double d5) {
        double d6;
        double d7 = d + this.xo;
        double d8 = d2 + this.yo;
        double d9 = d3 + this.zo;
        int floor = Mth.floor(d7);
        int floor2 = Mth.floor(d8);
        int floor3 = Mth.floor(d9);
        double d10 = d7 - floor;
        double d11 = d8 - floor2;
        double d12 = d9 - floor3;
        double smoothstep = Mth.smoothstep(d10);
        double smoothstep2 = Mth.smoothstep(d11);
        double smoothstep3 = Mth.smoothstep(d12);
        if (d4 != 0.0d) {
            d6 = Mth.floor(Math.min(d5, d11) / d4) * d4;
        } else {
            d6 = 0.0d;
        }
        return sampleAndLerp(floor, floor2, floor3, d10, d11 - d6, d12, smoothstep, smoothstep2, smoothstep3);
    }

    private static double gradDot(int i, double d, double d2, double d3) {
        return SimplexNoise.dot(SimplexNoise.GRADIENT[i & 15], d, d2, d3);
    }

    /* renamed from: p */
    private int m82p(int i) {
        return this.p[i & 255] & 255;
    }

    public double sampleAndLerp(int i, int i2, int i3, double d, double d2, double d3, double d4, double d5, double d6) {
        int m82p = m82p(i) + i2;
        int m82p2 = m82p(m82p) + i3;
        int m82p3 = m82p(m82p + 1) + i3;
        int m82p4 = m82p(i + 1) + i2;
        int m82p5 = m82p(m82p4) + i3;
        int m82p6 = m82p(m82p4 + 1) + i3;
        return Mth.lerp3(d4, d5, d6, gradDot(m82p(m82p2), d, d2, d3), gradDot(m82p(m82p5), d - 1.0d, d2, d3), gradDot(m82p(m82p3), d, d2 - 1.0d, d3), gradDot(m82p(m82p6), d - 1.0d, d2 - 1.0d, d3), gradDot(m82p(m82p2 + 1), d, d2, d3 - 1.0d), gradDot(m82p(m82p5 + 1), d - 1.0d, d2, d3 - 1.0d), gradDot(m82p(m82p3 + 1), d, d2 - 1.0d, d3 - 1.0d), gradDot(m82p(m82p6 + 1), d - 1.0d, d2 - 1.0d, d3 - 1.0d));
    }
}
