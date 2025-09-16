package net.minecraft.world.level.block.entity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/TrappedChestBlockEntity.class */
public class TrappedChestBlockEntity extends ChestBlockEntity {
    public TrappedChestBlockEntity() {
        super(BlockEntityType.TRAPPED_CHEST);
    }

    @Override // net.minecraft.world.level.block.entity.ChestBlockEntity
    protected void signalOpenCount() {
        super.signalOpenCount();
        this.level.updateNeighborsAt(this.worldPosition.below(), getBlockState().getBlock());
    }
}
