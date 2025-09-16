package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/AreaEffectCloud.class */
public class AreaEffectCloud extends Entity {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
    private Potion potion;
    private final List<MobEffectInstance> effects;
    private final Map<Entity, Integer> victims;
    private int duration;
    private int waitTime;
    private int reapplicationDelay;
    private boolean fixedColor;
    private int durationOnUse;
    private float radiusOnUse;
    private float radiusPerTick;
    private LivingEntity owner;
    private UUID ownerUUID;

    public AreaEffectCloud(EntityType<? extends AreaEffectCloud> entityType, Level level) {
        super(entityType, level);
        this.potion = Potions.EMPTY;
        this.effects = Lists.newArrayList();
        this.victims = Maps.newHashMap();
        this.duration = 600;
        this.waitTime = 20;
        this.reapplicationDelay = 20;
        this.noPhysics = true;
        setRadius(3.0f);
    }

    public AreaEffectCloud(Level level, double d, double d2, double d3) {
        this(EntityType.AREA_EFFECT_CLOUD, level);
        setPos(d, d2, d3);
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        getEntityData().define(DATA_COLOR, 0);
        getEntityData().define(DATA_RADIUS, Float.valueOf(0.5f));
        getEntityData().define(DATA_WAITING, false);
        getEntityData().define(DATA_PARTICLE, ParticleTypes.ENTITY_EFFECT);
    }

