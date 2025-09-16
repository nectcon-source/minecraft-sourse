package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/RailBlock.class */
public class RailBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

    protected RailBlock(BlockBehaviour.Properties properties) {
        super(false, properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH));
    }

    @Override // net.minecraft.world.level.block.BaseRailBlock
    protected void updateState(BlockState blockState, Level level, BlockPos blockPos, Block block) {
        if (block.defaultBlockState().isSignalSource() && new RailState(level, blockPos, blockState).countPotentialConnections() == 3) {
            updateDir(level, blockPos, blockState, false);
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
                case NORTH_SOUTH:
                    return var1.setValue(SHAPE, RailShape.EAST_WEST);
                case EAST_WEST:
                    return var1.setValue(SHAPE, RailShape.NORTH_SOUTH);
            }
            case CLOCKWISE_90:
                switch((RailShape)var1.getValue(SHAPE)) {
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
                case NORTH_SOUTH:
                    return var1.setValue(SHAPE, RailShape.EAST_WEST);
                case EAST_WEST:
                    return var1.setValue(SHAPE, RailShape.NORTH_SOUTH);
            }
            default:
                return var1;
        }
    }



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
        builder.add(SHAPE);
    }
}
