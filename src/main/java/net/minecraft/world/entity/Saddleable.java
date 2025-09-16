package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundSource;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/Saddleable.class */
public interface Saddleable {
    boolean isSaddleable();

    void equipSaddle(@Nullable SoundSource soundSource);

    boolean isSaddled();
}
