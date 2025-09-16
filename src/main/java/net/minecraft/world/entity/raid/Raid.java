package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/raid/Raid.class */
public class Raid {
    private static final Component RAID_NAME_COMPONENT = new TranslatableComponent("event.minecraft.raid");
    private static final Component VICTORY = new TranslatableComponent("event.minecraft.raid.victory");
    private static final Component DEFEAT = new TranslatableComponent("event.minecraft.raid.defeat");
    private static final Component RAID_BAR_VICTORY_COMPONENT = RAID_NAME_COMPONENT.copy().append(" - ").append(VICTORY);
    private static final Component RAID_BAR_DEFEAT_COMPONENT = RAID_NAME_COMPONENT.copy().append(" - ").append(DEFEAT);
    private final Map<Integer, Raider> groupToLeaderMap;
    private final Map<Integer, Set<Raider>> groupRaiderMap;
    private final Set<UUID> heroesOfTheVillage;
    private long ticksActive;
    private BlockPos center;
    private final ServerLevel level;
    private boolean started;

    /* renamed from: id */
    private final int f458id;
    private float totalHealth;
    private int badOmenLevel;
    private boolean active;
    private int groupsSpawned;
    private final ServerBossEvent raidEvent;
    private int postRaidTicks;
    private int raidCooldownTicks;
    private final Random random;
    private final int numGroups;
    private RaidStatus status;
    private int celebrationTicks;
    private Optional<BlockPos> waveSpawnPos;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/raid/Raid$RaidStatus.class */
    enum RaidStatus {
        ONGOING,
        VICTORY,
        LOSS,
        STOPPED;

        private static final RaidStatus[] VALUES = values();

