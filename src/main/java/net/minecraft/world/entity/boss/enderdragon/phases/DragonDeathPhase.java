package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/phases/DragonDeathPhase.class */
public class DragonDeathPhase extends AbstractDragonPhaseInstance {
    private Vec3 targetLocation;
    private int time;

    public DragonDeathPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void doClientTick() {
        int i = this.time;
        this.time = i + 1;
        if (i % 10 == 0) {
            this.dragon.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.dragon.getX() + ((this.dragon.getRandom().nextFloat() - 0.5f) * 8.0f), this.dragon.getY() + 2.0d + ((this.dragon.getRandom().nextFloat() - 0.5f) * 4.0f), this.dragon.getZ() + ((this.dragon.getRandom().nextFloat() - 0.5f) * 8.0f), 0.0d, 0.0d, 0.0d);
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void doServerTick() {
        this.time++;
        if (this.targetLocation == null) {
            this.targetLocation = Vec3.atBottomCenterOf(this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION));
        }
        double distanceToSqr = this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (distanceToSqr < 100.0d || distanceToSqr > 22500.0d || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            this.dragon.setHealth(0.0f);
        } else {
            this.dragon.setHealth(1.0f);
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void begin() {
        this.targetLocation = null;
        this.time = 0;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public float getFlySpeed() {
        return 3.0f;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public EnderDragonPhase<DragonDeathPhase> getPhase() {
        return EnderDragonPhase.DYING;
    }
}
