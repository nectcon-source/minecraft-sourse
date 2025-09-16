package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/EntityBlock.class */
public interface EntityBlock {
    @Nullable
    BlockEntity newBlockEntity(BlockGetter blockGetter);
}
