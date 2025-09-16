package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/traits/C1Transformer.class */
public interface C1Transformer extends AreaTransformer1, DimensionOffset1Transformer {
    int apply(Context context, int i);

    @Override // net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1
    default int applyPixel(BigContext<?> bigContext, Area area, int i, int i2) {
        return apply(bigContext, area.get(getParentX(i + 1), getParentY(i2 + 1)));
    }
}
