package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.IntRange;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/hoglin/HoglinAi.class */
public class HoglinAi {
    private static final IntRange RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final IntRange ADULT_FOLLOW_RANGE = IntRange.of(5, 16);

    protected static Brain<?> makeBrain(Brain<Hoglin> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        initRetreatActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Hoglin> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
    }

    private static void initIdleActivity(Brain<Hoglin> brain) {
//        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_REPELLENT, 200), new AnimalMakeLove(EntityType.HOGLIN, 0.6f), SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0f, 8, true), new StartAttacking(HoglinAi::findNearestValidAttackTarget), new RunIf((v0) -> {
//            return v0.isAdult();
//        }, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4f, 8, false)), new RunSometimes(new SetEntityLookTarget(8.0f), IntRange.of(30, 60)), new BabyFollowAdult(ADULT_FOLLOW_RANGE, 0.6f), createIdleMovementBehaviors()));
        brain.addActivity(
                Activity.IDLE,
                10,
               ImmutableList.<Behavior<? super Hoglin>>of(
                        new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_REPELLENT, 200),
                        new AnimalMakeLove(EntityType.HOGLIN, 0.6F),
                        SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, true),
                        new StartAttacking<>(HoglinAi::findNearestValidAttackTarget),
                        new RunIf<>(Hoglin::isAdult, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4F, 8, false)),
                        new RunSometimes<>(new SetEntityLookTarget(8.0F), IntRange.of(30, 60)),
                        new BabyFollowAdult(ADULT_FOLLOW_RANGE, 0.6F),
                        createIdleMovementBehaviors()
                )
        );

    }

    private static void initFightActivity(Brain<Hoglin> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_REPELLENT, 200), new AnimalMakeLove(EntityType.HOGLIN, 0.6f), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0f), new RunIf((v0) -> {
//            return v0.isAdult();
//        }, new MeleeAttack(40)), new RunIf((v0) -> {
//            return v0.isBaby();
//        }, new MeleeAttack(15)), new StopAttackingIfTargetInvalid(), new EraseMemoryIf(HoglinAi::isBreeding, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
        brain.addActivityAndRemoveMemoryWhenStopped(
                Activity.FIGHT,
                10,
                ImmutableList.<Behavior<? super Hoglin>>of(
                        new BecomePassiveIfMemoryPresent(MemoryModuleType.NEAREST_REPELLENT, 200),
                        new AnimalMakeLove(EntityType.HOGLIN, 0.6F),
                        new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F),
                        new RunIf<>(Hoglin::isAdult, new MeleeAttack(40)),
                        new RunIf<>(AgableMob::isBaby, new MeleeAttack(15)),
                        new StopAttackingIfTargetInvalid(),
                        new EraseMemoryIf<>(HoglinAi::isBreeding, MemoryModuleType.ATTACK_TARGET)
                ),
                MemoryModuleType.ATTACK_TARGET
        );
    }

    private static void initRetreatActivity(Brain<Hoglin> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.3f, 15, false), createIdleMovementBehaviors(), new RunSometimes(new SetEntityLookTarget(8.0f), IntRange.of(30, 60)), new EraseMemoryIf(HoglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
    brain.addActivityAndRemoveMemoryWhenStopped(
            Activity.AVOID,
            10,
            ImmutableList.of(
                    SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.3F, 15, false),
                    createIdleMovementBehaviors(),
                    new RunSometimes<>(new SetEntityLookTarget(8.0F), IntRange.of(30, 60)),
                    new EraseMemoryIf<>(HoglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
            ),
            MemoryModuleType.AVOID_TARGET
    );
    }

    private static RunOne<Hoglin> createIdleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(0.4f), 2), Pair.of(new SetWalkTargetFromLookTarget(0.4f, 3), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    protected static void updateActivity(Hoglin hoglin) {
        Brain<Hoglin> brain = hoglin.getBrain();
        Activity orElse = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
        if (orElse != brain.getActiveNonCoreActivity().orElse(null)) {
            Optional<SoundEvent> soundForCurrentActivity = getSoundForCurrentActivity(hoglin);
            hoglin.getClass();
            soundForCurrentActivity.ifPresent(hoglin::playSound);
        }
        hoglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    protected static void onHitTarget(Hoglin hoglin, LivingEntity livingEntity) {
        if (hoglin.isBaby()) {
            return;
        }
        if (livingEntity.getType() == EntityType.PIGLIN && piglinsOutnumberHoglins(hoglin)) {
            setAvoidTarget(hoglin, livingEntity);
            broadcastRetreat(hoglin, livingEntity);
        } else {
            broadcastAttackTarget(hoglin, livingEntity);
        }
    }

    private static void broadcastRetreat(Hoglin hoglin, LivingEntity livingEntity) {
        getVisibleAdultHoglins(hoglin).forEach(hoglin2 -> {
            retreatFromNearestTarget(hoglin2, livingEntity);
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void retreatFromNearestTarget(Hoglin hoglin, LivingEntity livingEntity) {
        Brain<Hoglin> brain = hoglin.getBrain();
        setAvoidTarget(hoglin, BehaviorUtils.getNearestTarget(hoglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), BehaviorUtils.getNearestTarget(hoglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), livingEntity)));
    }

    private static void setAvoidTarget(Hoglin hoglin, LivingEntity livingEntity) {
        hoglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        hoglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        hoglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, RETREAT_DURATION.randomValue(hoglin.level.random));
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Hoglin hoglin) {
        if (isPacified(hoglin) || isBreeding(hoglin)) {
            return Optional.empty();
        }
        return hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
    }

    static boolean isPosNearNearestRepellent(Hoglin hoglin, BlockPos blockPos) {
        Optional<BlockPos> var2 = hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_REPELLENT);
        return var2.isPresent() && ((BlockPos)var2.get()).closerThan(blockPos, (double)8.0F);
    }

    private static boolean wantsToStopFleeing(Hoglin hoglin) {
        return hoglin.isAdult() && !piglinsOutnumberHoglins(hoglin);
    }

    private static boolean piglinsOutnumberHoglins(Hoglin hoglin) {
        return !hoglin.isBaby() && ((Integer) hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0)).intValue() > ((Integer) hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0)).intValue() + 1;
    }

    protected static void wasHurtBy(Hoglin hoglin, LivingEntity livingEntity) {
        Brain<Hoglin> brain = hoglin.getBrain();
        brain.eraseMemory(MemoryModuleType.PACIFIED);
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        if (hoglin.isBaby()) {
            retreatFromNearestTarget(hoglin, livingEntity);
        } else {
            maybeRetaliate(hoglin, livingEntity);
        }
    }

    private static void maybeRetaliate(Hoglin hoglin, LivingEntity livingEntity) {
        if ((hoglin.getBrain().isActive(Activity.AVOID) && livingEntity.getType() == EntityType.PIGLIN) || !EntitySelector.ATTACK_ALLOWED.test(livingEntity) || livingEntity.getType() == EntityType.HOGLIN || BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(hoglin, livingEntity, 4.0d)) {
            return;
        }
        setAttackTarget(hoglin, livingEntity);
        broadcastAttackTarget(hoglin, livingEntity);
    }

    private static void setAttackTarget(Hoglin hoglin, LivingEntity livingEntity) {
        Brain<Hoglin> brain = hoglin.getBrain();
        brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, livingEntity, 200L);
    }

    private static void broadcastAttackTarget(Hoglin hoglin, LivingEntity livingEntity) {
        getVisibleAdultHoglins(hoglin).forEach(hoglin2 -> {
            setAttackTargetIfCloserThanCurrent(hoglin2, livingEntity);
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void setAttackTargetIfCloserThanCurrent(Hoglin hoglin, LivingEntity livingEntity) {
        if (isPacified(hoglin)) {
            return;
        }
        setAttackTarget(hoglin, BehaviorUtils.getNearestTarget(hoglin, hoglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET), livingEntity));
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Hoglin hoglin) {
        return hoglin.getBrain().getActiveNonCoreActivity().map(activity -> {
            return getSoundForActivity(hoglin, activity);
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static SoundEvent getSoundForActivity(Hoglin hoglin, Activity activity) {
        if (activity == Activity.AVOID || hoglin.isConverting()) {
            return SoundEvents.HOGLIN_RETREAT;
        }
        if (activity == Activity.FIGHT) {
            return SoundEvents.HOGLIN_ANGRY;
        }
        if (isNearRepellent(hoglin)) {
            return SoundEvents.HOGLIN_RETREAT;
        }
        return SoundEvents.HOGLIN_AMBIENT;
    }

    private static List<Hoglin> getVisibleAdultHoglins(Hoglin hoglin) {
        return (List) hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
    }

    private static boolean isNearRepellent(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean isBreeding(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    protected static boolean isPacified(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
    }
}
