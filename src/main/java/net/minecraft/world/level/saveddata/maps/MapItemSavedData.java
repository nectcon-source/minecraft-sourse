package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/saveddata/maps/MapItemSavedData.class */
public class MapItemSavedData extends SavedData {
    private static final Logger LOGGER = LogManager.getLogger();

    /* renamed from: x */
    public int x;

    /* renamed from: z */
    public int z;
    public ResourceKey<Level> dimension;
    public boolean trackingPosition;
    public boolean unlimitedTracking;
    public byte scale;
    public byte[] colors;
    public boolean locked;
    public final List<HoldingPlayer> carriedBy;
    private final Map<Player, HoldingPlayer> carriedByPlayers;
    private final Map<String, MapBanner> bannerMarkers;
    public final Map<String, MapDecoration> decorations;
    private final Map<String, MapFrame> frameMarkers;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/saveddata/maps/MapItemSavedData$HoldingPlayer.class */
    public class HoldingPlayer {
        public final Player player;
        private int minDirtyX;
        private int minDirtyY;
        private int tick;
        public int step;
        private boolean dirtyData = true;
        private int maxDirtyX = 127;
        private int maxDirtyY = 127;

        public HoldingPlayer(Player player) {
            this.player = player;
        }

        @Nullable
        public Packet<?> nextUpdatePacket(ItemStack itemStack) {
            if (this.dirtyData) {
                this.dirtyData = false;
                return new ClientboundMapItemDataPacket(MapItem.getMapId(itemStack), MapItemSavedData.this.scale, MapItemSavedData.this.trackingPosition, MapItemSavedData.this.locked, MapItemSavedData.this.decorations.values(), MapItemSavedData.this.colors, this.minDirtyX, this.minDirtyY, (this.maxDirtyX + 1) - this.minDirtyX, (this.maxDirtyY + 1) - this.minDirtyY);
            }
            int i = this.tick;
            this.tick = i + 1;
            if (i % 5 == 0) {
                return new ClientboundMapItemDataPacket(MapItem.getMapId(itemStack), MapItemSavedData.this.scale, MapItemSavedData.this.trackingPosition, MapItemSavedData.this.locked, MapItemSavedData.this.decorations.values(), MapItemSavedData.this.colors, 0, 0, 0, 0);
            }
            return null;
        }

        public void markDirty(int i, int i2) {
            if (this.dirtyData) {
                this.minDirtyX = Math.min(this.minDirtyX, i);
                this.minDirtyY = Math.min(this.minDirtyY, i2);
                this.maxDirtyX = Math.max(this.maxDirtyX, i);
                this.maxDirtyY = Math.max(this.maxDirtyY, i2);
                return;
            }
            this.dirtyData = true;
            this.minDirtyX = i;
            this.minDirtyY = i2;
            this.maxDirtyX = i;
            this.maxDirtyY = i2;
        }
    }

    public MapItemSavedData(String str) {
        super(str);
        this.colors = new byte[16384];
        this.carriedBy = Lists.newArrayList();
        this.carriedByPlayers = Maps.newHashMap();
        this.bannerMarkers = Maps.newHashMap();
        this.decorations = Maps.newLinkedHashMap();
        this.frameMarkers = Maps.newHashMap();
    }

    public void setProperties(int i, int i2, int i3, boolean z, boolean z2, ResourceKey<Level> resourceKey) {
        this.scale = (byte) i3;
        setOrigin(i, i2, this.scale);
        this.dimension = resourceKey;
        this.trackingPosition = z;
        this.unlimitedTracking = z2;
        setDirty();
    }

