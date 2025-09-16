package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Silverfish.class */
public class Silverfish extends Monster {
    private SilverfishWakeUpFriendsGoal friendsGoal;

    public Silverfish(EntityType<? extends Silverfish> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.friendsGoal = new SilverfishWakeUpFriendsGoal(this);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, this.friendsGoal);
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0d, false));
        this.goalSelector.addGoal(5, new SilverfishMergeWithStoneGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
    }

    @Override // net.minecraft.world.entity.Entity
    public double getMyRidingOffset() {
        return 0.1d;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.13f;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0d).add(Attributes.MOVEMENT_SPEED, 0.25d).add(Attributes.ATTACK_DAMAGE, 1.0d);
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.SILVERFISH_STEP, 0.15f, 1.0f);
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        if (((damageSource instanceof EntityDamageSource) || damageSource == DamageSource.MAGIC) && this.friendsGoal != null) {
            this.friendsGoal.notifyHurt();
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        this.yBodyRot = this.yRot;
        super.tick();
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void setYBodyRot(float f) {
        this.yRot = f;
        super.setYBodyRot(f);
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.PathfinderMob
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (InfestedBlock.isCompatibleHostBlock(levelReader.getBlockState(blockPos.below()))) {
            return 10.0f;
        }
        return super.getWalkTargetValue(blockPos, levelReader);
    }

    public static boolean checkSliverfishSpawnRules(EntityType<Silverfish> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return checkAnyLightMonsterSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random) && levelAccessor.getNearestPlayer(((double) blockPos.getX()) + 0.5d, ((double) blockPos.getY()) + 0.5d, ((double) blockPos.getZ()) + 0.5d, 5.0d, true) == null;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Silverfish$SilverfishWakeUpFriendsGoal.class */
    static class SilverfishWakeUpFriendsGoal extends Goal {
        private final Silverfish silverfish;
        private int lookForFriends;

        public SilverfishWakeUpFriendsGoal(Silverfish silverfish) {
            this.silverfish = silverfish;
        }

        public void notifyHurt() {
            if (this.lookForFriends == 0) {
                this.lookForFriends = 20;
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return this.lookForFriends > 0;
        }

        /* JADX WARN: Code restructure failed: missing block: B:47:0x00dd, code lost:
        
            if (r9 > 0) goto L41;
         */
        /* JADX WARN: Code restructure failed: missing block: B:48:0x00e0, code lost:
        
            r0 = 1;
         */
        /* JADX WARN: Code restructure failed: missing block: B:51:0x00e4, code lost:
        
            r0 = 0;
         */
        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public void tick() {
            --this.lookForFriends;
            if (this.lookForFriends <= 0) {
                Level var1 = this.silverfish.level;
                Random var2x = this.silverfish.getRandom();
                BlockPos var3xx = this.silverfish.blockPosition();

                for(int var4xxx = 0; var4xxx <= 5 && var4xxx >= -5; var4xxx = (var4xxx <= 0 ? 1 : 0) - var4xxx) {
                    for(int var5xxxx = 0; var5xxxx <= 10 && var5xxxx >= -10; var5xxxx = (var5xxxx <= 0 ? 1 : 0) - var5xxxx) {
                        for(int var6xxxxx = 0; var6xxxxx <= 10 && var6xxxxx >= -10; var6xxxxx = (var6xxxxx <= 0 ? 1 : 0) - var6xxxxx) {
                            BlockPos var7xxxxxx = var3xx.offset(var5xxxx, var4xxx, var6xxxxx);
                            BlockState var8xxxxxxx = var1.getBlockState(var7xxxxxx);
                            Block var9xxxxxxxx = var8xxxxxxx.getBlock();
                            if (var9xxxxxxxx instanceof InfestedBlock) {
                                if (var1.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                                    var1.destroyBlock(var7xxxxxx, true, this.silverfish);
                                } else {
                                    var1.setBlock(var7xxxxxx, ((InfestedBlock)var9xxxxxxxx).getHostBlock().defaultBlockState(), 3);
                                }

                                if (var2x.nextBoolean()) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Silverfish$SilverfishMergeWithStoneGoal.class */
    static class SilverfishMergeWithStoneGoal extends RandomStrollGoal {
        private Direction selectedDirection;
        private boolean doMerge;

        public SilverfishMergeWithStoneGoal(Silverfish silverfish) {
            super(silverfish, 1.0d, 10);
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.RandomStrollGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (this.mob.getTarget() != null || !this.mob.getNavigation().isDone()) {
                return false;
            }
            Random random = this.mob.getRandom();
            if (this.mob.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && random.nextInt(10) == 0) {
                this.selectedDirection = Direction.getRandom(random);
                if (InfestedBlock.isCompatibleHostBlock(this.mob.level.getBlockState(new BlockPos(this.mob.getX(), this.mob.getY() + 0.5d, this.mob.getZ()).relative(this.selectedDirection)))) {
                    this.doMerge = true;
                    return true;
                }
            }
            this.doMerge = false;
            return super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.RandomStrollGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            if (this.doMerge) {
                return false;
            }
            return super.canContinueToUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.RandomStrollGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            if (!this.doMerge) {
                super.start();
                return;
            }
            LevelAccessor levelAccessor = this.mob.level;
            BlockPos relative = new BlockPos(this.mob.getX(), this.mob.getY() + 0.5d, this.mob.getZ()).relative(this.selectedDirection);
            BlockState blockState = levelAccessor.getBlockState(relative);
            if (InfestedBlock.isCompatibleHostBlock(blockState)) {
                levelAccessor.setBlock(relative, InfestedBlock.stateByHostBlock(blockState.getBlock()), 3);
                this.mob.spawnAnim();
                this.mob.remove();
            }
        }
    }
}
