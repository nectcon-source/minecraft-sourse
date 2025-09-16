package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/PoweredRailBlock.class */
public class PoweredRailBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected PoweredRailBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
        registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH)).setValue(POWERED, false));
    }

    protected boolean findPoweredRailSignal(Level level, BlockPos blockPos, BlockState blockState, boolean z, int i) {
        if (i >= 8) {
            return false;
        }
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z2 = blockPos.getZ();
        boolean z3 = true;
        RailShape railShape = (RailShape) blockState.getValue(SHAPE);
        switch (railShape) {
            case NORTH_SOUTH:
                if (z) {
                    z2++;
                    break;
                } else {
                    z2--;
                    break;
                }
            case EAST_WEST:
                if (z) {
                    x--;
                    break;
                } else {
                    x++;
                    break;
                }
            case ASCENDING_EAST:
                if (z) {
                    x--;
                } else {
                    x++;
                    y++;
                    z3 = false;
                }
                railShape = RailShape.EAST_WEST;
                break;
            case ASCENDING_WEST:
                if (z) {
                    x--;
                    y++;
                    z3 = false;
                } else {
                    x++;
                }
                railShape = RailShape.EAST_WEST;
                break;
            case ASCENDING_NORTH:
                if (z) {
                    z2++;
                } else {
                    z2--;
                    y++;
                    z3 = false;
                }
                railShape = RailShape.NORTH_SOUTH;
                break;
            case ASCENDING_SOUTH:
                if (z) {
                    z2++;
                    y++;
                    z3 = false;
                } else {
                    z2--;
                }
                railShape = RailShape.NORTH_SOUTH;
                break;
        }
        if (isSameRailWithPower(level, new BlockPos(x, y, z2), z, i, railShape)) {
            return true;
        }
        if (z3 && isSameRailWithPower(level, new BlockPos(x, y - 1, z2), z, i, railShape)) {
            return true;
        }
        return false;
    }

    protected boolean isSameRailWithPower(Level level, BlockPos blockPos, boolean z, int i, RailShape railShape) {
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.is(this)) {
            return false;
        }
        RailShape railShape2 = (RailShape) blockState.getValue(SHAPE);
        if (railShape == RailShape.EAST_WEST && (railShape2 == RailShape.NORTH_SOUTH || railShape2 == RailShape.ASCENDING_NORTH || railShape2 == RailShape.ASCENDING_SOUTH)) {
            return false;
        }
        if ((railShape != RailShape.NORTH_SOUTH || (railShape2 != RailShape.EAST_WEST && railShape2 != RailShape.ASCENDING_EAST && railShape2 != RailShape.ASCENDING_WEST)) && ((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            if (level.hasNeighborSignal(blockPos)) {
                return true;
            }
            return findPoweredRailSignal(level, blockPos, blockState, z, i + 1);
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.BaseRailBlock
    protected void updateState(BlockState blockState, Level level, BlockPos blockPos, Block block) {
        boolean booleanValue = ((Boolean) blockState.getValue(POWERED)).booleanValue();
        boolean z = level.hasNeighborSignal(blockPos) || findPoweredRailSignal(level, blockPos, blockState, true, 0) || findPoweredRailSignal(level, blockPos, blockState, false, 0);
        if (z != booleanValue) {
            level.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, Boolean.valueOf(z)), 3);
            level.updateNeighborsAt(blockPos.below(), this);
            if (((RailShape) blockState.getValue(SHAPE)).isAscending()) {
                level.updateNeighborsAt(blockPos.above(), this);
            }
        }
    }

    @Override // net.minecraft.world.level.block.BaseRailBlock
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Failed to find switch 'out' block (already processed)
        	at jadx.core.dex.visitors.regions.maker.SwitchRegionMaker.calcSwitchOut(SwitchRegionMaker.java:202)
        	at jadx.core.dex.visitors.regions.maker.SwitchRegionMaker.process(SwitchRegionMaker.java:61)
        	at jadx.core.dex.visitors.regions.maker.RegionMaker.traverse(RegionMaker.java:115)
        	at jadx.core.dex.visitors.regions.maker.RegionMaker.makeRegion(RegionMaker.java:69)
        	at jadx.core.dex.visitors.regions.maker.SwitchRegionMaker.processFallThroughCases(SwitchRegionMaker.java:105)
        	at jadx.core.dex.visitors.regions.maker.SwitchRegionMaker.process(SwitchRegionMaker.java:64)
        	at jadx.core.dex.visitors.regions.maker.RegionMaker.traverse(RegionMaker.java:115)
        	at jadx.core.dex.visitors.regions.maker.RegionMaker.makeRegion(RegionMaker.java:69)
        	at jadx.core.dex.visitors.regions.maker.RegionMaker.makeMthRegion(RegionMaker.java:49)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:25)
        */
    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public net.minecraft.world.level.block.state.BlockState rotate(net.minecraft.world.level.block.state.BlockState var1, net.minecraft.world.level.block.Rotation var2) {
        switch(var2) {
            case CLOCKWISE_180:
                switch((RailShape)var1.getValue(SHAPE)) {
                case ASCENDING_EAST:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_WEST);
                case ASCENDING_WEST:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_EAST);
                case ASCENDING_NORTH:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_SOUTH:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                case SOUTH_EAST:
                    return var1.setValue(SHAPE, RailShape.NORTH_WEST);
                case SOUTH_WEST:
                    return var1.setValue(SHAPE, RailShape.NORTH_EAST);
                case NORTH_WEST:
                    return var1.setValue(SHAPE, RailShape.SOUTH_EAST);
                case NORTH_EAST:
                    return var1.setValue(SHAPE, RailShape.SOUTH_WEST);
            }
            case COUNTERCLOCKWISE_90:
                switch((RailShape)var1.getValue(SHAPE)) {
                case NORTH_SOUTH:
                    return var1.setValue(SHAPE, RailShape.EAST_WEST);
                case EAST_WEST:
                    return var1.setValue(SHAPE, RailShape.NORTH_SOUTH);
                case ASCENDING_EAST:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                case ASCENDING_WEST:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_NORTH:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_WEST);
                case ASCENDING_SOUTH:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_EAST);
                case SOUTH_EAST:
                    return var1.setValue(SHAPE, RailShape.NORTH_EAST);
                case SOUTH_WEST:
                    return var1.setValue(SHAPE, RailShape.SOUTH_EAST);
                case NORTH_WEST:
                    return var1.setValue(SHAPE, RailShape.SOUTH_WEST);
                case NORTH_EAST:
                    return var1.setValue(SHAPE, RailShape.NORTH_WEST);
            }
            case CLOCKWISE_90:
                switch((RailShape)var1.getValue(SHAPE)) {
                case NORTH_SOUTH:
                    return var1.setValue(SHAPE, RailShape.EAST_WEST);
                case EAST_WEST:
                    return var1.setValue(SHAPE, RailShape.NORTH_SOUTH);
                case ASCENDING_EAST:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_WEST:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                case ASCENDING_NORTH:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_EAST);
                case ASCENDING_SOUTH:
                    return var1.setValue(SHAPE, RailShape.ASCENDING_WEST);
                case SOUTH_EAST:
                    return var1.setValue(SHAPE, RailShape.SOUTH_WEST);
                case SOUTH_WEST:
                    return var1.setValue(SHAPE, RailShape.NORTH_WEST);
                case NORTH_WEST:
                    return var1.setValue(SHAPE, RailShape.NORTH_EAST);
                case NORTH_EAST:
                    return var1.setValue(SHAPE, RailShape.SOUTH_EAST);
            }
            default:
                return var1;
        }
    }

    /* renamed from: net.minecraft.world.level.block.PoweredRailBlock$1 */
    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/PoweredRailBlock$1.class */


    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        RailShape railShape = (RailShape) blockState.getValue(SHAPE);
        switch (mirror) {
            case LEFT_RIGHT:
                switch (railShape) {
                    case ASCENDING_NORTH:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_SOUTH:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    case SOUTH_EAST:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.NORTH_EAST);
                    case SOUTH_WEST:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.NORTH_WEST);
                    case NORTH_WEST:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case NORTH_EAST:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
                }
            case FRONT_BACK:
                switch (railShape) {
                    case ASCENDING_EAST:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_WEST:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    case SOUTH_EAST:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case SOUTH_WEST:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_WEST:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_EAST:
                        return (BlockState) blockState.setValue(SHAPE, RailShape.NORTH_WEST);
                }
        }
        return super.mirror(blockState, mirror);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, POWERED);
    }
}
