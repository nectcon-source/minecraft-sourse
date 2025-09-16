package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/blockplacers/ColumnPlacer.class */
public class ColumnPlacer extends BlockPlacer {
    public static final Codec<ColumnPlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.INT.fieldOf("min_size").forGetter(columnPlacer -> {
            return Integer.valueOf(columnPlacer.minSize);
        }), Codec.INT.fieldOf("extra_size").forGetter(columnPlacer2 -> {
            return Integer.valueOf(columnPlacer2.extraSize);
        })).apply(instance, (v1, v2) -> {
            return new ColumnPlacer(v1, v2);
        });
    });
    private final int minSize;
    private final int extraSize;

    public ColumnPlacer(int i, int i2) {
        this.minSize = i;
        this.extraSize = i2;
    }

    @Override // net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer
    protected BlockPlacerType<?> type() {
        return BlockPlacerType.COLUMN_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer
    public void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        int nextInt = this.minSize + random.nextInt(random.nextInt(this.extraSize + 1) + 1);
        for (int i = 0; i < nextInt; i++) {
            levelAccessor.setBlock(mutable, blockState, 2);
            mutable.move(Direction.UP);
        }
    }
}
