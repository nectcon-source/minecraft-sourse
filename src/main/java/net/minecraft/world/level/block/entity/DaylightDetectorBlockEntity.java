package net.minecraft.world.level.block.entity;

import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/DaylightDetectorBlockEntity.class */
public class DaylightDetectorBlockEntity extends BlockEntity implements TickableBlockEntity {
    public DaylightDetectorBlockEntity() {
        super(BlockEntityType.DAYLIGHT_DETECTOR);
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        if (this.level != null && !this.level.isClientSide && this.level.getGameTime() % 20 == 0) {
            BlockState blockState = getBlockState();
            if (blockState.getBlock() instanceof DaylightDetectorBlock) {
                DaylightDetectorBlock.updateSignalStrength(blockState, this.level, this.worldPosition);
            }
        }
    }
}
