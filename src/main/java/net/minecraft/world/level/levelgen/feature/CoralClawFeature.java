package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/CoralClawFeature.class */
public class CoralClawFeature extends CoralFeature {
    public CoralClawFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.CoralFeature
    protected boolean placeFeature(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState) {
        Direction direction;
        int nextInt;
        if (!placeCoralBlock(levelAccessor, random, blockPos, blockState)) {
            return false;
        }
        Direction randomDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int nextInt2 = random.nextInt(2) + 2;
        List<Direction> newArrayList = Lists.newArrayList(new Direction[]{randomDirection, randomDirection.getClockWise(), randomDirection.getCounterClockWise()});
        Collections.shuffle(newArrayList, random);
        for (Direction direction2 : newArrayList.subList(0, nextInt2)) {
            BlockPos.MutableBlockPos mutable = blockPos.mutable();
            int nextInt3 = random.nextInt(2) + 1;
            mutable.move(direction2);
            if (direction2 == randomDirection) {
                direction = randomDirection;
                nextInt = random.nextInt(3) + 2;
            } else {
                mutable.move(Direction.UP);
                direction = (Direction) Util.getRandom(new Direction[]{direction2, Direction.UP}, random);
                nextInt = random.nextInt(3) + 3;
            }
            for (int i = 0; i < nextInt3 && placeCoralBlock(levelAccessor, random, mutable, blockState); i++) {
                mutable.move(direction);
            }
            mutable.move(direction.getOpposite());
            mutable.move(Direction.UP);
            for (int i2 = 0; i2 < nextInt; i2++) {
                mutable.move(randomDirection);
                if (!placeCoralBlock(levelAccessor, random, mutable, blockState)) {
                    break;
                }
                if (random.nextFloat() < 0.25f) {
                    mutable.move(Direction.UP);
                }
            }
        }
        return true;
    }
}
