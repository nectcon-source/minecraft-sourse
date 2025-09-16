package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/AddEdgeLayer.class */
public class AddEdgeLayer {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/AddEdgeLayer$CoolWarm.class */
    public enum CoolWarm implements CastleTransformer {
        INSTANCE;

        @Override // net.minecraft.world.level.newbiome.layer.traits.CastleTransformer
        public int apply(Context context, int i, int i2, int i3, int i4, int i5) {
            if (i5 == 1 && (i == 3 || i2 == 3 || i4 == 3 || i3 == 3 || i == 4 || i2 == 4 || i4 == 4 || i3 == 4)) {
                return 2;
            }
            return i5;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/AddEdgeLayer$HeatIce.class */
    public enum HeatIce implements CastleTransformer {
        INSTANCE;

        @Override // net.minecraft.world.level.newbiome.layer.traits.CastleTransformer
        public int apply(Context context, int i, int i2, int i3, int i4, int i5) {
            if (i5 == 4 && (i == 1 || i2 == 1 || i4 == 1 || i3 == 1 || i == 2 || i2 == 2 || i4 == 2 || i3 == 2)) {
                return 3;
            }
            return i5;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/AddEdgeLayer$IntroduceSpecial.class */
    public enum IntroduceSpecial implements C0Transformer {
        INSTANCE;

        @Override // net.minecraft.world.level.newbiome.layer.traits.C0Transformer
        public int apply(Context context, int i) {
            if (!Layers.isShallowOcean(i) && context.nextRandom(13) == 0) {
                i |= ((1 + context.nextRandom(15)) << 8) & 3840;
            }
            return i;
        }
    }
}
