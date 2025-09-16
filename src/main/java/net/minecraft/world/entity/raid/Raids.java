package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/raid/Raids.class */
public class Raids extends SavedData {
    private final Map<Integer, Raid> raidMap;
    private final ServerLevel level;
    private int nextAvailableID;
    private int tick;

    public Raids(ServerLevel serverLevel) {
        super(getFileId(serverLevel.dimensionType()));
        this.raidMap = Maps.newHashMap();
        this.level = serverLevel;
        this.nextAvailableID = 1;
        setDirty();
    }

    public Raid get(int i) {
        return this.raidMap.get(Integer.valueOf(i));
    }

    public void tick() {
        this.tick++;
        Iterator<Raid> it = this.raidMap.values().iterator();
        while (it.hasNext()) {
            Raid next = it.next();
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                next.stop();
            }
            if (next.isStopped()) {
                it.remove();
                setDirty();
            } else {
                next.tick();
            }
        }
        if (this.tick % 200 == 0) {
            setDirty();
        }
        DebugPackets.sendRaids(this.level, this.raidMap.values());
    }

    public static boolean canJoinRaid(Raider raider, Raid raid) {
        return raider != null && raid != null && raid.getLevel() != null && raider.isAlive() && raider.canJoinRaid() && raider.getNoActionTime() <= 2400 && raider.level.dimensionType() == raid.getLevel().dimensionType();
    }

    @Nullable
    public Raid createOrExtendRaid(ServerPlayer serverPlayer) {
        BlockPos blockPos;
        if (serverPlayer.isSpectator() || this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS) || !serverPlayer.level.dimensionType().hasRaids()) {
            return null;
        }
        BlockPos blockPosition = serverPlayer.blockPosition();
        List<PoiRecord> list = (List) this.level.getPoiManager().getInRange(PoiType.ALL, blockPosition, 64, PoiManager.Occupancy.IS_OCCUPIED).collect(Collectors.toList());
        int i = 0;
        Vec3 vec3 = Vec3.ZERO;
        Iterator<PoiRecord> it = list.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next().getPos();
            vec3 = vec3.add(pos.getX(), pos.getY(), pos.getZ());
            i++;
        }
        if (i > 0) {
            blockPos = new BlockPos(vec3.scale(1.0d / i));
        } else {
            blockPos = blockPosition;
        }
        Raid orCreateRaid = getOrCreateRaid(serverPlayer.getLevel(), blockPos);
        boolean z = false;
        if (!orCreateRaid.isStarted()) {
            if (!this.raidMap.containsKey(Integer.valueOf(orCreateRaid.getId()))) {
                this.raidMap.put(Integer.valueOf(orCreateRaid.getId()), orCreateRaid);
            }
            z = true;
        } else if (orCreateRaid.getBadOmenLevel() < orCreateRaid.getMaxBadOmenLevel()) {
            z = true;
        } else {
            serverPlayer.removeEffect(MobEffects.BAD_OMEN);
            serverPlayer.connection.send(new ClientboundEntityEventPacket(serverPlayer, (byte) 43));
        }
        if (z) {
            orCreateRaid.absorbBadOmen(serverPlayer);
            serverPlayer.connection.send(new ClientboundEntityEventPacket(serverPlayer, (byte) 43));
            if (!orCreateRaid.hasFirstWaveSpawned()) {
                serverPlayer.awardStat(Stats.RAID_TRIGGER);
                CriteriaTriggers.BAD_OMEN.trigger(serverPlayer);
            }
        }
        setDirty();
        return orCreateRaid;
    }

    private Raid getOrCreateRaid(ServerLevel serverLevel, BlockPos blockPos) {
        Raid raidAt = serverLevel.getRaidAt(blockPos);
        return raidAt != null ? raidAt : new Raid(getUniqueId(), serverLevel, blockPos);
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public void load(CompoundTag compoundTag) {
        this.nextAvailableID = compoundTag.getInt("NextAvailableID");
        this.tick = compoundTag.getInt("Tick");
        ListTag list = compoundTag.getList("Raids", 10);
        for (int i = 0; i < list.size(); i++) {
            Raid raid = new Raid(this.level, list.getCompound(i));
            this.raidMap.put(Integer.valueOf(raid.getId()), raid);
        }
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("NextAvailableID", this.nextAvailableID);
        compoundTag.putInt("Tick", this.tick);
        ListTag listTag = new ListTag();
        for (Raid raid : this.raidMap.values()) {
            CompoundTag compoundTag2 = new CompoundTag();
            raid.save(compoundTag2);
            listTag.add(compoundTag2);
        }
        compoundTag.put("Raids", listTag);
        return compoundTag;
    }

    public static String getFileId(DimensionType dimensionType) {
        return "raids" + dimensionType.getFileSuffix();
    }

    private int getUniqueId() {
        int i = this.nextAvailableID + 1;
        this.nextAvailableID = i;
        return i;
    }

    @Nullable
    public Raid getNearbyRaid(BlockPos blockPos, int i) {
        Raid raid = null;
        double d = i;
        for (Raid raid2 : this.raidMap.values()) {
            double distSqr = raid2.getCenter().distSqr(blockPos);
            if (raid2.isActive() && distSqr < d) {
                raid = raid2;
                d = distSqr;
            }
        }
        return raid;
    }
}
