package net.minecraft.world.level.border;

import com.google.common.collect.Lists;
import com.mojang.serialization.DynamicLike;
import java.util.Iterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/border/WorldBorder.class */
public class WorldBorder {
    private double centerX;
    private double centerZ;
    public static final Settings DEFAULT_SETTINGS = new Settings(0.0d, 0.0d, 0.2d, 5.0d, 5, 15, 6.0E7d, 0, 0.0d);
    private final List<BorderChangeListener> listeners = Lists.newArrayList();
    private double damagePerBlock = 0.2d;
    private double damageSafeZone = 5.0d;
    private int warningTime = 15;
    private int warningBlocks = 5;
    private int absoluteMaxSize = 29999984;
    private BorderExtent extent = new StaticBorderExtent(6.0E7d);

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/border/WorldBorder$BorderExtent.class */
    interface BorderExtent {
        double getMinX();

        double getMaxX();

        double getMinZ();

        double getMaxZ();

        double getSize();

        double getLerpSpeed();

        long getLerpRemainingTime();

        double getLerpTarget();

        BorderStatus getStatus();

        void onAbsoluteMaxSizeChange();

        void onCenterChange();

        BorderExtent update();

        VoxelShape getCollisionShape();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/border/WorldBorder$MovingBorderExtent.class */
    class MovingBorderExtent implements BorderExtent {
        private final double from;

        /* renamed from: to */
        private final double to;
        private final long lerpEnd;
        private final long lerpBegin;
        private final double lerpDuration;

