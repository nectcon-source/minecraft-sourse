package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IntRange;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/piglin/PiglinAi.class */
public class PiglinAi {
    public static final Item BARTERING_ITEM = Items.GOLD_INGOT;
    private static final IntRange TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
    private static final IntRange RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
    private static final IntRange RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
    private static final IntRange RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final IntRange AVOID_ZOMBIFIED_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final IntRange BABY_AVOID_NEMESIS_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final Set<Item> FOOD_ITEMS = ImmutableSet.of(Items.PORKCHOP, Items.COOKED_PORKCHOP);

    protected static Brain<?> makeBrain(Piglin piglin, Brain<Piglin> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initAdmireItemActivity(brain);
        initFightActivity(piglin, brain);
        initCelebrateActivity(brain);
        initRetreatActivity(brain);
        initRideHoglinActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    protected static void initMemories(Piglin piglin) {
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, TIME_BETWEEN_HUNTS.randomValue(piglin.level.random));
    }

    private static void initCoreActivity(Brain<Piglin> brain) {
        brain.addActivity(Activity.CORE, 0, (ImmutableList<? extends Behavior<? super Piglin>>) ImmutableList.<Behavior<? super Piglin>>of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), new InteractWithDoor(), babyAvoidNemesis(), avoidZombified(), new StopHoldingItemIfNoLongerAdmiring(), new StartAdmiringItemIfSeen(120), new StartCelebratingIfTargetDead(300, PiglinAi::wantsToDance), new StopBeingAngryIfTargetDead()));
    }

    private static void initIdleActivity(Brain<Piglin> brain) {
        brain.addActivity(
                Activity.IDLE,
                10,
                ImmutableList.of(
                        new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
                        new StartAttacking<>(AbstractPiglin::isAdult, PiglinAi::findNearestValidAttackTarget),
                        new RunIf<>(Piglin::canHunt, new StartHuntingHoglin<>()),
                        avoidRepellent(),
                        babySometimesRideBabyHoglin(),
                        createIdleLookBehaviors(),
                        createIdleMovementBehaviors(),
                        new SetLookAndInteract(EntityType.PLAYER, 4)
                )
        );
    }

    private static void initFightActivity(Piglin piglin, Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(
                Activity.FIGHT,
                10,
                (ImmutableList<? extends Behavior<? super Piglin>>) ImmutableList.<Behavior<? super Piglin>>of(
                        new StopAttackingIfTargetInvalid(var1x -> !isNearestValidAttackTarget(piglin, (LivingEntity) var1x)),
                        new RunIf<>(PiglinAi::hasCrossbow, new BackUpIfTooClose<>(5, 0.75F)),
                        new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F),
                        new MeleeAttack(20),
                        new CrossbowAttack(),
                        new RememberIfHoglinWasKilled(),
                        new EraseMemoryIf<>(PiglinAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)
                ),
                MemoryModuleType.ATTACK_TARGET
        );
    }

    private static void initCelebrateActivity(Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(
                Activity.CELEBRATE,
                10,
                (ImmutableList<? extends Behavior<? super Piglin>>) ImmutableList.<Behavior<? super Piglin>>of(
                        avoidRepellent(),
                        new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
                        new StartAttacking<>(AbstractPiglin::isAdult, PiglinAi::findNearestValidAttackTarget),
                        new RunIf<>(var0x -> !var0x.isDancing(), new GoToCelebrateLocation<>(2, 1.0F)),
                        new RunIf<>(Piglin::isDancing, new GoToCelebrateLocation<>(4, 0.6F)),
                        new RunOne(
                                ImmutableList.of(
                                        Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0F), 1), Pair.of(new RandomStroll(0.6F, 2, 1), 1), Pair.of(new DoNothing(10, 20), 1)
                                )
                        )
                ),
                MemoryModuleType.CELEBRATE_LOCATION
        );
    }

    private static void initAdmireItemActivity(Brain<Piglin> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(Activity.ADMIRE_ITEM, 10, ImmutableList.of(new GoToWantedItem(PiglinAi::isNotHoldingLovedItemInOffHand, 1.0f, true, 9), new StopAdmiringIfItemTooFarAway(9), new StopAdmiringIfTiredOfTryingToReachItem(200, 200)), MemoryModuleType.ADMIRING_ITEM);
        brain.addActivityAndRemoveMemoryWhenStopped(
                Activity.ADMIRE_ITEM,
                10,
                 ImmutableList.<Behavior<? super Piglin>>of(
                        new GoToWantedItem<>(PiglinAi::isNotHoldingLovedItemInOffHand, 1.0F, true, 9),
                        new StopAdmiringIfItemTooFarAway(9),
                        new StopAdmiringIfTiredOfTryingToReachItem(200, 200)
                ),
                MemoryModuleType.ADMIRING_ITEM
        );
    }

    private static void initRetreatActivity(Brain<Piglin> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true), createIdleLookBehaviors(), createIdleMovementBehaviors(), new EraseMemoryIf(PiglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
    brain.addActivityAndRemoveMemoryWhenStopped(
            Activity.AVOID,
            10,
            ImmutableList.of(
                    SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true),
                    createIdleLookBehaviors(),
                    createIdleMovementBehaviors(),
                    new EraseMemoryIf<>(PiglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
            ),
            MemoryModuleType.AVOID_TARGET
    );
    }

    private static void initRideHoglinActivity(Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(
                Activity.RIDE,
                10,
                ImmutableList.<Behavior<? super Piglin>>of(
                        new Mount(0.8F),
                        new SetEntityLookTarget(PiglinAi::isPlayerHoldingLovedItem, 8.0F),
                        new RunIf<>(Entity::isPassenger, createIdleLookBehaviors()),
                        new DismountOrSkipMounting<>(8, PiglinAi::wantsToStopRiding)
                ),
                MemoryModuleType.RIDE_TARGET
        );
    }

    private static RunOne<Piglin> createIdleLookBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0f), 1), Pair.of(new SetEntityLookTarget(EntityType.PIGLIN, 8.0f), 1), Pair.of(new SetEntityLookTarget(8.0f), 1), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<Piglin> createIdleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(0.6f), 2), Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), 2), Pair.of(new RunIf((v0) -> {
            return doesntSeeAnyPlayerHoldingLovedItem((LivingEntity) v0);
        }, new SetWalkTargetFromLookTarget(0.6f, 3)), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static SetWalkTargetAwayFrom<BlockPos> avoidRepellent() {
        return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0f, 8, false);
    }

    private static CopyMemoryWithExpiry<Piglin, LivingEntity> babyAvoidNemesis() {
        return new CopyMemoryWithExpiry<>((v0) -> {
            return v0.isBaby();
        }, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
    }

    private static CopyMemoryWithExpiry<Piglin, LivingEntity> avoidZombified() {
        return new CopyMemoryWithExpiry<>(PiglinAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION);
    }

    protected static void updateActivity(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        Activity orElse = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
        if (orElse != brain.getActiveNonCoreActivity().orElse(null)) {
            Optional<SoundEvent> soundForCurrentActivity = getSoundForCurrentActivity(piglin);
            piglin.getClass();
            soundForCurrentActivity.ifPresent(piglin::playSound);
        }
        piglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && isBabyRidingBaby(piglin)) {
            piglin.stopRiding();
        }
        if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
            brain.eraseMemory(MemoryModuleType.DANCING);
        }
        piglin.setDancing(brain.hasMemoryValue(MemoryModuleType.DANCING));
    }

    private static boolean isBabyRidingBaby(Piglin piglin) {
        if (!piglin.isBaby()) {
            return false;
        }
        Entity vehicle = piglin.getVehicle();
        return ((vehicle instanceof Piglin) && ((Piglin) vehicle).isBaby()) || ((vehicle instanceof Hoglin) && ((Hoglin) vehicle).isBaby());
    }

    protected static void pickUpItem(Piglin piglin, ItemEntity itemEntity) {
        ItemStack removeOneItemFromItemEntity;
        stopWalking(piglin);
        if (itemEntity.getItem().getItem() == Items.GOLD_NUGGET) {
            piglin.take(itemEntity, itemEntity.getItem().getCount());
            removeOneItemFromItemEntity = itemEntity.getItem();
            itemEntity.remove();
        } else {
            piglin.take(itemEntity, 1);
            removeOneItemFromItemEntity = removeOneItemFromItemEntity(itemEntity);
        }
        Item item = removeOneItemFromItemEntity.getItem();
        if (isLovedItem(item)) {
            piglin.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            holdInOffhand(piglin, removeOneItemFromItemEntity);
            admireGoldItem(piglin);
        } else if (isFood(item) && !hasEatenRecently(piglin)) {
            eat(piglin);
        } else {
            if (piglin.equipItemIfPossible(removeOneItemFromItemEntity)) {
                return;
            }
            putInInventory(piglin, removeOneItemFromItemEntity);
        }
    }

    private static void holdInOffhand(Piglin piglin, ItemStack itemStack) {
        if (isHoldingItemInOffHand(piglin)) {
            piglin.spawnAtLocation(piglin.getItemInHand(InteractionHand.OFF_HAND));
        }
        piglin.holdInOffHand(itemStack);
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        ItemStack split = item.split(1);
        if (item.isEmpty()) {
            itemEntity.remove();
        } else {
            itemEntity.setItem(item);
        }
        return split;
    }

    protected static void stopHoldingOffHandItem(Piglin piglin, boolean z) {
        ItemStack itemInHand = piglin.getItemInHand(InteractionHand.OFF_HAND);
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (piglin.isAdult()) {
            boolean isBarterCurrency = isBarterCurrency(itemInHand.getItem());
            if (z && isBarterCurrency) {
                throwItems(piglin, getBarterResponseItems(piglin));
                return;
            } else {
                if (!isBarterCurrency && !piglin.equipItemIfPossible(itemInHand)) {
                    putInInventory(piglin, itemInHand);
                    return;
                }
                return;
            }
        }
        if (!piglin.equipItemIfPossible(itemInHand)) {
            ItemStack mainHandItem = piglin.getMainHandItem();
            if (isLovedItem(mainHandItem.getItem())) {
                putInInventory(piglin, mainHandItem);
            } else {
                throwItems(piglin, Collections.singletonList(mainHandItem));
            }
            piglin.holdInMainHand(itemInHand);
        }
    }

    protected static void cancelAdmiring(Piglin piglin) {
        if (isAdmiringItem(piglin) && !piglin.getOffhandItem().isEmpty()) {
            piglin.spawnAtLocation(piglin.getOffhandItem());
            piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    private static void putInInventory(Piglin piglin, ItemStack itemStack) {
        throwItemsTowardRandomPos(piglin, Collections.singletonList(piglin.addToInventory(itemStack)));
    }

    private static void throwItems(Piglin piglin, List<ItemStack> list) {
        Optional<Player> var2 = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (var2.isPresent()) {
            throwItemsTowardPlayer(piglin, (Player)var2.get(), list);
        } else {
            throwItemsTowardRandomPos(piglin, list);
        }
    }

    private static void throwItemsTowardRandomPos(Piglin piglin, List<ItemStack> list) {
        throwItemsTowardPos(piglin, list, getRandomNearbyPos(piglin));
    }

    private static void throwItemsTowardPlayer(Piglin piglin, Player player, List<ItemStack> list) {
        throwItemsTowardPos(piglin, list, player.position());
    }

    private static void throwItemsTowardPos(Piglin piglin, List<ItemStack> list, Vec3 vec3) {
        if (!list.isEmpty()) {
            piglin.swing(InteractionHand.OFF_HAND);
            Iterator<ItemStack> it = list.iterator();
            while (it.hasNext()) {
                BehaviorUtils.throwItem(piglin, it.next(), vec3.add(0.0d, 1.0d, 0.0d));
            }
        }
    }

    private static List<ItemStack> getBarterResponseItems(Piglin piglin) {
        return piglin.level.getServer().getLootTables().get(BuiltInLootTables.PIGLIN_BARTERING).getRandomItems(new LootContext.Builder((ServerLevel) piglin.level).withParameter(LootContextParams.THIS_ENTITY, piglin).withRandom(piglin.level.random).create(LootContextParamSets.PIGLIN_BARTER));
    }

    private static boolean wantsToDance(LivingEntity livingEntity, LivingEntity livingEntity2) {
        return livingEntity2.getType() == EntityType.HOGLIN && new Random(livingEntity.level.getGameTime()).nextFloat() < 0.1f;
    }

    protected static boolean wantsToPickup(Piglin piglin, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item.is(ItemTags.PIGLIN_REPELLENTS)) {
            return false;
        }
        if (isAdmiringDisabled(piglin) && piglin.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        }
        if (isBarterCurrency(item)) {
            return isNotHoldingLovedItemInOffHand(piglin);
        }
        boolean canAddToInventory = piglin.canAddToInventory(itemStack);
        if (item == Items.GOLD_NUGGET) {
            return canAddToInventory;
        }
        if (isFood(item)) {
            return !hasEatenRecently(piglin) && canAddToInventory;
        }
        if (isLovedItem(item)) {
            return isNotHoldingLovedItemInOffHand(piglin) && canAddToInventory;
        }
        return piglin.canReplaceCurrentItem(itemStack);
    }

    protected static boolean isLovedItem(Item item) {
        return item.is(ItemTags.PIGLIN_LOVED);
    }

    private static boolean wantsToStopRiding(Piglin piglin, Entity entity) {
        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;
            return !mob.isBaby() || !mob.isAlive() || wasHurtRecently(piglin) || wasHurtRecently(mob) || ((mob instanceof Piglin) && mob.getVehicle() == null);
        }
        return false;
    }

    private static boolean isNearestValidAttackTarget(Piglin piglin, LivingEntity livingEntity) {
        return findNearestValidAttackTarget(piglin).filter(livingEntity2 -> {
            return livingEntity2 == livingEntity;
        }).isPresent();
    }

    private static boolean isNearZombified(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
            return piglin.closerThan((LivingEntity) brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get(), 6.0d);
        }
        return false;
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        if (isNearZombified(piglin)) {
            return Optional.empty();
        }
        Optional<LivingEntity> livingEntityFromUUIDMemory = BehaviorUtils.getLivingEntityFromUUIDMemory(piglin, MemoryModuleType.ANGRY_AT);
        if (livingEntityFromUUIDMemory.isPresent() && isAttackAllowed(livingEntityFromUUIDMemory.get())) {
            return livingEntityFromUUIDMemory;
        }
        if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
            Optional memory = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
            if (memory.isPresent()) {
                return memory;
            }
        }
        Optional memory2 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
        if (memory2.isPresent()) {
            return memory2;
        }
        Optional memory3 = brain.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
        if (memory3.isPresent() && isAttackAllowed((LivingEntity) memory3.get())) {
            return memory3;
        }
        return Optional.empty();
    }

    public static void angerNearbyPiglins(Player player, boolean z) {
        player.level.getEntitiesOfClass(Piglin.class, player.getBoundingBox().inflate(16.0d)).stream().filter((v0) -> {
            return isIdle(v0);
        }).filter(piglin -> {
            return !z || BehaviorUtils.canSee(piglin, player);
        }).forEach(piglin2 -> {
            if (piglin2.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                setAngerTargetToNearestTargetablePlayerIfFound(piglin2, player);
            } else {
                setAngerTarget(piglin2, player);
            }
        });
    }

    public static InteractionResult mobInteract(Piglin piglin, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (canAdmire(piglin, itemInHand)) {
            holdInOffhand(piglin, itemInHand.split(1));
            admireGoldItem(piglin);
            stopWalking(piglin);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    protected static boolean canAdmire(Piglin piglin, ItemStack itemStack) {
        return !isAdmiringDisabled(piglin) && !isAdmiringItem(piglin) && piglin.isAdult() && isBarterCurrency(itemStack.getItem());
    }

    protected static void wasHurtBy(Piglin piglin, LivingEntity livingEntity) {
        if (livingEntity instanceof Piglin) {
            return;
        }
        if (isHoldingItemInOffHand(piglin)) {
            stopHoldingOffHandItem(piglin, false);
        }
        Brain<Piglin> brain = piglin.getBrain();
        brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
        brain.eraseMemory(MemoryModuleType.DANCING);
        brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
        if (livingEntity instanceof Player) {
            brain.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
        }
        getAvoidTarget(piglin).ifPresent(livingEntity2 -> {
            if (livingEntity2.getType() != livingEntity.getType()) {
                brain.eraseMemory(MemoryModuleType.AVOID_TARGET);
            }
        });
        if (piglin.isBaby()) {
            brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, 100L);
            if (isAttackAllowed(livingEntity)) {
                broadcastAngerTarget(piglin, livingEntity);
                return;
            }
            return;
        }
        if (livingEntity.getType() == EntityType.HOGLIN && hoglinsOutnumberPiglins(piglin)) {
            setAvoidTargetAndDontHuntForAWhile(piglin, livingEntity);
            broadcastRetreat(piglin, livingEntity);
        } else {
            maybeRetaliate(piglin, livingEntity);
        }
    }

    protected static void maybeRetaliate(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        if (abstractPiglin.getBrain().isActive(Activity.AVOID) || !isAttackAllowed(livingEntity) || BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(abstractPiglin, livingEntity, 4.0d)) {
            return;
        }
        if (livingEntity.getType() == EntityType.PLAYER && abstractPiglin.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            setAngerTargetToNearestTargetablePlayerIfFound(abstractPiglin, livingEntity);
            broadcastUniversalAnger(abstractPiglin);
        } else {
            setAngerTarget(abstractPiglin, livingEntity);
            broadcastAngerTarget(abstractPiglin, livingEntity);
        }
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Piglin piglin) {
        return piglin.getBrain().getActiveNonCoreActivity().map(activity -> {
            return getSoundForActivity(piglin, activity);
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static SoundEvent getSoundForActivity(Piglin piglin, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.PIGLIN_ANGRY;
        }
        if (piglin.isConverting()) {
            return SoundEvents.PIGLIN_RETREAT;
        }
        if (activity == Activity.AVOID && isNearAvoidTarget(piglin)) {
            return SoundEvents.PIGLIN_RETREAT;
        }
        if (activity == Activity.ADMIRE_ITEM) {
            return SoundEvents.PIGLIN_ADMIRING_ITEM;
        }
        if (activity == Activity.CELEBRATE) {
            return SoundEvents.PIGLIN_CELEBRATE;
        }
        if (seesPlayerHoldingLovedItem(piglin)) {
            return SoundEvents.PIGLIN_JEALOUS;
        }
        if (isNearRepellent(piglin)) {
            return SoundEvents.PIGLIN_RETREAT;
        }
        return SoundEvents.PIGLIN_AMBIENT;
    }

    private static boolean isNearAvoidTarget(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return false;
        }
        return ((LivingEntity) brain.getMemory(MemoryModuleType.AVOID_TARGET).get()).closerThan(piglin, 12.0d);
    }

    protected static boolean hasAnyoneNearbyHuntedRecently(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY) || getVisibleAdultPiglins(piglin).stream().anyMatch(abstractPiglin -> {
            return abstractPiglin.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY);
        });
    }

    private static List<AbstractPiglin> getVisibleAdultPiglins(Piglin piglin) {
        return  piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    private static List<AbstractPiglin> getAdultPiglins(AbstractPiglin abstractPiglin) {
        return  abstractPiglin.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    public static boolean isWearingGold(LivingEntity livingEntity) {
        Iterator<ItemStack> it = livingEntity.getArmorSlots().iterator();
        while (it.hasNext()) {
            Item item = it.next().getItem();
            if ((item instanceof ArmorItem) && ((ArmorItem) item).getMaterial() == ArmorMaterials.GOLD) {
                return true;
            }
        }
        return false;
    }

    private static void stopWalking(Piglin piglin) {
        piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        piglin.getNavigation().stop();
    }

    private static RunSometimes<Piglin> babySometimesRideBabyHoglin() {
        return new RunSometimes<>(
                new CopyMemoryWithExpiry<>(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION),
                RIDE_START_INTERVAL
        );
    }

    protected static void broadcastAngerTarget(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        getAdultPiglins(abstractPiglin).forEach(abstractPiglin2 -> {
            if (livingEntity.getType() == EntityType.HOGLIN && (!abstractPiglin2.canHunt() || !((Hoglin) livingEntity).canBeHunted())) {
                return;
            }
            setAngerTargetIfCloserThanCurrent(abstractPiglin2, livingEntity);
        });
    }

    protected static void broadcastUniversalAnger(AbstractPiglin abstractPiglin) {
        getAdultPiglins(abstractPiglin).forEach(abstractPiglin2 -> {
            getNearestVisibleTargetablePlayer(abstractPiglin2).ifPresent(player -> {
                setAngerTarget(abstractPiglin2, player);
            });
        });
    }

    protected static void broadcastDontKillAnyMoreHoglinsForAWhile(Piglin piglin) {
        getVisibleAdultPiglins(piglin).forEach(PiglinAi::dontKillAnyMoreHoglinsForAWhile);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static void setAngerTarget(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        if (!isAttackAllowed(livingEntity)) {
            return;
        }
        abstractPiglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, livingEntity.getUUID(), 600L);
        if (livingEntity.getType() == EntityType.HOGLIN && abstractPiglin.canHunt()) {
            dontKillAnyMoreHoglinsForAWhile(abstractPiglin);
        }
        if (livingEntity.getType() == EntityType.PLAYER && abstractPiglin.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
        }
    }

    private static void setAngerTargetToNearestTargetablePlayerIfFound(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        Optional<Player> nearestVisibleTargetablePlayer = getNearestVisibleTargetablePlayer(abstractPiglin);
        if (nearestVisibleTargetablePlayer.isPresent()) {
            setAngerTarget(abstractPiglin, nearestVisibleTargetablePlayer.get());
        } else {
            setAngerTarget(abstractPiglin, livingEntity);
        }
    }

    private static void setAngerTargetIfCloserThanCurrent(AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        Optional<LivingEntity> angerTarget = getAngerTarget(abstractPiglin);
        LivingEntity nearestTarget = BehaviorUtils.getNearestTarget(abstractPiglin, angerTarget, livingEntity);
        if (angerTarget.isPresent() && angerTarget.get() == nearestTarget) {
            return;
        }
        setAngerTarget(abstractPiglin, nearestTarget);
    }

    private static Optional<LivingEntity> getAngerTarget(AbstractPiglin abstractPiglin) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(abstractPiglin, MemoryModuleType.ANGRY_AT);
    }

    public static Optional<LivingEntity> getAvoidTarget(Piglin piglin) {
        if (piglin.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return piglin.getBrain().getMemory(MemoryModuleType.AVOID_TARGET);
        }
        return Optional.empty();
    }

    public static Optional<Player> getNearestVisibleTargetablePlayer(AbstractPiglin abstractPiglin) {
        if (abstractPiglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER)) {
            return abstractPiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
        }
        return Optional.empty();
    }

    private static void broadcastRetreat(Piglin piglin, LivingEntity livingEntity) {
        getVisibleAdultPiglins(piglin).stream().filter(abstractPiglin -> {
            return abstractPiglin instanceof Piglin;
        }).forEach(abstractPiglin2 -> {
            retreatFromNearestTarget((Piglin) abstractPiglin2, livingEntity);
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void retreatFromNearestTarget(Piglin piglin, LivingEntity livingEntity) {
        Brain<Piglin> brain = piglin.getBrain();
        setAvoidTargetAndDontHuntForAWhile(piglin, BehaviorUtils.getNearestTarget(piglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), BehaviorUtils.getNearestTarget(piglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), livingEntity)));
    }

    private static boolean wantsToStopFleeing(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        }
        LivingEntity livingEntity =  brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
        EntityType<?> type = livingEntity.getType();
        if (type == EntityType.HOGLIN) {
            return piglinsEqualOrOutnumberHoglins(piglin);
        }
        return isZombified(type) && !brain.isMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, livingEntity);
    }

    private static boolean piglinsEqualOrOutnumberHoglins(Piglin piglin) {
        return !hoglinsOutnumberPiglins(piglin);
    }

    private static boolean hoglinsOutnumberPiglins(Piglin piglin) {
        return  piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0).intValue() > ((Integer) piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0)).intValue() + 1;
    }

    private static void setAvoidTargetAndDontHuntForAWhile(Piglin piglin, LivingEntity livingEntity) {
        piglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, RETREAT_DURATION.randomValue(piglin.level.random));
        dontKillAnyMoreHoglinsForAWhile(piglin);
    }

    protected static void dontKillAnyMoreHoglinsForAWhile(AbstractPiglin abstractPiglin) {
        abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, TIME_BETWEEN_HUNTS.randomValue(abstractPiglin.level.random));
    }

    private static void eat(Piglin piglin) {
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    private static Vec3 getRandomNearbyPos(Piglin piglin) {
        Vec3 landPos = RandomPos.getLandPos(piglin, 4, 2);
        return landPos == null ? piglin.position() : landPos;
    }

    private static boolean hasEatenRecently(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    protected static boolean isIdle(AbstractPiglin abstractPiglin) {
        return abstractPiglin.getBrain().isActive(Activity.IDLE);
    }

    private static boolean hasCrossbow(LivingEntity livingEntity) {
        return livingEntity.isHolding(Items.CROSSBOW);
    }

    private static void admireGoldItem(LivingEntity livingEntity) {
        livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 120L);
    }

    private static boolean isAdmiringItem(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
    }

    private static boolean isBarterCurrency(Item item) {
        return item == BARTERING_ITEM;
    }

    private static boolean isFood(Item item) {
        return FOOD_ITEMS.contains(item);
    }

    private static boolean isAttackAllowed(LivingEntity livingEntity) {
        return EntitySelector.ATTACK_ALLOWED.test(livingEntity);
    }

    private static boolean isNearRepellent(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean seesPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return !seesPlayerHoldingLovedItem(livingEntity);
    }

    public static boolean isPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return livingEntity.getType() == EntityType.PLAYER && livingEntity.isHolding(PiglinAi::isLovedItem);
    }

    private static boolean isAdmiringDisabled(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
    }

    private static boolean wasHurtRecently(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    private static boolean isHoldingItemInOffHand(Piglin piglin) {
        return !piglin.getOffhandItem().isEmpty();
    }

    private static boolean isNotHoldingLovedItemInOffHand(Piglin piglin) {
        return piglin.getOffhandItem().isEmpty() || !isLovedItem(piglin.getOffhandItem().getItem());
    }

    public static boolean isZombified(EntityType entityType) {
        return entityType == EntityType.ZOMBIFIED_PIGLIN || entityType == EntityType.ZOGLIN;
    }
}