    public void setRadius(float f) {
        if (!this.level.isClientSide) {
            getEntityData().set(DATA_RADIUS, Float.valueOf(f));
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void refreshDimensions() {
        double x = getX();
        double y = getY();
        double z = getZ();
        super.refreshDimensions();
        setPos(x, y, z);
    }

    public float getRadius() {
        return ((Float) getEntityData().get(DATA_RADIUS)).floatValue();
    }

    public void setPotion(Potion potion) {
        this.potion = potion;
        if (!this.fixedColor) {
            updateColor();
        }
    }

    private void updateColor() {
        if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
            getEntityData().set(DATA_COLOR, 0);
        } else {
            getEntityData().set(DATA_COLOR, Integer.valueOf(PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects))));
        }
    }

    public void addEffect(MobEffectInstance mobEffectInstance) {
        this.effects.add(mobEffectInstance);
        if (!this.fixedColor) {
            updateColor();
        }
    }

    public int getColor() {
        return ((Integer) getEntityData().get(DATA_COLOR)).intValue();
    }

    public void setFixedColor(int i) {
        this.fixedColor = true;
        getEntityData().set(DATA_COLOR, Integer.valueOf(i));
    }

    public ParticleOptions getParticle() {
        return (ParticleOptions) getEntityData().get(DATA_PARTICLE);
    }

    public void setParticle(ParticleOptions particleOptions) {
        getEntityData().set(DATA_PARTICLE, particleOptions);
    }

    protected void setWaiting(boolean z) {
        getEntityData().set(DATA_WAITING, Boolean.valueOf(z));
    }

    public boolean isWaiting() {
        return ((Boolean) getEntityData().get(DATA_WAITING)).booleanValue();
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int i) {
        this.duration = i;
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick()  {
        super.tick();
        boolean isWaiting = isWaiting();
        float radius = getRadius();
        if (this.level.isClientSide) {
            ParticleOptions particle = getParticle();
            if (isWaiting) {
                if (this.random.nextBoolean()) {
                    for (int i = 0; i < 2; i++) {
                        float nextFloat = this.random.nextFloat() * 6.2831855f;
                        float sqrt = Mth.sqrt(this.random.nextFloat()) * 0.2f;
                        float cos = Mth.cos(nextFloat) * sqrt;
                        float sin = Mth.sin(nextFloat) * sqrt;
                        if (particle.getType() == ParticleTypes.ENTITY_EFFECT) {
                            int color = this.random.nextBoolean() ? 16777215 : getColor();
                            this.level.addAlwaysVisibleParticle(particle, getX() + cos, getY(), getZ() + sin, ((color >> 16) & 255) / 255.0f, ((color >> 8) & 255) / 255.0f, (color & 255) / 255.0f);
                        } else {
                            this.level.addAlwaysVisibleParticle(particle, getX() + cos, getY(), getZ() + sin, 0.0d, 0.0d, 0.0d);
                        }
                    }
                    return;
                }
                return;
            }
            float f = 3.1415927f * radius * radius;
            for (int i2 = 0; i2 < f; i2++) {
                float nextFloat2 = this.random.nextFloat() * 6.2831855f;
                float sqrt2 = Mth.sqrt(this.random.nextFloat()) * radius;
                float cos2 = Mth.cos(nextFloat2) * sqrt2;
                float sin2 = Mth.sin(nextFloat2) * sqrt2;
                if (particle.getType() == ParticleTypes.ENTITY_EFFECT) {
                    int color2 = getColor();
                    this.level.addAlwaysVisibleParticle(particle, getX() + cos2, getY(), getZ() + sin2, ((color2 >> 16) & 255) / 255.0f, ((color2 >> 8) & 255) / 255.0f, (color2 & 255) / 255.0f);
                } else {
                    this.level.addAlwaysVisibleParticle(particle, getX() + cos2, getY(), getZ() + sin2, (0.5d - this.random.nextDouble()) * 0.15d, 0.009999999776482582d, (0.5d - this.random.nextDouble()) * 0.15d);
                }
            }
            return;
        }
        if (this.tickCount >= this.waitTime + this.duration) {
            remove();
            return;
        }
        boolean z = this.tickCount < this.waitTime;
        if (isWaiting != z) {
            setWaiting(z);
        }
        if (z) {
            return;
        }
        if (this.radiusPerTick != 0.0f) {
            radius += this.radiusPerTick;
            if (radius < 0.5f) {
                remove();
                return;
            }
            setRadius(radius);
        }
        if (this.tickCount % 5 == 0) {
            Iterator<Map.Entry<Entity, Integer>> it = this.victims.entrySet().iterator();
            while (it.hasNext()) {
                if (this.tickCount >= it.next().getValue().intValue()) {
                    it.remove();
                }
            }
            List<MobEffectInstance> newArrayList = Lists.newArrayList();
            for (MobEffectInstance mobEffectInstance : this.potion.getEffects()) {
                newArrayList.add(new MobEffectInstance(mobEffectInstance.getEffect(), mobEffectInstance.getDuration() / 4, mobEffectInstance.getAmplifier(), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible()));
            }
            newArrayList.addAll(this.effects);
            if (newArrayList.isEmpty()) {
                this.victims.clear();
                return;
            }
            List<LivingEntity> entitiesOfClass = this.level.getEntitiesOfClass(LivingEntity.class, getBoundingBox());
            if (!entitiesOfClass.isEmpty()) {
                for (LivingEntity livingEntity : entitiesOfClass) {
                    if (!this.victims.containsKey(livingEntity) && livingEntity.isAffectedByPotions()) {
                        double x = livingEntity.getX() - getX();
                        double z2 = livingEntity.getZ() - getZ();
                        if ((x * x) + (z2 * z2) <= radius * radius) {
                            this.victims.put(livingEntity, Integer.valueOf(this.tickCount + this.reapplicationDelay));
                            for (MobEffectInstance mobEffectInstance2 : newArrayList) {
                                if (mobEffectInstance2.getEffect().isInstantenous()) {
                                    mobEffectInstance2.getEffect().applyInstantenousEffect(this, getOwner(), livingEntity, mobEffectInstance2.getAmplifier(), 0.5d);
                                } else {
                                    livingEntity.addEffect(new MobEffectInstance(mobEffectInstance2));
                                }
                            }
                            if (this.radiusOnUse != 0.0f) {
                                radius += this.radiusOnUse;
                                if (radius < 0.5f) {
                                    remove();
                                    return;
                                }
                                setRadius(radius);
                            }
                            if (this.durationOnUse != 0) {
                                this.duration += this.durationOnUse;
                                if (this.duration <= 0) {
                                    remove();
                                    return;
                                }
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
    }

    public void setRadiusOnUse(float f) {
        this.radiusOnUse = f;
    }

    public void setRadiusPerTick(float f) {
        this.radiusPerTick = f;
    }

    public void setWaitTime(int i) {
        this.waitTime = i;
    }

    public void setOwner(@Nullable LivingEntity livingEntity) {
        this.owner = livingEntity;
        this.ownerUUID = livingEntity == null ? null : livingEntity.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && (this.level instanceof ServerLevel)) {
            Entity entity = ((ServerLevel) this.level).getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity) entity;
            }
        }
        return this.owner;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.tickCount = compoundTag.getInt("Age");
        this.duration = compoundTag.getInt("Duration");
        this.waitTime = compoundTag.getInt("WaitTime");
        this.reapplicationDelay = compoundTag.getInt("ReapplicationDelay");
        this.durationOnUse = compoundTag.getInt("DurationOnUse");
        this.radiusOnUse = compoundTag.getFloat("RadiusOnUse");
        this.radiusPerTick = compoundTag.getFloat("RadiusPerTick");
        setRadius(compoundTag.getFloat("Radius"));
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
        }
        if (compoundTag.contains("Particle", 8)) {
            try {
                setParticle(ParticleArgument.readParticle(new StringReader(compoundTag.getString("Particle"))));
            } catch (CommandSyntaxException e) {
                LOGGER.warn("Couldn't load custom particle {}", compoundTag.getString("Particle"), e);
            }
        }
        if (compoundTag.contains("Color", 99)) {
            setFixedColor(compoundTag.getInt("Color"));
        }
        if (compoundTag.contains("Potion", 8)) {
            setPotion(PotionUtils.getPotion(compoundTag));
        }
        if (compoundTag.contains("Effects", 9)) {
            ListTag list = compoundTag.getList("Effects", 10);
            this.effects.clear();
            for (int i = 0; i < list.size(); i++) {
                MobEffectInstance load = MobEffectInstance.load(list.getCompound(i));
                if (load != null) {
                    addEffect(load);
                }
            }
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("Age", this.tickCount);
        compoundTag.putInt("Duration", this.duration);
        compoundTag.putInt("WaitTime", this.waitTime);
        compoundTag.putInt("ReapplicationDelay", this.reapplicationDelay);
        compoundTag.putInt("DurationOnUse", this.durationOnUse);
        compoundTag.putFloat("RadiusOnUse", this.radiusOnUse);
        compoundTag.putFloat("RadiusPerTick", this.radiusPerTick);
        compoundTag.putFloat("Radius", getRadius());
        compoundTag.putString("Particle", getParticle().writeToString());
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
        if (this.fixedColor) {
            compoundTag.putInt("Color", getColor());
        }
        if (this.potion != Potions.EMPTY && this.potion != null) {
            compoundTag.putString("Potion", Registry.POTION.getKey(this.potion).toString());
        }
        if (!this.effects.isEmpty()) {
            ListTag listTag = new ListTag();
            Iterator<MobEffectInstance> it = this.effects.iterator();
            while (it.hasNext()) {
                listTag.add(it.next().save(new CompoundTag()));
            }
            compoundTag.put("Effects", listTag);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_RADIUS.equals(entityDataAccessor)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override // net.minecraft.world.entity.Entity
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override // net.minecraft.world.entity.Entity
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(getRadius() * 2.0f, 0.5f);
    }
}
