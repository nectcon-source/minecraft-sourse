package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Giant.class */
public class Giant extends Monster {
    public Giant(EntityType<? extends Giant> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 10.440001f;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0d).add(Attributes.MOVEMENT_SPEED, 0.5d).add(Attributes.ATTACK_DAMAGE, 50.0d);
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.PathfinderMob
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return levelReader.getBrightness(blockPos) - 0.5f;
    }
}
