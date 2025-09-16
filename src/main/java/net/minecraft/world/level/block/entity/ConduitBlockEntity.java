package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/ConduitBlockEntity.class */
public class ConduitBlockEntity extends BlockEntity implements TickableBlockEntity {
    private static final Block[] VALID_BLOCKS = {Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
    public int tickCount;
    private float activeRotation;
    private boolean isActive;
    private boolean isHunting;
    private final List<BlockPos> effectBlocks;

    @Nullable
    private LivingEntity destroyTarget;

    @Nullable
    private UUID destroyTargetUUID;
    private long nextAmbientSoundActivation;

    public ConduitBlockEntity() {
        this(BlockEntityType.CONDUIT);
    }

    public ConduitBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
        this.effectBlocks = Lists.newArrayList();
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        if (compoundTag.hasUUID("Target")) {
            this.destroyTargetUUID = compoundTag.getUUID("Target");
        } else {
            this.destroyTargetUUID = null;
        }
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (this.destroyTarget != null) {
            compoundTag.putUUID("Target", this.destroyTarget.getUUID());
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 5, getUpdateTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        this.tickCount++;
        long gameTime = this.level.getGameTime();
        if (gameTime % 40 == 0) {
            setActive(updateShape());
            if (!this.level.isClientSide && isActive()) {
                applyEffects();
                updateDestroyTarget();
            }
        }
        if (gameTime % 80 == 0 && isActive()) {
            playSound(SoundEvents.CONDUIT_AMBIENT);
        }
        if (gameTime > this.nextAmbientSoundActivation && isActive()) {
            this.nextAmbientSoundActivation = gameTime + 60 + this.level.getRandom().nextInt(40);
            playSound(SoundEvents.CONDUIT_AMBIENT_SHORT);
        }
        if (this.level.isClientSide) {
            updateClientTarget();
            animationTick();
            if (isActive()) {
                this.activeRotation += 1.0f;
            }
        }
    }

    private boolean updateShape() {
        this.effectBlocks.clear();
        for (int i = -1; i <= 1; i++) {
            for (int i2 = -1; i2 <= 1; i2++) {
                for (int i3 = -1; i3 <= 1; i3++) {
                    if (!this.level.isWaterAt(this.worldPosition.offset(i, i2, i3))) {
                        return false;
                    }
                }
            }
        }
        for (int i4 = -2; i4 <= 2; i4++) {
            for (int i5 = -2; i5 <= 2; i5++) {
                for (int i6 = -2; i6 <= 2; i6++) {
                    int abs = Math.abs(i4);
                    int abs2 = Math.abs(i5);
                    int abs3 = Math.abs(i6);
                    if ((abs > 1 || abs2 > 1 || abs3 > 1) && ((i4 == 0 && (abs2 == 2 || abs3 == 2)) || ((i5 == 0 && (abs == 2 || abs3 == 2)) || (i6 == 0 && (abs == 2 || abs2 == 2))))) {
                        BlockPos offset = this.worldPosition.offset(i4, i5, i6);
                        BlockState blockState = this.level.getBlockState(offset);
                        for (Block block : VALID_BLOCKS) {
                            if (blockState.is(block)) {
                                this.effectBlocks.add(offset);
                            }
                        }
                    }
                }
            }
        }
        setHunting(this.effectBlocks.size() >= 42);
        return this.effectBlocks.size() >= 16;
    }

    private void applyEffects() {
        int var1 = this.effectBlocks.size();
        int var2 = var1 / 7 * 16;
        int var3 = this.worldPosition.getX();
        int var4 = this.worldPosition.getY();
        int var5 = this.worldPosition.getZ();
        AABB var6 = (new AABB((double)var3, (double)var4, (double)var5, (double)(var3 + 1), (double)(var4 + 1), (double)(var5 + 1))).inflate((double)var2).expandTowards((double)0.0F, (double)this.level.getMaxBuildHeight(), (double)0.0F);
        List<Player> var7 = this.level.getEntitiesOfClass(Player.class, var6);
        if (!var7.isEmpty()) {
            for(Player var9 : var7) {
                if (this.worldPosition.closerThan(var9.blockPosition(), (double)var2) && var9.isInWaterOrRain()) {
                    var9.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true));
                }
            }

        }
    }

    private void updateDestroyTarget() {
        LivingEntity livingEntity = this.destroyTarget;
        if (this.effectBlocks.size() < 42) {
            this.destroyTarget = null;
        } else if (this.destroyTarget == null && this.destroyTargetUUID != null) {
            this.destroyTarget = findDestroyTarget();
            this.destroyTargetUUID = null;
        } else if (this.destroyTarget == null) {
            List<LivingEntity> entitiesOfClass = this.level.getEntitiesOfClass(LivingEntity.class, getDestroyRangeAABB(), livingEntity2 -> {
                return (livingEntity2 instanceof Enemy) && livingEntity2.isInWaterOrRain();
            });
            if (!entitiesOfClass.isEmpty()) {
                this.destroyTarget = entitiesOfClass.get(this.level.random.nextInt(entitiesOfClass.size()));
            }
        } else if (!this.destroyTarget.isAlive() || !this.worldPosition.closerThan(this.destroyTarget.blockPosition(), 8.0d)) {
            this.destroyTarget = null;
        }
        if (this.destroyTarget != null) {
            this.level.playSound(null, this.destroyTarget.getX(), this.destroyTarget.getY(), this.destroyTarget.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.BLOCKS, 1.0f, 1.0f);
            this.destroyTarget.hurt(DamageSource.MAGIC, 4.0f);
        }
        if (livingEntity != this.destroyTarget) {
            BlockState blockState = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, 2);
        }
    }

    private void updateClientTarget() {
        if (this.destroyTargetUUID == null) {
            this.destroyTarget = null;
            return;
        }
        if (this.destroyTarget == null || !this.destroyTarget.getUUID().equals(this.destroyTargetUUID)) {
            this.destroyTarget = findDestroyTarget();
            if (this.destroyTarget == null) {
                this.destroyTargetUUID = null;
            }
        }
    }

    private AABB getDestroyRangeAABB() {
        int var1 = this.worldPosition.getX();
        int var2 = this.worldPosition.getY();
        int var3 = this.worldPosition.getZ();
        return (new AABB((double)var1, (double)var2, (double)var3, (double)(var1 + 1), (double)(var2 + 1), (double)(var3 + 1))).inflate((double)8.0F);
    }

    @Nullable
    private LivingEntity findDestroyTarget() {
        List<LivingEntity> entitiesOfClass = this.level.getEntitiesOfClass(LivingEntity.class, getDestroyRangeAABB(), livingEntity -> {
            return livingEntity.getUUID().equals(this.destroyTargetUUID);
        });
        if (entitiesOfClass.size() == 1) {
            return entitiesOfClass.get(0);
        }
        return null;
    }

    private void animationTick() {
        Random random = this.level.random;
        double sin = (Mth.sin((this.tickCount + 35) * 0.1f) / 2.0f) + 0.5f;
        Vec3 vec3 = new Vec3(this.worldPosition.getX() + 0.5d, this.worldPosition.getY() + 1.5d + (((sin * sin) + sin) * 0.30000001192092896d), this.worldPosition.getZ() + 0.5d);
        for (BlockPos blockPos : this.effectBlocks) {
            if (random.nextInt(50) == 0) {
                float nextFloat = (-0.5f) + random.nextFloat();
                float nextFloat2 = (-2.0f) + random.nextFloat();
                float nextFloat3 = (-0.5f) + random.nextFloat();
                BlockPos subtract = blockPos.subtract(this.worldPosition);
                Vec3 add = new Vec3(nextFloat, nextFloat2, nextFloat3).add(subtract.getX(), subtract.getY(), subtract.getZ());
                this.level.addParticle(ParticleTypes.NAUTILUS, vec3.x, vec3.y, vec3.z, add.x, add.y, add.z);
            }
        }
        if (this.destroyTarget != null) {
            Vec3 vec32 = new Vec3(this.destroyTarget.getX(), this.destroyTarget.getEyeY(), this.destroyTarget.getZ());
            Vec3 vec33 = new Vec3(((-0.5f) + random.nextFloat()) * (3.0f + this.destroyTarget.getBbWidth()), (-1.0f) + (random.nextFloat() * this.destroyTarget.getBbHeight()), ((-0.5f) + random.nextFloat()) * (3.0f + this.destroyTarget.getBbWidth()));
            this.level.addParticle(ParticleTypes.NAUTILUS, vec32.x, vec32.y, vec32.z, vec33.x, vec33.y, vec33.z);
        }
    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean isHunting() {
        return this.isHunting;
    }

    private void setActive(boolean z) {
        if (z != this.isActive) {
            playSound(z ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE);
        }
        this.isActive = z;
    }

    private void setHunting(boolean z) {
        this.isHunting = z;
    }

    public float getActiveRotation(float f) {
        return (this.activeRotation + f) * (-0.0375f);
    }

    public void playSound(SoundEvent soundEvent) {
        this.level.playSound((Player) null, this.worldPosition, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}
