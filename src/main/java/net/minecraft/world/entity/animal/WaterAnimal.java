package net.minecraft.world.entity.animal;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/WaterAnimal.class */
public abstract class WaterAnimal extends PathfinderMob {
    protected WaterAnimal(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
        setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public MobType getMobType() {
        return MobType.WATER;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this);
    }

    @Override // net.minecraft.world.entity.Mob
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected int getExperienceReward(Player player) {
        return 1 + this.level.random.nextInt(3);
    }

    protected void handleAirSupply(int i) {
        if (isAlive() && !isInWaterOrBubble()) {
            setAirSupply(i - 1);
            if (getAirSupply() == -20) {
                setAirSupply(0);
                hurt(DamageSource.DROWN, 2.0f);
                return;
            }
            return;
        }
        setAirSupply(300);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void baseTick() {
        int airSupply = getAirSupply();
        super.baseTick();
        handleAirSupply(airSupply);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPushedByFluid() {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canBeLeashed(Player player) {
        return false;
    }
}
