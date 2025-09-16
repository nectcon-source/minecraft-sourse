package net.minecraft.world.entity.animal.horse;

import java.util.Arrays;
import java.util.Comparator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/Variant.class */
public enum Variant {
    WHITE(0),
    CREAMY(1),
    CHESTNUT(2),
    BROWN(3),
    BLACK(4),
    GRAY(5),
    DARKBROWN(6);

    private static final Variant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Variant::getId)).toArray(var0 -> new Variant[var0]);


    /* renamed from: id */
    private final int id;

    Variant(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public static Variant byId(int i) {
        return BY_ID[i % BY_ID.length];
    }
}
