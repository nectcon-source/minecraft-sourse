package net.minecraft.world.entity.monster;

import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/ElderGuardian.class */
public class ElderGuardian extends Guardian {
    public static final float ELDER_SIZE_SCALE = EntityType.ELDER_GUARDIAN.getWidth() / EntityType.GUARDIAN.getWidth();

    public ElderGuardian(EntityType<? extends ElderGuardian> entityType, Level level) {
        super(entityType, level);
        setPersistenceRequired();
        if (this.randomStrollGoal != null) {
            this.randomStrollGoal.setInterval(400);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Guardian.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.30000001192092896d).add(Attributes.ATTACK_DAMAGE, 8.0d).add(Attributes.MAX_HEALTH, 80.0d);
    }

    @Override // net.minecraft.world.entity.monster.Guardian
    public int getAttackDuration() {
        return 60;
    }

    @Override // net.minecraft.world.entity.monster.Guardian, net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_AMBIENT : SoundEvents.ELDER_GUARDIAN_AMBIENT_LAND;
    }

    @Override // net.minecraft.world.entity.monster.Guardian, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_HURT : SoundEvents.ELDER_GUARDIAN_HURT_LAND;
    }

    @Override // net.minecraft.world.entity.monster.Guardian, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_DEATH : SoundEvents.ELDER_GUARDIAN_DEATH_LAND;
    }

    @Override // net.minecraft.world.entity.monster.Guardian
    protected SoundEvent getFlopSound() {
        return SoundEvents.ELDER_GUARDIAN_FLOP;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        super.customServerAiStep();
        if ((this.tickCount + getId()) % 1200 == 0) {
            MobEffect mobEffect = MobEffects.DIG_SLOWDOWN;
            for (ServerPlayer serverPlayer : ((ServerLevel) this.level).getPlayers(serverPlayer2 -> {
                return distanceToSqr(serverPlayer2) < 2500.0d && serverPlayer2.gameMode.isSurvival();
            })) {
                if (!serverPlayer.hasEffect(mobEffect) || serverPlayer.getEffect(mobEffect).getAmplifier() < 2 || serverPlayer.getEffect(mobEffect).getDuration() < 1200) {
                    serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, isSilent() ? 0.0f : 1.0f));
                    serverPlayer.addEffect(new MobEffectInstance(mobEffect, 6000, 2));
                }
            }
        }
        if (!hasRestriction()) {
            restrictTo(blockPosition(), 16);
        }
    }
}
