package net.minecraft.world.level.block.state.pattern;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/pattern/BlockInWorld.class */
public class BlockInWorld {
    private final LevelReader level;
    private final BlockPos pos;
    private final boolean loadChunks;
    private BlockState state;
    private BlockEntity entity;
    private boolean cachedEntity;

    public BlockInWorld(LevelReader levelReader, BlockPos blockPos, boolean z) {
        this.level = levelReader;
        this.pos = blockPos.immutable();
        this.loadChunks = z;
    }

    public BlockState getState() {
        if (this.state == null && (this.loadChunks || this.level.hasChunkAt(this.pos))) {
            this.state = this.level.getBlockState(this.pos);
        }
        return this.state;
    }

    @Nullable
    public BlockEntity getEntity() {
        if (this.entity == null && !this.cachedEntity) {
            this.entity = this.level.getBlockEntity(this.pos);
            this.cachedEntity = true;
        }
        return this.entity;
    }

    public LevelReader getLevel() {
        return this.level;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public static Predicate<BlockInWorld> hasState(Predicate<BlockState> predicate) {
        return blockInWorld -> {
            return blockInWorld != null && predicate.test(blockInWorld.getState());
        };
    }
}
