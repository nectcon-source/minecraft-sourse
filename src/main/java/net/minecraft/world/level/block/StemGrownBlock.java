package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.BlockBehaviour;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/StemGrownBlock.class */
public abstract class StemGrownBlock extends Block {
    public abstract StemBlock getStem();

    public abstract AttachedStemBlock getAttachedStem();

    public StemGrownBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
