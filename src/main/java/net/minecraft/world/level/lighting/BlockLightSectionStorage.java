//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class BlockLightSectionStorage extends LayerLightSectionStorage<BlockLightSectionStorage.BlockDataLayerStorageMap> {
    protected BlockLightSectionStorage(LightChunkGetter var1) {
        super(LightLayer.BLOCK, var1, new BlockDataLayerStorageMap(new Long2ObjectOpenHashMap()));
    }

    protected int getLightValue(long var1) {
        long var3 = SectionPos.blockToSection(var1);
        DataLayer var5 = this.getDataLayer(var3, false);
        return var5 == null ? 0 : var5.get(SectionPos.sectionRelative(BlockPos.getX(var1)), SectionPos.sectionRelative(BlockPos.getY(var1)), SectionPos.sectionRelative(BlockPos.getZ(var1)));
    }

    public static final class BlockDataLayerStorageMap extends DataLayerStorageMap<BlockDataLayerStorageMap> {
        public BlockDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> var1) {
            super(var1);
        }

        public BlockDataLayerStorageMap copy() {
            return new BlockDataLayerStorageMap(this.map.clone());
        }
    }
}
