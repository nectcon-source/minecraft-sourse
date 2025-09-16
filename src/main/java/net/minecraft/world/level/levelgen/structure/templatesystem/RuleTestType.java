package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/RuleTestType.class */
public interface RuleTestType<P extends RuleTest> {
    public static final RuleTestType<AlwaysTrueTest> ALWAYS_TRUE_TEST = register("always_true", AlwaysTrueTest.CODEC);
    public static final RuleTestType<BlockMatchTest> BLOCK_TEST = register("block_match", BlockMatchTest.CODEC);
    public static final RuleTestType<BlockStateMatchTest> BLOCKSTATE_TEST = register("blockstate_match", BlockStateMatchTest.CODEC);
    public static final RuleTestType<TagMatchTest> TAG_TEST = register("tag_match", TagMatchTest.CODEC);
    public static final RuleTestType<RandomBlockMatchTest> RANDOM_BLOCK_TEST = register("random_block_match", RandomBlockMatchTest.CODEC);
    public static final RuleTestType<RandomBlockStateMatchTest> RANDOM_BLOCKSTATE_TEST = register("random_blockstate_match", RandomBlockStateMatchTest.CODEC);

    Codec<P> codec();

//    static <P extends RuleTest> RuleTestType<P> register(String str, Codec<P> codec) {
//        return (RuleTestType) Registry.register(Registry.RULE_TEST, str, () -> {
//            return codec;
//        });
//    }
static <P extends RuleTest> RuleTestType<P> register(String str, Codec<P> codec) {
    return (RuleTestType<P>) Registry.register(
            Registry.RULE_TEST,
            str,
            () -> (Codec<RuleTest>)codec
    );
}
}
