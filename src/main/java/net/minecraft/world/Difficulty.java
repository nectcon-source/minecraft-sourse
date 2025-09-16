package net.minecraft.world;

import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/Difficulty.class */
public enum Difficulty {
    PEACEFUL(0, "peaceful"),
    EASY(1, "easy"),
    NORMAL(2, "normal"),
    HARD(3, "hard");

    private static final Difficulty[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt((v0) -> {
        return v0.getId();
    })).toArray(i -> {
        return new Difficulty[i];
    });

    /* renamed from: id */
    private final int id;
    private final String key;

    Difficulty(int i, String str) {
        this.id = i;
        this.key = str;
    }

    public int getId() {
        return this.id;
    }

    public Component getDisplayName() {
        return new TranslatableComponent("options.difficulty." + this.key);
    }

    public static Difficulty byId(int i) {
        return BY_ID[i % BY_ID.length];
    }

    @Nullable
    public static Difficulty byName(String str) {
        for (Difficulty difficulty : values()) {
            if (difficulty.key.equals(str)) {
                return difficulty;
            }
        }
        return null;
    }

    public String getKey() {
        return this.key;
    }

    public Difficulty nextById() {
        return BY_ID[(this.id + 1) % BY_ID.length];
    }
}
