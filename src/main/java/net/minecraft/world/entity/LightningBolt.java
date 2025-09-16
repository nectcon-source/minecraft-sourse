package net.minecraft.world.entity;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/LightningBolt.class */
public class LightningBolt extends Entity {
    private int life;
    public long seed;
    private int flashes;
    private boolean visualOnly;

    @Nullable
    private ServerPlayer cause;

    public LightningBolt(EntityType<? extends LightningBolt> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.life = 2;
        this.seed = this.random.nextLong();
        this.flashes = this.random.nextInt(3) + 1;
    }

    public void setVisualOnly(boolean z) {
        this.visualOnly = z;
    }

    @Override // net.minecraft.world.entity.Entity
    public SoundSource getSoundSource() {
        return SoundSource.WEATHER;
    }

    public void setCause(@Nullable ServerPlayer serverPlayer) {
        this.cause = serverPlayer;
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick(){
        super.tick();
        if (this.life == 2) {
            Difficulty difficulty = this.level.getDifficulty();
            if (difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD) {
                spawnFire(4);
            }
            this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 10000.0f, 0.8f + (this.random.nextFloat() * 0.2f));
            this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 2.0f, 0.5f + (this.random.nextFloat() * 0.2f));
        }
        this.life--;
        if (this.life < 0) {
            if (this.flashes == 0) {
                remove();
            } else if (this.life < (-this.random.nextInt(10))) {
                this.flashes--;
                this.life = 1;
                this.seed = this.random.nextLong();
                spawnFire(0);
            }
        }
        if (this.life >= 0) {
            if (!(this.level instanceof ServerLevel)) {
                this.level.setSkyFlashTime(2);
                return;
            }
            if (!this.visualOnly) {
                List<Entity> entities = this.level.getEntities(this, new AABB(getX() - 3.0d, getY() - 3.0d, getZ() - 3.0d, getX() + 3.0d, getY() + 6.0d + 3.0d, getZ() + 3.0d), (v0) -> {
                    return v0.isAlive();
                });
                Iterator<Entity> it = entities.iterator();
                while (it.hasNext()) {
                    it.next().thunderHit((ServerLevel) this.level, this);
                }
                if (this.cause != null) {
                    CriteriaTriggers.CHANNELED_LIGHTNING.trigger(this.cause, entities);
                }
            }
        }
    }

    private void spawnFire(int i) {
        if (this.visualOnly || this.level.isClientSide || !this.level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }
        BlockPos blockPosition = blockPosition();
        BlockState state = BaseFireBlock.getState(this.level, blockPosition);
        if (this.level.getBlockState(blockPosition).isAir() && state.canSurvive(this.level, blockPosition)) {
            this.level.setBlockAndUpdate(blockPosition, state);
        }
        for (int i2 = 0; i2 < i; i2++) {
            BlockPos offset = blockPosition.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
            BlockState state2 = BaseFireBlock.getState(this.level, offset);
            if (this.level.getBlockState(offset).isAir() && state2.canSurvive(this.level, offset)) {
                this.level.setBlockAndUpdate(offset, state2);
            }
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        double viewScale = 64.0d * getViewScale();
        return d < viewScale * viewScale;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
    }

    @Override // net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override // net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
