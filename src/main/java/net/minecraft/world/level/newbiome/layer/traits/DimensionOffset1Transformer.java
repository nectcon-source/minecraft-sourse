package net.minecraft.world.level.newbiome.layer.traits;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/traits/DimensionOffset1Transformer.class */
public interface DimensionOffset1Transformer extends DimensionTransformer {
    @Override // net.minecraft.world.level.newbiome.layer.traits.DimensionTransformer
    default int getParentX(int i) {
        return i - 1;
    }

    @Override // net.minecraft.world.level.newbiome.layer.traits.DimensionTransformer
    default int getParentY(int i) {
        return i - 1;
    }
}
