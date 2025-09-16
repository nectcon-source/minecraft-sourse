package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/traits/BishopTransformer.class */
public interface BishopTransformer extends AreaTransformer1, DimensionOffset1Transformer {
    int apply(Context context, int i, int i2, int i3, int i4, int i5);

    @Override // net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1
    default int applyPixel(BigContext<?> bigContext, Area area, int i, int i2) {
        return apply(bigContext, area.get(getParentX(i + 0), getParentY(i2 + 2)), area.get(getParentX(i + 2), getParentY(i2 + 2)), area.get(getParentX(i + 2), getParentY(i2 + 0)), area.get(getParentX(i + 0), getParentY(i2 + 0)), area.get(getParentX(i + 1), getParentY(i2 + 1)));
    }
}
