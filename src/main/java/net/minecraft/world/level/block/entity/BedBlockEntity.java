package net.minecraft.world.level.block.entity;

import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BedBlockEntity.class */
public class BedBlockEntity extends BlockEntity {
    private DyeColor color;

    public BedBlockEntity() {
        super(BlockEntityType.BED);
    }

    public BedBlockEntity(DyeColor dyeColor) {
        this();
        setColor(dyeColor);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 11, getUpdateTag());
    }

    public DyeColor getColor() {
        if (this.color == null) {
            this.color = ((BedBlock) getBlockState().getBlock()).getColor();
        }
        return this.color;
    }

    public void setColor(DyeColor dyeColor) {
        this.color = dyeColor;
    }
}