        /* JADX INFO: Access modifiers changed from: private */
        public static RaidStatus getByName(String str) {
            for (RaidStatus raidStatus : VALUES) {
                if (str.equalsIgnoreCase(raidStatus.name())) {
                    return raidStatus;
                }
            }
            return ONGOING;
        }

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/raid/Raid$RaiderType.class */
    enum RaiderType {
        VINDICATOR(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
        EVOKER(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
        PILLAGER(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
        WITCH(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
        RAVAGER(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

        private static final RaiderType[] VALUES = values();
        private final EntityType<? extends Raider> entityType;
        private final int[] spawnsPerWaveBeforeBonus;

        RaiderType(EntityType entityType, int[] iArr) {
            this.entityType = entityType;
            this.spawnsPerWaveBeforeBonus = iArr;
        }
    }

    public Raid(int i, ServerLevel serverLevel, BlockPos blockPos) {
        this.groupToLeaderMap = Maps.newHashMap();
        this.groupRaiderMap = Maps.newHashMap();
        this.heroesOfTheVillage = Sets.newHashSet();
        this.raidEvent = new ServerBossEvent(RAID_NAME_COMPONENT, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        this.random = new Random();
        this.waveSpawnPos = Optional.empty();
        this.f458id = i;
        this.level = serverLevel;
        this.active = true;
        this.raidCooldownTicks = 300;
        this.raidEvent.setPercent(0.0f);
        this.center = blockPos;
        this.numGroups = getNumGroups(serverLevel.getDifficulty());
        this.status = RaidStatus.ONGOING;
    }

    public Raid(ServerLevel serverLevel, CompoundTag compoundTag) {
        this.groupToLeaderMap = Maps.newHashMap();
        this.groupRaiderMap = Maps.newHashMap();
        this.heroesOfTheVillage = Sets.newHashSet();
        this.raidEvent = new ServerBossEvent(RAID_NAME_COMPONENT, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        this.random = new Random();
        this.waveSpawnPos = Optional.empty();
        this.level = serverLevel;
        this.f458id = compoundTag.getInt("Id");
        this.started = compoundTag.getBoolean("Started");
        this.active = compoundTag.getBoolean("Active");
        this.ticksActive = compoundTag.getLong("TicksActive");
        this.badOmenLevel = compoundTag.getInt("BadOmenLevel");
        this.groupsSpawned = compoundTag.getInt("GroupsSpawned");
        this.raidCooldownTicks = compoundTag.getInt("PreRaidTicks");
        this.postRaidTicks = compoundTag.getInt("PostRaidTicks");
        this.totalHealth = compoundTag.getFloat("TotalHealth");
        this.center = new BlockPos(compoundTag.getInt("CX"), compoundTag.getInt("CY"), compoundTag.getInt("CZ"));
        this.numGroups = compoundTag.getInt("NumGroups");
        this.status = RaidStatus.getByName(compoundTag.getString("Status"));
        this.heroesOfTheVillage.clear();
        if (compoundTag.contains("HeroesOfTheVillage", 9)) {
            ListTag list = compoundTag.getList("HeroesOfTheVillage", 11);
            for (int i = 0; i < list.size(); i++) {
                this.heroesOfTheVillage.add(NbtUtils.loadUUID(list.get(i)));
            }
        }
    }

    public boolean isOver() {
        return isVictory() || isLoss();
    }

    public boolean isBetweenWaves() {
        return hasFirstWaveSpawned() && getTotalRaidersAlive() == 0 && this.raidCooldownTicks > 0;
    }

    public boolean hasFirstWaveSpawned() {
        return this.groupsSpawned > 0;
    }

    public boolean isStopped() {
        return this.status == RaidStatus.STOPPED;
    }

    public boolean isVictory() {
        return this.status == RaidStatus.VICTORY;
    }

    public boolean isLoss() {
        return this.status == RaidStatus.LOSS;
    }

    public Level getLevel() {
        return this.level;
    }

    public boolean isStarted() {
        return this.started;
    }

    public int getGroupsSpawned() {
        return this.groupsSpawned;
    }

    private Predicate<ServerPlayer> validPlayer() {
        return serverPlayer -> {
            return serverPlayer.isAlive() && this.level.getRaidAt(serverPlayer.blockPosition()) == this;
        };
    }

    private void updatePlayers() {
        Set<ServerPlayer> newHashSet = Sets.newHashSet(this.raidEvent.getPlayers());
        List<ServerPlayer> players = this.level.getPlayers(validPlayer());
        for (ServerPlayer serverPlayer : players) {
            if (!newHashSet.contains(serverPlayer)) {
                this.raidEvent.addPlayer(serverPlayer);
            }
        }
        for (ServerPlayer serverPlayer2 : newHashSet) {
            if (!players.contains(serverPlayer2)) {
                this.raidEvent.removePlayer(serverPlayer2);
            }
        }
    }

    public int getMaxBadOmenLevel() {
        return 5;
    }

    public int getBadOmenLevel() {
        return this.badOmenLevel;
    }

    public void absorbBadOmen(Player player) {
        if (player.hasEffect(MobEffects.BAD_OMEN)) {
            this.badOmenLevel += player.getEffect(MobEffects.BAD_OMEN).getAmplifier() + 1;
            this.badOmenLevel = Mth.clamp(this.badOmenLevel, 0, getMaxBadOmenLevel());
        }
        player.removeEffect(MobEffects.BAD_OMEN);
    }

    public void stop() {
        this.active = false;
        this.raidEvent.removeAllPlayers();
        this.status = RaidStatus.STOPPED;
    }

    public void tick() {
        if (isStopped()) {
            return;
        }
        if (this.status != RaidStatus.ONGOING) {
            if (isOver()) {
                this.celebrationTicks++;
                if (this.celebrationTicks >= 600) {
                    stop();
                    return;
                }
                if (this.celebrationTicks % 20 == 0) {
                    updatePlayers();
                    this.raidEvent.setVisible(true);
                    if (isVictory()) {
                        this.raidEvent.setPercent(0.0f);
                        this.raidEvent.setName(RAID_BAR_VICTORY_COMPONENT);
                        return;
                    } else {
                        this.raidEvent.setName(RAID_BAR_DEFEAT_COMPONENT);
                        return;
                    }
                }
                return;
            }
            return;
        }
        boolean z = this.active;
        this.active = this.level.hasChunkAt(this.center);
        if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
            stop();
            return;
        }
        if (z != this.active) {
            this.raidEvent.setVisible(this.active);
        }
        if (!this.active) {
            return;
        }
        if (!this.level.isVillage(this.center)) {
            moveRaidCenterToNearbyVillageSection();
        }
        if (!this.level.isVillage(this.center)) {
            if (this.groupsSpawned > 0) {
                this.status = RaidStatus.LOSS;
            } else {
                stop();
            }
        }
        this.ticksActive++;
        if (this.ticksActive >= 48000) {
            stop();
            return;
        }
        int totalRaidersAlive = getTotalRaidersAlive();
        if (totalRaidersAlive == 0 && hasMoreWaves()) {
            if (this.raidCooldownTicks > 0) {
                boolean isPresent = this.waveSpawnPos.isPresent();
                boolean z2 = !isPresent && this.raidCooldownTicks % 5 == 0;
                if (isPresent && !this.level.getChunkSource().isEntityTickingChunk(new ChunkPos(this.waveSpawnPos.get()))) {
                    z2 = true;
                }
                if (z2) {
                    int i = 0;
                    if (this.raidCooldownTicks < 100) {
                        i = 1;
                    } else if (this.raidCooldownTicks < 40) {
                        i = 2;
                    }
                    this.waveSpawnPos = getValidSpawnPos(i);
                }
                if (this.raidCooldownTicks == 300 || this.raidCooldownTicks % 20 == 0) {
                    updatePlayers();
                }
                this.raidCooldownTicks--;
                this.raidEvent.setPercent(Mth.clamp((300 - this.raidCooldownTicks) / 300.0f, 0.0f, 1.0f));
            } else if (this.raidCooldownTicks == 0 && this.groupsSpawned > 0) {
                this.raidCooldownTicks = 300;
                this.raidEvent.setName(RAID_NAME_COMPONENT);
                return;
            }
        }
        if (this.ticksActive % 20 == 0) {
            updatePlayers();
            updateRaiders();
            if (totalRaidersAlive > 0) {
                if (totalRaidersAlive <= 2) {
                    this.raidEvent.setName(RAID_NAME_COMPONENT.copy().append(" - ").append(new TranslatableComponent("event.minecraft.raid.raiders_remaining", Integer.valueOf(totalRaidersAlive))));
                } else {
                    this.raidEvent.setName(RAID_NAME_COMPONENT);
                }
            } else {
                this.raidEvent.setName(RAID_NAME_COMPONENT);
            }
        }
        boolean z3 = false;
        int i2 = 0;
        while (true) {
            if (!shouldSpawnGroup()) {
                break;
            }
            BlockPos findRandomSpawnPos = this.waveSpawnPos.isPresent() ? this.waveSpawnPos.get() : findRandomSpawnPos(i2, 20);
            if (findRandomSpawnPos != null) {
                this.started = true;
                spawnGroup(findRandomSpawnPos);
                if (!z3) {
                    playSound(findRandomSpawnPos);
                    z3 = true;
                }
            } else {
                i2++;
            }
            if (i2 > 3) {
                stop();
                break;
            }
        }
        if (isStarted() && !hasMoreWaves() && totalRaidersAlive == 0) {
            if (this.postRaidTicks < 40) {
                this.postRaidTicks++;
            } else {
                this.status = RaidStatus.VICTORY;
                Iterator<UUID> it = this.heroesOfTheVillage.iterator();
                while (it.hasNext()) {
                    Entity entity = this.level.getEntity(it.next());
                    if ((entity instanceof LivingEntity) && !entity.isSpectator()) {
                        LivingEntity livingEntity = (LivingEntity) entity;
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 48000, this.badOmenLevel - 1, false, false, true));
                        if (livingEntity instanceof ServerPlayer) {
                            ServerPlayer serverPlayer = (ServerPlayer) livingEntity;
                            serverPlayer.awardStat(Stats.RAID_WIN);
                            CriteriaTriggers.RAID_WIN.trigger(serverPlayer);
                        }
                    }
                }
            }
        }
        setDirty();
    }

    private void moveRaidCenterToNearbyVillageSection() {
        Stream<SectionPos> cube = SectionPos.cube(SectionPos.of(this.center), 2);
        ServerLevel serverLevel = this.level;
        serverLevel.getClass();
        cube.filter(serverLevel::isVillage).map((v0) -> {
            return v0.center();
        }).min(Comparator.comparingDouble(blockPos -> {
            return blockPos.distSqr(this.center);
        })).ifPresent(this::setCenter);
    }

    private Optional<BlockPos> getValidSpawnPos(int i) {
        for (int i2 = 0; i2 < 3; i2++) {
            BlockPos findRandomSpawnPos = findRandomSpawnPos(i, 1);
            if (findRandomSpawnPos != null) {
                return Optional.of(findRandomSpawnPos);
            }
        }
        return Optional.empty();
    }

    private boolean hasMoreWaves() {
        return hasBonusWave() ? !hasSpawnedBonusWave() : !isFinalWave();
    }

    private boolean isFinalWave() {
        return getGroupsSpawned() == this.numGroups;
    }

    private boolean hasBonusWave() {
        return this.badOmenLevel > 1;
    }

    private boolean hasSpawnedBonusWave() {
        return getGroupsSpawned() > this.numGroups;
    }

    private boolean shouldSpawnBonusGroup() {
        return isFinalWave() && getTotalRaidersAlive() == 0 && hasBonusWave();
    }

    private void updateRaiders() {
        Iterator<Set<Raider>> it = this.groupRaiderMap.values().iterator();
        Set<Raider> newHashSet = Sets.newHashSet();
        while (it.hasNext()) {
            for (Raider raider : it.next()) {
                BlockPos blockPosition = raider.blockPosition();
                if (raider.removed || raider.level.dimension() != this.level.dimension() || this.center.distSqr(blockPosition) >= 12544.0d) {
                    newHashSet.add(raider);
                } else if (raider.tickCount > 600) {
                    if (this.level.getEntity(raider.getUUID()) == null) {
                        newHashSet.add(raider);
                    }
                    if (!this.level.isVillage(blockPosition) && raider.getNoActionTime() > 2400) {
                        raider.setTicksOutsideRaid(raider.getTicksOutsideRaid() + 1);
                    }
                    if (raider.getTicksOutsideRaid() >= 30) {
                        newHashSet.add(raider);
                    }
                }
            }
        }
        Iterator<Raider> it2 = newHashSet.iterator();
        while (it2.hasNext()) {
            removeFromRaid(it2.next(), true);
        }
    }

    private void playSound(BlockPos blockPos) {
        Collection<ServerPlayer> players = this.raidEvent.getPlayers();
        for (ServerPlayer serverPlayer : this.level.players()) {
            Vec3 position = serverPlayer.position();
            Vec3 atCenterOf = Vec3.atCenterOf(blockPos);
            float sqrt = Mth.sqrt(((atCenterOf.x - position.x) * (atCenterOf.x - position.x)) + ((atCenterOf.z - position.z) * (atCenterOf.z - position.z)));
            double d = position.x + ((13.0f / sqrt) * (atCenterOf.x - position.x));
            double d2 = position.z + ((13.0f / sqrt) * (atCenterOf.z - position.z));
            if (sqrt <= 64.0f || players.contains(serverPlayer)) {
                serverPlayer.connection.send(new ClientboundSoundPacket(SoundEvents.RAID_HORN, SoundSource.NEUTRAL, d, serverPlayer.getY(), d2, 64.0f, 1.0f));
            }
        }
    }

    private void spawnGroup(BlockPos blockPos) {
        boolean z = false;
        int i = this.groupsSpawned + 1;
        this.totalHealth = 0.0f;
        DifficultyInstance currentDifficultyAt = this.level.getCurrentDifficultyAt(blockPos);
        boolean shouldSpawnBonusGroup = shouldSpawnBonusGroup();
        for (RaiderType raiderType : RaiderType.VALUES) {
            int defaultNumSpawns = getDefaultNumSpawns(raiderType, i, shouldSpawnBonusGroup) + getPotentialBonusSpawns(raiderType, this.random, i, currentDifficultyAt, shouldSpawnBonusGroup);
            int i2 = 0;
            for (int i3 = 0; i3 < defaultNumSpawns; i3++) {
                Raider raider = (Raider) raiderType.entityType.create(this.level);
                if (!z && raider.canBeLeader()) {
                    raider.setPatrolLeader(true);
                    setLeader(i, raider);
                    z = true;
                }
                joinRaid(i, raider, blockPos, false);
                if (raiderType.entityType == EntityType.RAVAGER) {
                    Raider raider2 = null;
                    if (i == getNumGroups(Difficulty.NORMAL)) {
                        raider2 = EntityType.PILLAGER.create(this.level);
                    } else if (i >= getNumGroups(Difficulty.HARD)) {
                        if (i2 == 0) {
                            raider2 = EntityType.EVOKER.create(this.level);
                        } else {
                            raider2 = EntityType.VINDICATOR.create(this.level);
                        }
                    }
                    i2++;
                    if (raider2 != null) {
                        joinRaid(i, raider2, blockPos, false);
                        raider2.moveTo(blockPos, 0.0f, 0.0f);
                        raider2.startRiding(raider);
                    }
                }
            }
        }
        this.waveSpawnPos = Optional.empty();
        this.groupsSpawned++;
        updateBossbar();
        setDirty();
    }

    public void joinRaid(int i, Raider raider, @Nullable BlockPos blockPos, boolean z) {
        if (addWaveMob(i, raider)) {
            raider.setCurrentRaid(this);
            raider.setWave(i);
            raider.setCanJoinRaid(true);
            raider.setTicksOutsideRaid(0);
            if (!z && blockPos != null) {
                raider.setPos(blockPos.getX() + 0.5d, blockPos.getY() + 1.0d, blockPos.getZ() + 0.5d);
                raider.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(blockPos), MobSpawnType.EVENT, null, null);
                raider.applyRaidBuffs(i, false);
                raider.setOnGround(true);
                this.level.addFreshEntityWithPassengers(raider);
            }
        }
    }

    public void updateBossbar() {
        this.raidEvent.setPercent(Mth.clamp(getHealthOfLivingRaiders() / this.totalHealth, 0.0f, 1.0f));
    }

    public float getHealthOfLivingRaiders() {
        float f = 0.0f;
        Iterator<Set<Raider>> it = this.groupRaiderMap.values().iterator();
        while (it.hasNext()) {
            Iterator<Raider> it2 = it.next().iterator();
            while (it2.hasNext()) {
                f += it2.next().getHealth();
            }
        }
        return f;
    }

    private boolean shouldSpawnGroup() {
        return this.raidCooldownTicks == 0 && (this.groupsSpawned < this.numGroups || shouldSpawnBonusGroup()) && getTotalRaidersAlive() == 0;
    }

    public int getTotalRaidersAlive() {
        return this.groupRaiderMap.values().stream().mapToInt((v0) -> {
            return v0.size();
        }).sum();
    }

    public void removeFromRaid(Raider raider, boolean z) {
        Set<Raider> set = this.groupRaiderMap.get(Integer.valueOf(raider.getWave()));
        if (set != null && set.remove(raider)) {
            if (z) {
                this.totalHealth -= raider.getHealth();
            }
            raider.setCurrentRaid(null);
            updateBossbar();
            setDirty();
        }
    }

    private void setDirty() {
        this.level.getRaids().setDirty();
    }

    public static ItemStack getLeaderBannerInstance() {
        ItemStack itemStack = new ItemStack(Items.WHITE_BANNER);
        itemStack.getOrCreateTagElement("BlockEntityTag").put("Patterns", new BannerPattern.Builder().addPattern(BannerPattern.RHOMBUS_MIDDLE, DyeColor.CYAN).addPattern(BannerPattern.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY).addPattern(BannerPattern.STRIPE_CENTER, DyeColor.GRAY).addPattern(BannerPattern.BORDER, DyeColor.LIGHT_GRAY).addPattern(BannerPattern.STRIPE_MIDDLE, DyeColor.BLACK).addPattern(BannerPattern.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY).addPattern(BannerPattern.CIRCLE_MIDDLE, DyeColor.LIGHT_GRAY).addPattern(BannerPattern.BORDER, DyeColor.BLACK).toListTag());
        itemStack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        itemStack.setHoverName(new TranslatableComponent("block.minecraft.ominous_banner").withStyle(ChatFormatting.GOLD));
        return itemStack;
    }

    @Nullable
    public Raider getLeader(int i) {
        return this.groupToLeaderMap.get(Integer.valueOf(i));
    }

    @Nullable
    private BlockPos findRandomSpawnPos(int i, int i2) {
        int i3 = i == 0 ? 2 : 2 - i;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i4 = 0; i4 < i2; i4++) {
            float nextFloat = this.level.random.nextFloat() * 6.2831855f;
            int x = this.center.getX() + Mth.floor(Mth.cos(nextFloat) * 32.0f * i3) + this.level.random.nextInt(5);
            int z = this.center.getZ() + Mth.floor(Mth.sin(nextFloat) * 32.0f * i3) + this.level.random.nextInt(5);
            mutableBlockPos.set(x, this.level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
            if ((!this.level.isVillage(mutableBlockPos) || i >= 2) && this.level.hasChunksAt(mutableBlockPos.getX() - 10, mutableBlockPos.getY() - 10, mutableBlockPos.getZ() - 10, mutableBlockPos.getX() + 10, mutableBlockPos.getY() + 10, mutableBlockPos.getZ() + 10) && this.level.getChunkSource().isEntityTickingChunk(new ChunkPos(mutableBlockPos)) && (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, this.level, mutableBlockPos, EntityType.RAVAGER) || (this.level.getBlockState(mutableBlockPos.below()).is(Blocks.SNOW) && this.level.getBlockState(mutableBlockPos).isAir()))) {
                return mutableBlockPos;
            }
        }
        return null;
    }

    private boolean addWaveMob(int i, Raider raider) {
        return addWaveMob(i, raider, true);
    }

    public boolean addWaveMob(int i, Raider raider, boolean z) {
        this.groupRaiderMap.computeIfAbsent(Integer.valueOf(i), num -> {
            return Sets.newHashSet();
        });
        Set<Raider> set = this.groupRaiderMap.get(Integer.valueOf(i));
        Raider raider2 = null;
        Iterator<Raider> it = set.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Raider next = it.next();
            if (next.getUUID().equals(raider.getUUID())) {
                raider2 = next;
                break;
            }
        }
        if (raider2 != null) {
            set.remove(raider2);
            set.add(raider);
        }
        set.add(raider);
        if (z) {
            this.totalHealth += raider.getHealth();
        }
        updateBossbar();
        setDirty();
        return true;
    }

