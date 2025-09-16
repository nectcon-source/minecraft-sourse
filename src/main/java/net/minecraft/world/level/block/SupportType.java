package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SupportType.class */
public enum SupportType {
    FULL { // from class: net.minecraft.world.level.block.SupportType.1
        @Override // net.minecraft.world.level.block.SupportType
        public boolean isSupporting(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            return Block.isFaceFull(blockState.getBlockSupportShape(blockGetter, blockPos), direction);
        }
    },
    CENTER { // from class: net.minecraft.world.level.block.SupportType.2
        private final int CENTER_SUPPORT_WIDTH = 1;
        private final VoxelShape CENTER_SUPPORT_SHAPE = Block.box(7.0d, 0.0d, 7.0d, 9.0d, 10.0d, 9.0d);

        @Override // net.minecraft.world.level.block.SupportType
        public boolean isSupporting(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            return !Shapes.joinIsNotEmpty(blockState.getBlockSupportShape(blockGetter, blockPos).getFaceShape(direction), this.CENTER_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
        }
    },
    RIGID { // from class: net.minecraft.world.level.block.SupportType.3
        private final int RIGID_SUPPORT_WIDTH = 2;
        private final VoxelShape RIGID_SUPPORT_SHAPE = Shapes.join(Shapes.block(), Block.box(2.0d, 0.0d, 2.0d, 14.0d, 16.0d, 14.0d), BooleanOp.ONLY_FIRST);

        @Override // net.minecraft.world.level.block.SupportType
        public boolean isSupporting(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            return !Shapes.joinIsNotEmpty(blockState.getBlockSupportShape(blockGetter, blockPos).getFaceShape(direction), this.RIGID_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
        }
    };

    public abstract boolean isSupporting(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction);
}
