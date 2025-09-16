package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/HugeRedMushroomFeature.class */
public class HugeRedMushroomFeature extends AbstractHugeMushroomFeature {
    public HugeRedMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature
    protected void makeCap(LevelAccessor levelAccessor, Random random, BlockPos blockPos, int i, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration) {
        int i2 = i - 3;
        while (i2 <= i) {
            int i3 = i2 < i ? hugeMushroomFeatureConfiguration.foliageRadius : hugeMushroomFeatureConfiguration.foliageRadius - 1;
            int i4 = hugeMushroomFeatureConfiguration.foliageRadius - 2;
            int i5 = -i3;
            while (i5 <= i3) {
                int i6 = -i3;
                while (i6 <= i3) {
                    boolean z = i5 == (-i3);
                    boolean z2 = i5 == i3;
                    boolean z3 = i6 == (-i3);
                    boolean z4 = i6 == i3;
                    boolean z5 = z || z2;
                    boolean z6 = z3 || z4;
                    if (i2 >= i || z5 != z6) {
                        mutableBlockPos.setWithOffset(blockPos, i5, i2, i6);
                        if (!levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) {
                            setBlock(levelAccessor, mutableBlockPos, (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) hugeMushroomFeatureConfiguration.capProvider.getState(random, blockPos).setValue(HugeMushroomBlock.UP, Boolean.valueOf(i2 >= i - 1))).setValue(HugeMushroomBlock.WEST, Boolean.valueOf(i5 < (-i4)))).setValue(HugeMushroomBlock.EAST, Boolean.valueOf(i5 > i4))).setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(i6 < (-i4)))).setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(i6 > i4)));
                        }
                    }
                    i6++;
                }
                i5++;
            }
            i2++;
        }
    }

    @Override // net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature
    protected int getTreeRadiusForHeight(int i, int i2, int i3, int i4) {
        int i5 = 0;
        if (i4 < i2 && i4 >= i2 - 3) {
            i5 = i3;
        } else if (i4 == i2) {
            i5 = i3;
        }
        return i5;
    }
}
