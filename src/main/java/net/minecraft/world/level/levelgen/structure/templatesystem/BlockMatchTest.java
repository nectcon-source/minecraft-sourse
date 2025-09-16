package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.class */
public class BlockMatchTest extends RuleTest {
    public static final Codec<BlockMatchTest> CODEC = Registry.BLOCK.fieldOf("block").xmap(BlockMatchTest::new, blockMatchTest -> {
        return blockMatchTest.block;
    }).codec();
    private final Block block;

    public BlockMatchTest(Block block) {
        this.block = block;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest
    public boolean test(BlockState blockState, Random random) {
        return blockState.is(this.block);
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest
    protected RuleTestType<?> getType() {
        return RuleTestType.BLOCK_TEST;
    }
}
