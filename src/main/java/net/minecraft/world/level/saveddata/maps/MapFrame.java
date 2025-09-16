package net.minecraft.world.level.saveddata.maps;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/saveddata/maps/MapFrame.class */
public class MapFrame {
    private final BlockPos pos;
    private final int rotation;
    private final int entityId;

    public MapFrame(BlockPos blockPos, int i, int i2) {
        this.pos = blockPos;
        this.rotation = i;
        this.entityId = i2;
    }

    public static MapFrame load(CompoundTag compoundTag) {
        return new MapFrame(NbtUtils.readBlockPos(compoundTag.getCompound("Pos")), compoundTag.getInt("Rotation"), compoundTag.getInt("EntityId"));
    }

    public CompoundTag save() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("Pos", NbtUtils.writeBlockPos(this.pos));
        compoundTag.putInt("Rotation", this.rotation);
        compoundTag.putInt("EntityId", this.entityId);
        return compoundTag;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getId() {
        return frameId(this.pos);
    }

    public static String frameId(BlockPos blockPos) {
        return "frame-" + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
    }
}
