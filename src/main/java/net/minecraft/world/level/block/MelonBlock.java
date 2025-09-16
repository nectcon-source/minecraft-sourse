package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.BlockBehaviour;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/MelonBlock.class */
public class MelonBlock extends StemGrownBlock {
    protected MelonBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.StemGrownBlock
    public StemBlock getStem() {
        return (StemBlock) Blocks.MELON_STEM;
    }

    @Override // net.minecraft.world.level.block.StemGrownBlock
    public AttachedStemBlock getAttachedStem() {
        return (AttachedStemBlock) Blocks.ATTACHED_MELON_STEM;
    }
}
