package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/piglin/PiglinBruteAi.class */
public class PiglinBruteAi {
    protected static Brain<?> makeBrain(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
        initCoreActivity(piglinBrute, brain);
        initIdleActivity(piglinBrute, brain);
        initFightActivity(piglinBrute, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    protected static void initMemories(PiglinBrute piglinBrute) {
        piglinBrute.getBrain().setMemory( MemoryModuleType.HOME,  GlobalPos.of(piglinBrute.level.dimension(), piglinBrute.blockPosition()));
    }

    private static void initCoreActivity(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
        brain.addActivity(
                Activity.CORE, 0, ImmutableList.<Behavior<? super PiglinBrute>>of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), new InteractWithDoor(), new StopBeingAngryIfTargetDead())
        );
    }

    private static void initIdleActivity(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(new StartAttacking<>(PiglinBruteAi::findNearestValidAttackTarget), createIdleLookBehaviors(), createIdleMovementBehaviors(), new SetLookAndInteract(EntityType.PLAYER, 4)));
    }

    private static void initFightActivity(PiglinBrute piglinBrute, Brain<PiglinBrute> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, (ImmutableList<? extends Behavior<? super PiglinBrute>>) ImmutableList.<Behavior<? super PiglinBrute>>of(new StopAttackingIfTargetInvalid((var1x) -> !isNearestValidAttackTarget( piglinBrute, (LivingEntity) var1x)), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F), new MeleeAttack(20)), MemoryModuleType.ATTACK_TARGET);
    }

    private static RunOne<PiglinBrute> createIdleLookBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0f), 1), Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0f), 1), Pair.of(new SetEntityLookTarget(EntityType.PIGLIN_BRUTE, 8.0f), 1), Pair.of(new SetEntityLookTarget(8.0f), 1), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<PiglinBrute> createIdleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(0.6f), 2), Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), 2), Pair.of(InteractWith.of(EntityType.PIGLIN_BRUTE, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), 2), Pair.of(new StrollToPoi(MemoryModuleType.HOME, 0.6f, 2, 100), 2), Pair.of(new StrollAroundPoi(MemoryModuleType.HOME, 0.6f, 5), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    protected static void updateActivity(PiglinBrute piglinBrute) {
        Brain<PiglinBrute> brain = piglinBrute.getBrain();
        Activity orElse = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        if (orElse != brain.getActiveNonCoreActivity().orElse(null)) {
            playActivitySound(piglinBrute);
        }
        piglinBrute.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    private static boolean isNearestValidAttackTarget(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        return findNearestValidAttackTarget(abstractPiglin).filter(livingEntity2 -> {
            return livingEntity2 == livingEntity;
        }).isPresent();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(AbstractPiglin abstractPiglin) {
        Optional<LivingEntity> livingEntityFromUUIDMemory = BehaviorUtils.getLivingEntityFromUUIDMemory(abstractPiglin, MemoryModuleType.ANGRY_AT);
        if (livingEntityFromUUIDMemory.isPresent() && isAttackAllowed(livingEntityFromUUIDMemory.get())) {
            return livingEntityFromUUIDMemory;
        }
        Optional<? extends LivingEntity> targetIfWithinRange = getTargetIfWithinRange(abstractPiglin, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
        if (targetIfWithinRange.isPresent()) {
            return targetIfWithinRange;
        }
        return abstractPiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
    }

    private static boolean isAttackAllowed(LivingEntity livingEntity) {
        return EntitySelector.ATTACK_ALLOWED.test(livingEntity);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static Optional<? extends LivingEntity> getTargetIfWithinRange(AbstractPiglin abstractPiglin, MemoryModuleType<? extends LivingEntity> memoryModuleType) {
        return abstractPiglin.getBrain().getMemory(memoryModuleType).filter(livingEntity -> {
            return livingEntity.closerThan(abstractPiglin, 12.0d);
        });
    }

    protected static void wasHurtBy(PiglinBrute piglinBrute, LivingEntity livingEntity) {
        if (livingEntity instanceof AbstractPiglin) {
            return;
        }
        PiglinAi.maybeRetaliate(piglinBrute, livingEntity);
    }

    protected static void maybePlayActivitySound(PiglinBrute piglinBrute) {
        if (piglinBrute.level.random.nextFloat() < 0.0125d) {
            playActivitySound(piglinBrute);
        }
    }

    private static void playActivitySound(PiglinBrute piglinBrute) {
        piglinBrute.getBrain().getActiveNonCoreActivity().ifPresent(activity -> {
            if (activity == Activity.FIGHT) {
                piglinBrute.playAngrySound();
            }
        });
    }
}
