package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/CoralTreeFeature.class */
public class CoralTreeFeature extends CoralFeature {
    public CoralTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.CoralFeature
    protected boolean placeFeature(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState) {
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        int nextInt = random.nextInt(3) + 1;
        for (int i = 0; i < nextInt; i++) {
            if (!placeCoralBlock(levelAccessor, random, mutable, blockState)) {
                return true;
            }
            mutable.move(Direction.UP);
        }
        BlockPos immutable = mutable.immutable();
        int nextInt2 = random.nextInt(3) + 2;
        List<Direction> newArrayList = Lists.newArrayList(Direction.Plane.HORIZONTAL);
        Collections.shuffle(newArrayList, random);
        for (Direction direction : newArrayList.subList(0, nextInt2)) {
            mutable.set(immutable);
            mutable.move(direction);
            int nextInt3 = random.nextInt(5) + 2;
            int i2 = 0;
            for (int i3 = 0; i3 < nextInt3 && placeCoralBlock(levelAccessor, random, mutable, blockState); i3++) {
                i2++;
                mutable.move(Direction.UP);
                if (i3 == 0 || (i2 >= 2 && random.nextFloat() < 0.25f)) {
                    mutable.move(direction);
                    i2 = 0;
                }
            }
        }
        return true;
    }
}
