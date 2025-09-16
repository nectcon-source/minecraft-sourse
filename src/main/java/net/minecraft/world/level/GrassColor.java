package net.minecraft.world.level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/GrassColor.class */
public class GrassColor {
    private static int[] pixels = new int[65536];

    public static void init(int[] iArr) {
        pixels = iArr;
    }

    public static int get(double d, double d2) {
        int i = (((int) ((1.0d - (d2 * d)) * 255.0d)) << 8) | ((int) ((1.0d - d) * 255.0d));
        if (i > pixels.length) {
            return -65281;
        }
        return pixels[i];
    }
}
