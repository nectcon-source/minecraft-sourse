package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/CoralMushroomFeature.class */
public class CoralMushroomFeature extends CoralFeature {
    public CoralMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.CoralFeature
    protected boolean placeFeature(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState) {
        int nextInt = random.nextInt(3) + 3;
        int nextInt2 = random.nextInt(3) + 3;
        int nextInt3 = random.nextInt(3) + 3;
        int nextInt4 = random.nextInt(3) + 1;
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        for (int i = 0; i <= nextInt2; i++) {
            for (int i2 = 0; i2 <= nextInt; i2++) {
                for (int i3 = 0; i3 <= nextInt3; i3++) {
                    mutable.set(i + blockPos.getX(), i2 + blockPos.getY(), i3 + blockPos.getZ());
                    mutable.move(Direction.DOWN, nextInt4);
                    if (((i == 0 || i == nextInt2) && (i2 == 0 || i2 == nextInt)) || (((i3 != 0 && i3 != nextInt3) || (i2 != 0 && i2 != nextInt)) && (((i == 0 || i == nextInt2) && (i3 == 0 || i3 == nextInt3)) || ((i != 0 && i != nextInt2 && i2 != 0 && i2 != nextInt && i3 != 0 && i3 != nextInt3) || random.nextFloat() < 0.1f || placeCoralBlock(levelAccessor, random, mutable, blockState))))) {
                    }
                }
            }
        }
        return true;
    }
}
