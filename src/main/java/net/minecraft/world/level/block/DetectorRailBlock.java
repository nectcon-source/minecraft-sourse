package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DetectorRailBlock.class */
public class DetectorRailBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public DetectorRailBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
        registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any().setValue(POWERED, false)).setValue(SHAPE, RailShape.NORTH_SOUTH));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (level.isClientSide || ((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            return;
        }
        checkPressed(level, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            return;
        }
        checkPressed(serverLevel, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return ((Boolean) blockState.getValue(POWERED)).booleanValue() ? 15 : 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return (((Boolean) blockState.getValue(POWERED)).booleanValue() && direction == Direction.UP) ? 15 : 0;
    }

    private void checkPressed(Level level, BlockPos blockPos, BlockState blockState) {
        if (!canSurvive(blockState, level, blockPos)) {
            return;
        }
        boolean booleanValue = ((Boolean) blockState.getValue(POWERED)).booleanValue();
        boolean z = false;
        if (!getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, null).isEmpty()) {
            z = true;
        }
        if (z && !booleanValue) {
            BlockState blockState2 = (BlockState) blockState.setValue(POWERED, true);
            level.setBlock(blockPos, blockState2, 3);
            updatePowerToConnected(level, blockPos, blockState2, true);
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.below(), this);
            level.setBlocksDirty(blockPos, blockState, blockState2);
        }
        if (!z && booleanValue) {
            BlockState blockState3 = (BlockState) blockState.setValue(POWERED, false);
            level.setBlock(blockPos, blockState3, 3);
            updatePowerToConnected(level, blockPos, blockState3, false);
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.below(), this);
            level.setBlocksDirty(blockPos, blockState, blockState3);
        }
        if (z) {
            level.getBlockTicks().scheduleTick(blockPos, this, 20);
        }
        level.updateNeighbourForOutputSignal(blockPos, this);
    }

    protected void updatePowerToConnected(Level level, BlockPos blockPos, BlockState blockState, boolean z) {
        for (BlockPos blockPos2 : new RailState(level, blockPos, blockState).getConnections()) {
            BlockState blockState2 = level.getBlockState(blockPos2);
            blockState2.neighborChanged(level, blockPos2, blockState2.getBlock(), blockPos, false);
        }
    }

    @Override // net.minecraft.world.level.block.BaseRailBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        checkPressed(level, blockPos, updateState(blockState, level, blockPos, z));
    }

    @Override // net.minecraft.world.level.block.BaseRailBlock
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        if (((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            List<MinecartCommandBlock> interactingMinecartOfType = getInteractingMinecartOfType(level, blockPos, MinecartCommandBlock.class, null);
            if (!interactingMinecartOfType.isEmpty()) {
                return interactingMinecartOfType.get(0).getCommandBlock().getSuccessCount();
            }
            List<AbstractMinecart> interactingMinecartOfType2 = getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, EntitySelector.CONTAINER_ENTITY_SELECTOR);
            if (!interactingMinecartOfType2.isEmpty()) {
                return AbstractContainerMenu.getRedstoneSignalFromContainer((Container) interactingMinecartOfType2.get(0));
            }
            return 0;
        }
        return 0;
    }

    protected <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level level, BlockPos blockPos, Class<T> cls, @Nullable Predicate<Entity> predicate) {
        return level.getEntitiesOfClass(cls, getSearchBB(blockPos), predicate);
    }

    private AABB getSearchBB(BlockPos blockPos) {
        return new AABB(blockPos.getX() + 0.2d, blockPos.getY(), blockPos.getZ() + 0.2d, (blockPos.getX() + 1) - 0.2d, (blockPos.getY() + 1) - 0.2d, (blockPos.getZ() + 1) - 0.2d);
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
        builder.add(SHAPE, POWERED);
    }
}
