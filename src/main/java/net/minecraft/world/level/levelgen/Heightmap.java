//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.world.level.levelgen;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class Heightmap {
    private static final Predicate<BlockState> NOT_AIR = (var0) -> !var0.isAir();
    private static final Predicate<BlockState> MATERIAL_MOTION_BLOCKING = (var0) -> var0.getMaterial().blocksMotion();
    private final BitStorage data = new BitStorage(9, 256);
    private final Predicate<BlockState> isOpaque;
    private final ChunkAccess chunk;

    public Heightmap(ChunkAccess var1, Types var2) {
        this.isOpaque = var2.isOpaque();
        this.chunk = var1;
    }

    public static void primeHeightmaps(ChunkAccess var0, Set<Types> var1) {
        int var2 = var1.size();
        ObjectList<Heightmap> var3 = new ObjectArrayList(var2);
        ObjectListIterator<Heightmap> var4 = var3.iterator();
        int var5 = var0.getHighestSectionPosition() + 16;
        BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();

        for(int var7 = 0; var7 < 16; ++var7) {
            for(int var8 = 0; var8 < 16; ++var8) {
                for(Types var10 : var1) {
                    var3.add(var0.getOrCreateHeightmapUnprimed(var10));
                }

                for(int var12 = var5 - 1; var12 >= 0; --var12) {
                    var6.set(var7, var12, var8);
                    BlockState var13 = var0.getBlockState(var6);
                    if (!var13.is(Blocks.AIR)) {
                        while(var4.hasNext()) {
                            Heightmap var11 = (Heightmap)var4.next();
                            if (var11.isOpaque.test(var13)) {
                                var11.setHeight(var7, var8, var12 + 1);
                                var4.remove();
                            }
                        }

                        if (var3.isEmpty()) {
                            break;
                        }

                        var4.back(var2);
                    }
                }
            }
        }

    }

    public boolean update(int var1, int var2, int var3, BlockState var4) {
        int var5 = this.getFirstAvailable(var1, var3);
        if (var2 <= var5 - 2) {
            return false;
        } else {
            if (this.isOpaque.test(var4)) {
                if (var2 >= var5) {
                    this.setHeight(var1, var3, var2 + 1);
                    return true;
                }
            } else if (var5 - 1 == var2) {
                BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();

                for(int var7 = var2 - 1; var7 >= 0; --var7) {
                    var6.set(var1, var7, var3);
                    if (this.isOpaque.test(this.chunk.getBlockState(var6))) {
                        this.setHeight(var1, var3, var7 + 1);
                        return true;
                    }
                }

                this.setHeight(var1, var3, 0);
                return true;
            }

            return false;
        }
    }

    public int getFirstAvailable(int var1, int var2) {
        return this.getFirstAvailable(getIndex(var1, var2));
    }

    private int getFirstAvailable(int var1) {
        return this.data.get(var1);
    }

    private void setHeight(int var1, int var2, int var3) {
        this.data.set(getIndex(var1, var2), var3);
    }

    public void setRawData(long[] var1) {
        System.arraycopy(var1, 0, this.data.getRaw(), 0, var1.length);
    }

    public long[] getRawData() {
        return this.data.getRaw();
    }

    private static int getIndex(int var0, int var1) {
        return var0 + var1 * 16;
    }

    public static enum Usage {
        WORLDGEN,
        LIVE_WORLD,
        CLIENT;
    }

    public static enum Types implements StringRepresentable {
        WORLD_SURFACE_WG("WORLD_SURFACE_WG", Heightmap.Usage.WORLDGEN, Heightmap.NOT_AIR),
        WORLD_SURFACE("WORLD_SURFACE", Heightmap.Usage.CLIENT, Heightmap.NOT_AIR),
        OCEAN_FLOOR_WG("OCEAN_FLOOR_WG", Heightmap.Usage.WORLDGEN, Heightmap.MATERIAL_MOTION_BLOCKING),
        OCEAN_FLOOR("OCEAN_FLOOR", Heightmap.Usage.LIVE_WORLD, Heightmap.MATERIAL_MOTION_BLOCKING),
        MOTION_BLOCKING("MOTION_BLOCKING", Heightmap.Usage.CLIENT, (var0) -> var0.getMaterial().blocksMotion() || !var0.getFluidState().isEmpty()),
        MOTION_BLOCKING_NO_LEAVES("MOTION_BLOCKING_NO_LEAVES", Heightmap.Usage.LIVE_WORLD, (var0) -> (var0.getMaterial().blocksMotion() || !var0.getFluidState().isEmpty()) && !(var0.getBlock() instanceof LeavesBlock));

        public static final Codec<Types> CODEC = StringRepresentable.fromEnum(Types::values, Types::getFromKey);
        private final String serializationKey;
        private final Usage usage;
        private final Predicate<BlockState> isOpaque;
        private static final Map<String, Types> REVERSE_LOOKUP = Util.make(Maps.newHashMap(), (var0) -> {
            for(Types var4 : values()) {
                var0.put(var4.serializationKey, var4);
            }

        });

        private Types(String var3, Usage var4, Predicate<BlockState> var5) {
            this.serializationKey = var3;
            this.usage = var4;
            this.isOpaque = var5;
        }

        public String getSerializationKey() {
            return this.serializationKey;
        }

        public boolean sendToClient() {
            return this.usage == Heightmap.Usage.CLIENT;
        }

        public boolean keepAfterWorldgen() {
            return this.usage != Heightmap.Usage.WORLDGEN;
        }

        @Nullable
        public static Types getFromKey(String var0) {
            return REVERSE_LOOKUP.get(var0);
        }

        public Predicate<BlockState> isOpaque() {
            return this.isOpaque;
        }

        public String getSerializedName() {
            return this.serializationKey;
        }
    }
}
