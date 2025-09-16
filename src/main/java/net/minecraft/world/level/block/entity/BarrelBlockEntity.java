package net.minecraft.world.level.block.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BarrelBlockEntity.class */
public class BarrelBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items;
    private int openCount;

    private BarrelBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
        this.items = NonNullList.withSize(27, ItemStack.EMPTY);
    }

    public BarrelBlockEntity() {
        this(BlockEntityType.BARREL);
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (!trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.items);
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.items);
        }
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return 27;
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected Component getDefaultName() {
        return new TranslatableComponent("container.barrel");
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return ChestMenu.threeRows(i, inventory, this);
    }

    @Override // net.minecraft.world.Container
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }
            this.openCount++;
            BlockState blockState = getBlockState();
            if (!((Boolean) blockState.getValue(BarrelBlock.OPEN)).booleanValue()) {
                playSound(blockState, SoundEvents.BARREL_OPEN);
                updateBlockState(blockState, true);
            }
            scheduleRecheck();
        }
    }

    private void scheduleRecheck() {
        this.level.getBlockTicks().scheduleTick(getBlockPos(), getBlockState().getBlock(), 5);
    }

    public void recheckOpen() {
        this.openCount = ChestBlockEntity.getOpenCount(this.level, this, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
        if (this.openCount > 0) {
            scheduleRecheck();
            return;
        }
        BlockState blockState = getBlockState();
        if (!blockState.is(Blocks.BARREL)) {
            setRemoved();
        } else if (((Boolean) blockState.getValue(BarrelBlock.OPEN)).booleanValue()) {
            playSound(blockState, SoundEvents.BARREL_CLOSE);
            updateBlockState(blockState, false);
        }
    }

    @Override // net.minecraft.world.Container
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            this.openCount--;
        }
    }

    private void updateBlockState(BlockState blockState, boolean z) {
        this.level.setBlock(getBlockPos(), (BlockState) blockState.setValue(BarrelBlock.OPEN, Boolean.valueOf(z)), 3);
    }

    private void playSound(BlockState blockState, SoundEvent soundEvent) {
        Vec3i normal = ((Direction) blockState.getValue(BarrelBlock.FACING)).getNormal();
        this.level.playSound(null, this.worldPosition.getX() + 0.5d + (normal.getX() / 2.0d), this.worldPosition.getY() + 0.5d + (normal.getY() / 2.0d), this.worldPosition.getZ() + 0.5d + (normal.getZ() / 2.0d), soundEvent, SoundSource.BLOCKS, 0.5f, (this.level.random.nextFloat() * 0.1f) + 0.9f);
    }
}
