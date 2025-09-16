package net.minecraft.world.level.newbiome.layer;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/Layer.class */
public class Layer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final LazyArea area;

    public Layer(AreaFactory<LazyArea> areaFactory) {
        this.area = areaFactory.make();
    }

    public Biome get(Registry<Biome> registry, int i, int i2) {
        int i3 = this.area.get(i, i2);
        ResourceKey<Biome> byId = Biomes.byId(i3);
        if (byId == null) {
            throw new IllegalStateException("Unknown biome id emitted by layers: " + i3);
        }
        Biome biome = registry.get(byId);
        if (biome == null) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw ( Util.pauseInIde(new IllegalStateException("Unknown biome id: " + i3)));
            }
            LOGGER.warn("Unknown biome id: ", Integer.valueOf(i3));
            return registry.get(Biomes.byId(0));
        }
        return biome;
    }
}
