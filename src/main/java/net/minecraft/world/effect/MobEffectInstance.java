package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/effect/MobEffectInstance.class */
public class MobEffectInstance implements Comparable<MobEffectInstance> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final MobEffect effect;
    private int duration;
    private int amplifier;
    private boolean splash;
    private boolean ambient;
    private boolean noCounter;
    private boolean visible;
    private boolean showIcon;

    @Nullable
    private MobEffectInstance hiddenEffect;

    public MobEffectInstance(MobEffect mobEffect) {
        this(mobEffect, 0, 0);
    }

    public MobEffectInstance(MobEffect mobEffect, int i) {
        this(mobEffect, i, 0);
    }

    public MobEffectInstance(MobEffect mobEffect, int i, int i2) {
        this(mobEffect, i, i2, false, true);
    }

    public MobEffectInstance(MobEffect mobEffect, int i, int i2, boolean z, boolean z2) {
        this(mobEffect, i, i2, z, z2, z2);
    }

    public MobEffectInstance(MobEffect mobEffect, int i, int i2, boolean z, boolean z2, boolean z3) {
        this(mobEffect, i, i2, z, z2, z3, null);
    }

    public MobEffectInstance(MobEffect mobEffect, int i, int i2, boolean z, boolean z2, boolean z3, @Nullable MobEffectInstance mobEffectInstance) {
        this.effect = mobEffect;
        this.duration = i;
        this.amplifier = i2;
        this.ambient = z;
        this.visible = z2;
        this.showIcon = z3;
        this.hiddenEffect = mobEffectInstance;
    }

    public MobEffectInstance(MobEffectInstance mobEffectInstance) {
        this.effect = mobEffectInstance.effect;
        setDetailsFrom(mobEffectInstance);
    }

    void setDetailsFrom(MobEffectInstance mobEffectInstance) {
        this.duration = mobEffectInstance.duration;
        this.amplifier = mobEffectInstance.amplifier;
        this.ambient = mobEffectInstance.ambient;
        this.visible = mobEffectInstance.visible;
        this.showIcon = mobEffectInstance.showIcon;
    }

    public boolean update(MobEffectInstance mobEffectInstance) {
        if (this.effect != mobEffectInstance.effect) {
            LOGGER.warn("This method should only be called for matching effects!");
        }
        boolean z = false;
        if (mobEffectInstance.amplifier > this.amplifier) {
            if (mobEffectInstance.duration < this.duration) {
                MobEffectInstance mobEffectInstance2 = this.hiddenEffect;
                this.hiddenEffect = new MobEffectInstance(this);
                this.hiddenEffect.hiddenEffect = mobEffectInstance2;
            }
            this.amplifier = mobEffectInstance.amplifier;
            this.duration = mobEffectInstance.duration;
            z = true;
        } else if (mobEffectInstance.duration > this.duration) {
            if (mobEffectInstance.amplifier == this.amplifier) {
                this.duration = mobEffectInstance.duration;
                z = true;
            } else if (this.hiddenEffect == null) {
                this.hiddenEffect = new MobEffectInstance(mobEffectInstance);
            } else {
                this.hiddenEffect.update(mobEffectInstance);
            }
        }
        if ((!mobEffectInstance.ambient && this.ambient) || z) {
            this.ambient = mobEffectInstance.ambient;
            z = true;
        }
        if (mobEffectInstance.visible != this.visible) {
            this.visible = mobEffectInstance.visible;
            z = true;
        }
        if (mobEffectInstance.showIcon != this.showIcon) {
            this.showIcon = mobEffectInstance.showIcon;
            z = true;
        }
        return z;
    }

    public MobEffect getEffect() {
        return this.effect;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public boolean isAmbient() {
        return this.ambient;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean showIcon() {
        return this.showIcon;
    }

    public boolean tick(LivingEntity livingEntity, Runnable runnable) {
        if (this.duration > 0) {
            if (this.effect.isDurationEffectTick(this.duration, this.amplifier)) {
                applyEffect(livingEntity);
            }
            tickDownDuration();
            if (this.duration == 0 && this.hiddenEffect != null) {
                setDetailsFrom(this.hiddenEffect);
                this.hiddenEffect = this.hiddenEffect.hiddenEffect;
                runnable.run();
            }
        }
        return this.duration > 0;
    }

    private int tickDownDuration() {
        if (this.hiddenEffect != null) {
            this.hiddenEffect.tickDownDuration();
        }
        int i = this.duration - 1;
        this.duration = i;
        return i;
    }

    public void applyEffect(LivingEntity livingEntity) {
        if (this.duration > 0) {
            this.effect.applyEffectTick(livingEntity, this.amplifier);
        }
    }

    public String getDescriptionId() {
        return this.effect.getDescriptionId();
    }

    public String toString() {
        String str;
        if (this.amplifier > 0) {
            str = getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.duration;
        } else {
            str = getDescriptionId() + ", Duration: " + this.duration;
        }
        if (this.splash) {
            str = str + ", Splash: true";
        }
        if (!this.visible) {
            str = str + ", Particles: false";
        }
        if (!this.showIcon) {
            str = str + ", Show Icon: false";
        }
        return str;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MobEffectInstance) {
            MobEffectInstance mobEffectInstance = (MobEffectInstance) obj;
            return this.duration == mobEffectInstance.duration && this.amplifier == mobEffectInstance.amplifier && this.splash == mobEffectInstance.splash && this.ambient == mobEffectInstance.ambient && this.effect.equals(mobEffectInstance.effect);
        }
        return false;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * this.effect.hashCode()) + this.duration)) + this.amplifier)) + (this.splash ? 1 : 0))) + (this.ambient ? 1 : 0);
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putByte("Id", (byte) MobEffect.getId(getEffect()));
        writeDetailsTo(compoundTag);
        return compoundTag;
    }

    private void writeDetailsTo(CompoundTag compoundTag) {
        compoundTag.putByte("Amplifier", (byte) getAmplifier());
        compoundTag.putInt("Duration", getDuration());
        compoundTag.putBoolean("Ambient", isAmbient());
        compoundTag.putBoolean("ShowParticles", isVisible());
        compoundTag.putBoolean("ShowIcon", showIcon());
        if (this.hiddenEffect != null) {
            CompoundTag compoundTag2 = new CompoundTag();
            this.hiddenEffect.save(compoundTag2);
            compoundTag.put("HiddenEffect", compoundTag2);
        }
    }

    public static MobEffectInstance load(CompoundTag compoundTag) {
        MobEffect byId = MobEffect.byId(compoundTag.getByte("Id"));
        if (byId == null) {
            return null;
        }
        return loadSpecifiedEffect(byId, compoundTag);
    }

    private static MobEffectInstance loadSpecifiedEffect(MobEffect mobEffect, CompoundTag compoundTag) {
        int i = compoundTag.getByte("Amplifier");
        int i2 = compoundTag.getInt("Duration");
        boolean z = compoundTag.getBoolean("Ambient");
        boolean z2 = true;
        if (compoundTag.contains("ShowParticles", 1)) {
            z2 = compoundTag.getBoolean("ShowParticles");
        }
        boolean z3 = z2;
        if (compoundTag.contains("ShowIcon", 1)) {
            z3 = compoundTag.getBoolean("ShowIcon");
        }
        MobEffectInstance mobEffectInstance = null;
        if (compoundTag.contains("HiddenEffect", 10)) {
            mobEffectInstance = loadSpecifiedEffect(mobEffect, compoundTag.getCompound("HiddenEffect"));
        }
        return new MobEffectInstance(mobEffect, i2, i < 0 ? 0 : i, z, z2, z3, mobEffectInstance);
    }

    public void setNoCounter(boolean z) {
        this.noCounter = z;
    }

    public boolean isNoCounter() {
        return this.noCounter;
    }

    @Override // java.lang.Comparable
    public int compareTo(MobEffectInstance mobEffectInstance) {
        if ((getDuration() > 32147 && mobEffectInstance.getDuration() > 32147) || (isAmbient() && mobEffectInstance.isAmbient())) {
            return ComparisonChain.start().compare(Boolean.valueOf(isAmbient()), Boolean.valueOf(mobEffectInstance.isAmbient())).compare(getEffect().getColor(), mobEffectInstance.getEffect().getColor()).result();
        }
        return ComparisonChain.start().compare(Boolean.valueOf(isAmbient()), Boolean.valueOf(mobEffectInstance.isAmbient())).compare(getDuration(), mobEffectInstance.getDuration()).compare(getEffect().getColor(), mobEffectInstance.getEffect().getColor()).result();
    }
}
