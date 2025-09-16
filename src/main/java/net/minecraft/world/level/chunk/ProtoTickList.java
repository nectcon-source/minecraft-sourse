package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/ProtoTickList.class */
public class ProtoTickList<T> implements TickList<T> {
    protected final Predicate<T> ignore;
    private final ChunkPos chunkPos;
    private final ShortList[] toBeTicked;

    public ProtoTickList(Predicate<T> predicate, ChunkPos chunkPos) {
        this(predicate, chunkPos, new ListTag());
    }

    public ProtoTickList(Predicate<T> predicate, ChunkPos chunkPos, ListTag listTag) {
        this.toBeTicked = new ShortList[16];
        this.ignore = predicate;
        this.chunkPos = chunkPos;
        for (int i = 0; i < listTag.size(); i++) {
            ListTag list = listTag.getList(i);
            for (int i2 = 0; i2 < list.size(); i2++) {
                ChunkAccess.getOrCreateOffsetList(this.toBeTicked, i).add(list.getShort(i2));
            }
        }
    }

    public ListTag save() {
        return ChunkSerializer.packOffsets(this.toBeTicked);
    }

    public void copyOut(TickList<T> tickList, Function<BlockPos, T> function) {
        for (int i = 0; i < this.toBeTicked.length; i++) {
            if (this.toBeTicked[i] != null) {
                ShortListIterator it = this.toBeTicked[i].iterator();
                while (it.hasNext()) {
                    BlockPos unpackOffsetCoordinates = ProtoChunk.unpackOffsetCoordinates(((Short) it.next()).shortValue(), i, this.chunkPos);
                    tickList.scheduleTick(unpackOffsetCoordinates, function.apply(unpackOffsetCoordinates), 0);
                }
                this.toBeTicked[i].clear();
            }
        }
    }

    @Override // net.minecraft.world.level.TickList
    public boolean hasScheduledTick(BlockPos blockPos, T t) {
        return false;
    }

    @Override // net.minecraft.world.level.TickList
    public void scheduleTick(BlockPos blockPos, T t, int i, TickPriority tickPriority) {
        ChunkAccess.getOrCreateOffsetList(this.toBeTicked, blockPos.getY() >> 4).add(ProtoChunk.packOffsetCoordinates(blockPos));
    }

    @Override // net.minecraft.world.level.TickList
    public boolean willTickThisTick(BlockPos blockPos, T t) {
        return false;
    }
}
