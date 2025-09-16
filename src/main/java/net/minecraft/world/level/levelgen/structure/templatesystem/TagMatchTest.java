package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest.class */
public class TagMatchTest extends RuleTest {
    public static final Codec<TagMatchTest> CODEC = Tag.codec(() -> {
        return SerializationTags.getInstance().getBlocks();
    }).fieldOf("tag").xmap(TagMatchTest::new, tagMatchTest -> {
        return tagMatchTest.tag;
    }).codec();
    private final Tag<Block> tag;

    public TagMatchTest(Tag<Block> tag) {
        this.tag = tag;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest
    public boolean test(BlockState blockState, Random random) {
        return blockState.is(this.tag);
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest
    protected RuleTestType<?> getType() {
        return RuleTestType.TAG_TEST;
    }
}