    public void setLeader(int i, Raider raider) {
        this.groupToLeaderMap.put(Integer.valueOf(i), raider);
        raider.setItemSlot(EquipmentSlot.HEAD, getLeaderBannerInstance());
        raider.setDropChance(EquipmentSlot.HEAD, 2.0f);
    }

    public void removeLeader(int i) {
        this.groupToLeaderMap.remove(Integer.valueOf(i));
    }

    public BlockPos getCenter() {
        return this.center;
    }

    private void setCenter(BlockPos blockPos) {
        this.center = blockPos;
    }

    public int getId() {
        return this.f458id;
    }

    private int getDefaultNumSpawns(RaiderType raiderType, int i, boolean z) {
        return z ? raiderType.spawnsPerWaveBeforeBonus[this.numGroups] : raiderType.spawnsPerWaveBeforeBonus[i];
    }

    private int getPotentialBonusSpawns(RaiderType raiderType, Random random, int i, DifficultyInstance difficultyInstance, boolean z) {
        int i2;
        Difficulty difficulty = difficultyInstance.getDifficulty();
        boolean z2 = difficulty == Difficulty.EASY;
        boolean z3 = difficulty == Difficulty.NORMAL;
        switch (raiderType) {
            case WITCH:
                if (!z2 && i > 2 && i != 4) {
                    i2 = 1;
                    break;
                } else {
                    return 0;
                }
            case PILLAGER:
            case VINDICATOR:
                if (z2) {
                    i2 = random.nextInt(2);
                    break;
                } else if (z3) {
                    i2 = 1;
                    break;
                } else {
                    i2 = 2;
                    break;
                }
            case RAVAGER:
                i2 = (z2 || !z) ? 0 : 1;
                break;
            default:
                return 0;
        }
        if (i2 > 0) {
            return random.nextInt(i2 + 1);
        }
        return 0;
    }

