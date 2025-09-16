package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.GolemSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/npc/Villager.class */
public class Villager extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder {
    private int updateMerchantTimer;
    private boolean increaseProfessionLevelOnUpdate;

    @Nullable
    private Player lastTradedPlayer;
    private byte foodLevel;
    private final GossipContainer gossips;
    private long lastGossipTime;
    private long lastGossipDecayTime;
    private int villagerXp;
    private long lastRestockGameTime;
    private int numberOfRestocksToday;
    private long lastRestockCheckDayTime;
    private boolean assignProfessionWhenSpawned;
    private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.VILLAGER_DATA);
    public static final Map<Item, Integer> FOOD_POINTS = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
    private static final Set<Item> WANTED_ITEMS = ImmutableSet.of(Items.BREAD, Items.POTATO, Items.CARROT, Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT, new Item[]{Items.BEETROOT_SEEDS});
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT, MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.WALK_TARGET, new MemoryModuleType[]{MemoryModuleType.LOOK_TARGET, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.BREED_TARGET, MemoryModuleType.PATH, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleType.HIDING_PLACE, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_SLEPT, MemoryModuleType.LAST_WOKEN, MemoryModuleType.LAST_WORKED_AT_POI, MemoryModuleType.GOLEM_DETECTED_RECENTLY});
    private static final ImmutableList<SensorType<? extends Sensor<? super Villager>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_BED, SensorType.HURT_BY, SensorType.VILLAGER_HOSTILES, SensorType.VILLAGER_BABIES, SensorType.SECONDARY_POIS, SensorType.GOLEM_DETECTED);
    public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<Villager, PoiType>> POI_MEMORIES = ImmutableMap.of(MemoryModuleType.HOME, (villager, poiType) -> {
        return poiType == PoiType.HOME;
    }, MemoryModuleType.JOB_SITE, (villager2, poiType2) -> {
        return villager2.getVillagerData().getProfession().getJobPoiType() == poiType2;
    }, MemoryModuleType.POTENTIAL_JOB_SITE, (villager3, poiType3) -> {
        return PoiType.ALL_JOBS.test(poiType3);
    }, MemoryModuleType.MEETING_POINT, (villager4, poiType4) -> {
        return poiType4 == PoiType.MEETING;
    });

    public Villager(EntityType<? extends Villager> entityType, Level level) {
        this(entityType, level, VillagerType.PLAINS);
    }

    public Villager(EntityType<? extends Villager> entityType, Level level, VillagerType villagerType) {
        super(entityType, level);
        this.gossips = new GossipContainer();
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        getNavigation().setCanFloat(true);
        setCanPickUpLoot(true);
        setVillagerData(getVillagerData().setType(villagerType).setProfession(VillagerProfession.NONE));
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public Brain<Villager> getBrain() {
        return (Brain<Villager>) super.getBrain();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected Brain.Provider<Villager> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<Villager> makeBrain = brainProvider().makeBrain(dynamic);
        registerBrainGoals(makeBrain);
        return makeBrain;
    }

    public void refreshBrain(ServerLevel serverLevel) {
        Brain<Villager> brain = getBrain();
        brain.stopAll(serverLevel, this);
        this.brain = brain.copyWithoutBehaviors();
        registerBrainGoals(getBrain());
    }

    private void registerBrainGoals(Brain<Villager> brain) {
        VillagerProfession profession = getVillagerData().getProfession();
        if (isBaby()) {
            brain.setSchedule(Schedule.VILLAGER_BABY);
            brain.addActivity(Activity.PLAY, VillagerGoalPackages.getPlayPackage(0.5f));
        } else {
            brain.setSchedule(Schedule.VILLAGER_DEFAULT);
            brain.addActivityWithConditions(Activity.WORK, VillagerGoalPackages.getWorkPackage(profession, 0.5f), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT)));
        }
        brain.addActivity(Activity.CORE, VillagerGoalPackages.getCorePackage(profession, 0.5f));
        brain.addActivityWithConditions(Activity.MEET, VillagerGoalPackages.getMeetPackage(profession, 0.5f), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT)));
        brain.addActivity(Activity.REST, VillagerGoalPackages.getRestPackage(profession, 0.5f));
        brain.addActivity(Activity.IDLE, VillagerGoalPackages.getIdlePackage(profession, 0.5f));
        brain.addActivity(Activity.PANIC, VillagerGoalPackages.getPanicPackage(profession, 0.5f));
        brain.addActivity(Activity.PRE_RAID, VillagerGoalPackages.getPreRaidPackage(profession, 0.5f));
        brain.addActivity(Activity.RAID, VillagerGoalPackages.getRaidPackage(profession, 0.5f));
        brain.addActivity(Activity.HIDE, VillagerGoalPackages.getHidePackage(profession, 0.5f));
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setActiveActivityIfPossible(Activity.IDLE);
        brain.updateActivityFromSchedule(this.level.getDayTime(), this.level.getGameTime());
    }

    @Override // net.minecraft.world.entity.AgableMob
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        if (this.level instanceof ServerLevel) {
            refreshBrain((ServerLevel) this.level);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5d).add(Attributes.FOLLOW_RANGE, 48.0d);
    }

    public boolean assignProfessionWhenSpawned() {
        return this.assignProfessionWhenSpawned;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        Raid raidAt;
        this.level.getProfiler().push("villagerBrain");
        getBrain().tick((ServerLevel) this.level, this);
        this.level.getProfiler().pop();
        if (this.assignProfessionWhenSpawned) {
            this.assignProfessionWhenSpawned = false;
        }
        if (!isTrading() && this.updateMerchantTimer > 0) {
            this.updateMerchantTimer--;
            if (this.updateMerchantTimer <= 0) {
                if (this.increaseProfessionLevelOnUpdate) {
                    increaseMerchantCareer();
                    this.increaseProfessionLevelOnUpdate = false;
                }
                addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            }
        }
        if (this.lastTradedPlayer != null && (this.level instanceof ServerLevel)) {
            ((ServerLevel) this.level).onReputationEvent(ReputationEventType.TRADE, this.lastTradedPlayer, this);
            this.level.broadcastEntityEvent(this, (byte) 14);
            this.lastTradedPlayer = null;
        }
        if (!isNoAi() && this.random.nextInt(100) == 0 && (raidAt = ((ServerLevel) this.level).getRaidAt(blockPosition())) != null && raidAt.isActive() && !raidAt.isOver()) {
            this.level.broadcastEntityEvent(this, (byte) 42);
        }
        if (getVillagerData().getProfession() == VillagerProfession.NONE && isTrading()) {
            stopTrading();
        }
        super.customServerAiStep();
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (getUnhappyCounter() > 0) {
            setUnhappyCounter(getUnhappyCounter() - 1);
        }
        maybeDecayGossip();
    }

    @Override // net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (player.getItemInHand(interactionHand).getItem() != Items.VILLAGER_SPAWN_EGG && isAlive() && !isTrading() && !isSleeping()) {
            if (isBaby()) {
                setUnhappy();
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            boolean isEmpty = getOffers().isEmpty();
            if (interactionHand == InteractionHand.MAIN_HAND) {
                if (isEmpty && !this.level.isClientSide) {
                    setUnhappy();
                }
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (isEmpty) {
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            if (!this.level.isClientSide && !this.offers.isEmpty()) {
                startTrading(player);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(player, interactionHand);
    }

    private void setUnhappy() {
        setUnhappyCounter(40);
        if (!this.level.isClientSide()) {
            playSound(SoundEvents.VILLAGER_NO, getSoundVolume(), getVoicePitch());
        }
    }

    private void startTrading(Player player) {
        updateSpecialPrices(player);
        setTradingPlayer(player);
        openTradingScreen(player, getDisplayName(), getVillagerData().getLevel());
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.item.trading.Merchant
    public void setTradingPlayer(@Nullable Player player) {
        boolean z = getTradingPlayer() != null && player == null;
        super.setTradingPlayer(player);
        if (z) {
            stopTrading();
        }
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager
    protected void stopTrading() {
        super.stopTrading();
        resetSpecialPrices();
    }

    private void resetSpecialPrices() {
        Iterator<MerchantOffer> it = getOffers().iterator();
        while (it.hasNext()) {
            it.next().resetSpecialPriceDiff();
        }
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public boolean canRestock() {
        return true;
    }

    public void restock() {
        updateDemand();
        Iterator<MerchantOffer> it = getOffers().iterator();
        while (it.hasNext()) {
            it.next().resetUses();
        }
        this.lastRestockGameTime = this.level.getGameTime();
        this.numberOfRestocksToday++;
    }

    private boolean needsToRestock() {
        Iterator<MerchantOffer> it = getOffers().iterator();
        while (it.hasNext()) {
            if (it.next().needsRestock()) {
                return true;
            }
        }
        return false;
    }

    private boolean allowedToRestock() {
        return this.numberOfRestocksToday == 0 || (this.numberOfRestocksToday < 2 && this.level.getGameTime() > this.lastRestockGameTime + 2400);
    }

    public boolean shouldRestock() {
        long j = this.lastRestockGameTime + 12000;
        long gameTime = this.level.getGameTime();
        boolean z = gameTime > j;
        long dayTime = this.level.getDayTime();
        if (this.lastRestockCheckDayTime > 0) {
            z |= dayTime / 24000 > this.lastRestockCheckDayTime / 24000;
        }
        this.lastRestockCheckDayTime = dayTime;
        if (z) {
            this.lastRestockGameTime = gameTime;
            resetNumberOfRestocks();
        }
        return allowedToRestock() && needsToRestock();
    }

    private void catchUpDemand() {
        int i = 2 - this.numberOfRestocksToday;
        if (i > 0) {
            Iterator<MerchantOffer> it = getOffers().iterator();
            while (it.hasNext()) {
                it.next().resetUses();
            }
        }
        for (int i2 = 0; i2 < i; i2++) {
            updateDemand();
        }
    }

    private void updateDemand() {
        Iterator<MerchantOffer> it = getOffers().iterator();
        while (it.hasNext()) {
            it.next().updateDemand();
        }
    }

    private void updateSpecialPrices(Player player) {
        int var2 = this.getPlayerReputation(player);
        if (var2 != 0) {
            for(MerchantOffer var4 : this.getOffers()) {
                var4.addToSpecialPriceDiff(-Mth.floor((float)var2 * var4.getPriceMultiplier()));
            }
        }

        if (player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) {
            MobEffectInstance var10 = player.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
            int var11 = var10.getAmplifier();

            for(MerchantOffer var6 : this.getOffers()) {
                double var7 = 0.3 + (double)0.0625F * (double)var11;
                int var9 = (int)Math.floor(var7 * (double)var6.getBaseCostA().getCount());
                var6.addToSpecialPriceDiff(-Math.max(var9, 1));
            }
        }
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        DataResult encodeStart = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, getVillagerData());
        Logger logger = LOGGER;
        logger.getClass();
        encodeStart.resultOrPartial(logger::error).ifPresent(tag -> {
            compoundTag.put("VillagerData", (Tag) tag);
        });
        compoundTag.putByte("FoodLevel", this.foodLevel);
        compoundTag.put("Gossips", (Tag) this.gossips.store(NbtOps.INSTANCE).getValue());
        compoundTag.putInt("Xp", this.villagerXp);
        compoundTag.putLong("LastRestock", this.lastRestockGameTime);
        compoundTag.putLong("LastGossipDecay", this.lastGossipDecayTime);
        compoundTag.putInt("RestocksToday", this.numberOfRestocksToday);
        if (this.assignProfessionWhenSpawned) {
            compoundTag.putBoolean("AssignProfessionWhenSpawned", true);
        }
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("VillagerData", 10)) {
            DataResult<VillagerData> parse = VillagerData.CODEC.parse(new Dynamic(NbtOps.INSTANCE, compoundTag.get("VillagerData")));
            Logger logger = LOGGER;
            logger.getClass();
            parse.resultOrPartial(logger::error).ifPresent(this::setVillagerData);
        }
        if (compoundTag.contains("Offers", 10)) {
            this.offers = new MerchantOffers(compoundTag.getCompound("Offers"));
        }
        if (compoundTag.contains("FoodLevel", 1)) {
            this.foodLevel = compoundTag.getByte("FoodLevel");
        }
        this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, compoundTag.getList("Gossips", 10)));
        if (compoundTag.contains("Xp", 3)) {
            this.villagerXp = compoundTag.getInt("Xp");
        }
        this.lastRestockGameTime = compoundTag.getLong("LastRestock");
        this.lastGossipDecayTime = compoundTag.getLong("LastGossipDecay");
        setCanPickUpLoot(true);
        if (this.level instanceof ServerLevel) {
            refreshBrain((ServerLevel) this.level);
        }
        this.numberOfRestocksToday = compoundTag.getInt("RestocksToday");
        if (compoundTag.contains("AssignProfessionWhenSpawned")) {
            this.assignProfessionWhenSpawned = compoundTag.getBoolean("AssignProfessionWhenSpawned");
        }
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (isSleeping()) {
            return null;
        }
        if (isTrading()) {
            return SoundEvents.VILLAGER_TRADE;
        }
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    public void playWorkSound() {
        SoundEvent workSound = getVillagerData().getProfession().getWorkSound();
        if (workSound != null) {
            playSound(workSound, getSoundVolume(), getVoicePitch());
        }
    }

    public void setVillagerData(VillagerData villagerData) {
        if (getVillagerData().getProfession() != villagerData.getProfession()) {
            this.offers = null;
        }
        this.entityData.set(DATA_VILLAGER_DATA, villagerData);
    }

    @Override // net.minecraft.world.entity.npc.VillagerDataHolder
    public VillagerData getVillagerData() {
        return (VillagerData) this.entityData.get(DATA_VILLAGER_DATA);
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager
    protected void rewardTradeXp(MerchantOffer merchantOffer) {
        int nextInt = 3 + this.random.nextInt(4);
        this.villagerXp += merchantOffer.getXp();
        this.lastTradedPlayer = getTradingPlayer();
        if (shouldIncreaseLevel()) {
            this.updateMerchantTimer = 40;
            this.increaseProfessionLevelOnUpdate = true;
            nextInt += 5;
        }
        if (merchantOffer.shouldRewardExp()) {
            this.level.addFreshEntity(new ExperienceOrb(this.level, getX(), getY() + 0.5d, getZ(), nextInt));
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void setLastHurtByMob(@Nullable LivingEntity livingEntity) {
        if (livingEntity != null && (this.level instanceof ServerLevel)) {
            ((ServerLevel) this.level).onReputationEvent(ReputationEventType.VILLAGER_HURT, livingEntity, this);
            if (isAlive() && (livingEntity instanceof Player)) {
                this.level.broadcastEntityEvent(this, (byte) 13);
            }
        }
        super.setLastHurtByMob(livingEntity);
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.entity.LivingEntity
    public void die(DamageSource damageSource) {
        LOGGER.info("Villager {} died, message: '{}'", this, damageSource.getLocalizedDeathMessage(this).getString());
        Entity entity = damageSource.getEntity();
        if (entity != null) {
            tellWitnessesThatIWasMurdered(entity);
        }
        releaseAllPois();
        super.die(damageSource);
    }

    private void releaseAllPois() {
        releasePoi(MemoryModuleType.HOME);
        releasePoi(MemoryModuleType.JOB_SITE);
        releasePoi(MemoryModuleType.POTENTIAL_JOB_SITE);
        releasePoi(MemoryModuleType.MEETING_POINT);
    }

    private void tellWitnessesThatIWasMurdered(Entity entity) {
        if (this.level instanceof ServerLevel) {
            Optional<List<LivingEntity>> var2 = this.brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
            if (var2.isPresent()) {
                ServerLevel var3 = (ServerLevel)this.level;
                var2.get().stream().filter((var0) -> var0 instanceof ReputationEventHandler).forEach((var2x) -> var3.onReputationEvent(ReputationEventType.VILLAGER_KILLED, entity, (ReputationEventHandler)var2x));
            }
        }
    }

    public void releasePoi(MemoryModuleType<GlobalPos> memoryModuleType) {
        if (!(this.level instanceof ServerLevel)) {
            return;
        }
        MinecraftServer server = ((ServerLevel) this.level).getServer();
        this.brain.getMemory(memoryModuleType).ifPresent(globalPos -> {
            ServerLevel level = server.getLevel(globalPos.dimension());
            if (level == null) {
                return;
            }
            PoiManager poiManager = level.getPoiManager();
            Optional<PoiType> type = poiManager.getType(globalPos.pos());
            BiPredicate<Villager, PoiType> biPredicate = POI_MEMORIES.get(memoryModuleType);
            if (type.isPresent() && biPredicate.test(this, type.get())) {
                poiManager.release(globalPos.pos());
                DebugPackets.sendPoiTicketCountPacket(level, globalPos.pos());
            }
        });
    }

    @Override // net.minecraft.world.entity.AgableMob
    public boolean canBreed() {
        return this.foodLevel + countFoodPointsInInventory() >= 12 && getAge() == 0;
    }

    private boolean hungry() {
        return this.foodLevel < 12;
    }

    private void eatUntilFull() {
        Integer num;
        if (!hungry() || countFoodPointsInInventory() == 0) {
            return;
        }
        for (int i = 0; i < getInventory().getContainerSize(); i++) {
            ItemStack item = getInventory().getItem(i);
            if (!item.isEmpty() && (num = FOOD_POINTS.get(item.getItem())) != null) {
                for (int count = item.getCount(); count > 0; count--) {
                    this.foodLevel = (byte) (this.foodLevel + num.intValue());
                    getInventory().removeItem(i, 1);
                    if (!hungry()) {
                        return;
                    }
                }
            }
        }
    }

    public int getPlayerReputation(Player player) {
        return this.gossips.getReputation(player.getUUID(), gossipType -> {
            return true;
        });
    }

    private void digestFood(int i) {
        this.foodLevel = (byte) (this.foodLevel - i);
    }

    public void eatAndDigestFood() {
        eatUntilFull();
        digestFood(12);
    }

    public void setOffers(MerchantOffers merchantOffers) {
        this.offers = merchantOffers;
    }

    private boolean shouldIncreaseLevel() {
        int level = getVillagerData().getLevel();
        return VillagerData.canLevelUp(level) && this.villagerXp >= VillagerData.getMaxXpPerLevel(level);
    }

    private void increaseMerchantCareer() {
        setVillagerData(getVillagerData().setLevel(getVillagerData().getLevel() + 1));
        updateTrades();
    }

    @Override // net.minecraft.world.entity.Entity
    protected Component getTypeName() {
        return new TranslatableComponent(getType().getDescriptionId() + '.' + Registry.VILLAGER_PROFESSION.getKey(getVillagerData().getProfession()).getPath());
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 12) {
            addParticlesAroundSelf(ParticleTypes.HEART);
            return;
        }
        if (b == 13) {
            addParticlesAroundSelf(ParticleTypes.ANGRY_VILLAGER);
            return;
        }
        if (b == 14) {
            addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
        } else if (b == 42) {
            addParticlesAroundSelf(ParticleTypes.SPLASH);
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (mobSpawnType == MobSpawnType.BREEDING) {
            setVillagerData(getVillagerData().setProfession(VillagerProfession.NONE));
        }
        if (mobSpawnType == MobSpawnType.COMMAND || mobSpawnType == MobSpawnType.SPAWN_EGG || mobSpawnType == MobSpawnType.SPAWNER || mobSpawnType == MobSpawnType.DISPENSER) {
            setVillagerData(getVillagerData().setType(VillagerType.byBiome(serverLevelAccessor.getBiomeName(blockPosition()))));
        }
        if (mobSpawnType == MobSpawnType.STRUCTURE) {
            this.assignProfessionWhenSpawned = true;
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override // net.minecraft.world.entity.AgableMob
    public Villager getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        VillagerType type;
        double nextDouble = this.random.nextDouble();
        if (nextDouble < 0.5d) {
            type = VillagerType.byBiome(serverLevel.getBiomeName(blockPosition()));
        } else if (nextDouble < 0.75d) {
            type = getVillagerData().getType();
        } else {
            type = ((Villager) agableMob).getVillagerData().getType();
        }
        Villager villager = new Villager(EntityType.VILLAGER, serverLevel, type);
        villager.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(villager.blockPosition()), MobSpawnType.BREEDING, null, null);
        return villager;
    }

    @Override // net.minecraft.world.entity.Entity
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        if (serverLevel.getDifficulty() != Difficulty.PEACEFUL) {
            LOGGER.info("Villager {} was struck by lightning {}.", this, lightningBolt);
            Witch create = EntityType.WITCH.create(serverLevel);
            create.moveTo(getX(), getY(), getZ(), this.yRot, this.xRot);
            create.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(create.blockPosition()), MobSpawnType.CONVERSION, null, null);
            create.setNoAi(isNoAi());
            if (hasCustomName()) {
                create.setCustomName(getCustomName());
                create.setCustomNameVisible(isCustomNameVisible());
            }
            create.setPersistenceRequired();
            serverLevel.addFreshEntityWithPassengers(create);
            releaseAllPois();
            remove();
            return;
        }
        super.thunderHit(serverLevel, lightningBolt);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        if (wantsToPickUp(item)) {
            SimpleContainer inventory = getInventory();
            if (!inventory.canAddItem(item)) {
                return;
            }
            onItemPickup(itemEntity);
            take(itemEntity, item.getCount());
            ItemStack addItem = inventory.addItem(item);
            if (addItem.isEmpty()) {
                itemEntity.remove();
            } else {
                item.setCount(addItem.getCount());
            }
        }
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean wantsToPickUp(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return (WANTED_ITEMS.contains(item) || getVillagerData().getProfession().getRequestedItems().contains(item)) && getInventory().canAddItem(itemStack);
    }

    public boolean hasExcessFood() {
        return countFoodPointsInInventory() >= 24;
    }

    public boolean wantsMoreFood() {
        return countFoodPointsInInventory() < 12;
    }

    private int countFoodPointsInInventory() {
        SimpleContainer inventory = getInventory();
        return FOOD_POINTS.entrySet().stream().mapToInt(entry -> {
            return inventory.countItem((Item) entry.getKey()) * ((Integer) entry.getValue()).intValue();
        }).sum();
    }

    public boolean hasFarmSeeds() {
        return getInventory().hasAnyOf(ImmutableSet.of(Items.WHEAT_SEEDS, Items.POTATO, Items.CARROT, Items.BEETROOT_SEEDS));
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager
    protected void updateTrades() {
        VillagerTrades.ItemListing[] itemListingArr;
        VillagerData villagerData = getVillagerData();
        Int2ObjectMap<VillagerTrades.ItemListing[]> int2ObjectMap = VillagerTrades.TRADES.get(villagerData.getProfession());
        if (int2ObjectMap == null || int2ObjectMap.isEmpty() || (itemListingArr = (VillagerTrades.ItemListing[]) int2ObjectMap.get(villagerData.getLevel())) == null) {
            return;
        }
        addOffersFromItemListings(getOffers(), itemListingArr, 2);
    }

    public void gossip(ServerLevel serverLevel, Villager villager, long j) {
        if (j < this.lastGossipTime || j >= this.lastGossipTime + 1200) {
            if (j >= villager.lastGossipTime && j < villager.lastGossipTime + 1200) {
                return;
            }
            this.gossips.transferFrom(villager.gossips, this.random, 10);
            this.lastGossipTime = j;
            villager.lastGossipTime = j;
            spawnGolemIfNeeded(serverLevel, j, 5);
        }
    }

    private void maybeDecayGossip() {
        long gameTime = this.level.getGameTime();
        if (this.lastGossipDecayTime == 0) {
            this.lastGossipDecayTime = gameTime;
        } else {
            if (gameTime < this.lastGossipDecayTime + 24000) {
                return;
            }
            this.gossips.decay();
            this.lastGossipDecayTime = gameTime;
        }
    }

    public void spawnGolemIfNeeded(ServerLevel serverLevel, long j, int i) {
        if (!wantsToSpawnGolem(j)) {
            return;
        }
        List<Villager> entitiesOfClass = serverLevel.getEntitiesOfClass(Villager.class, getBoundingBox().inflate(10.0d, 10.0d, 10.0d));
        if (((List) entitiesOfClass.stream().filter(villager -> {
            return villager.wantsToSpawnGolem(j);
        }).limit(5L).collect(Collectors.toList())).size() < i || trySpawnGolem(serverLevel) == null) {
            return;
        }
        entitiesOfClass.forEach((v0) -> {
            GolemSensor.golemDetected(v0);
        });
    }

    public boolean wantsToSpawnGolem(long j) {
        if (!golemSpawnConditionsMet(this.level.getGameTime()) || this.brain.hasMemoryValue(MemoryModuleType.GOLEM_DETECTED_RECENTLY)) {
            return false;
        }
        return true;
    }

    @Nullable
    private IronGolem trySpawnGolem(ServerLevel serverLevel) {
        IronGolem create;
        BlockPos blockPosition = blockPosition();
        for (int i = 0; i < 10; i++) {
            BlockPos findSpawnPositionForGolemInColumn = findSpawnPositionForGolemInColumn(blockPosition, serverLevel.random.nextInt(16) - 8, serverLevel.random.nextInt(16) - 8);
            if (findSpawnPositionForGolemInColumn != null && (create = EntityType.IRON_GOLEM.create(serverLevel, null, null, null, findSpawnPositionForGolemInColumn, MobSpawnType.MOB_SUMMONED, false, false)) != null) {
                if (create.checkSpawnRules(serverLevel, MobSpawnType.MOB_SUMMONED) && create.checkSpawnObstruction(serverLevel)) {
                    serverLevel.addFreshEntityWithPassengers(create);
                    return create;
                }
                create.remove();
            }
        }
        return null;
    }

    @Nullable
    private BlockPos findSpawnPositionForGolemInColumn(BlockPos blockPos, double d, double d2) {
        BlockPos offset = blockPos.offset(d, 6.0d, d2);
        BlockState blockState = this.level.getBlockState(offset);
        for (int i = 6; i >= -6; i--) {
            BlockPos blockPos2 = offset;
            BlockState blockState2 = blockState;
            offset = blockPos2.below();
            blockState = this.level.getBlockState(offset);
            if ((blockState2.isAir() || blockState2.getMaterial().isLiquid()) && blockState.getMaterial().isSolidBlocking()) {
                return blockPos2;
            }
        }
        return null;
    }

    @Override // net.minecraft.world.entity.ReputationEventHandler
    public void onReputationEventFrom(ReputationEventType reputationEventType, Entity entity) {
        if (reputationEventType == ReputationEventType.ZOMBIE_VILLAGER_CURED) {
            this.gossips.add(entity.getUUID(), GossipType.MAJOR_POSITIVE, 20);
            this.gossips.add(entity.getUUID(), GossipType.MINOR_POSITIVE, 25);
        } else if (reputationEventType == ReputationEventType.TRADE) {
            this.gossips.add(entity.getUUID(), GossipType.TRADING, 2);
        } else if (reputationEventType == ReputationEventType.VILLAGER_HURT) {
            this.gossips.add(entity.getUUID(), GossipType.MINOR_NEGATIVE, 25);
        } else if (reputationEventType == ReputationEventType.VILLAGER_KILLED) {
            this.gossips.add(entity.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
        }
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.item.trading.Merchant
    public int getVillagerXp() {
        return this.villagerXp;
    }

    public void setVillagerXp(int i) {
        this.villagerXp = i;
    }

    private void resetNumberOfRestocks() {
        catchUpDemand();
        this.numberOfRestocksToday = 0;
    }

    public GossipContainer getGossips() {
        return this.gossips;
    }

    public void setGossips(Tag tag) {
        this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, tag));
    }

    @Override // net.minecraft.world.entity.Mob
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void startSleeping(BlockPos blockPos) {
        super.startSleeping(blockPos);
        this.brain.setMemory( MemoryModuleType.LAST_SLEPT,  Long.valueOf(this.level.getGameTime()));
        this.brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void stopSleeping() {
        super.stopSleeping();
        this.brain.setMemory( MemoryModuleType.LAST_WOKEN,  Long.valueOf(this.level.getGameTime()));
    }

    private boolean golemSpawnConditionsMet(long j) {
        Optional<Long> var3 = this.brain.getMemory(MemoryModuleType.LAST_SLEPT);
        if (var3.isPresent()) {
            return j - (Long) var3.get() < 24000L;
        } else {
            return false;
        }
    }
}
