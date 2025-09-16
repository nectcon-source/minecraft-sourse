package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/RareBiomeLargeLayer.class */
public enum RareBiomeLargeLayer implements C1Transformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.C1Transformer
    public int apply(Context context, int i) {
        if (context.nextRandom(10) == 0 && i == 21) {
            return 168;
        }
        return i;
    }
}
