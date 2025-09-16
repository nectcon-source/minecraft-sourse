package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/traits/AreaTransformer1.class */
public interface AreaTransformer1 extends DimensionTransformer {
    int applyPixel(BigContext<?> bigContext, Area area, int i, int i2);

    default <R extends Area> AreaFactory<R> run(BigContext<R> bigContext, AreaFactory<R> areaFactory) {
        return () -> {
            Area make = areaFactory.make();
            return bigContext.createResult((PixelTransformer) (i, i2) -> {
                bigContext.initRandom(i, i2);
                return applyPixel(bigContext, make, i, i2);
            }, (R) make);
        };
    }
}
