package net.minecraft.world.level.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableInt;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BellBlockEntity.class */
public class BellBlockEntity extends BlockEntity implements TickableBlockEntity {
    private long lastRingTimestamp;
    public int ticks;
    public boolean shaking;
    public Direction clickDirection;
    private List<LivingEntity> nearbyEntities;
    private boolean resonating;
    private int resonationTicks;

    public BellBlockEntity() {
        super(BlockEntityType.BELL);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public boolean triggerEvent(int i, int i2) {
        if (i == 1) {
            updateEntities();
            this.resonationTicks = 0;
            this.clickDirection = Direction.from3DDataValue(i2);
            this.ticks = 0;
            this.shaking = true;
            return true;
        }
        return super.triggerEvent(i, i2);
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        if (this.shaking) {
            this.ticks++;
        }
        if (this.ticks >= 50) {
            this.shaking = false;
            this.ticks = 0;
        }
        if (this.ticks >= 5 && this.resonationTicks == 0 && areRaidersNearby()) {
            this.resonating = true;
            playResonateSound();
        }
        if (this.resonating) {
            if (this.resonationTicks < 40) {
                this.resonationTicks++;
                return;
            }
            makeRaidersGlow(this.level);
            showBellParticles(this.level);
            this.resonating = false;
        }
    }

    private void playResonateSound() {
        this.level.playSound((Player) null, getBlockPos(), SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    public void onHit(Direction direction) {
        BlockPos blockPos = getBlockPos();
        this.clickDirection = direction;
        if (this.shaking) {
            this.ticks = 0;
        } else {
            this.shaking = true;
        }
        this.level.blockEvent(blockPos, getBlockState().getBlock(), 1, direction.get3DDataValue());
    }

    private void updateEntities() {
        BlockPos blockPos = getBlockPos();
        if (this.level.getGameTime() > this.lastRingTimestamp + 60 || this.nearbyEntities == null) {
            this.lastRingTimestamp = this.level.getGameTime();
            this.nearbyEntities = this.level.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos).inflate(48.0d));
        }
        if (!this.level.isClientSide) {
            for (LivingEntity livingEntity : this.nearbyEntities) {
                if (livingEntity.isAlive() && !livingEntity.removed && blockPos.closerThan(livingEntity.position(), 32.0d)) {
                    livingEntity.getBrain().setMemory(MemoryModuleType.HEARD_BELL_TIME,  Long.valueOf(this.level.getGameTime()));
                }
            }
        }
    }

    private boolean areRaidersNearby() {
        BlockPos blockPos = getBlockPos();
        for (LivingEntity livingEntity : this.nearbyEntities) {
            if (livingEntity.isAlive() && !livingEntity.removed && blockPos.closerThan(livingEntity.position(), 32.0d) && livingEntity.getType().is(EntityTypeTags.RAIDERS)) {
                return true;
            }
        }
        return false;
    }

    private void makeRaidersGlow(Level level) {
        if (level.isClientSide) {
            return;
        }
        this.nearbyEntities.stream().filter(this::isRaiderWithinRange).forEach(this::glow);
    }

    private void showBellParticles(Level level) {
        if (!level.isClientSide) {
            return;
        }
        BlockPos blockPos = getBlockPos();
        MutableInt mutableInt = new MutableInt(16700985);
        int count = (int) this.nearbyEntities.stream().filter(livingEntity -> {
            return blockPos.closerThan(livingEntity.position(), 48.0d);
        }).count();
        this.nearbyEntities.stream().filter(this::isRaiderWithinRange).forEach(livingEntity2 -> {
            float sqrt = Mth.sqrt(((livingEntity2.getX() - blockPos.getX()) * (livingEntity2.getX() - blockPos.getX())) + ((livingEntity2.getZ() - blockPos.getZ()) * (livingEntity2.getZ() - blockPos.getZ())));
            double x = blockPos.getX() + 0.5f + ((1.0f / sqrt) * (livingEntity2.getX() - blockPos.getX()));
            double z = blockPos.getZ() + 0.5f + ((1.0f / sqrt) * (livingEntity2.getZ() - blockPos.getZ()));
            int clamp = Mth.clamp((count - 21) / (-2), 3, 15);
            for (int i = 0; i < clamp; i++) {
                int addAndGet = mutableInt.addAndGet(5);
                level.addParticle(ParticleTypes.ENTITY_EFFECT, x, blockPos.getY() + 0.5f, z, FastColor.ARGB32.red(addAndGet) / 255.0d, FastColor.ARGB32.green(addAndGet) / 255.0d, FastColor.ARGB32.blue(addAndGet) / 255.0d);
            }
        });
    }

    private boolean isRaiderWithinRange(LivingEntity livingEntity) {
        return livingEntity.isAlive() && !livingEntity.removed && getBlockPos().closerThan(livingEntity.position(), 48.0d) && livingEntity.getType().is(EntityTypeTags.RAIDERS);
    }

    private void glow(LivingEntity livingEntity) {
        livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
    }
}
