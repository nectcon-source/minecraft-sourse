package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/LinearPosTest.class */
public class LinearPosTest extends PosRuleTest {
    public static final Codec<LinearPosTest> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.FLOAT.fieldOf("min_chance").orElse(Float.valueOf(0.0f)).forGetter(linearPosTest -> {
            return Float.valueOf(linearPosTest.minChance);
        }), Codec.FLOAT.fieldOf("max_chance").orElse(Float.valueOf(0.0f)).forGetter(linearPosTest2 -> {
            return Float.valueOf(linearPosTest2.maxChance);
        }), Codec.INT.fieldOf("min_dist").orElse(0).forGetter(linearPosTest3 -> {
            return Integer.valueOf(linearPosTest3.minDist);
        }), Codec.INT.fieldOf("max_dist").orElse(0).forGetter(linearPosTest4 -> {
            return Integer.valueOf(linearPosTest4.maxDist);
        })).apply(instance, (v1, v2, v3, v4) -> {
            return new LinearPosTest(v1, v2, v3, v4);
        });
    });
    private final float minChance;
    private final float maxChance;
    private final int minDist;
    private final int maxDist;

    public LinearPosTest(float f, float f2, int i, int i2) {
        if (i >= i2) {
            throw new IllegalArgumentException("Invalid range: [" + i + "," + i2 + "]");
        }
        this.minChance = f;
        this.maxChance = f2;
        this.minDist = i;
        this.maxDist = i2;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest
    public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random) {
        return ((double) random.nextFloat()) <= Mth.clampedLerp((double) this.minChance, (double) this.maxChance, Mth.inverseLerp((double) blockPos2.distManhattan(blockPos3), (double) this.minDist, (double) this.maxDist));
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.LINEAR_POS_TEST;
    }
}
