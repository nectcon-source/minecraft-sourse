package net.minecraft.world.level.newbiome.context;

import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/context/Context.class */
public interface Context {
    int nextRandom(int i);

    ImprovedNoise getBiomeNoise();
}
