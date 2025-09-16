package net.minecraft.world.level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/TickPriority.class */
public enum TickPriority {
    EXTREMELY_HIGH(-3),
    VERY_HIGH(-2),
    HIGH(-1),
    NORMAL(0),
    LOW(1),
    VERY_LOW(2),
    EXTREMELY_LOW(3);

    private final int value;

    TickPriority(int i) {
        this.value = i;
    }

    public static TickPriority byValue(int i) {
        for (TickPriority tickPriority : values()) {
            if (tickPriority.value == i) {
                return tickPriority;
            }
        }
        if (i < EXTREMELY_HIGH.value) {
            return EXTREMELY_HIGH;
        }
        return EXTREMELY_LOW;
    }

    public int getValue() {
        return this.value;
    }
}
