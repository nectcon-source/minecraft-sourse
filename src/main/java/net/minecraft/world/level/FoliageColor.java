package net.minecraft.world.level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/FoliageColor.class */
public class FoliageColor {
    private static int[] pixels = new int[65536];

    public static void init(int[] iArr) {
        pixels = iArr;
    }

    public static int get(double d, double d2) {
        return pixels[(((int) ((1.0d - (d2 * d)) * 255.0d)) << 8) | ((int) ((1.0d - d) * 255.0d))];
    }

    public static int getEvergreenColor() {
        return 6396257;
    }

    public static int getBirchColor() {
        return 8431445;
    }

    public static int getDefaultColor() {
        return 4764952;
    }
}
