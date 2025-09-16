package net.minecraft.world.entity.vehicle;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/MinecartHopper.class */
public class MinecartHopper extends AbstractMinecartContainer implements Hopper {
    private boolean enabled;
    private int cooldownTime;
    private final BlockPos lastPosition;

    public MinecartHopper(EntityType<? extends MinecartHopper> entityType, Level level) {
        super(entityType, level);
        this.enabled = true;
        this.cooldownTime = -1;
        this.lastPosition = BlockPos.ZERO;
    }

    public MinecartHopper(Level level, double d, double d2, double d3) {
        super(EntityType.HOPPER_MINECART, d, d2, d3, level);
        this.enabled = true;
        this.cooldownTime = -1;
        this.lastPosition = BlockPos.ZERO;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.HOPPER;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.HOPPER.defaultBlockState();
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public int getDefaultDisplayOffset() {
        return 1;
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return 5;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public void activateMinecart(int i, int i2, int i3, boolean z) {
        boolean z2 = !z;
        if (z2 != isEnabled()) {
            setEnabled(z2);
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean z) {
        this.enabled = z;
    }

    @Override // net.minecraft.world.level.block.entity.Hopper
    public Level getLevel() {
        return this.level;
    }

    @Override // net.minecraft.world.level.block.entity.Hopper
    public double getLevelX() {
        return getX();
    }

    @Override // net.minecraft.world.level.block.entity.Hopper
    public double getLevelY() {
        return getY() + 0.5d;
    }

    @Override // net.minecraft.world.level.block.entity.Hopper
    public double getLevelZ() {
        return getZ();
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (!this.level.isClientSide && isAlive() && isEnabled()) {
            if (blockPosition().equals(this.lastPosition)) {
                this.cooldownTime--;
            } else {
                setCooldown(0);
            }
            if (!isOnCooldown()) {
                setCooldown(0);
                if (suckInItems()) {
                    setCooldown(4);
                    setChanged();
                }
            }
        }
    }

    public boolean suckInItems() {
        if (HopperBlockEntity.suckInItems(this)) {
            return true;
        }
        List<ItemEntity> entitiesOfClass = this.level.getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(0.25d, 0.0d, 0.25d), EntitySelector.ENTITY_STILL_ALIVE);
        if (!entitiesOfClass.isEmpty()) {
            HopperBlockEntity.addItem(this, entitiesOfClass.get(0));
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecartContainer, net.minecraft.world.entity.vehicle.AbstractMinecart
    public void destroy(DamageSource damageSource) {
        super.destroy(damageSource);
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            spawnAtLocation(Blocks.HOPPER);
        }
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecartContainer, net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("TransferCooldown", this.cooldownTime);
        compoundTag.putBoolean("Enabled", this.enabled);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecartContainer, net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.cooldownTime = compoundTag.getInt("TransferCooldown");
        this.enabled = compoundTag.contains("Enabled") ? compoundTag.getBoolean("Enabled") : true;
    }

    public void setCooldown(int i) {
        this.cooldownTime = i;
    }

    public boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecartContainer
    public AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new HopperMenu(i, inventory, this);
    }
}
