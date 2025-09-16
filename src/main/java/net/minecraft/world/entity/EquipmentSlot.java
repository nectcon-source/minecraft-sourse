package net.minecraft.world.entity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/EquipmentSlot.class */
public enum EquipmentSlot {
    MAINHAND(Type.HAND, 0, 0, "mainhand"),
    OFFHAND(Type.HAND, 1, 5, "offhand"),
    FEET(Type.ARMOR, 0, 1, "feet"),
    LEGS(Type.ARMOR, 1, 2, "legs"),
    CHEST(Type.ARMOR, 2, 3, "chest"),
    HEAD(Type.ARMOR, 3, 4, "head");

    private final Type type;
    private final int index;
    private final int filterFlag;
    private final String name;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/EquipmentSlot$Type.class */
    public enum Type {
        HAND,
        ARMOR
    }

    EquipmentSlot(Type type, int i, int i2, String str) {
        this.type = type;
        this.index = i;
        this.filterFlag = i2;
        this.name = str;
    }

    public Type getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public int getFilterFlag() {
        return this.filterFlag;
    }

    public String getName() {
        return this.name;
    }

    public static EquipmentSlot byName(String str) {
        for (EquipmentSlot equipmentSlot : values()) {
            if (equipmentSlot.getName().equals(str)) {
                return equipmentSlot;
            }
        }
        throw new IllegalArgumentException("Invalid slot '" + str + "'");
    }

    public static EquipmentSlot byTypeAndIndex(Type type, int i) {
        for (EquipmentSlot equipmentSlot : values()) {
            if (equipmentSlot.getType() == type && equipmentSlot.getIndex() == i) {
                return equipmentSlot;
            }
        }
        throw new IllegalArgumentException("Invalid slot '" + type + "': " + i);
    }
}