    public void setOrigin(double d, double d2, int i) {
        int i2 = 128 * (1 << i);
        int floor = Mth.floor((d + 64.0d) / i2);
        int floor2 = Mth.floor((d2 + 64.0d) / i2);
        this.x = ((floor * i2) + (i2 / 2)) - 64;
        this.z = ((floor2 * i2) + (i2 / 2)) - 64;
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public void load(CompoundTag compoundTag) {
        DataResult<ResourceKey<Level>> parseLegacy = DimensionType.parseLegacy(new Dynamic(NbtOps.INSTANCE, compoundTag.get("dimension")));
        Logger logger = LOGGER;
        logger.getClass();
        this.dimension = (ResourceKey) parseLegacy.resultOrPartial(logger::error).orElseThrow(() -> {
            return new IllegalArgumentException("Invalid map dimension: " + compoundTag.get("dimension"));
        });
        this.x = compoundTag.getInt("xCenter");
        this.z = compoundTag.getInt("zCenter");
        this.scale = (byte) Mth.clamp((int) compoundTag.getByte("scale"), 0, 4);
        this.trackingPosition = !compoundTag.contains("trackingPosition", 1) || compoundTag.getBoolean("trackingPosition");
        this.unlimitedTracking = compoundTag.getBoolean("unlimitedTracking");
        this.locked = compoundTag.getBoolean("locked");
        this.colors = compoundTag.getByteArray("colors");
        if (this.colors.length != 16384) {
            this.colors = new byte[16384];
        }
        ListTag list = compoundTag.getList("banners", 10);
        for (int i = 0; i < list.size(); i++) {
            MapBanner load = MapBanner.load(list.getCompound(i));
            this.bannerMarkers.put(load.getId(), load);
            addDecoration(load.getDecoration(), null, load.getId(), load.getPos().getX(), load.getPos().getZ(), 180.0d, load.getName());
        }
        ListTag list2 = compoundTag.getList("frames", 10);
        for (int i2 = 0; i2 < list2.size(); i2++) {
            MapFrame load2 = MapFrame.load(list2.getCompound(i2));
            this.frameMarkers.put(load2.getId(), load2);
            addDecoration(MapDecoration.Type.FRAME, null, "frame-" + load2.getEntityId(), load2.getPos().getX(), load2.getPos().getZ(), load2.getRotation(), null);
        }
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public CompoundTag save(CompoundTag compoundTag) {
        DataResult encodeStart = ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.dimension.location());
        Logger logger = LOGGER;
        logger.getClass();
        encodeStart.resultOrPartial(logger::error).ifPresent(tag -> {
            compoundTag.put("dimension", (Tag) tag);
        });
        compoundTag.putInt("xCenter", this.x);
        compoundTag.putInt("zCenter", this.z);
        compoundTag.putByte("scale", this.scale);
        compoundTag.putByteArray("colors", this.colors);
        compoundTag.putBoolean("trackingPosition", this.trackingPosition);
        compoundTag.putBoolean("unlimitedTracking", this.unlimitedTracking);
        compoundTag.putBoolean("locked", this.locked);
        ListTag listTag = new ListTag();
        Iterator<MapBanner> it = this.bannerMarkers.values().iterator();
        while (it.hasNext()) {
            listTag.add(it.next().save());
        }
        compoundTag.put("banners", listTag);
        ListTag listTag2 = new ListTag();
        Iterator<MapFrame> it2 = this.frameMarkers.values().iterator();
        while (it2.hasNext()) {
            listTag2.add(it2.next().save());
        }
        compoundTag.put("frames", listTag2);
        return compoundTag;
    }

    public void lockData(MapItemSavedData mapItemSavedData) {
        this.locked = true;
        this.x = mapItemSavedData.x;
        this.z = mapItemSavedData.z;
        this.bannerMarkers.putAll(mapItemSavedData.bannerMarkers);
        this.decorations.putAll(mapItemSavedData.decorations);
        System.arraycopy(mapItemSavedData.colors, 0, this.colors, 0, mapItemSavedData.colors.length);
        setDirty();
    }

    public void tickCarriedBy(Player player, ItemStack itemStack) {
        if (!this.carriedByPlayers.containsKey(player)) {
            HoldingPlayer holdingPlayer = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, holdingPlayer);
            this.carriedBy.add(holdingPlayer);
        }
        if (!player.inventory.contains(itemStack)) {
            this.decorations.remove(player.getName().getString());
        }
        for (int i = 0; i < this.carriedBy.size(); i++) {
            HoldingPlayer holdingPlayer2 = this.carriedBy.get(i);
            String string = holdingPlayer2.player.getName().getString();
            if (holdingPlayer2.player.removed || (!holdingPlayer2.player.inventory.contains(itemStack) && !itemStack.isFramed())) {
                this.carriedByPlayers.remove(holdingPlayer2.player);
                this.carriedBy.remove(holdingPlayer2);
                this.decorations.remove(string);
            } else if (!itemStack.isFramed() && holdingPlayer2.player.level.dimension() == this.dimension && this.trackingPosition) {
                addDecoration(MapDecoration.Type.PLAYER, holdingPlayer2.player.level, string, holdingPlayer2.player.getX(), holdingPlayer2.player.getZ(), holdingPlayer2.player.yRot, null);
            }
        }
        if (itemStack.isFramed() && this.trackingPosition) {
            ItemFrame frame = itemStack.getFrame();
            BlockPos pos = frame.getPos();
            MapFrame mapFrame = this.frameMarkers.get(MapFrame.frameId(pos));
            if (mapFrame != null && frame.getId() != mapFrame.getEntityId() && this.frameMarkers.containsKey(mapFrame.getId())) {
                this.decorations.remove("frame-" + mapFrame.getEntityId());
            }
            MapFrame mapFrame2 = new MapFrame(pos, frame.getDirection().get2DDataValue() * 90, frame.getId());
            addDecoration(MapDecoration.Type.FRAME, player.level, "frame-" + frame.getId(), pos.getX(), pos.getZ(), frame.getDirection().get2DDataValue() * 90, null);
            this.frameMarkers.put(mapFrame2.getId(), mapFrame2);
        }
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("Decorations", 9)) {
            ListTag list = tag.getList("Decorations", 10);
            for (int i2 = 0; i2 < list.size(); i2++) {
                CompoundTag compound = list.getCompound(i2);
                if (!this.decorations.containsKey(compound.getString("id"))) {
                    addDecoration(MapDecoration.Type.byIcon(compound.getByte("type")), player.level, compound.getString("id"), compound.getDouble("x"), compound.getDouble("z"), compound.getDouble("rot"), null);
                }
            }
        }
    }

    public static void addTargetDecoration(ItemStack itemStack, BlockPos blockPos, String str, MapDecoration.Type type) {
        ListTag listTag;
        if (itemStack.hasTag() && itemStack.getTag().contains("Decorations", 9)) {
            listTag = itemStack.getTag().getList("Decorations", 10);
        } else {
            listTag = new ListTag();
            itemStack.addTagElement("Decorations", listTag);
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putByte("type", type.getIcon());
        compoundTag.putString("id", str);
        compoundTag.putDouble("x", blockPos.getX());
        compoundTag.putDouble("z", blockPos.getZ());
        compoundTag.putDouble("rot", 180.0d);
        listTag.add(compoundTag);
        if (type.hasMapColor()) {
            itemStack.getOrCreateTagElement("display").putInt("MapColor", type.getMapColor());
        }
    }

    private void addDecoration(MapDecoration.Type type, @Nullable LevelAccessor levelAccessor, String str, double d, double d2, double d3, @Nullable Component component) {
        byte b;
        int i = 1 << this.scale;
        float f = ((float) (d - this.x)) / i;
        float f2 = ((float) (d2 - this.z)) / i;
        byte b2 = (byte) ((f * 2.0f) + 0.5d);
        byte b3 = (byte) ((f2 * 2.0f) + 0.5d);
        if (f >= -63.0f && f2 >= -63.0f && f <= 63.0f && f2 <= 63.0f) {
            b = (byte) (((d3 + (d3 < 0.0d ? -8.0d : 8.0d)) * 16.0d) / 360.0d);
            if (this.dimension == Level.NETHER && levelAccessor != null) {
                int dayTime = (int) (levelAccessor.getLevelData().getDayTime() / 10);
                b = (byte) (((((dayTime * dayTime) * 34187121) + (dayTime * 121)) >> 15) & 15);
            }
        } else if (type == MapDecoration.Type.PLAYER) {
            if (Math.abs(f) < 320.0f && Math.abs(f2) < 320.0f) {
                type = MapDecoration.Type.PLAYER_OFF_MAP;
            } else if (this.unlimitedTracking) {
                type = MapDecoration.Type.PLAYER_OFF_LIMITS;
            } else {
                this.decorations.remove(str);
                return;
            }
            b = 0;
            if (f <= -63.0f) {
                b2 = Byte.MIN_VALUE;
            }
            if (f2 <= -63.0f) {
                b3 = Byte.MIN_VALUE;
            }
            if (f >= 63.0f) {
                b2 = Byte.MAX_VALUE;
            }
            if (f2 >= 63.0f) {
                b3 = Byte.MAX_VALUE;
            }
        } else {
            this.decorations.remove(str);
            return;
        }
        this.decorations.put(str, new MapDecoration(type, b2, b3, b, component));
    }

    @Nullable
    public Packet<?> getUpdatePacket(ItemStack itemStack, BlockGetter blockGetter, Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            return null;
        }
        return holdingPlayer.nextUpdatePacket(itemStack);
    }

    public void setDirty(int i, int i2) {
        setDirty();
        Iterator<HoldingPlayer> it = this.carriedBy.iterator();
        while (it.hasNext()) {
            it.next().markDirty(i, i2);
        }
    }

    public HoldingPlayer getHoldingPlayer(Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            holdingPlayer = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, holdingPlayer);
            this.carriedBy.add(holdingPlayer);
        }
        return holdingPlayer;
    }

    public void toggleBanner(LevelAccessor levelAccessor, BlockPos blockPos) {
        MapBanner fromWorld;
        double x = blockPos.getX() + 0.5d;
        double z = blockPos.getZ() + 0.5d;
        int i = 1 << this.scale;
        double d = (x - this.x) / i;
        double d2 = (z - this.z) / i;
        boolean z2 = false;
        if (d < -63.0d || d2 < -63.0d || d > 63.0d || d2 > 63.0d || (fromWorld = MapBanner.fromWorld(levelAccessor, blockPos)) == null) {
            return;
        }
        boolean z3 = true;
        if (this.bannerMarkers.containsKey(fromWorld.getId()) && this.bannerMarkers.get(fromWorld.getId()).equals(fromWorld)) {
            this.bannerMarkers.remove(fromWorld.getId());
            this.decorations.remove(fromWorld.getId());
            z3 = false;
            z2 = true;
        }
        if (z3) {
            this.bannerMarkers.put(fromWorld.getId(), fromWorld);
            addDecoration(fromWorld.getDecoration(), levelAccessor, fromWorld.getId(), x, z, 180.0d, fromWorld.getName());
            z2 = true;
        }
        if (z2) {
            setDirty();
        }
    }

    public void checkBanners(BlockGetter blockGetter, int i, int i2) {
        Iterator<MapBanner> it = this.bannerMarkers.values().iterator();
        while (it.hasNext()) {
            MapBanner next = it.next();
            if (next.getPos().getX() == i && next.getPos().getZ() == i2 && !next.equals(MapBanner.fromWorld(blockGetter, next.getPos()))) {
                it.remove();
                this.decorations.remove(next.getId());
            }
        }
    }

    public void removedFromFrame(BlockPos blockPos, int i) {
        this.decorations.remove("frame-" + i);
        this.frameMarkers.remove(MapFrame.frameId(blockPos));
    }
}
