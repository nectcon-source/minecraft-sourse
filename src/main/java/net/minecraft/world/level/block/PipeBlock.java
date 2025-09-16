package net.minecraft.world.level.block;

import com.google.common.collect.Maps;

import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/PipeBlock.class */
public class PipeBlock extends Block {
    private static final Direction[] DIRECTIONS = Direction.values();
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    /* renamed from: UP */
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION =  Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
        enumMap.put( Direction.NORTH,  NORTH);
        enumMap.put( Direction.EAST,  EAST);
        enumMap.put( Direction.SOUTH,  SOUTH);
        enumMap.put( Direction.WEST,  WEST);
        enumMap.put( Direction.UP, UP);
        enumMap.put( Direction.DOWN,  DOWN);
    });
    protected final VoxelShape[] shapeByIndex;

    protected PipeBlock(float f, BlockBehaviour.Properties properties) {
        super(properties);
        this.shapeByIndex = makeShapes(f);
    }

    private VoxelShape[] makeShapes(float f) {
        float f2 = 0.5f - f;
        float f3 = 0.5f + f;
        VoxelShape box = Block.box(f2 * 16.0f, f2 * 16.0f, f2 * 16.0f, f3 * 16.0f, f3 * 16.0f, f3 * 16.0f);
        VoxelShape[] voxelShapeArr = new VoxelShape[DIRECTIONS.length];
        for (int i = 0; i < DIRECTIONS.length; i++) {
            Direction direction = DIRECTIONS[i];
            voxelShapeArr[i] = Shapes.box(0.5d + Math.min(-f, direction.getStepX() * 0.5d), 0.5d + Math.min(-f, direction.getStepY() * 0.5d), 0.5d + Math.min(-f, direction.getStepZ() * 0.5d), 0.5d + Math.max(f, direction.getStepX() * 0.5d), 0.5d + Math.max(f, direction.getStepY() * 0.5d), 0.5d + Math.max(f, direction.getStepZ() * 0.5d));
        }
        VoxelShape[] voxelShapeArr2 = new VoxelShape[64];
        for (int i2 = 0; i2 < 64; i2++) {
            VoxelShape voxelShape = box;
            for (int i3 = 0; i3 < DIRECTIONS.length; i3++) {
                if ((i2 & (1 << i3)) != 0) {
                    voxelShape = Shapes.or(voxelShape, voxelShapeArr[i3]);
                }
            }
            voxelShapeArr2[i2] = voxelShape;
        }
        return voxelShapeArr2;
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapeByIndex[getAABBIndex(blockState)];
    }

    protected int getAABBIndex(BlockState blockState) {
        int i = 0;
        for (int i2 = 0; i2 < DIRECTIONS.length; i2++) {
            if (((Boolean) blockState.getValue(PROPERTY_BY_DIRECTION.get(DIRECTIONS[i2]))).booleanValue()) {
                i |= 1 << i2;
            }
        }
        return i;
    }
}
