package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/traits/AreaTransformer0.class */
public interface AreaTransformer0 {
    int applyPixel(Context context, int i, int i2);

    default <R extends Area> AreaFactory<R> run(BigContext<R> bigContext) {
        return () -> {
            return bigContext.createResult((i, i2) -> {
                bigContext.initRandom(i, i2);
                return applyPixel(bigContext, i, i2);
            });
        };
    }
}
