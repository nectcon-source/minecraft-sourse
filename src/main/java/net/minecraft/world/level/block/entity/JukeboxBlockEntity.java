package net.minecraft.world.level.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/JukeboxBlockEntity.class */
public class JukeboxBlockEntity extends BlockEntity implements Clearable {
    private ItemStack record;

    public JukeboxBlockEntity() {
        super(BlockEntityType.JUKEBOX);
        this.record = ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        if (compoundTag.contains("RecordItem", 10)) {
            setRecord(ItemStack.of(compoundTag.getCompound("RecordItem")));
        }
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (!getRecord().isEmpty()) {
            compoundTag.put("RecordItem", getRecord().save(new CompoundTag()));
        }
        return compoundTag;
    }

    public ItemStack getRecord() {
        return this.record;
    }

    public void setRecord(ItemStack itemStack) {
        this.record = itemStack;
        setChanged();
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        setRecord(ItemStack.EMPTY);
    }
}
