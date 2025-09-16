package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/MinecartSpawner.class */
public class MinecartSpawner extends AbstractMinecart {
    private final BaseSpawner spawner;

    public MinecartSpawner(EntityType<? extends MinecartSpawner> entityType, Level level) {
        super(entityType, level);
        this.spawner = new BaseSpawner() { // from class: net.minecraft.world.entity.vehicle.MinecartSpawner.1
            @Override // net.minecraft.world.level.BaseSpawner
            public void broadcastEvent(int i) {
                MinecartSpawner.this.level.broadcastEntityEvent(MinecartSpawner.this, (byte) i);
            }

            @Override // net.minecraft.world.level.BaseSpawner
            public Level getLevel() {
                return MinecartSpawner.this.level;
            }

            @Override // net.minecraft.world.level.BaseSpawner
            public BlockPos getPos() {
                return MinecartSpawner.this.blockPosition();
            }
        };
    }

    public MinecartSpawner(Level level, double d, double d2, double d3) {
        super(EntityType.SPAWNER_MINECART, level, d, d2, d3);
        this.spawner = new BaseSpawner() { // from class: net.minecraft.world.entity.vehicle.MinecartSpawner.1
            @Override // net.minecraft.world.level.BaseSpawner
            public void broadcastEvent(int i) {
                MinecartSpawner.this.level.broadcastEntityEvent(MinecartSpawner.this, (byte) i);
            }

            @Override // net.minecraft.world.level.BaseSpawner
            public Level getLevel() {
                return MinecartSpawner.this.level;
            }

            @Override // net.minecraft.world.level.BaseSpawner
            public BlockPos getPos() {
                return MinecartSpawner.this.blockPosition();
            }
        };
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.SPAWNER;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.SPAWNER.defaultBlockState();
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.spawner.load(compoundTag);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.spawner.save(compoundTag);
    }

    @Override // net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        this.spawner.onEventTriggered(b);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        this.spawner.tick();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean onlyOpCanSetNbt() {
        return true;
    }
}