    public boolean isActive() {
        return this.active;
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("Id", this.f458id);
        compoundTag.putBoolean("Started", this.started);
        compoundTag.putBoolean("Active", this.active);
        compoundTag.putLong("TicksActive", this.ticksActive);
        compoundTag.putInt("BadOmenLevel", this.badOmenLevel);
        compoundTag.putInt("GroupsSpawned", this.groupsSpawned);
        compoundTag.putInt("PreRaidTicks", this.raidCooldownTicks);
        compoundTag.putInt("PostRaidTicks", this.postRaidTicks);
        compoundTag.putFloat("TotalHealth", this.totalHealth);
        compoundTag.putInt("NumGroups", this.numGroups);
        compoundTag.putString("Status", this.status.getName());
        compoundTag.putInt("CX", this.center.getX());
        compoundTag.putInt("CY", this.center.getY());
        compoundTag.putInt("CZ", this.center.getZ());
        ListTag listTag = new ListTag();
        Iterator<UUID> it = this.heroesOfTheVillage.iterator();
        while (it.hasNext()) {
            listTag.add(NbtUtils.createUUID(it.next()));
        }
        compoundTag.put("HeroesOfTheVillage", listTag);
        return compoundTag;
    }

    public int getNumGroups(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return 3;
            case NORMAL:
                return 5;
            case HARD:
                return 7;
            default:
                return 0;
        }
    }

    public float getEnchantOdds() {
        int badOmenLevel = getBadOmenLevel();
        if (badOmenLevel == 2) {
            return 0.1f;
        }
        if (badOmenLevel == 3) {
            return 0.25f;
        }
        if (badOmenLevel == 4) {
            return 0.5f;
        }
        if (badOmenLevel == 5) {
            return 0.75f;
        }
        return 0.0f;
    }

    public void addHeroOfTheVillage(Entity entity) {
        this.heroesOfTheVillage.add(entity.getUUID());
    }
}
