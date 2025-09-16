package net.minecraft.world.entity.player;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/player/PlayerModelPart.class */
public enum PlayerModelPart {
    CAPE(0, "cape"),
    JACKET(1, "jacket"),
    LEFT_SLEEVE(2, "left_sleeve"),
    RIGHT_SLEEVE(3, "right_sleeve"),
    LEFT_PANTS_LEG(4, "left_pants_leg"),
    RIGHT_PANTS_LEG(5, "right_pants_leg"),
    HAT(6, "hat");

    private final int bit;
    private final int mask;

    /* renamed from: id */
    private final String f453id;
    private final Component name;

    PlayerModelPart(int i, String str) {
        this.bit = i;
        this.mask = 1 << i;
        this.f453id = str;
        this.name = new TranslatableComponent("options.modelPart." + str);
    }

    public int getMask() {
        return this.mask;
    }

    public String getId() {
        return this.f453id;
    }

    public Component getName() {
        return this.name;
    }
}
