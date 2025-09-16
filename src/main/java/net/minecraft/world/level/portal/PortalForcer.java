package net.minecraft.world.level.portal;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/portal/PortalForcer.class */
public class PortalForcer {
    private final ServerLevel level;

    public PortalForcer(ServerLevel serverLevel) {
        this.level = serverLevel;
    }

//    public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos blockPos, boolean z) {
//        PoiManager var3 = this.level.getPoiManager();
//        int var4 = z ? 16 : 128;
//        var3.ensureLoadedAndValid(this.level, blockPos, var4);
//        Optional<PoiRecord> var5 = var3.getInSquare((var0) -> var0 == PoiType.NETHER_PORTAL, blockPos, var4, PoiManager.Occupancy.ANY).sorted(Comparator.comparingDouble((var1x) -> var1x.getPos().distSqr(blockPos)).thenComparingInt((var0) -> var0.getPos().getY())).filter((var1x) -> this.level.getBlockState(var1x.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS)).findFirst();
//        return var5.map((var1x) -> {
//            BlockPos var2 = var1x.getPos();
//            this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(var2), 3, var2);
//            BlockState var3_ = this.level.getBlockState(var2);
//            return BlockUtil.getLargestRectangleAround(var2, (Direction.Axis)var3_.getValue(BlockStateProperties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, (var2x) -> this.level.getBlockState(var2x) == var3_);
//        });
//    }
public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos blockPos, boolean isNether) {
    PoiManager poiManager = this.level.getPoiManager();
    int searchRadius = isNether ? 16 : 128;
    poiManager.ensureLoadedAndValid(this.level, blockPos, searchRadius);

    Optional<PoiRecord> portalRecord = poiManager.getInSquare(
                    poiType -> poiType == PoiType.NETHER_PORTAL,
                    blockPos,
                    searchRadius,
                    PoiManager.Occupancy.ANY
            )
            .sorted(Comparator.<PoiRecord>comparingDouble(
                    record -> record.getPos().distSqr(blockPos)
            ).thenComparingInt(
                    record -> record.getPos().getY()
            ))
            .filter(record ->
                    this.level.getBlockState(record.getPos())
                            .hasProperty(BlockStateProperties.HORIZONTAL_AXIS)
            )
            .findFirst();

    return portalRecord.map(record -> {
        BlockPos portalPos = record.getPos();
        this.level.getChunkSource().addRegionTicket(
                TicketType.PORTAL,
                new ChunkPos(portalPos),
                3,
                portalPos
        );
        BlockState portalState = this.level.getBlockState(portalPos);
        return BlockUtil.getLargestRectangleAround(
                portalPos,
                portalState.getValue(BlockStateProperties.HORIZONTAL_AXIS),
                21,
                Direction.Axis.Y,
                21,
                pos -> this.level.getBlockState(pos) == portalState
        );
    });
}
    public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos blockPos, Direction.Axis axis) {
        Direction var3 = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double var4 = (double)-1.0F;
        BlockPos var6 = null;
        double var7 = (double)-1.0F;
        BlockPos var9 = null;
        WorldBorder var10 = this.level.getWorldBorder();
        int var11 = this.level.getHeight() - 1;
        BlockPos.MutableBlockPos var12 = blockPos.mutable();

        for(BlockPos.MutableBlockPos var14 : BlockPos.spiralAround(blockPos, 16, Direction.EAST, Direction.SOUTH)) {
            int var15 = Math.min(var11, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, var14.getX(), var14.getZ()));
            int var16 = 1;
            if (var10.isWithinBounds(var14) && var10.isWithinBounds(var14.move(var3, 1))) {
                var14.move(var3.getOpposite(), 1);

                for(int var17 = var15; var17 >= 0; --var17) {
                    var14.setY(var17);
                    if (this.level.isEmptyBlock(var14)) {
                        int var18;
                        for(var18 = var17; var17 > 0 && this.level.isEmptyBlock(var14.move(Direction.DOWN)); --var17) {
                        }

                        if (var17 + 4 <= var11) {
                            int var19 = var18 - var17;
                            if (var19 <= 0 || var19 >= 3) {
                                var14.setY(var17);
                                if (this.canHostFrame(var14, var12, var3, 0)) {
                                    double var20 = blockPos.distSqr(var14);
                                    if (this.canHostFrame(var14, var12, var3, -1) && this.canHostFrame(var14, var12, var3, 1) && (var4 == (double)-1.0F || var4 > var20)) {
                                        var4 = var20;
                                        var6 = var14.immutable();
                                    }

                                    if (var4 == (double)-1.0F && (var7 == (double)-1.0F || var7 > var20)) {
                                        var7 = var20;
                                        var9 = var14.immutable();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (var4 == (double)-1.0F && var7 != (double)-1.0F) {
            var6 = var9;
            var4 = var7;
        }

        if (var4 == (double)-1.0F) {
            var6 = (new BlockPos(blockPos.getX(), Mth.clamp(blockPos.getY(), 70, this.level.getHeight() - 10), blockPos.getZ())).immutable();
            Direction var22 = var3.getClockWise();
            if (!var10.isWithinBounds(var6)) {
                return Optional.empty();
            }

            for(int var25 = -1; var25 < 2; ++var25) {
                for(int var28 = 0; var28 < 2; ++var28) {
                    for(int var30 = -1; var30 < 3; ++var30) {
                        BlockState var31 = var30 < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        var12.setWithOffset(var6, var28 * var3.getStepX() + var25 * var22.getStepX(), var30, var28 * var3.getStepZ() + var25 * var22.getStepZ());
                        this.level.setBlockAndUpdate(var12, var31);
                    }
                }
            }
        }

        for(int var23 = -1; var23 < 3; ++var23) {
            for(int var26 = -1; var26 < 4; ++var26) {
                if (var23 == -1 || var23 == 2 || var26 == -1 || var26 == 3) {
                    var12.setWithOffset(var6, var23 * var3.getStepX(), var26, var23 * var3.getStepZ());
                    this.level.setBlock(var12, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
            }
        }

        BlockState var24 = (BlockState)Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);

        for(int var27 = 0; var27 < 2; ++var27) {
            for(int var29 = 0; var29 < 3; ++var29) {
                var12.setWithOffset(var6, var27 * var3.getStepX(), var29, var27 * var3.getStepZ());
                this.level.setBlock(var12, var24, 18);
            }
        }

        return Optional.of(new BlockUtil.FoundRectangle(var6.immutable(), 2, 3));
    }

    private boolean canHostFrame(BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos, Direction direction, int i) {
        Direction clockWise = direction.getClockWise();
        for (int i2 = -1; i2 < 3; i2++) {
            for (int i3 = -1; i3 < 4; i3++) {
                mutableBlockPos.setWithOffset(blockPos, (direction.getStepX() * i2) + (clockWise.getStepX() * i), i3, (direction.getStepZ() * i2) + (clockWise.getStepZ() * i));
                if (i3 < 0 && !this.level.getBlockState(mutableBlockPos).getMaterial().isSolid()) {
                    return false;
                }
                if (i3 >= 0 && !this.level.isEmptyBlock(mutableBlockPos)) {
                    return false;
                }
            }
        }
        return true;
    }
}
