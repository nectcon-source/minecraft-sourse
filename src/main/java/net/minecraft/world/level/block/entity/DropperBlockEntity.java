package net.minecraft.world.level.block.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/DropperBlockEntity.class */
public class DropperBlockEntity extends DispenserBlockEntity {
    public DropperBlockEntity() {
        super(BlockEntityType.DROPPER);
    }

    @Override // net.minecraft.world.level.block.entity.DispenserBlockEntity, net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected Component getDefaultName() {
        return new TranslatableComponent("container.dropper");
    }
}
