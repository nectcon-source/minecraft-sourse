package net.minecraft.world.level.block;

import com.google.common.collect.UnmodifiableIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/CrossCollisionBlock.class */
public class CrossCollisionBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = (Map) PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter(entry -> {
        return ((Direction) entry.getKey()).getAxis().isHorizontal();
    }).collect(Util.toMap());
    protected final VoxelShape[] collisionShapeByIndex;
    protected final VoxelShape[] shapeByIndex;
    private final Object2IntMap<BlockState> stateToIndex;

    protected CrossCollisionBlock(float f, float f2, float f3, float f4, float f5, BlockBehaviour.Properties properties) {
        super(properties);
        this.stateToIndex = new Object2IntOpenHashMap();
        this.collisionShapeByIndex = makeShapes(f, f2, f5, 0.0f, f5);
        this.shapeByIndex = makeShapes(f, f2, f3, 0.0f, f4);
        UnmodifiableIterator it = this.stateDefinition.getPossibleStates().iterator();
        while (it.hasNext()) {
            getAABBIndex((BlockState) it.next());
        }
    }

    protected VoxelShape[] makeShapes(float f, float f2, float f3, float f4, float f5) {
        float f6 = 8.0f - f;
        float f7 = 8.0f + f;
        float f8 = 8.0f - f2;
        float f9 = 8.0f + f2;
        VoxelShape box = Block.box(f6, 0.0d, f6, f7, f3, f7);
        VoxelShape box2 = Block.box(f8, f4, 0.0d, f9, f5, f9);
        VoxelShape box3 = Block.box(f8, f4, f8, f9, f5, 16.0d);
        VoxelShape box4 = Block.box(0.0d, f4, f8, f9, f5, f9);
        VoxelShape box5 = Block.box(f8, f4, f8, 16.0d, f5, f9);
        VoxelShape m90or = Shapes.or(box2, box5);
        VoxelShape m90or2 = Shapes.or(box3, box4);
        VoxelShape[] voxelShapeArr = new VoxelShape[16];
        voxelShapeArr[0] = Shapes.empty();
        voxelShapeArr[1] = box3;
        voxelShapeArr[2] = box4;
        voxelShapeArr[3] = m90or2;
        voxelShapeArr[4] = box2;
        voxelShapeArr[5] = Shapes.or(box3, box2);
        voxelShapeArr[6] = Shapes.or(box4, box2);
        voxelShapeArr[7] = Shapes.or(m90or2, box2);
        voxelShapeArr[8] = box5;
        voxelShapeArr[9] = Shapes.or(box3, box5);
        voxelShapeArr[10] = Shapes.or(box4, box5);
        voxelShapeArr[11] = Shapes.or(m90or2, box5);
        voxelShapeArr[12] = m90or;
        voxelShapeArr[13] = Shapes.or(box3, m90or);
        voxelShapeArr[14] = Shapes.or(box4, m90or);
        voxelShapeArr[15] = Shapes.or(m90or2, m90or);
        for (int i = 0; i < 16; i++) {
            voxelShapeArr[i] = Shapes.or(box, voxelShapeArr[i]);
        }
        return voxelShapeArr;
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return !((Boolean) blockState.getValue(WATERLOGGED)).booleanValue();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapeByIndex[getAABBIndex(blockState)];
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.collisionShapeByIndex[getAABBIndex(blockState)];
    }

    private static int indexFor(Direction direction) {
        return 1 << direction.get2DDataValue();
    }

    protected int getAABBIndex(BlockState blockState) {
        return this.stateToIndex.computeIntIfAbsent(blockState, blockState2 -> {
            int i = 0;
            if (((Boolean) blockState2.getValue(NORTH)).booleanValue()) {
                i = 0 | indexFor(Direction.NORTH);
            }
            if (((Boolean) blockState2.getValue(EAST)).booleanValue()) {
                i |= indexFor(Direction.EAST);
            }
            if (((Boolean) blockState2.getValue(SOUTH)).booleanValue()) {
                i |= indexFor(Direction.SOUTH);
            }
            if (((Boolean) blockState2.getValue(WEST)).booleanValue()) {
                i |= indexFor(Direction.WEST);
            }
            return i;
        });
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(EAST, blockState.getValue(WEST))).setValue(SOUTH, blockState.getValue(NORTH))).setValue(WEST, blockState.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(EAST))).setValue(EAST, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(NORTH));
            case CLOCKWISE_90:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(WEST))).setValue(EAST, blockState.getValue(NORTH))).setValue(SOUTH, blockState.getValue(EAST))).setValue(WEST, blockState.getValue(SOUTH));
            default:
                return blockState;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT:
                return (BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(NORTH));
            case FRONT_BACK:
                return (BlockState) ((BlockState) blockState.setValue(EAST, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(EAST));
            default:
                return super.mirror(blockState, mirror);
        }
    }
}
