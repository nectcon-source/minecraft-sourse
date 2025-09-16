package net.minecraft.world.entity;

import net.minecraft.sounds.SoundSource;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/Shearable.class */
public interface Shearable {
    void shear(SoundSource soundSource);

    boolean readyForShearing();
}
