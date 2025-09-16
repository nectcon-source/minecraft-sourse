package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/AxisAlignedLinearPosTest.class */
public class AxisAlignedLinearPosTest extends PosRuleTest {
    public static final Codec<AxisAlignedLinearPosTest> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.FLOAT.fieldOf("min_chance").orElse(Float.valueOf(0.0f)).forGetter(axisAlignedLinearPosTest -> {
            return Float.valueOf(axisAlignedLinearPosTest.minChance);
        }), Codec.FLOAT.fieldOf("max_chance").orElse(Float.valueOf(0.0f)).forGetter(axisAlignedLinearPosTest2 -> {
            return Float.valueOf(axisAlignedLinearPosTest2.maxChance);
        }), Codec.INT.fieldOf("min_dist").orElse(0).forGetter(axisAlignedLinearPosTest3 -> {
            return Integer.valueOf(axisAlignedLinearPosTest3.minDist);
        }), Codec.INT.fieldOf("max_dist").orElse(0).forGetter(axisAlignedLinearPosTest4 -> {
            return Integer.valueOf(axisAlignedLinearPosTest4.maxDist);
        }), Direction.Axis.CODEC.fieldOf("axis").orElse(Direction.Axis.Y).forGetter(axisAlignedLinearPosTest5 -> {
            return axisAlignedLinearPosTest5.axis;
        })).apply(instance, (v1, v2, v3, v4, v5) -> {
            return new AxisAlignedLinearPosTest(v1, v2, v3, v4, v5);
        });
    });
    private final float minChance;
    private final float maxChance;
    private final int minDist;
    private final int maxDist;
    private final Direction.Axis axis;

    public AxisAlignedLinearPosTest(float f, float f2, int i, int i2, Direction.Axis axis) {
        if (i >= i2) {
            throw new IllegalArgumentException("Invalid range: [" + i + "," + i2 + "]");
        }
        this.minChance = f;
        this.maxChance = f2;
        this.minDist = i;
        this.maxDist = i2;
        this.axis = axis;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest
    public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random) {
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, this.axis);
        return ((double) random.nextFloat()) <= Mth.clampedLerp((double) this.minChance, (double) this.maxChance, Mth.inverseLerp((double) ((int) ((((float) Math.abs((blockPos2.getX() - blockPos3.getX()) * direction.getStepX())) + ((float) Math.abs((blockPos2.getY() - blockPos3.getY()) * direction.getStepY()))) + ((float) Math.abs((blockPos2.getZ() - blockPos3.getZ()) * direction.getStepZ())))), (double) this.minDist, (double) this.maxDist));
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.AXIS_ALIGNED_LINEAR_POS_TEST;
    }
}
