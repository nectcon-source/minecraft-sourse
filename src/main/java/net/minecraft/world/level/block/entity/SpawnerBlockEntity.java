package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/SpawnerBlockEntity.class */
public class SpawnerBlockEntity extends BlockEntity implements TickableBlockEntity {
    private final BaseSpawner spawner;

    public SpawnerBlockEntity() {
        super(BlockEntityType.MOB_SPAWNER);
        this.spawner = new BaseSpawner() { // from class: net.minecraft.world.level.block.entity.SpawnerBlockEntity.1
            @Override // net.minecraft.world.level.BaseSpawner
            public void broadcastEvent(int i) {
                SpawnerBlockEntity.this.level.blockEvent(SpawnerBlockEntity.this.worldPosition, Blocks.SPAWNER, i, 0);
            }

            @Override // net.minecraft.world.level.BaseSpawner
            public Level getLevel() {
                return SpawnerBlockEntity.this.level;
            }

            @Override // net.minecraft.world.level.BaseSpawner
            public BlockPos getPos() {
                return SpawnerBlockEntity.this.worldPosition;
            }

            @Override // net.minecraft.world.level.BaseSpawner
            public void setNextSpawnData(SpawnData spawnData) {
                super.setNextSpawnData(spawnData);
                if (getLevel() != null) {
                    BlockState blockState = getLevel().getBlockState(getPos());
                    getLevel().sendBlockUpdated(SpawnerBlockEntity.this.worldPosition, blockState, blockState, 4);
                }
            }
        };
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.spawner.load(compoundTag);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        this.spawner.save(compoundTag);
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        this.spawner.tick();
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 1, getUpdateTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        CompoundTag save = save(new CompoundTag());
        save.remove("SpawnPotentials");
        return save;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public boolean triggerEvent(int i, int i2) {
        if (this.spawner.onEventTriggered(i)) {
            return true;
        }
        return super.triggerEvent(i, i2);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public BaseSpawner getSpawner() {
        return this.spawner;
    }
}
