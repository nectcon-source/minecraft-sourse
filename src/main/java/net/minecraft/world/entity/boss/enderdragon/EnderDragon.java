package net.minecraft.world.entity.boss.enderdragon;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/EnderDragon.class */
public class EnderDragon extends Mob implements Enemy {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(EnderDragon.class, EntityDataSerializers.INT);
    private static final TargetingConditions CRYSTAL_DESTROY_TARGETING = new TargetingConditions().range(64.0d);
    public final double[][] positions;
    public int posPointer;
    private final EnderDragonPart[] subEntities;
    public final EnderDragonPart head;
    private final EnderDragonPart neck;
    private final EnderDragonPart body;
    private final EnderDragonPart tail1;
    private final EnderDragonPart tail2;
    private final EnderDragonPart tail3;
    private final EnderDragonPart wing1;
    private final EnderDragonPart wing2;
    public float oFlapTime;
    public float flapTime;
    public boolean inWall;
    public int dragonDeathTime;
    public float yRotA;

    @Nullable
    public EndCrystal nearestCrystal;

    @Nullable
    private final EndDragonFight dragonFight;
    private final EnderDragonPhaseManager phaseManager;
    private int growlTime;
    private int sittingDamageReceived;
    private final Node[] nodes;
    private final int[] nodeAdjacency;
    private final BinaryHeap openSet;

