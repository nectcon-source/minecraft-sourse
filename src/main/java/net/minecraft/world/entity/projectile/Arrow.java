package net.minecraft.world.entity.projectile;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/Arrow.class */
public class Arrow extends AbstractArrow {
    private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
    private Potion potion;
    private final Set<MobEffectInstance> effects;
    private boolean fixedColor;

    public Arrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
    }

    public Arrow(Level level, double d, double d2, double d3) {
        super(EntityType.ARROW, d, d2, d3, level);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
    }

    public Arrow(Level level, LivingEntity livingEntity) {
        super(EntityType.ARROW, livingEntity, level);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
    }

    public void setEffectsFromItem(ItemStack itemStack) {
        if (itemStack.getItem() != Items.TIPPED_ARROW) {
            if (itemStack.getItem() == Items.ARROW) {
                this.potion = Potions.EMPTY;
                this.effects.clear();
                this.entityData.set(ID_EFFECT_COLOR, -1);
                return;
            }
            return;
        }
        this.potion = PotionUtils.getPotion(itemStack);
        Collection<MobEffectInstance> customEffects = PotionUtils.getCustomEffects(itemStack);
        if (!customEffects.isEmpty()) {
            Iterator<MobEffectInstance> it = customEffects.iterator();
            while (it.hasNext()) {
                this.effects.add(new MobEffectInstance(it.next()));
            }
        }
        int customColor = getCustomColor(itemStack);
        if (customColor == -1) {
            updateColor();
        } else {
            setFixedColor(customColor);
        }
    }

    public static int getCustomColor(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("CustomPotionColor", 99)) {
            return tag.getInt("CustomPotionColor");
        }
        return -1;
    }

    private void updateColor() {
        this.fixedColor = false;
        if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
            this.entityData.set(ID_EFFECT_COLOR, -1);
        } else {
            this.entityData.set(ID_EFFECT_COLOR, Integer.valueOf(PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects))));
        }
    }

    public void addEffect(MobEffectInstance mobEffectInstance) {
        this.effects.add(mobEffectInstance);
        getEntityData().set(ID_EFFECT_COLOR, Integer.valueOf(PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects))));
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_EFFECT_COLOR, -1);
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            if (this.inGround) {
                if (this.inGroundTime % 5 == 0) {
                    makeParticle(1);
                    return;
                }
                return;
            }
            makeParticle(2);
            return;
        }
        if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
            this.level.broadcastEntityEvent(this, (byte) 0);
            this.potion = Potions.EMPTY;
            this.effects.clear();
            this.entityData.set(ID_EFFECT_COLOR, -1);
        }
    }

    private void makeParticle(int i) {
        int var2 = this.getColor();
        if (var2 != -1 && i> 0) {
            double var3x = (double)(var2 >> 16 & 0xFF) / 255.0;
            double var5xx = (double)(var2 >> 8 & 0xFF) / 255.0;
            double var7xxx = (double)(var2 >> 0 & 0xFF) / 255.0;

            for(int var9xxxx = 0; var9xxxx < i; ++var9xxxx) {
                this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), var3x, var5xx, var7xxx);
            }
        }
    }

    public int getColor() {
        return ((Integer) this.entityData.get(ID_EFFECT_COLOR)).intValue();
    }

    private void setFixedColor(int i) {
        this.fixedColor = true;
        this.entityData.set(ID_EFFECT_COLOR, Integer.valueOf(i));
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.potion != Potions.EMPTY && this.potion != null) {
            compoundTag.putString("Potion", Registry.POTION.getKey(this.potion).toString());
        }
        if (this.fixedColor) {
            compoundTag.putInt("Color", getColor());
        }
        if (!this.effects.isEmpty()) {
            ListTag listTag = new ListTag();
            Iterator<MobEffectInstance> it = this.effects.iterator();
            while (it.hasNext()) {
                listTag.add(it.next().save(new CompoundTag()));
            }
            compoundTag.put("CustomPotionEffects", listTag);
        }
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("Potion", 8)) {
            this.potion = PotionUtils.getPotion(compoundTag);
        }
        Iterator<MobEffectInstance> it = PotionUtils.getCustomEffects(compoundTag).iterator();
        while (it.hasNext()) {
            addEffect(it.next());
        }
        if (compoundTag.contains("Color", 99)) {
            setFixedColor(compoundTag.getInt("Color"));
        } else {
            updateColor();
        }
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow
    protected void doPostHurtEffects(LivingEntity livingEntity) {
        super.doPostHurtEffects(livingEntity);
        for (MobEffectInstance mobEffectInstance : this.potion.getEffects()) {
            livingEntity.addEffect(new MobEffectInstance(mobEffectInstance.getEffect(), Math.max(mobEffectInstance.getDuration() / 8, 1), mobEffectInstance.getAmplifier(), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible()));
        }
        if (!this.effects.isEmpty()) {
            Iterator<MobEffectInstance> it = this.effects.iterator();
            while (it.hasNext()) {
                livingEntity.addEffect(it.next());
            }
        }
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow
    protected ItemStack getPickupItem() {
        if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
            return new ItemStack(Items.ARROW);
        }
        ItemStack itemStack = new ItemStack(Items.TIPPED_ARROW);
        PotionUtils.setPotion(itemStack, this.potion);
        PotionUtils.setCustomEffects(itemStack, this.effects);
        if (this.fixedColor) {
            itemStack.getOrCreateTag().putInt("CustomPotionColor", getColor());
        }
        return itemStack;
    }

    @Override // net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 0) {
            int var2 = this.getColor();
            if (var2 != -1) {
                double var3x = (double)(var2 >> 16 & 0xFF) / 255.0;
                double var5xx = (double)(var2 >> 8 & 0xFF) / 255.0;
                double var7xxx = (double)(var2 >> 0 & 0xFF) / 255.0;

                for(int var9xxxx = 0; var9xxxx < 20; ++var9xxxx) {
                    this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), var3x, var5xx, var7xxx);
                }
            }
        } else {
            super.handleEntityEvent(b);
        }
    }
}
