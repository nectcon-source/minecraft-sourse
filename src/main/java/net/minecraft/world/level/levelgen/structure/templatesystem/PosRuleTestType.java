package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/PosRuleTestType.class */
public interface PosRuleTestType<P extends PosRuleTest> {
    public static final PosRuleTestType<PosAlwaysTrueTest> ALWAYS_TRUE_TEST = register("always_true", PosAlwaysTrueTest.CODEC);
    public static final PosRuleTestType<LinearPosTest> LINEAR_POS_TEST = register("linear_pos", LinearPosTest.CODEC);
    public static final PosRuleTestType<AxisAlignedLinearPosTest> AXIS_ALIGNED_LINEAR_POS_TEST = register("axis_aligned_linear_pos", AxisAlignedLinearPosTest.CODEC);

    Codec<P> codec();

//    static <P extends PosRuleTest> PosRuleTestType<P> register(String str, Codec<P> codec) {
//        return (PosRuleTestType) Registry.register(Registry.POS_RULE_TEST, str, () -> {
//            return codec;
//        });
//    }
static <P extends PosRuleTest> PosRuleTestType<P> register(String str, Codec<P> codec) {
    return (PosRuleTestType<P>) Registry.register(
            Registry.POS_RULE_TEST,
            str,
            () -> (Codec<PosRuleTest>)codec
    );
}
}