    public EnderDragon(EntityType<? extends EnderDragon> entityType, Level level) {
        super(EntityType.ENDER_DRAGON, level);
        this.positions = new double[64][3];
        this.posPointer = -1;
        this.growlTime = 100;
        this.nodes = new Node[24];
        this.nodeAdjacency = new int[24];
        this.openSet = new BinaryHeap();
        this.head = new EnderDragonPart(this, "head", 1.0f, 1.0f);
        this.neck = new EnderDragonPart(this, "neck", 3.0f, 3.0f);
        this.body = new EnderDragonPart(this, "body", 5.0f, 3.0f);
        this.tail1 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.tail2 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.tail3 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.wing1 = new EnderDragonPart(this, "wing", 4.0f, 2.0f);
        this.wing2 = new EnderDragonPart(this, "wing", 4.0f, 2.0f);
        this.subEntities = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};
        setHealth(getMaxHealth());
        this.noPhysics = true;
        this.noCulling = true;
        if (level instanceof ServerLevel) {
            this.dragonFight = ((ServerLevel) level).dragonFight();
        } else {
            this.dragonFight = null;
        }
        this.phaseManager = new EnderDragonPhaseManager(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0d);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(DATA_PHASE, Integer.valueOf(EnderDragonPhase.HOVERING.getId()));
    }

    public double[] getLatencyPos(int i, float f) {
        if (isDeadOrDying()) {
            f = 0.0f;
        }
        float f2 = 1.0f - f;
        int i2 = (this.posPointer - i) & 63;
        int i3 = ((this.posPointer - i) - 1) & 63;
        double d = this.positions[i2][0];
        double d2 = this.positions[i2][1];
        return new double[]{d + (Mth.wrapDegrees(this.positions[i3][0] - d) * f2), d2 + ((this.positions[i3][1] - d2) * f2), Mth.lerp(f2, this.positions[i2][2], this.positions[i3][2])};
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (this.level.isClientSide) {
            setHealth(getHealth());
            if (!isSilent()) {
                float cos = Mth.cos(this.flapTime * 6.2831855f);
                if (Mth.cos(this.oFlapTime * 6.2831855f) <= -0.3f && cos >= -0.3f) {
                    this.level.playLocalSound(getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, getSoundSource(), 5.0f, 0.8f + (this.random.nextFloat() * 0.3f), false);
                }
                if (!this.phaseManager.getCurrentPhase().isSitting()) {
                    int i = this.growlTime - 1;
                    this.growlTime = i;
                    if (i < 0) {
                        this.level.playLocalSound(getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_GROWL, getSoundSource(), 2.5f, 0.8f + (this.random.nextFloat() * 0.3f), false);
                        this.growlTime = 200 + this.random.nextInt(200);
                    }
                }
            }
        }
        this.oFlapTime = this.flapTime;
        if (isDeadOrDying()) {
            this.level.addParticle(ParticleTypes.EXPLOSION, getX() + ((this.random.nextFloat() - 0.5f) * 8.0f), getY() + 2.0d + ((this.random.nextFloat() - 0.5f) * 4.0f), getZ() + ((this.random.nextFloat() - 0.5f) * 8.0f), 0.0d, 0.0d, 0.0d);
            return;
        }
        checkCrystals();
        Vec3 deltaMovement = getDeltaMovement();
        float sqrt = (0.2f / ((Mth.sqrt(getHorizontalDistanceSqr(deltaMovement)) * 10.0f) + 1.0f)) * ((float) Math.pow(2.0d, deltaMovement.y));
        if (this.phaseManager.getCurrentPhase().isSitting()) {
            this.flapTime += 0.1f;
        } else if (this.inWall) {
            this.flapTime += sqrt * 0.5f;
        } else {
            this.flapTime += sqrt;
        }
        this.yRot = Mth.wrapDegrees(this.yRot);
        if (isNoAi()) {
            this.flapTime = 0.5f;
            return;
        }
        if (this.posPointer < 0) {
            for (int i2 = 0; i2 < this.positions.length; i2++) {
                this.positions[i2][0] = this.yRot;
                this.positions[i2][1] = getY();
            }
        }
        int i3 = this.posPointer + 1;
        this.posPointer = i3;
        if (i3 == this.positions.length) {
            this.posPointer = 0;
        }
        this.positions[this.posPointer][0] = this.yRot;
        this.positions[this.posPointer][1] = getY();
        if (this.level.isClientSide) {
            if (this.lerpSteps > 0) {
                double x = getX() + ((this.lerpX - getX()) / this.lerpSteps);
                double y = getY() + ((this.lerpY - getY()) / this.lerpSteps);
                double z = getZ() + ((this.lerpZ - getZ()) / this.lerpSteps);
                this.yRot = (float) (this.yRot + (Mth.wrapDegrees(this.lerpYRot - this.yRot) / this.lerpSteps));
                this.xRot = (float) (this.xRot + ((this.lerpXRot - this.xRot) / this.lerpSteps));
                this.lerpSteps--;
                setPos(x, y, z);
                setRot(this.yRot, this.xRot);
            }
            this.phaseManager.getCurrentPhase().doClientTick();
        } else {
            DragonPhaseInstance currentPhase = this.phaseManager.getCurrentPhase();
            currentPhase.doServerTick();
            if (this.phaseManager.getCurrentPhase() != currentPhase) {
                currentPhase = this.phaseManager.getCurrentPhase();
                currentPhase.doServerTick();
            }
            Vec3 flyTargetLocation = currentPhase.getFlyTargetLocation();
            if (flyTargetLocation != null) {
                double x2 = flyTargetLocation.x - getX();
                double y2 = flyTargetLocation.y - getY();
                double z2 = flyTargetLocation.z - getZ();
                double d = (x2 * x2) + (y2 * y2) + (z2 * z2);
                float flySpeed = currentPhase.getFlySpeed();
                double sqrt2 = Mth.sqrt((x2 * x2) + (z2 * z2));
                if (sqrt2 > 0.0d) {
                    y2 = Mth.clamp(y2 / sqrt2, -flySpeed, flySpeed);
                }
                setDeltaMovement(getDeltaMovement().add(0.0d, y2 * 0.01d, 0.0d));
                this.yRot = Mth.wrapDegrees(this.yRot);
                double clamp = Mth.clamp(Mth.wrapDegrees((180.0d - (Mth.atan2(x2, z2) * 57.2957763671875d)) - this.yRot), -50.0d, 50.0d);
                Vec3 normalize = flyTargetLocation.subtract(getX(), getY(), getZ()).normalize();
                Vec3 normalize2 = new Vec3(Mth.sin(this.yRot * 0.017453292f), getDeltaMovement().y, -Mth.cos(this.yRot * 0.017453292f)).normalize();
                float max = Math.max((((float) normalize2.dot(normalize)) + 0.5f) / 1.5f, 0.0f);
                this.yRotA *= 0.8f;
                this.yRotA = (float) (this.yRotA + (clamp * currentPhase.getTurnSpeed()));
                this.yRot += this.yRotA * 0.1f;
                float f = (float) (2.0d / (d + 1.0d));
                moveRelative(0.06f * ((max * f) + (1.0f - f)), new Vec3(0.0d, 0.0d, -1.0d));
                if (this.inWall) {
                    move(MoverType.SELF, getDeltaMovement().scale(0.800000011920929d));
                } else {
                    move(MoverType.SELF, getDeltaMovement());
                }
                double dot = 0.8d + ((0.15d * (getDeltaMovement().normalize().dot(normalize2) + 1.0d)) / 2.0d);
                setDeltaMovement(getDeltaMovement().multiply(dot, 0.9100000262260437d, dot));
            }
        }
        this.yBodyRot = this.yRot;
        Vec3[] vec3Arr = new Vec3[this.subEntities.length];
        for (int i4 = 0; i4 < this.subEntities.length; i4++) {
            vec3Arr[i4] = new Vec3(this.subEntities[i4].getX(), this.subEntities[i4].getY(), this.subEntities[i4].getZ());
        }
        float f2 = ((float) (getLatencyPos(5, 1.0f)[1] - getLatencyPos(10, 1.0f)[1])) * 10.0f * 0.017453292f;
        float cos2 = Mth.cos(f2);
        float sin = Mth.sin(f2);
        float f3 = this.yRot * 0.017453292f;
        float sin2 = Mth.sin(f3);
        float cos3 = Mth.cos(f3);
        tickPart(this.body, sin2 * 0.5f, 0.0d, (-cos3) * 0.5f);
        tickPart(this.wing1, cos3 * 4.5f, 2.0d, sin2 * 4.5f);
        tickPart(this.wing2, cos3 * (-4.5f), 2.0d, sin2 * (-4.5f));
        if (!this.level.isClientSide && this.hurtTime == 0) {
            knockBack(this.level.getEntities(this, this.wing1.getBoundingBox().inflate(4.0d, 2.0d, 4.0d).move(0.0d, -2.0d, 0.0d), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
            knockBack(this.level.getEntities(this, this.wing2.getBoundingBox().inflate(4.0d, 2.0d, 4.0d).move(0.0d, -2.0d, 0.0d), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
            hurt(this.level.getEntities(this, this.head.getBoundingBox().inflate(1.0d), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
            hurt(this.level.getEntities(this, this.neck.getBoundingBox().inflate(1.0d), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        }
        float sin3 = Mth.sin((this.yRot * 0.017453292f) - (this.yRotA * 0.01f));
        float cos4 = Mth.cos((this.yRot * 0.017453292f) - (this.yRotA * 0.01f));
        float headYOffset = getHeadYOffset();
        tickPart(this.head, sin3 * 6.5f * cos2, headYOffset + (sin * 6.5f), (-cos4) * 6.5f * cos2);
        tickPart(this.neck, sin3 * 5.5f * cos2, headYOffset + (sin * 5.5f), (-cos4) * 5.5f * cos2);
        double[] latencyPos = getLatencyPos(5, 1.0f);
        for (int i5 = 0; i5 < 3; i5++) {
            EnderDragonPart enderDragonPart = null;
            if (i5 == 0) {
                enderDragonPart = this.tail1;
            }
            if (i5 == 1) {
                enderDragonPart = this.tail2;
            }
            if (i5 == 2) {
                enderDragonPart = this.tail3;
            }
            double[] latencyPos2 = getLatencyPos(12 + (i5 * 2), 1.0f);
            float rotWrap = (this.yRot * 0.017453292f) + (rotWrap(latencyPos2[0] - latencyPos[0]) * 0.017453292f);
            float sin4 = Mth.sin(rotWrap);
            float cos5 = Mth.cos(rotWrap);
            float f4 = (i5 + 1) * 2.0f;
            tickPart(enderDragonPart, (-((sin2 * 1.5f) + (sin4 * f4))) * cos2, ((latencyPos2[1] - latencyPos[1]) - ((f4 + 1.5f) * sin)) + 1.5d, ((cos3 * 1.5f) + (cos5 * f4)) * cos2);
        }
        if (!this.level.isClientSide) {
            this.inWall = checkWalls(this.head.getBoundingBox()) | checkWalls(this.neck.getBoundingBox()) | checkWalls(this.body.getBoundingBox());
            if (this.dragonFight != null) {
                this.dragonFight.updateDragon(this);
            }
        }
        for (int i6 = 0; i6 < this.subEntities.length; i6++) {
            this.subEntities[i6].xo = vec3Arr[i6].x;
            this.subEntities[i6].yo = vec3Arr[i6].y;
            this.subEntities[i6].zo = vec3Arr[i6].z;
            this.subEntities[i6].xOld = vec3Arr[i6].x;
            this.subEntities[i6].yOld = vec3Arr[i6].y;
            this.subEntities[i6].zOld = vec3Arr[i6].z;
        }
    }

    private void tickPart(EnderDragonPart enderDragonPart, double d, double d2, double d3) {
        enderDragonPart.setPos(getX() + d, getY() + d2, getZ() + d3);
    }

    private float getHeadYOffset() {
        if (this.phaseManager.getCurrentPhase().isSitting()) {
            return -1.0f;
        }
        return (float) (getLatencyPos(5, 1.0f)[1] - getLatencyPos(0, 1.0f)[1]);
    }

    private void checkCrystals() {
        if (this.nearestCrystal != null) {
            if (this.nearestCrystal.removed) {
                this.nearestCrystal = null;
            } else if (this.tickCount % 10 == 0 && getHealth() < getMaxHealth()) {
                setHealth(getHealth() + 1.0f);
            }
        }
        if (this.random.nextInt(10) == 0) {
            EndCrystal endCrystal = null;
            double d = Double.MAX_VALUE;
            for (EndCrystal endCrystal2 : this.level.getEntitiesOfClass(EndCrystal.class, getBoundingBox().inflate(32.0d))) {
                double distanceToSqr = endCrystal2.distanceToSqr(this);
                if (distanceToSqr < d) {
                    d = distanceToSqr;
                    endCrystal = endCrystal2;
                }
            }
            this.nearestCrystal = endCrystal;
        }
    }

    private void knockBack(List<Entity> list) {
        double d = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0d;
        double d2 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0d;
        for (Entity entity : list) {
            if (entity instanceof LivingEntity) {
                double x = entity.getX() - d;
                double z = entity.getZ() - d2;
                double max = Math.max((x * x) + (z * z), 0.1d);
                entity.push((x / max) * 4.0d, 0.20000000298023224d, (z / max) * 4.0d);
                if (!this.phaseManager.getCurrentPhase().isSitting() && ((LivingEntity) entity).getLastHurtByMobTimestamp() < entity.tickCount - 2) {
                    entity.hurt(DamageSource.mobAttack(this), 5.0f);
                    doEnchantDamageEffects(this, entity);
                }
            }
        }
    }

    private void hurt(List<Entity> list) {
        for (Entity entity : list) {
            if (entity instanceof LivingEntity) {
                entity.hurt(DamageSource.mobAttack(this), 10.0f);
                doEnchantDamageEffects(this, entity);
            }
        }
    }

    private float rotWrap(double d) {
        return (float) Mth.wrapDegrees(d);
    }

    private boolean checkWalls(AABB aabb) {
        int floor = Mth.floor(aabb.minX);
        int floor2 = Mth.floor(aabb.minY);
        int floor3 = Mth.floor(aabb.minZ);
        int floor4 = Mth.floor(aabb.maxX);
        int floor5 = Mth.floor(aabb.maxY);
        int floor6 = Mth.floor(aabb.maxZ);
        boolean z = false;
        boolean z2 = false;
        for (int i = floor; i <= floor4; i++) {
            for (int i2 = floor2; i2 <= floor5; i2++) {
                for (int i3 = floor3; i3 <= floor6; i3++) {
                    BlockPos blockPos = new BlockPos(i, i2, i3);
                    BlockState blockState = this.level.getBlockState(blockPos);
                    Block block = blockState.getBlock();
                    if (!blockState.isAir() && blockState.getMaterial() != Material.FIRE) {
                        if (!this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || BlockTags.DRAGON_IMMUNE.contains(block)) {
                            z = true;
                        } else {
                            z2 = this.level.removeBlock(blockPos, false) || z2;
                        }
                    }
                }
            }
        }
        if (z2) {
            this.level.levelEvent(2008, new BlockPos(floor + this.random.nextInt((floor4 - floor) + 1), floor2 + this.random.nextInt((floor5 - floor2) + 1), floor3 + this.random.nextInt((floor6 - floor3) + 1)), 0);
        }
        return z;
    }

    public boolean hurt(EnderDragonPart enderDragonPart, DamageSource damageSource, float f) {
        if (this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
            return false;
        }
        float onHurt = this.phaseManager.getCurrentPhase().onHurt(damageSource, f);
        if (enderDragonPart != this.head) {
            onHurt = (onHurt / 4.0f) + Math.min(onHurt, 1.0f);
        }
        if (onHurt < 0.01f) {
            return false;
        }
        if ((damageSource.getEntity() instanceof Player) || damageSource.isExplosion()) {
            float health = getHealth();
            reallyHurt(damageSource, onHurt);
            if (isDeadOrDying() && !this.phaseManager.getCurrentPhase().isSitting()) {
                setHealth(1.0f);
                this.phaseManager.setPhase(EnderDragonPhase.DYING);
            }
            if (this.phaseManager.getCurrentPhase().isSitting()) {
                this.sittingDamageReceived = (int) (this.sittingDamageReceived + (health - getHealth()));
                if (this.sittingDamageReceived > 0.25f * getMaxHealth()) {
                    this.sittingDamageReceived = 0;
                    this.phaseManager.setPhase(EnderDragonPhase.TAKEOFF);
                    return true;
                }
                return true;
            }
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if ((damageSource instanceof EntityDamageSource) && ((EntityDamageSource) damageSource).isThorns()) {
            hurt(this.body, damageSource, f);
            return false;
        }
        return false;
    }

    protected boolean reallyHurt(DamageSource damageSource, float f) {
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void kill() {
        remove();
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
            this.dragonFight.setDragonKilled(this);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void tickDeath() {
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
        }
        this.dragonDeathTime++;
        if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, getX() + ((this.random.nextFloat() - 0.5f) * 8.0f), getY() + 2.0d + ((this.random.nextFloat() - 0.5f) * 4.0f), getZ() + ((this.random.nextFloat() - 0.5f) * 8.0f), 0.0d, 0.0d, 0.0d);
        }
        boolean z = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
        int i = 500;
        if (this.dragonFight != null && !this.dragonFight.hasPreviouslyKilledDragon()) {
            i = 12000;
        }
        if (!this.level.isClientSide) {
            if (this.dragonDeathTime > 150 && this.dragonDeathTime % 5 == 0 && z) {
                dropExperience(Mth.floor(i * 0.08f));
            }
            if (this.dragonDeathTime == 1 && !isSilent()) {
                this.level.globalLevelEvent(1028, blockPosition(), 0);
            }
        }
        move(MoverType.SELF, new Vec3(0.0d, 0.10000000149011612d, 0.0d));
        this.yRot += 20.0f;
        this.yBodyRot = this.yRot;
        if (this.dragonDeathTime == 200 && !this.level.isClientSide) {
            if (z) {
                dropExperience(Mth.floor(i * 0.2f));
            }
            if (this.dragonFight != null) {
                this.dragonFight.setDragonKilled(this);
            }
            remove();
        }
    }

    private void dropExperience(int i) {
        while (i > 0) {
            int experienceValue = ExperienceOrb.getExperienceValue(i);
            i -= experienceValue;
            this.level.addFreshEntity(new ExperienceOrb(this.level, getX(), getY(), getZ(), experienceValue));
        }
    }

    public int findClosestNode() {
        int floor;
        int floor2;
        if (this.nodes[0] == null) {
            for (int i = 0; i < 24; i++) {
                int i2 = 5;
                int i3 = i;
                if (i < 12) {
                    floor = Mth.floor(60.0f * Mth.cos(2.0f * ((-3.1415927f) + (0.2617994f * i3))));
                    floor2 = Mth.floor(60.0f * Mth.sin(2.0f * ((-3.1415927f) + (0.2617994f * i3))));
                } else if (i < 20) {
                    int i4 = i3 - 12;
                    floor = Mth.floor(40.0f * Mth.cos(2.0f * ((-3.1415927f) + (0.3926991f * i4))));
                    floor2 = Mth.floor(40.0f * Mth.sin(2.0f * ((-3.1415927f) + (0.3926991f * i4))));
                    i2 = 5 + 10;
                } else {
                    int i5 = i3 - 20;
                    floor = Mth.floor(20.0f * Mth.cos(2.0f * ((-3.1415927f) + (0.7853982f * i5))));
                    floor2 = Mth.floor(20.0f * Mth.sin(2.0f * ((-3.1415927f) + (0.7853982f * i5))));
                }
                this.nodes[i] = new Node(floor, Math.max(this.level.getSeaLevel() + 10, this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(floor, 0, floor2)).getY() + i2), floor2);
            }
            this.nodeAdjacency[0] = 6146;
            this.nodeAdjacency[1] = 8197;
            this.nodeAdjacency[2] = 8202;
            this.nodeAdjacency[3] = 16404;
            this.nodeAdjacency[4] = 32808;
            this.nodeAdjacency[5] = 32848;
            this.nodeAdjacency[6] = 65696;
            this.nodeAdjacency[7] = 131392;
            this.nodeAdjacency[8] = 131712;
            this.nodeAdjacency[9] = 263424;
            this.nodeAdjacency[10] = 526848;
            this.nodeAdjacency[11] = 525313;
            this.nodeAdjacency[12] = 1581057;
            this.nodeAdjacency[13] = 3166214;
            this.nodeAdjacency[14] = 2138120;
            this.nodeAdjacency[15] = 6373424;
            this.nodeAdjacency[16] = 4358208;
            this.nodeAdjacency[17] = 12910976;
            this.nodeAdjacency[18] = 9044480;
            this.nodeAdjacency[19] = 9706496;
            this.nodeAdjacency[20] = 15216640;
            this.nodeAdjacency[21] = 13688832;
            this.nodeAdjacency[22] = 11763712;
            this.nodeAdjacency[23] = 8257536;
        }
        return findClosestNode(getX(), getY(), getZ());
    }

    public int findClosestNode(double d, double d2, double d3) {
        float f = 10000.0f;
        int i = 0;
        Node node = new Node(Mth.floor(d), Mth.floor(d2), Mth.floor(d3));
        int i2 = 0;
        if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
            i2 = 12;
        }
        for (int i3 = i2; i3 < 24; i3++) {
            if (this.nodes[i3] != null) {
                float distanceToSqr = this.nodes[i3].distanceToSqr(node);
                if (distanceToSqr < f) {
                    f = distanceToSqr;
                    i = i3;
                }
            }
        }
        return i;
    }

    @Nullable
    public Path findPath(int i, int i2, @Nullable Node node) {
        for (int i3 = 0; i3 < 24; i3++) {
            Node node2 = this.nodes[i3];
            node2.closed = false;
            node2.f = 0.0f;
            node2.g = 0.0f;
            node2.h = 0.0f;
            node2.cameFrom = null;
            node2.heapIdx = -1;
        }
        Node node3 = this.nodes[i];
        Node node4 = this.nodes[i2];
        node3.g = 0.0f;
        node3.h = node3.distanceTo(node4);
        node3.f = node3.h;
        this.openSet.clear();
        this.openSet.insert(node3);
        Node node5 = node3;
        int i4 = 0;
        if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
            i4 = 12;
        }
        while (!this.openSet.isEmpty()) {
            Node pop = this.openSet.pop();
            if (pop.equals(node4)) {
                if (node != null) {
                    node.cameFrom = node4;
                    node4 = node;
                }
                return reconstructPath(node3, node4);
            }
            if (pop.distanceTo(node4) < node5.distanceTo(node4)) {
                node5 = pop;
            }
            pop.closed = true;
            int i5 = 0;
            int i6 = 0;
            while (true) {
                if (i6 >= 24) {
                    break;
                }
                if (this.nodes[i6] != pop) {
                    i6++;
                } else {
                    i5 = i6;
                    break;
                }
            }
            for (int i7 = i4; i7 < 24; i7++) {
                if ((this.nodeAdjacency[i5] & (1 << i7)) > 0) {
                    Node node6 = this.nodes[i7];
                    if (!node6.closed) {
                        float distanceTo = pop.g + pop.distanceTo(node6);
                        if (!node6.inOpenSet() || distanceTo < node6.g) {
                            node6.cameFrom = pop;
                            node6.g = distanceTo;
                            node6.h = node6.distanceTo(node4);
                            if (node6.inOpenSet()) {
                                this.openSet.changeCost(node6, node6.g + node6.h);
                            } else {
                                node6.f = node6.g + node6.h;
                                this.openSet.insert(node6);
                            }
                        }
                    }
                }
            }
        }
        if (node5 == node3) {
            return null;
        }
        LOGGER.debug("Failed to find path from {} to {}", Integer.valueOf(i), Integer.valueOf(i2));
        if (node != null) {
            node.cameFrom = node5;
            node5 = node;
        }
        return reconstructPath(node3, node5);
    }

    private Path reconstructPath(Node node, Node node2) {
        List<Node> newArrayList = Lists.newArrayList();
        Node node3 = node2;
        newArrayList.add(0, node3);
        while (node3.cameFrom != null) {
            node3 = node3.cameFrom;
            newArrayList.add(0, node3);
        }
        return new Path(newArrayList, new BlockPos(node2.x, node2.y, node2.z), true);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("DragonPhase", this.phaseManager.getCurrentPhase().getPhase().getId());
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("DragonPhase")) {
            this.phaseManager.setPhase(EnderDragonPhase.getById(compoundTag.getInt("DragonPhase")));
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.Entity
    public void checkDespawn() {
    }

    public EnderDragonPart[] getSubEntities() {
        return this.subEntities;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getSoundVolume() {
        return 5.0f;
    }

    public float getHeadPartYOffset(int i, double[] dArr, double[] dArr2) {
        double max;
        DragonPhaseInstance currentPhase = this.phaseManager.getCurrentPhase();
        EnderDragonPhase<? extends DragonPhaseInstance> phase = currentPhase.getPhase();
        if (phase == EnderDragonPhase.LANDING || phase == EnderDragonPhase.TAKEOFF) {
            max = i / Math.max(Mth.sqrt(this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION).distSqr(position(), true)) / 4.0f, 1.0f);
        } else if (currentPhase.isSitting()) {
            max = i;
        } else if (i == 6) {
            max = 0.0d;
        } else {
            max = dArr2[1] - dArr[1];
        }
        return (float) max;
    }

    public Vec3 getHeadLookVector(float f) {
        Vec3 viewVector;
        DragonPhaseInstance currentPhase = this.phaseManager.getCurrentPhase();
        EnderDragonPhase<? extends DragonPhaseInstance> phase = currentPhase.getPhase();
        if (phase == EnderDragonPhase.LANDING || phase == EnderDragonPhase.TAKEOFF) {
            float max = 6.0f / Math.max(Mth.sqrt(this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION).distSqr(position(), true)) / 4.0f, 1.0f);
            float f2 = this.xRot;
            this.xRot = (-max) * 1.5f * 5.0f;
            viewVector = getViewVector(f);
            this.xRot = f2;
        } else if (currentPhase.isSitting()) {
            float f3 = this.xRot;
            this.xRot = -45.0f;
            viewVector = getViewVector(f);
            this.xRot = f3;
        } else {
            viewVector = getViewVector(f);
        }
        return viewVector;
    }

    public void onCrystalDestroyed(EndCrystal endCrystal, BlockPos blockPos, DamageSource damageSource) {
        Player nearestPlayer;
        if (damageSource.getEntity() instanceof Player) {
            nearestPlayer = (Player) damageSource.getEntity();
        } else {
            nearestPlayer = this.level.getNearestPlayer(CRYSTAL_DESTROY_TARGETING, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        if (endCrystal == this.nearestCrystal) {
            hurt(this.head, DamageSource.explosion(nearestPlayer), 10.0f);
        }
        this.phaseManager.getCurrentPhase().onCrystalDestroyed(endCrystal, blockPos, damageSource, nearestPlayer);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_PHASE.equals(entityDataAccessor) && this.level.isClientSide) {
            this.phaseManager.setPhase(EnderDragonPhase.getById(((Integer) getEntityData().get(DATA_PHASE)).intValue()));
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public EnderDragonPhaseManager getPhaseManager() {
        return this.phaseManager;
    }

    @Nullable
    public EndDragonFight getDragonFight() {
        return this.dragonFight;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean addEffect(MobEffectInstance mobEffectInstance) {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean canChangeDimensions() {
        return false;
    }
}
