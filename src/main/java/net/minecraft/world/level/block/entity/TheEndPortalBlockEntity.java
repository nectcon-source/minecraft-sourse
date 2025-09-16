package net.minecraft.world.level.block.entity;

import net.minecraft.core.Direction;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/TheEndPortalBlockEntity.class */
public class TheEndPortalBlockEntity extends BlockEntity {
    public TheEndPortalBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    public TheEndPortalBlockEntity() {
        this(BlockEntityType.END_PORTAL);
    }

    public boolean shouldRenderFace(Direction direction) {
        return direction == Direction.UP;
    }
}
