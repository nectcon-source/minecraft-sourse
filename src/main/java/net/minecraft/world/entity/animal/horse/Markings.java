package net.minecraft.world.entity.animal.horse;

import java.util.Arrays;
import java.util.Comparator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/Markings.class */
public enum Markings {
    NONE(0),
    WHITE(1),
    WHITE_FIELD(2),
    WHITE_DOTS(3),
    BLACK_DOTS(4);

    private static final Markings[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Markings::getId)).toArray(var0 -> new Markings[var0]);

    /* renamed from: id */
    private final int id;

    Markings(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public static Markings byId(int i) {
        return BY_ID[i % BY_ID.length];
    }
}
