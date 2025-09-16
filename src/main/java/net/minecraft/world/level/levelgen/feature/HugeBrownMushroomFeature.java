package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/HugeBrownMushroomFeature.class */
public class HugeBrownMushroomFeature extends AbstractHugeMushroomFeature {
    public HugeBrownMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature
    protected void makeCap(LevelAccessor levelAccessor, Random random, BlockPos blockPos, int i, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration) {
        int i2 = hugeMushroomFeatureConfiguration.foliageRadius;
        int i3 = -i2;
        while (i3 <= i2) {
            int i4 = -i2;
            while (i4 <= i2) {
                boolean z = i3 == (-i2);
                boolean z2 = i3 == i2;
                boolean z3 = i4 == (-i2);
                boolean z4 = i4 == i2;
                boolean z5 = z || z2;
                boolean z6 = z3 || z4;
                if (!z5 || !z6) {
                    mutableBlockPos.setWithOffset(blockPos, i3, i, i4);
                    if (!levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) {
                        setBlock(levelAccessor, mutableBlockPos, (BlockState) ((BlockState) ((BlockState) ((BlockState) hugeMushroomFeatureConfiguration.capProvider.getState(random, blockPos).setValue(HugeMushroomBlock.WEST, Boolean.valueOf(z || (z6 && i3 == 1 - i2)))).setValue(HugeMushroomBlock.EAST, Boolean.valueOf(z2 || (z6 && i3 == i2 - 1)))).setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(z3 || (z5 && i4 == 1 - i2)))).setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(z4 || (z5 && i4 == i2 - 1))));
                    }
                }
                i4++;
            }
            i3++;
        }
    }

    @Override // net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature
    protected int getTreeRadiusForHeight(int i, int i2, int i3, int i4) {
        if (i4 <= 3) {
            return 0;
        }
        return i3;
    }
}
