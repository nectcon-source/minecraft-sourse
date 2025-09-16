package net.minecraft.world.entity.ai.village.poi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/village/poi/PoiRecord.class */
public class PoiRecord {
    private final BlockPos pos;
    private final PoiType poiType;
    private int freeTickets;
    private final Runnable setDirty;

    public static Codec<PoiRecord> codec(Runnable runnable) {
        return RecordCodecBuilder.create(instance -> {
            return instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(poiRecord -> {
                return poiRecord.pos;
            }), Registry.POINT_OF_INTEREST_TYPE.fieldOf("type").forGetter(poiRecord2 -> {
                return poiRecord2.poiType;
            }), Codec.INT.fieldOf("free_tickets").orElse(0).forGetter(poiRecord3 -> {
                return Integer.valueOf(poiRecord3.freeTickets);
            }), RecordCodecBuilder.point(runnable)).apply(instance, (v1, v2, v3, v4) -> {
                return new PoiRecord(v1, v2, v3, v4);
            });
        });
    }

    private PoiRecord(BlockPos blockPos, PoiType poiType, int i, Runnable runnable) {
        this.pos = blockPos.immutable();
        this.poiType = poiType;
        this.freeTickets = i;
        this.setDirty = runnable;
    }

    public PoiRecord(BlockPos blockPos, PoiType poiType, Runnable runnable) {
        this(blockPos, poiType, poiType.getMaxTickets(), runnable);
    }

    protected boolean acquireTicket() {
        if (this.freeTickets <= 0) {
            return false;
        }
        this.freeTickets--;
        this.setDirty.run();
        return true;
    }

    protected boolean releaseTicket() {
        if (this.freeTickets >= this.poiType.getMaxTickets()) {
            return false;
        }
        this.freeTickets++;
        this.setDirty.run();
        return true;
    }

    public boolean hasSpace() {
        return this.freeTickets > 0;
    }

    public boolean isOccupied() {
        return this.freeTickets != this.poiType.getMaxTickets();
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public PoiType getPoiType() {
        return this.poiType;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.pos, ((PoiRecord) obj).pos);
    }

    public int hashCode() {
        return this.pos.hashCode();
    }
}
