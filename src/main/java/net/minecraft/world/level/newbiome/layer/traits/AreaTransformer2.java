package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/traits/AreaTransformer2.class */
public interface AreaTransformer2 extends DimensionTransformer {
    int applyPixel(Context context, Area area, Area area2, int i, int i2);

    default <R extends Area> AreaFactory<R> run(BigContext<R> bigContext, AreaFactory<R> areaFactory, AreaFactory<R> areaFactory2) {
        return () -> {
            Area make = areaFactory.make();
            Area make2 = areaFactory2.make();
            return bigContext.createResult((PixelTransformer) (i, i2) -> {
                bigContext.initRandom(i, i2);
                return applyPixel(bigContext, make, make2, i, i2);
            }, (R) make, (R) make2);
        };
    }
}
