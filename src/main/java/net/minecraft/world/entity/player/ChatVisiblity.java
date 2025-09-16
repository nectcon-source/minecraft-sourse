package net.minecraft.world.entity.player;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/player/ChatVisiblity.class */
public enum ChatVisiblity {
    FULL(0, "options.chat.visibility.full"),
    SYSTEM(1, "options.chat.visibility.system"),
    HIDDEN(2, "options.chat.visibility.hidden");

    private static final ChatVisiblity[] BY_ID = (ChatVisiblity[]) Arrays.stream(values()).sorted(Comparator.comparingInt((v0) -> {
        return v0.getId();
    })).toArray(i -> {
        return new ChatVisiblity[i];
    });

    /* renamed from: id */
    private final int f452id;
    private final String key;

    ChatVisiblity(int i, String str) {
        this.f452id = i;
        this.key = str;
    }

    public int getId() {
        return this.f452id;
    }

    public String getKey() {
        return this.key;
    }

    public static ChatVisiblity byId(int i) {
        return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
    }
}
