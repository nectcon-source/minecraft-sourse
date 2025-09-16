package net.minecraft.world.level.dimension.end;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.Features;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/dimension/end/EndDragonFight.class */
public class EndDragonFight {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Predicate<Entity> VALID_PLAYER = EntitySelector.ENTITY_STILL_ALIVE.and(EntitySelector.withinDistance(0.0d, 128.0d, 0.0d, 192.0d));
    private final ServerLevel level;
    private final BlockPattern exitPortalPattern;
    private int ticksSinceDragonSeen;
    private int crystalsAlive;
    private int ticksSinceCrystalsScanned;
    private int ticksSinceLastPlayerScan;
    private boolean dragonKilled;
    private boolean previouslyKilled;
    private UUID dragonUUID;
    private BlockPos portalLocation;
    private DragonRespawnAnimation respawnStage;
    private int respawnTime;
    private List<EndCrystal> respawnCrystals;
    private final ServerBossEvent dragonEvent = (ServerBossEvent) new ServerBossEvent(new TranslatableComponent("entity.minecraft.ender_dragon"), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS).setPlayBossMusic(true).setCreateWorldFog(true);
    private final List<Integer> gateways = Lists.newArrayList();
    private boolean needsStateScanning = true;

    public EndDragonFight(ServerLevel serverLevel, long j, CompoundTag compoundTag) {
        this.level = serverLevel;
        if (compoundTag.contains("DragonKilled", 99)) {
            if (compoundTag.hasUUID("Dragon")) {
                this.dragonUUID = compoundTag.getUUID("Dragon");
            }
            this.dragonKilled = compoundTag.getBoolean("DragonKilled");
            this.previouslyKilled = compoundTag.getBoolean("PreviouslyKilled");
            if (compoundTag.getBoolean("IsRespawning")) {
                this.respawnStage = DragonRespawnAnimation.START;
            }
            if (compoundTag.contains("ExitPortalLocation", 10)) {
                this.portalLocation = NbtUtils.readBlockPos(compoundTag.getCompound("ExitPortalLocation"));
            }
        } else {
            this.dragonKilled = true;
            this.previouslyKilled = true;
        }
        if (compoundTag.contains("Gateways", 9)) {
            ListTag list = compoundTag.getList("Gateways", 3);
            for (int i = 0; i < list.size(); i++) {
                this.gateways.add(Integer.valueOf(list.getInt(i)));
            }
        } else {
            this.gateways.addAll(ContiguousSet.create(Range.closedOpen(0, 20), DiscreteDomain.integers()));
            Collections.shuffle(this.gateways, new Random(j));
        }
        this.exitPortalPattern = BlockPatternBuilder.start().aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ").aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ").where('#', BlockInWorld.hasState(BlockPredicate.forBlock(Blocks.BEDROCK))).build();
    }

    public CompoundTag saveData() {
        CompoundTag compoundTag = new CompoundTag();
        if (this.dragonUUID != null) {
            compoundTag.putUUID("Dragon", this.dragonUUID);
        }
        compoundTag.putBoolean("DragonKilled", this.dragonKilled);
        compoundTag.putBoolean("PreviouslyKilled", this.previouslyKilled);
        if (this.portalLocation != null) {
            compoundTag.put("ExitPortalLocation", NbtUtils.writeBlockPos(this.portalLocation));
        }
        ListTag listTag = new ListTag();
        Iterator<Integer> it = this.gateways.iterator();
        while (it.hasNext()) {
            listTag.add(IntTag.valueOf(it.next().intValue()));
        }
        compoundTag.put("Gateways", listTag);
        return compoundTag;
    }

    /* JADX WARN: Code restructure failed: missing block: B:27:0x00c5, code lost:
    
        if (r1 >= 1200) goto L30;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void tick() {
        this.dragonEvent.setVisible(!this.dragonKilled);
        if (++this.ticksSinceLastPlayerScan >= 20) {
            this.updatePlayers();
            this.ticksSinceLastPlayerScan = 0;
        }

        if (!this.dragonEvent.getPlayers().isEmpty()) {
            this.level.getChunkSource().addRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
            boolean var1 = this.isArenaLoaded();
            if (this.needsStateScanning && var1) {
                this.scanState();
                this.needsStateScanning = false;
            }

            if (this.respawnStage != null) {
                if (this.respawnCrystals == null && var1) {
                    this.respawnStage = null;
                    this.tryRespawn();
                }

                this.respawnStage.tick(this.level, this, this.respawnCrystals, this.respawnTime++, this.portalLocation);
            }

            if (!this.dragonKilled) {
                if ((this.dragonUUID == null || ++this.ticksSinceDragonSeen >= 1200) && var1) {
                    this.findOrCreateDragon();
                    this.ticksSinceDragonSeen = 0;
                }

                if (++this.ticksSinceCrystalsScanned >= 100 && var1) {
                    this.updateCrystalCount();
                    this.ticksSinceCrystalsScanned = 0;
                }
            }
        } else {
            this.level.getChunkSource().removeRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
        }
    }

    private void scanState() {
        LOGGER.info("Scanning for legacy world dragon fight...");
        boolean hasActiveExitPortal = hasActiveExitPortal();
        if (hasActiveExitPortal) {
            LOGGER.info("Found that the dragon has been killed in this world already.");
            this.previouslyKilled = true;
        } else {
            LOGGER.info("Found that the dragon has not yet been killed in this world.");
            this.previouslyKilled = false;
            if (findExitPortal() == null) {
                spawnExitPortal(false);
            }
        }
        List<EnderDragon> dragons = this.level.getDragons();
        if (dragons.isEmpty()) {
            this.dragonKilled = true;
        } else {
            EnderDragon enderDragon = dragons.get(0);
            this.dragonUUID = enderDragon.getUUID();
            LOGGER.info("Found that there's a dragon still alive ({})", enderDragon);
            this.dragonKilled = false;
            if (!hasActiveExitPortal) {
                LOGGER.info("But we didn't have a portal, let's remove it.");
                enderDragon.remove();
                this.dragonUUID = null;
            }
        }
        if (!this.previouslyKilled && this.dragonKilled) {
            this.dragonKilled = false;
        }
    }

    private void findOrCreateDragon() {
        List<EnderDragon> dragons = this.level.getDragons();
        if (dragons.isEmpty()) {
            LOGGER.debug("Haven't seen the dragon, respawning it");
            createNewDragon();
        } else {
            LOGGER.debug("Haven't seen our dragon, but found another one to use.");
            this.dragonUUID = dragons.get(0).getUUID();
        }
    }

    protected void setRespawnStage(DragonRespawnAnimation dragonRespawnAnimation) {
        if (this.respawnStage == null) {
            throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");
        }
        this.respawnTime = 0;
        if (dragonRespawnAnimation == DragonRespawnAnimation.END) {
            this.respawnStage = null;
            this.dragonKilled = false;
            EnderDragon createNewDragon = createNewDragon();
            Iterator<ServerPlayer> it = this.dragonEvent.getPlayers().iterator();
            while (it.hasNext()) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(it.next(), createNewDragon);
            }
            return;
        }
        this.respawnStage = dragonRespawnAnimation;
    }

    private boolean hasActiveExitPortal() {
        for (int i = -8; i <= 8; i++) {
            for (int i2 = -8; i2 <= 8; i2++) {
                Iterator<BlockEntity> it = this.level.getChunk(i, i2).getBlockEntities().values().iterator();
                while (it.hasNext()) {
                    if (it.next() instanceof TheEndPortalBlockEntity) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    private BlockPattern.BlockPatternMatch findExitPortal() {
        BlockPattern.BlockPatternMatch find;
        for (int i = -8; i <= 8; i++) {
            for (int i2 = -8; i2 <= 8; i2++) {
                for (BlockEntity blockEntity : this.level.getChunk(i, i2).getBlockEntities().values()) {
                    if ((blockEntity instanceof TheEndPortalBlockEntity) && (find = this.exitPortalPattern.find(this.level, blockEntity.getBlockPos())) != null) {
                        BlockPos pos = find.getBlock(3, 3, 3).getPos();
                        if (this.portalLocation == null && pos.getX() == 0 && pos.getZ() == 0) {
                            this.portalLocation = pos;
                        }
                        return find;
                    }
                }
            }
        }
        for (int y = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION).getY(); y >= 0; y--) {
            BlockPattern.BlockPatternMatch find2 = this.exitPortalPattern.find(this.level, new BlockPos(EndPodiumFeature.END_PODIUM_LOCATION.getX(), y, EndPodiumFeature.END_PODIUM_LOCATION.getZ()));
            if (find2 != null) {
                if (this.portalLocation == null) {
                    this.portalLocation = find2.getBlock(3, 3, 3).getPos();
                }
                return find2;
            }
        }
        return null;
    }

    private boolean isArenaLoaded() {
        for (int i = -8; i <= 8; i++) {
            for (int i2 = 8; i2 <= 8; i2++) {
                ChunkAccess chunk = this.level.getChunk(i, i2, ChunkStatus.FULL, false);
                if (!(chunk instanceof LevelChunk) || !((LevelChunk) chunk).getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updatePlayers() {
        Set<ServerPlayer> newHashSet = Sets.newHashSet();
        for (ServerPlayer serverPlayer : this.level.getPlayers(VALID_PLAYER)) {
            this.dragonEvent.addPlayer(serverPlayer);
            newHashSet.add(serverPlayer);
        }
        Set<ServerPlayer> newHashSet2 = Sets.newHashSet(this.dragonEvent.getPlayers());
        newHashSet2.removeAll(newHashSet);
        Iterator<ServerPlayer> it = newHashSet2.iterator();
        while (it.hasNext()) {
            this.dragonEvent.removePlayer(it.next());
        }
    }

    private void updateCrystalCount() {
        this.ticksSinceCrystalsScanned = 0;
        this.crystalsAlive = 0;
        Iterator<SpikeFeature.EndSpike> it = SpikeFeature.getSpikesForLevel(this.level).iterator();
        while (it.hasNext()) {
            this.crystalsAlive += this.level.getEntitiesOfClass(EndCrystal.class, it.next().getTopBoundingBox()).size();
        }
        LOGGER.debug("Found {} end crystals still alive", Integer.valueOf(this.crystalsAlive));
    }

    public void setDragonKilled(EnderDragon enderDragon) {
        if (enderDragon.getUUID().equals(this.dragonUUID)) {
            this.dragonEvent.setPercent(0.0f);
            this.dragonEvent.setVisible(false);
            spawnExitPortal(true);
            spawnNewGateway();
            if (!this.previouslyKilled) {
                this.level.setBlockAndUpdate(this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION), Blocks.DRAGON_EGG.defaultBlockState());
            }
            this.previouslyKilled = true;
            this.dragonKilled = true;
        }
    }

    private void spawnNewGateway() {
        if (this.gateways.isEmpty()) {
            return;
        }
        int intValue = this.gateways.remove(this.gateways.size() - 1).intValue();
        spawnNewGateway(new BlockPos(Mth.floor(96.0d * Math.cos(2.0d * ((-3.141592653589793d) + (0.15707963267948966d * intValue)))), 75, Mth.floor(96.0d * Math.sin(2.0d * ((-3.141592653589793d) + (0.15707963267948966d * intValue))))));
    }

    private void spawnNewGateway(BlockPos blockPos) {
        this.level.levelEvent(3000, blockPos, 0);
        Features.END_GATEWAY_DELAYED.place(this.level, this.level.getChunkSource().getGenerator(), new Random(), blockPos);
    }

    private void spawnExitPortal(boolean z) {
        EndPodiumFeature endPodiumFeature = new EndPodiumFeature(z);
        if (this.portalLocation == null) {
            this.portalLocation = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION).below();
            while (this.level.getBlockState(this.portalLocation).is(Blocks.BEDROCK) && this.portalLocation.getY() > this.level.getSeaLevel()) {
                this.portalLocation = this.portalLocation.below();
            }
        }
        endPodiumFeature.configured(FeatureConfiguration.NONE).place(this.level, this.level.getChunkSource().getGenerator(), new Random(), this.portalLocation);
    }

    private EnderDragon createNewDragon() {
        this.level.getChunkAt(new BlockPos(0, 128, 0));
        EnderDragon create = EntityType.ENDER_DRAGON.create(this.level);
        create.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        create.moveTo(0.0d, 128.0d, 0.0d, this.level.random.nextFloat() * 360.0f, 0.0f);
        this.level.addFreshEntity(create);
        this.dragonUUID = create.getUUID();
        return create;
    }

    public void updateDragon(EnderDragon enderDragon) {
        if (enderDragon.getUUID().equals(this.dragonUUID)) {
            this.dragonEvent.setPercent(enderDragon.getHealth() / enderDragon.getMaxHealth());
            this.ticksSinceDragonSeen = 0;
            if (enderDragon.hasCustomName()) {
                this.dragonEvent.setName(enderDragon.getDisplayName());
            }
        }
    }

    public int getCrystalsAlive() {
        return this.crystalsAlive;
    }

    public void onCrystalDestroyed(EndCrystal endCrystal, DamageSource damageSource) {
        if (this.respawnStage != null && this.respawnCrystals.contains(endCrystal)) {
            LOGGER.debug("Aborting respawn sequence");
            this.respawnStage = null;
            this.respawnTime = 0;
            resetSpikeCrystals();
            spawnExitPortal(true);
            return;
        }
        updateCrystalCount();
        Entity entity = this.level.getEntity(this.dragonUUID);
        if (entity instanceof EnderDragon) {
            ((EnderDragon) entity).onCrystalDestroyed(endCrystal, endCrystal.blockPosition(), damageSource);
        }
    }

    public boolean hasPreviouslyKilledDragon() {
        return this.previouslyKilled;
    }

    public void tryRespawn() {
        if (this.dragonKilled && this.respawnStage == null) {
            BlockPos blockPos = this.portalLocation;
            if (blockPos == null) {
                LOGGER.debug("Tried to respawn, but need to find the portal first.");
                if (findExitPortal() == null) {
                    LOGGER.debug("Couldn't find a portal, so we made one.");
                    spawnExitPortal(true);
                } else {
                    LOGGER.debug("Found the exit portal & temporarily using it.");
                }
                blockPos = this.portalLocation;
            }
            List<EndCrystal> newArrayList = Lists.newArrayList();
            BlockPos above = blockPos.above(1);
            Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
            while (it.hasNext()) {
                List<EndCrystal> entitiesOfClass = this.level.getEntitiesOfClass(EndCrystal.class, new AABB(above.relative(it.next(), 2)));
                if (entitiesOfClass.isEmpty()) {
                    return;
                } else {
                    newArrayList.addAll(entitiesOfClass);
                }
            }
            LOGGER.debug("Found all crystals, respawning dragon.");
            respawnDragon(newArrayList);
        }
    }

    private void respawnDragon(List<EndCrystal> list) {
        if (this.dragonKilled && this.respawnStage == null) {
            BlockPattern.BlockPatternMatch findExitPortal = findExitPortal();
            while (true) {
                BlockPattern.BlockPatternMatch blockPatternMatch = findExitPortal;
                if (blockPatternMatch != null) {
                    for (int i = 0; i < this.exitPortalPattern.getWidth(); i++) {
                        for (int i2 = 0; i2 < this.exitPortalPattern.getHeight(); i2++) {
                            for (int i3 = 0; i3 < this.exitPortalPattern.getDepth(); i3++) {
                                BlockInWorld block = blockPatternMatch.getBlock(i, i2, i3);
                                if (block.getState().is(Blocks.BEDROCK) || block.getState().is(Blocks.END_PORTAL)) {
                                    this.level.setBlockAndUpdate(block.getPos(), Blocks.END_STONE.defaultBlockState());
                                }
                            }
                        }
                    }
                    findExitPortal = findExitPortal();
                } else {
                    this.respawnStage = DragonRespawnAnimation.START;
                    this.respawnTime = 0;
                    spawnExitPortal(false);
                    this.respawnCrystals = list;
                    return;
                }
            }
        }
    }

    public void resetSpikeCrystals() {
        Iterator<SpikeFeature.EndSpike> it = SpikeFeature.getSpikesForLevel(this.level).iterator();
        while (it.hasNext()) {
            for (EndCrystal endCrystal : this.level.getEntitiesOfClass(EndCrystal.class, it.next().getTopBoundingBox())) {
                endCrystal.setInvulnerable(false);
                endCrystal.setBeamTarget(null);
            }
        }
    }
}