        private MovingBorderExtent(double d, double d2, long j) {
            this.from = d;
            this.to = d2;
            this.lerpDuration = j;
            this.lerpBegin = Util.getMillis();
            this.lerpEnd = this.lerpBegin + j;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getMinX() {
            return Math.max(WorldBorder.this.getCenterX() - (getSize() / 2.0d), -WorldBorder.this.absoluteMaxSize);
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getMinZ() {
            return Math.max(WorldBorder.this.getCenterZ() - (getSize() / 2.0d), -WorldBorder.this.absoluteMaxSize);
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getMaxX() {
            return Math.min(WorldBorder.this.getCenterX() + (getSize() / 2.0d), WorldBorder.this.absoluteMaxSize);
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getMaxZ() {
            return Math.min(WorldBorder.this.getCenterZ() + (getSize() / 2.0d), WorldBorder.this.absoluteMaxSize);
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getSize() {
            double millis = (Util.getMillis() - this.lerpBegin) / this.lerpDuration;
            return millis < 1.0d ? Mth.lerp(millis, this.from, this.to) : this.to;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getLerpSpeed() {
            return Math.abs(this.from - this.to) / (this.lerpEnd - this.lerpBegin);
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public long getLerpRemainingTime() {
            return this.lerpEnd - Util.getMillis();
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getLerpTarget() {
            return this.to;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public BorderStatus getStatus() {
            return this.to < this.from ? BorderStatus.SHRINKING : BorderStatus.GROWING;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public void onCenterChange() {
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public void onAbsoluteMaxSizeChange() {
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public BorderExtent update() {
            if (getLerpRemainingTime() <= 0) {
                return WorldBorder.this.new StaticBorderExtent(this.to);
            }
            return this;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public VoxelShape getCollisionShape() {
            return Shapes.join(Shapes.INFINITY, Shapes.box(Math.floor(getMinX()), Double.NEGATIVE_INFINITY, Math.floor(getMinZ()), Math.ceil(getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(getMaxZ())), BooleanOp.ONLY_FIRST);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/border/WorldBorder$StaticBorderExtent.class */
    class StaticBorderExtent implements BorderExtent {
        private final double size;
        private double minX;
        private double minZ;
        private double maxX;
        private double maxZ;
        private VoxelShape shape;

        public StaticBorderExtent(double d) {
            this.size = d;
            updateBox();
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getMinX() {
            return this.minX;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getMaxX() {
            return this.maxX;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getMinZ() {
            return this.minZ;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getMaxZ() {
            return this.maxZ;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getSize() {
            return this.size;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public BorderStatus getStatus() {
            return BorderStatus.STATIONARY;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getLerpSpeed() {
            return 0.0d;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public long getLerpRemainingTime() {
            return 0L;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public double getLerpTarget() {
            return this.size;
        }

        private void updateBox() {
            this.minX = Math.max(WorldBorder.this.getCenterX() - (this.size / 2.0d), -WorldBorder.this.absoluteMaxSize);
            this.minZ = Math.max(WorldBorder.this.getCenterZ() - (this.size / 2.0d), -WorldBorder.this.absoluteMaxSize);
            this.maxX = Math.min(WorldBorder.this.getCenterX() + (this.size / 2.0d), WorldBorder.this.absoluteMaxSize);
            this.maxZ = Math.min(WorldBorder.this.getCenterZ() + (this.size / 2.0d), WorldBorder.this.absoluteMaxSize);
            this.shape = Shapes.join(Shapes.INFINITY, Shapes.box(Math.floor(getMinX()), Double.NEGATIVE_INFINITY, Math.floor(getMinZ()), Math.ceil(getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(getMaxZ())), BooleanOp.ONLY_FIRST);
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public void onAbsoluteMaxSizeChange() {
            updateBox();
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public void onCenterChange() {
            updateBox();
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public BorderExtent update() {
            return this;
        }

        @Override // net.minecraft.world.level.border.WorldBorder.BorderExtent
        public VoxelShape getCollisionShape() {
            return this.shape;
        }
    }

    public boolean isWithinBounds(BlockPos blockPos) {
        return ((double) (blockPos.getX() + 1)) > getMinX() && ((double) blockPos.getX()) < getMaxX() && ((double) (blockPos.getZ() + 1)) > getMinZ() && ((double) blockPos.getZ()) < getMaxZ();
    }

    public boolean isWithinBounds(ChunkPos chunkPos) {
        return ((double) chunkPos.getMaxBlockX()) > getMinX() && ((double) chunkPos.getMinBlockX()) < getMaxX() && ((double) chunkPos.getMaxBlockZ()) > getMinZ() && ((double) chunkPos.getMinBlockZ()) < getMaxZ();
    }

    public boolean isWithinBounds(AABB aabb) {
        return aabb.maxX > getMinX() && aabb.minX < getMaxX() && aabb.maxZ > getMinZ() && aabb.minZ < getMaxZ();
    }

    public double getDistanceToBorder(Entity entity) {
        return getDistanceToBorder(entity.getX(), entity.getZ());
    }

    public VoxelShape getCollisionShape() {
        return this.extent.getCollisionShape();
    }

    public double getDistanceToBorder(double d, double d2) {
        double minZ = d2 - getMinZ();
        return Math.min(Math.min(Math.min(d - getMinX(), getMaxX() - d), minZ), getMaxZ() - d2);
    }

    public BorderStatus getStatus() {
        return this.extent.getStatus();
    }

    public double getMinX() {
        return this.extent.getMinX();
    }

    public double getMinZ() {
        return this.extent.getMinZ();
    }

    public double getMaxX() {
        return this.extent.getMaxX();
    }

    public double getMaxZ() {
        return this.extent.getMaxZ();
    }

    public double getCenterX() {
        return this.centerX;
    }

    public double getCenterZ() {
        return this.centerZ;
    }

    public void setCenter(double d, double d2) {
        this.centerX = d;
        this.centerZ = d2;
        this.extent.onCenterChange();
        Iterator<BorderChangeListener> it = getListeners().iterator();
        while (it.hasNext()) {
            it.next().onBorderCenterSet(this, d, d2);
        }
    }

    public double getSize() {
        return this.extent.getSize();
    }

    public long getLerpRemainingTime() {
        return this.extent.getLerpRemainingTime();
    }

    public double getLerpTarget() {
        return this.extent.getLerpTarget();
    }

    public void setSize(double d) {
        this.extent = new StaticBorderExtent(d);
        Iterator<BorderChangeListener> it = getListeners().iterator();
        while (it.hasNext()) {
            it.next().onBorderSizeSet(this, d);
        }
    }

    public void lerpSizeBetween(double d, double d2, long j) {
        this.extent = d == d2 ? new StaticBorderExtent(d2) : new MovingBorderExtent(d, d2, j);
        Iterator<BorderChangeListener> it = getListeners().iterator();
        while (it.hasNext()) {
            it.next().onBorderSizeLerping(this, d, d2, j);
        }
    }

    protected List<BorderChangeListener> getListeners() {
        return Lists.newArrayList(this.listeners);
    }

    public void addListener(BorderChangeListener borderChangeListener) {
        this.listeners.add(borderChangeListener);
    }

    public void setAbsoluteMaxSize(int i) {
        this.absoluteMaxSize = i;
        this.extent.onAbsoluteMaxSizeChange();
    }

    public int getAbsoluteMaxSize() {
        return this.absoluteMaxSize;
    }

    public double getDamageSafeZone() {
        return this.damageSafeZone;
    }

    public void setDamageSafeZone(double d) {
        this.damageSafeZone = d;
        Iterator<BorderChangeListener> it = getListeners().iterator();
        while (it.hasNext()) {
            it.next().onBorderSetDamageSafeZOne(this, d);
        }
    }

    public double getDamagePerBlock() {
        return this.damagePerBlock;
    }

    public void setDamagePerBlock(double d) {
        this.damagePerBlock = d;
        Iterator<BorderChangeListener> it = getListeners().iterator();
        while (it.hasNext()) {
            it.next().onBorderSetDamagePerBlock(this, d);
        }
    }

    public double getLerpSpeed() {
        return this.extent.getLerpSpeed();
    }

    public int getWarningTime() {
        return this.warningTime;
    }

    public void setWarningTime(int i) {
        this.warningTime = i;
        Iterator<BorderChangeListener> it = getListeners().iterator();
        while (it.hasNext()) {
            it.next().onBorderSetWarningTime(this, i);
        }
    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }

    public void setWarningBlocks(int i) {
        this.warningBlocks = i;
        Iterator<BorderChangeListener> it = getListeners().iterator();
        while (it.hasNext()) {
            it.next().onBorderSetWarningBlocks(this, i);
        }
    }

    public void tick() {
        this.extent = this.extent.update();
    }

    public Settings createSettings() {
        return new Settings(null);
    }

    public void applySettings(Settings settings) {
        setCenter(settings.getCenterX(), settings.getCenterZ());
        setDamagePerBlock(settings.getDamagePerBlock());
        setDamageSafeZone(settings.getSafeZone());
        setWarningBlocks(settings.getWarningBlocks());
        setWarningTime(settings.getWarningTime());
        if (settings.getSizeLerpTime() > 0) {
            lerpSizeBetween(settings.getSize(), settings.getSizeLerpTarget(), settings.getSizeLerpTime());
        } else {
            setSize(settings.getSize());
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/border/WorldBorder$Settings.class */
    public static class Settings {
        private final double centerX;
        private final double centerZ;
        private final double damagePerBlock;
        private final double safeZone;
        private final int warningBlocks;
        private final int warningTime;
        private final double size;
        private final long sizeLerpTime;
        private final double sizeLerpTarget;

        private Settings(double d, double d2, double d3, double d4, int i, int i2, double d5, long j, double d6) {
            this.centerX = d;
            this.centerZ = d2;
            this.damagePerBlock = d3;
            this.safeZone = d4;
            this.warningBlocks = i;
            this.warningTime = i2;
            this.size = d5;
            this.sizeLerpTime = j;
            this.sizeLerpTarget = d6;
        }

        private Settings(WorldBorder worldBorder) {
            this.centerX = worldBorder.getCenterX();
            this.centerZ = worldBorder.getCenterZ();
            this.damagePerBlock = worldBorder.getDamagePerBlock();
            this.safeZone = worldBorder.getDamageSafeZone();
            this.warningBlocks = worldBorder.getWarningBlocks();
            this.warningTime = worldBorder.getWarningTime();
            this.size = worldBorder.getSize();
            this.sizeLerpTime = worldBorder.getLerpRemainingTime();
            this.sizeLerpTarget = worldBorder.getLerpTarget();
        }

        public double getCenterX() {
            return this.centerX;
        }

        public double getCenterZ() {
            return this.centerZ;
        }

        public double getDamagePerBlock() {
            return this.damagePerBlock;
        }

        public double getSafeZone() {
            return this.safeZone;
        }

        public int getWarningBlocks() {
            return this.warningBlocks;
        }

        public int getWarningTime() {
            return this.warningTime;
        }

        public double getSize() {
            return this.size;
        }

        public long getSizeLerpTime() {
            return this.sizeLerpTime;
        }

        public double getSizeLerpTarget() {
            return this.sizeLerpTarget;
        }

        public static Settings read(DynamicLike<?> dynamicLike, Settings settings) {
            double asDouble = dynamicLike.get("BorderCenterX").asDouble(settings.centerX);
            double asDouble2 = dynamicLike.get("BorderCenterZ").asDouble(settings.centerZ);
            double asDouble3 = dynamicLike.get("BorderSize").asDouble(settings.size);
            long asLong = dynamicLike.get("BorderSizeLerpTime").asLong(settings.sizeLerpTime);
            double asDouble4 = dynamicLike.get("BorderSizeLerpTarget").asDouble(settings.sizeLerpTarget);
            return new Settings(asDouble, asDouble2, dynamicLike.get("BorderDamagePerBlock").asDouble(settings.damagePerBlock), dynamicLike.get("BorderSafeZone").asDouble(settings.safeZone), dynamicLike.get("BorderWarningBlocks").asInt(settings.warningBlocks), dynamicLike.get("BorderWarningTime").asInt(settings.warningTime), asDouble3, asLong, asDouble4);
        }

        public void write(CompoundTag compoundTag) {
            compoundTag.putDouble("BorderCenterX", this.centerX);
            compoundTag.putDouble("BorderCenterZ", this.centerZ);
            compoundTag.putDouble("BorderSize", this.size);
            compoundTag.putLong("BorderSizeLerpTime", this.sizeLerpTime);
            compoundTag.putDouble("BorderSafeZone", this.safeZone);
            compoundTag.putDouble("BorderDamagePerBlock", this.damagePerBlock);
            compoundTag.putDouble("BorderSizeLerpTarget", this.sizeLerpTarget);
            compoundTag.putDouble("BorderWarningBlocks", this.warningBlocks);
            compoundTag.putDouble("BorderWarningTime", this.warningTime);
        }
    }
}
