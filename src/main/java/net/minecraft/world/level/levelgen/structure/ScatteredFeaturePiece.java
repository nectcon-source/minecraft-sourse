package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/ScatteredFeaturePiece.class */
public abstract class ScatteredFeaturePiece extends StructurePiece {
    protected final int width;
    protected final int height;
    protected final int depth;
    protected int heightPosition;

    protected ScatteredFeaturePiece(StructurePieceType structurePieceType, Random random, int i, int i2, int i3, int i4, int i5, int i6) {
        super(structurePieceType, 0);
        this.heightPosition = -1;
        this.width = i4;
        this.height = i5;
        this.depth = i6;
        setOrientation(Direction.Plane.HORIZONTAL.getRandomDirection(random));
        if (getOrientation().getAxis() == Direction.Axis.Z) {
            this.boundingBox = new BoundingBox(i, i2, i3, (i + i4) - 1, (i2 + i5) - 1, (i3 + i6) - 1);
        } else {
            this.boundingBox = new BoundingBox(i, i2, i3, (i + i6) - 1, (i2 + i5) - 1, (i3 + i4) - 1);
        }
    }

    protected ScatteredFeaturePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
        super(structurePieceType, compoundTag);
        this.heightPosition = -1;
        this.width = compoundTag.getInt("Width");
        this.height = compoundTag.getInt("Height");
        this.depth = compoundTag.getInt("Depth");
        this.heightPosition = compoundTag.getInt("HPos");
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("Width", this.width);
        compoundTag.putInt("Height", this.height);
        compoundTag.putInt("Depth", this.depth);
        compoundTag.putInt("HPos", this.heightPosition);
    }

    protected boolean updateAverageGroundHeight(LevelAccessor levelAccessor, BoundingBox boundingBox, int i) {
        if (this.heightPosition >= 0) {
            return true;
        }
        int i2 = 0;
        int i3 = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i4 = this.boundingBox.z0; i4 <= this.boundingBox.z1; i4++) {
            for (int i5 = this.boundingBox.x0; i5 <= this.boundingBox.x1; i5++) {
                mutableBlockPos.set(i5, 64, i4);
                if (boundingBox.isInside(mutableBlockPos)) {
                    i2 += levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos).getY();
                    i3++;
                }
            }
        }
        if (i3 == 0) {
            return false;
        }
        this.heightPosition = i2 / i3;
        this.boundingBox.move(0, (this.heightPosition - this.boundingBox.y0) + i, 0);
        return true;
    }
}
