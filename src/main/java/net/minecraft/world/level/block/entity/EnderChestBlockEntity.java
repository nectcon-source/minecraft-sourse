package net.minecraft.world.level.block.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/EnderChestBlockEntity.class */
public class EnderChestBlockEntity extends BlockEntity implements LidBlockEntity, TickableBlockEntity {
    public float openness;
    public float oOpenness;
    public int openCount;
    private int tickInterval;

    public EnderChestBlockEntity() {
        super(BlockEntityType.ENDER_CHEST);
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        int i = this.tickInterval + 1;
        this.tickInterval = i;
        if ((i % 20) * 4 == 0) {
            this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
        }
        this.oOpenness = this.openness;
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        if (this.openCount > 0 && this.openness == 0.0f) {
            this.level.playSound(null, x + 0.5d, y + 0.5d, z + 0.5d, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5f, (this.level.random.nextFloat() * 0.1f) + 0.9f);
        }
        if ((this.openCount == 0 && this.openness > 0.0f) || (this.openCount > 0 && this.openness < 1.0f)) {
            float f = this.openness;
            if (this.openCount > 0) {
                this.openness += 0.1f;
            } else {
                this.openness -= 0.1f;
            }
            if (this.openness > 1.0f) {
                this.openness = 1.0f;
            }
            if (this.openness < 0.5f && f >= 0.5f) {
                this.level.playSound(null, x + 0.5d, y + 0.5d, z + 0.5d, SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, (this.level.random.nextFloat() * 0.1f) + 0.9f);
            }
            if (this.openness < 0.0f) {
                this.openness = 0.0f;
            }
        }
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public boolean triggerEvent(int i, int i2) {
        if (i == 1) {
            this.openCount = i2;
            return true;
        }
        return super.triggerEvent(i, i2);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void setRemoved() {
        clearCache();
        super.setRemoved();
    }

    public void startOpen() {
        this.openCount++;
        this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
    }

    public void stopOpen() {
        this.openCount--;
        this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
    }

    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this || player.distanceToSqr(this.worldPosition.getX() + 0.5d, this.worldPosition.getY() + 0.5d, this.worldPosition.getZ() + 0.5d) > 64.0d) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.level.block.entity.LidBlockEntity
    public float getOpenNess(float f) {
        return Mth.lerp(f, this.oOpenness, this.openness);
    }
}
