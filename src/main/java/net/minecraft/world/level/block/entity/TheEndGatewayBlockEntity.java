package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.Features;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/TheEndGatewayBlockEntity.class */
public class TheEndGatewayBlockEntity extends TheEndPortalBlockEntity implements TickableBlockEntity {
    private static final Logger LOGGER = LogManager.getLogger();
    private long age;
    private int teleportCooldown;

    @Nullable
    private BlockPos exitPortal;
    private boolean exactTeleport;

    public TheEndGatewayBlockEntity() {
        super(BlockEntityType.END_GATEWAY);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.putLong("Age", this.age);
        if (this.exitPortal != null) {
            compoundTag.put("ExitPortal", NbtUtils.writeBlockPos(this.exitPortal));
        }
        if (this.exactTeleport) {
            compoundTag.putBoolean("ExactTeleport", this.exactTeleport);
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.age = compoundTag.getLong("Age");
        if (compoundTag.contains("ExitPortal", 10)) {
            this.exitPortal = NbtUtils.readBlockPos(compoundTag.getCompound("ExitPortal"));
        }
        this.exactTeleport = compoundTag.getBoolean("ExactTeleport");
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public double getViewDistance() {
        return 256.0d;
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        boolean isSpawning = isSpawning();
        boolean isCoolingDown = isCoolingDown();
        this.age++;
        if (isCoolingDown) {
            this.teleportCooldown--;
        } else if (!this.level.isClientSide) {
            List<Entity> entitiesOfClass = this.level.getEntitiesOfClass(Entity.class, new AABB(getBlockPos()), TheEndGatewayBlockEntity::canEntityTeleport);
            if (!entitiesOfClass.isEmpty()) {
                teleportEntity(entitiesOfClass.get(this.level.random.nextInt(entitiesOfClass.size())));
            }
            if (this.age % 2400 == 0) {
                triggerCooldown();
            }
        }
        if (isSpawning != isSpawning() || isCoolingDown != isCoolingDown()) {
            setChanged();
        }
    }

    public static boolean canEntityTeleport(Entity entity) {
        return EntitySelector.NO_SPECTATORS.test(entity) && !entity.getRootVehicle().isOnPortalCooldown();
    }

    public boolean isSpawning() {
        return this.age < 200;
    }

    public boolean isCoolingDown() {
        return this.teleportCooldown > 0;
    }

    public float getSpawnPercent(float f) {
        return Mth.clamp((this.age + f) / 200.0f, 0.0f, 1.0f);
    }

    public float getCooldownPercent(float f) {
        return 1.0f - Mth.clamp((this.teleportCooldown - f) / 40.0f, 0.0f, 1.0f);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 8, getUpdateTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    public void triggerCooldown() {
        if (!this.level.isClientSide) {
            this.teleportCooldown = 40;
            this.level.blockEvent(getBlockPos(), getBlockState().getBlock(), 1, 0);
            setChanged();
        }
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public boolean triggerEvent(int i, int i2) {
        if (i == 1) {
            this.teleportCooldown = 40;
            return true;
        }
        return super.triggerEvent(i, i2);
    }

    public void teleportEntity(Entity entity) {
        Entity rootVehicle;
        if (!(this.level instanceof ServerLevel) || isCoolingDown()) {
            return;
        }
        this.teleportCooldown = 100;
        if (this.exitPortal == null && this.level.dimension() == Level.END) {
            findExitPortal((ServerLevel) this.level);
        }
        if (this.exitPortal != null) {
            BlockPos findExitPosition = this.exactTeleport ? this.exitPortal : findExitPosition();
            if (entity instanceof ThrownEnderpearl) {
                Entity owner = ((ThrownEnderpearl) entity).getOwner();
                if (owner instanceof ServerPlayer) {
                    CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer) owner, this.level.getBlockState(getBlockPos()));
                }
                if (owner != null) {
                    rootVehicle = owner;
                    entity.remove();
                } else {
                    rootVehicle = entity;
                }
            } else {
                rootVehicle = entity.getRootVehicle();
            }
            rootVehicle.setPortalCooldown();
            rootVehicle.teleportToWithTicket(findExitPosition.getX() + 0.5d, findExitPosition.getY(), findExitPosition.getZ() + 0.5d);
        }
        triggerCooldown();
    }

    private BlockPos findExitPosition() {
        BlockPos findTallestBlock = findTallestBlock(this.level, this.exitPortal.offset(0, 2, 0), 5, false);
        LOGGER.debug("Best exit position for portal at {} is {}", this.exitPortal, findTallestBlock);
        return findTallestBlock.above();
    }

    private void findExitPortal(ServerLevel serverLevel) {
        Vec3 normalize = new Vec3(getBlockPos().getX(), 0.0d, getBlockPos().getZ()).normalize();
        Vec3 scale = normalize.scale(1024.0d);
        int i = 16;
        while (getChunk(serverLevel, scale).getHighestSectionPosition() > 0) {
            int i2 = i;
            i--;
            if (i2 <= 0) {
                break;
            }
            LOGGER.debug("Skipping backwards past nonempty chunk at {}", scale);
            scale = scale.add(normalize.scale(-16.0d));
        }
        int i3 = 16;
        while (getChunk(serverLevel, scale).getHighestSectionPosition() == 0) {
            int i4 = i3;
            i3--;
            if (i4 <= 0) {
                break;
            }
            LOGGER.debug("Skipping forward past empty chunk at {}", scale);
            scale = scale.add(normalize.scale(16.0d));
        }
        LOGGER.debug("Found chunk at {}", scale);
        this.exitPortal = findValidSpawnInChunk(getChunk(serverLevel, scale));
        if (this.exitPortal == null) {
            this.exitPortal = new BlockPos(scale.x + 0.5d, 75.0d, scale.z + 0.5d);
            LOGGER.debug("Failed to find suitable block, settling on {}", this.exitPortal);
            Features.END_ISLAND.place(serverLevel, serverLevel.getChunkSource().getGenerator(), new Random(this.exitPortal.asLong()), this.exitPortal);
        } else {
            LOGGER.debug("Found block at {}", this.exitPortal);
        }
        this.exitPortal = findTallestBlock(serverLevel, this.exitPortal, 16, true);
        LOGGER.debug("Creating portal at {}", this.exitPortal);
        this.exitPortal = this.exitPortal.above(10);
        createExitPortal(serverLevel, this.exitPortal);
        setChanged();
    }

    /* JADX WARN: Code restructure failed: missing block: B:27:0x007d, code lost:
    
        r11 = r0;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private static net.minecraft.core.BlockPos findTallestBlock(net.minecraft.world.level.BlockGetter var0, net.minecraft.core.BlockPos var1, int var2, boolean var3) {
        BlockPos var4 = null;

        for(int var5x = -var2; var5x <= var2; ++var5x) {
            for(int var6xx = -var2; var6xx <= var2; ++var6xx) {
                if (var5x != 0 || var6xx != 0 || var3) {
                    for(int var7xxx = 255; var7xxx > (var4 == null ? 0 : var4.getY()); --var7xxx) {
                        BlockPos var8xxxx = new BlockPos(var1.getX() + var5x, var7xxx, var1.getZ() + var6xx);
                        BlockState var9xxxxx = var0.getBlockState(var8xxxx);
                        if (var9xxxxx.isCollisionShapeFullBlock(var0, var8xxxx) && (var3 || !var9xxxxx.is(Blocks.BEDROCK))) {
                            var4 = var8xxxx;
                            break;
                        }
                    }
                }
            }
        }

        return var4 == null ? var1 : var4;
    }

    private static LevelChunk getChunk(Level level, Vec3 vec3) {
        return level.getChunk(Mth.floor(vec3.x / 16.0d), Mth.floor(vec3.z / 16.0d));
    }

    @Nullable
    private static BlockPos findValidSpawnInChunk(LevelChunk levelChunk) {
        ChunkPos pos = levelChunk.getPos();
        BlockPos blockPos = null;
        double d = 0.0d;
        for (BlockPos blockPos2 : BlockPos.betweenClosed(new BlockPos(pos.getMinBlockX(), 30, pos.getMinBlockZ()), new BlockPos(pos.getMaxBlockX(), (levelChunk.getHighestSectionPosition() + 16) - 1, pos.getMaxBlockZ()))) {
            BlockState blockState = levelChunk.getBlockState(blockPos2);
            BlockPos above = blockPos2.above();
            BlockPos above2 = blockPos2.above(2);
            if (blockState.is(Blocks.END_STONE) && !levelChunk.getBlockState(above).isCollisionShapeFullBlock(levelChunk, above) && !levelChunk.getBlockState(above2).isCollisionShapeFullBlock(levelChunk, above2)) {
                double distSqr = blockPos2.distSqr(0.0d, 0.0d, 0.0d, true);
                if (blockPos == null || distSqr < d) {
                    blockPos = blockPos2;
                    d = distSqr;
                }
            }
        }
        return blockPos;
    }

    private void createExitPortal(ServerLevel serverLevel, BlockPos blockPos) {
        Feature.END_GATEWAY.configured(EndGatewayConfiguration.knownExit(getBlockPos(), false)).place(serverLevel, serverLevel.getChunkSource().getGenerator(), new Random(), blockPos);
    }

    @Override // net.minecraft.world.level.block.entity.TheEndPortalBlockEntity
    public boolean shouldRenderFace(Direction direction) {
        return Block.shouldRenderFace(getBlockState(), this.level, getBlockPos(), direction);
    }

    public int getParticleAmount() {
        int i = 0;
        for (Direction direction : Direction.values()) {
            i += shouldRenderFace(direction) ? 1 : 0;
        }
        return i;
    }

    public void setExitPosition(BlockPos blockPos, boolean z) {
        this.exactTeleport = z;
        this.exitPortal = blockPos;
    }
}
