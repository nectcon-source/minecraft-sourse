package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/RailState.class */
public class RailState {
    private final Level level;
    private final BlockPos pos;
    private final BaseRailBlock block;
    private BlockState state;
    private final boolean isStraight;
    private final List<BlockPos> connections = Lists.newArrayList();

    public RailState(Level level, BlockPos blockPos, BlockState blockState) {
        this.level = level;
        this.pos = blockPos;
        this.state = blockState;
        this.block = (BaseRailBlock) blockState.getBlock();
        RailShape railShape = (RailShape) blockState.getValue(this.block.getShapeProperty());
        this.isStraight = this.block.isStraight();
        updateConnections(railShape);
    }

    public List<BlockPos> getConnections() {
        return this.connections;
    }

    private void updateConnections(RailShape railShape) {
        this.connections.clear();
        switch (railShape) {
            case NORTH_SOUTH:
                this.connections.add(this.pos.north());
                this.connections.add(this.pos.south());
                break;
            case EAST_WEST:
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.east());
                break;
            case ASCENDING_EAST:
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.east().above());
                break;
            case ASCENDING_WEST:
                this.connections.add(this.pos.west().above());
                this.connections.add(this.pos.east());
                break;
            case ASCENDING_NORTH:
                this.connections.add(this.pos.north().above());
                this.connections.add(this.pos.south());
                break;
            case ASCENDING_SOUTH:
                this.connections.add(this.pos.north());
                this.connections.add(this.pos.south().above());
                break;
            case SOUTH_EAST:
                this.connections.add(this.pos.east());
                this.connections.add(this.pos.south());
                break;
            case SOUTH_WEST:
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.south());
                break;
            case NORTH_WEST:
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.north());
                break;
            case NORTH_EAST:
                this.connections.add(this.pos.east());
                this.connections.add(this.pos.north());
                break;
        }
    }

    private void removeSoftConnections() {
        int i = 0;
        while (i < this.connections.size()) {
            RailState rail = getRail(this.connections.get(i));
            if (rail == null || !rail.connectsTo(this)) {
                int i2 = i;
                i--;
                this.connections.remove(i2);
            } else {
                this.connections.set(i, rail.pos);
            }
            i++;
        }
    }

    private boolean hasRail(BlockPos blockPos) {
        return BaseRailBlock.isRail(this.level, blockPos) || BaseRailBlock.isRail(this.level, blockPos.above()) || BaseRailBlock.isRail(this.level, blockPos.below());
    }

    @Nullable
    private RailState getRail(BlockPos blockPos) {
        BlockState blockState = this.level.getBlockState(blockPos);
        if (BaseRailBlock.isRail(blockState)) {
            return new RailState(this.level, blockPos, blockState);
        }
        BlockPos above = blockPos.above();
        BlockState blockState2 = this.level.getBlockState(above);
        if (BaseRailBlock.isRail(blockState2)) {
            return new RailState(this.level, above, blockState2);
        }
        BlockPos below = blockPos.below();
        BlockState blockState3 = this.level.getBlockState(below);
        if (BaseRailBlock.isRail(blockState3)) {
            return new RailState(this.level, below, blockState3);
        }
        return null;
    }

    private boolean connectsTo(RailState railState) {
        return hasConnection(railState.pos);
    }

    private boolean hasConnection(BlockPos blockPos) {
        for (int i = 0; i < this.connections.size(); i++) {
            BlockPos blockPos2 = this.connections.get(i);
            if (blockPos2.getX() == blockPos.getX() && blockPos2.getZ() == blockPos.getZ()) {
                return true;
            }
        }
        return false;
    }

    protected int countPotentialConnections() {
        int i = 0;
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            if (hasRail(this.pos.relative(it.next()))) {
                i++;
            }
        }
        return i;
    }

    private boolean canConnectTo(RailState railState) {
        return connectsTo(railState) || this.connections.size() != 2;
    }

    private void connectTo(RailState railState) {
        this.connections.add(railState.pos);
        BlockPos north = this.pos.north();
        BlockPos south = this.pos.south();
        BlockPos west = this.pos.west();
        BlockPos east = this.pos.east();
        boolean hasConnection = hasConnection(north);
        boolean hasConnection2 = hasConnection(south);
        boolean hasConnection3 = hasConnection(west);
        boolean hasConnection4 = hasConnection(east);
        RailShape railShape = null;
        if (hasConnection || hasConnection2) {
            railShape = RailShape.NORTH_SOUTH;
        }
        if (hasConnection3 || hasConnection4) {
            railShape = RailShape.EAST_WEST;
        }
        if (!this.isStraight) {
            if (hasConnection2 && hasConnection4 && !hasConnection && !hasConnection3) {
                railShape = RailShape.SOUTH_EAST;
            }
            if (hasConnection2 && hasConnection3 && !hasConnection && !hasConnection4) {
                railShape = RailShape.SOUTH_WEST;
            }
            if (hasConnection && hasConnection3 && !hasConnection2 && !hasConnection4) {
                railShape = RailShape.NORTH_WEST;
            }
            if (hasConnection && hasConnection4 && !hasConnection2 && !hasConnection3) {
                railShape = RailShape.NORTH_EAST;
            }
        }
        if (railShape == RailShape.NORTH_SOUTH) {
            if (BaseRailBlock.isRail(this.level, north.above())) {
                railShape = RailShape.ASCENDING_NORTH;
            }
            if (BaseRailBlock.isRail(this.level, south.above())) {
                railShape = RailShape.ASCENDING_SOUTH;
            }
        }
        if (railShape == RailShape.EAST_WEST) {
            if (BaseRailBlock.isRail(this.level, east.above())) {
                railShape = RailShape.ASCENDING_EAST;
            }
            if (BaseRailBlock.isRail(this.level, west.above())) {
                railShape = RailShape.ASCENDING_WEST;
            }
        }
        if (railShape == null) {
            railShape = RailShape.NORTH_SOUTH;
        }
        this.state = (BlockState) this.state.setValue(this.block.getShapeProperty(), railShape);
        this.level.setBlock(this.pos, this.state, 3);
    }

    private boolean hasNeighborRail(BlockPos blockPos) {
        RailState rail = getRail(blockPos);
        if (rail == null) {
            return false;
        }
        rail.removeSoftConnections();
        return rail.canConnectTo(this);
    }

    public RailState place(boolean z, boolean z2, RailShape railShape) {
        BlockPos north = this.pos.north();
        BlockPos south = this.pos.south();
        BlockPos west = this.pos.west();
        BlockPos east = this.pos.east();
        boolean hasNeighborRail = hasNeighborRail(north);
        boolean hasNeighborRail2 = hasNeighborRail(south);
        boolean hasNeighborRail3 = hasNeighborRail(west);
        boolean hasNeighborRail4 = hasNeighborRail(east);
        RailShape railShape2 = null;
        boolean z3 = hasNeighborRail || hasNeighborRail2;
        boolean z4 = hasNeighborRail3 || hasNeighborRail4;
        if (z3 && !z4) {
            railShape2 = RailShape.NORTH_SOUTH;
        }
        if (z4 && !z3) {
            railShape2 = RailShape.EAST_WEST;
        }
        boolean z5 = hasNeighborRail2 && hasNeighborRail4;
        boolean z6 = hasNeighborRail2 && hasNeighborRail3;
        boolean z7 = hasNeighborRail && hasNeighborRail4;
        boolean z8 = hasNeighborRail && hasNeighborRail3;
        if (!this.isStraight) {
            if (z5 && !hasNeighborRail && !hasNeighborRail3) {
                railShape2 = RailShape.SOUTH_EAST;
            }
            if (z6 && !hasNeighborRail && !hasNeighborRail4) {
                railShape2 = RailShape.SOUTH_WEST;
            }
            if (z8 && !hasNeighborRail2 && !hasNeighborRail4) {
                railShape2 = RailShape.NORTH_WEST;
            }
            if (z7 && !hasNeighborRail2 && !hasNeighborRail3) {
                railShape2 = RailShape.NORTH_EAST;
            }
        }
        if (railShape2 == null) {
            if (z3 && z4) {
                railShape2 = railShape;
            } else if (z3) {
                railShape2 = RailShape.NORTH_SOUTH;
            } else if (z4) {
                railShape2 = RailShape.EAST_WEST;
            }
            if (!this.isStraight) {
                if (z) {
                    if (z5) {
                        railShape2 = RailShape.SOUTH_EAST;
                    }
                    if (z6) {
                        railShape2 = RailShape.SOUTH_WEST;
                    }
                    if (z7) {
                        railShape2 = RailShape.NORTH_EAST;
                    }
                    if (z8) {
                        railShape2 = RailShape.NORTH_WEST;
                    }
                } else {
                    if (z8) {
                        railShape2 = RailShape.NORTH_WEST;
                    }
                    if (z7) {
                        railShape2 = RailShape.NORTH_EAST;
                    }
                    if (z6) {
                        railShape2 = RailShape.SOUTH_WEST;
                    }
                    if (z5) {
                        railShape2 = RailShape.SOUTH_EAST;
                    }
                }
            }
        }
        if (railShape2 == RailShape.NORTH_SOUTH) {
            if (BaseRailBlock.isRail(this.level, north.above())) {
                railShape2 = RailShape.ASCENDING_NORTH;
            }
            if (BaseRailBlock.isRail(this.level, south.above())) {
                railShape2 = RailShape.ASCENDING_SOUTH;
            }
        }
        if (railShape2 == RailShape.EAST_WEST) {
            if (BaseRailBlock.isRail(this.level, east.above())) {
                railShape2 = RailShape.ASCENDING_EAST;
            }
            if (BaseRailBlock.isRail(this.level, west.above())) {
                railShape2 = RailShape.ASCENDING_WEST;
            }
        }
        if (railShape2 == null) {
            railShape2 = railShape;
        }
        updateConnections(railShape2);
        this.state = (BlockState) this.state.setValue(this.block.getShapeProperty(), railShape2);
        if (z2 || this.level.getBlockState(this.pos) != this.state) {
            this.level.setBlock(this.pos, this.state, 3);
            for (int i = 0; i < this.connections.size(); i++) {
                RailState rail = getRail(this.connections.get(i));
                if (rail != null) {
                    rail.removeSoftConnections();
                    if (rail.canConnectTo(this)) {
                        rail.connectTo(this);
                    }
                }
            }
        }
        return this;
    }

    public BlockState getState() {
        return this.state;
    }
}
