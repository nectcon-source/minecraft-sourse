package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/LevelChunkSection.class */
public class LevelChunkSection {
    private static final Palette<BlockState> GLOBAL_BLOCKSTATE_PALETTE = new GlobalPalette(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState());
    private final int bottomBlockY;
    private short nonEmptyBlockCount;
    private short tickingBlockCount;
    private short tickingFluidCount;
    private final PalettedContainer<BlockState> states;

    public LevelChunkSection(int i) {
        this(i, (short) 0, (short) 0, (short) 0);
    }

    public LevelChunkSection(int i, short s, short s2, short s3) {
        this.bottomBlockY = i;
        this.nonEmptyBlockCount = s;
        this.tickingBlockCount = s2;
        this.tickingFluidCount = s3;
        this.states = new PalettedContainer<>(GLOBAL_BLOCKSTATE_PALETTE, Block.BLOCK_STATE_REGISTRY, NbtUtils::readBlockState, NbtUtils::writeBlockState, Blocks.AIR.defaultBlockState());
    }

    public BlockState getBlockState(int i, int i2, int i3) {
        return this.states.get(i, i2, i3);
    }

    public FluidState getFluidState(int i, int i2, int i3) {
        return this.states.get(i, i2, i3).getFluidState();
    }

    public void acquire() {
        this.states.acquire();
    }

    public void release() {
        this.states.release();
    }

    public BlockState setBlockState(int i, int i2, int i3, BlockState blockState) {
        return setBlockState(i, i2, i3, blockState, true);
    }

    public BlockState setBlockState(int i, int i2, int i3, BlockState blockState, boolean z) {
        BlockState andSetUnchecked;
        if (z) {
            andSetUnchecked = this.states.getAndSet(i, i2, i3, blockState);
        } else {
            andSetUnchecked = this.states.getAndSetUnchecked(i, i2, i3, blockState);
        }
        FluidState fluidState = andSetUnchecked.getFluidState();
        FluidState fluidState2 = blockState.getFluidState();
        if (!andSetUnchecked.isAir()) {
            this.nonEmptyBlockCount = (short) (this.nonEmptyBlockCount - 1);
            if (andSetUnchecked.isRandomlyTicking()) {
                this.tickingBlockCount = (short) (this.tickingBlockCount - 1);
            }
        }
        if (!fluidState.isEmpty()) {
            this.tickingFluidCount = (short) (this.tickingFluidCount - 1);
        }
        if (!blockState.isAir()) {
            this.nonEmptyBlockCount = (short) (this.nonEmptyBlockCount + 1);
            if (blockState.isRandomlyTicking()) {
                this.tickingBlockCount = (short) (this.tickingBlockCount + 1);
            }
        }
        if (!fluidState2.isEmpty()) {
            this.tickingFluidCount = (short) (this.tickingFluidCount + 1);
        }
        return andSetUnchecked;
    }

    public boolean isEmpty() {
        return this.nonEmptyBlockCount == 0;
    }

    public static boolean isEmpty(@Nullable LevelChunkSection levelChunkSection) {
        return levelChunkSection == LevelChunk.EMPTY_SECTION || levelChunkSection.isEmpty();
    }

    public boolean isRandomlyTicking() {
        return isRandomlyTickingBlocks() || isRandomlyTickingFluids();
    }

    public boolean isRandomlyTickingBlocks() {
        return this.tickingBlockCount > 0;
    }

    public boolean isRandomlyTickingFluids() {
        return this.tickingFluidCount > 0;
    }

    public int bottomBlockY() {
        return this.bottomBlockY;
    }

    public void recalcBlockCounts() {
        this.nonEmptyBlockCount = (short) 0;
        this.tickingBlockCount = (short) 0;
        this.tickingFluidCount = (short) 0;
        this.states.count((blockState, i) -> {
            FluidState fluidState = blockState.getFluidState();
            if (!blockState.isAir()) {
                this.nonEmptyBlockCount = (short) (this.nonEmptyBlockCount + i);
                if (blockState.isRandomlyTicking()) {
                    this.tickingBlockCount = (short) (this.tickingBlockCount + i);
                }
            }
            if (!fluidState.isEmpty()) {
                this.nonEmptyBlockCount = (short) (this.nonEmptyBlockCount + i);
                if (fluidState.isRandomlyTicking()) {
                    this.tickingFluidCount = (short) (this.tickingFluidCount + i);
                }
            }
        });
    }

    public PalettedContainer<BlockState> getStates() {
        return this.states;
    }

    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.nonEmptyBlockCount = friendlyByteBuf.readShort();
        this.states.read(friendlyByteBuf);
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeShort(this.nonEmptyBlockCount);
        this.states.write(friendlyByteBuf);
    }

    public int getSerializedSize() {
        return 2 + this.states.getSerializedSize();
    }

    public boolean maybeHas(Predicate<BlockState> predicate) {
        return this.states.maybeHas(predicate);
    }
}
