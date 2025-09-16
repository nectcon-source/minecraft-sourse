package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BaseContainerBlockEntity.class */
public abstract class BaseContainerBlockEntity extends BlockEntity implements Container, MenuProvider, Nameable {
    private LockCode lockKey;
    private Component name;

    protected abstract Component getDefaultName();

    protected abstract AbstractContainerMenu createMenu(int i, Inventory inventory);

    protected BaseContainerBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
        this.lockKey = LockCode.NO_LOCK;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.lockKey = LockCode.fromTag(compoundTag);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        this.lockKey.addToTag(compoundTag);
        if (this.name != null) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
        return compoundTag;
    }

    public void setCustomName(Component component) {
        this.name = component;
    }

    @Override // net.minecraft.world.Nameable
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return getDefaultName();
    }

    @Override // net.minecraft.world.MenuProvider
    public Component getDisplayName() {
        return getName();
    }

    @Override // net.minecraft.world.Nameable
    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    public boolean canOpen(Player player) {
        return canUnlock(player, this.lockKey, getDisplayName());
    }

    public static boolean canUnlock(Player player, LockCode lockCode, Component component) {
        if (player.isSpectator() || lockCode.unlocksWith(player.getMainHandItem())) {
            return true;
        }
        player.displayClientMessage(new TranslatableComponent("container.isLocked", component), true);
        player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0f, 1.0f);
        return false;
    }

    @Override // net.minecraft.world.inventory.MenuConstructor
    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (canOpen(player)) {
            return createMenu(i, inventory);
        }
        return null;
    }
}
