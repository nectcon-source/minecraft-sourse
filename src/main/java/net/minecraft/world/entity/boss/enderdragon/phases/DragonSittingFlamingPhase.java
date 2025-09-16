package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/phases/DragonSittingFlamingPhase.class */
public class DragonSittingFlamingPhase extends AbstractDragonSittingPhase {
    private int flameTicks;
    private int flameCount;
    private AreaEffectCloud flame;

    public DragonSittingFlamingPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void doClientTick() {
        this.flameTicks++;
        if (this.flameTicks % 2 == 0 && this.flameTicks < 10) {
            Vec3 normalize = this.dragon.getHeadLookVector(1.0f).normalize();
            normalize.yRot(-0.7853982f);
            double x = this.dragon.head.getX();
            double y = this.dragon.head.getY(0.5d);
            double z = this.dragon.head.getZ();
            for (int i = 0; i < 8; i++) {
                double nextGaussian = x + (this.dragon.getRandom().nextGaussian() / 2.0d);
                double nextGaussian2 = y + (this.dragon.getRandom().nextGaussian() / 2.0d);
                double nextGaussian3 = z + (this.dragon.getRandom().nextGaussian() / 2.0d);
                for (int i2 = 0; i2 < 6; i2++) {
                    this.dragon.level.addParticle(ParticleTypes.DRAGON_BREATH, nextGaussian, nextGaussian2, nextGaussian3, (-normalize.x) * 0.07999999821186066d * i2, (-normalize.y) * 0.6000000238418579d, (-normalize.z) * 0.07999999821186066d * i2);
                }
                normalize.yRot(0.19634955f);
            }
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void doServerTick() {
        this.flameTicks++;
        if (this.flameTicks >= 200) {
            if (this.flameCount >= 4) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
                return;
            } else {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_SCANNING);
                return;
            }
        }
        if (this.flameTicks == 10) {
            Vec3 normalize = new Vec3(this.dragon.head.getX() - this.dragon.getX(), 0.0d, this.dragon.head.getZ() - this.dragon.getZ()).normalize();
            double x = this.dragon.head.getX() + ((normalize.x * 5.0d) / 2.0d);
            double z = this.dragon.head.getZ() + ((normalize.z * 5.0d) / 2.0d);
            double y = this.dragon.head.getY(0.5d);
            double d = y;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(x, d, z);
            while (true) {
                if (!this.dragon.level.isEmptyBlock(mutableBlockPos)) {
                    break;
                }
                d -= 1.0d;
                if (d < 0.0d) {
                    d = y;
                    break;
                }
                mutableBlockPos.set(x, d, z);
            }
            this.flame = new AreaEffectCloud(this.dragon.level, x, Mth.floor(d) + 1, z);
            this.flame.setOwner(this.dragon);
            this.flame.setRadius(5.0f);
            this.flame.setDuration(200);
            this.flame.setParticle(ParticleTypes.DRAGON_BREATH);
            this.flame.addEffect(new MobEffectInstance(MobEffects.HARM));
            this.dragon.level.addFreshEntity(this.flame);
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void begin() {
        this.flameTicks = 0;
        this.flameCount++;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void end() {
        if (this.flame != null) {
            this.flame.remove();
            this.flame = null;
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public EnderDragonPhase<DragonSittingFlamingPhase> getPhase() {
        return EnderDragonPhase.SITTING_FLAMING;
    }

    public void resetFlameCount() {
        this.flameCount = 0;
    }
}
