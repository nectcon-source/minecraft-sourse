package net.minecraft.world.phys.shapes;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/DiscreteVoxelShape.class */
public abstract class DiscreteVoxelShape {
    private static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();
    protected final int xSize;
    protected final int ySize;
    protected final int zSize;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/DiscreteVoxelShape$IntFaceConsumer.class */
    public interface IntFaceConsumer {
        void consume(Direction direction, int i, int i2, int i3);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/DiscreteVoxelShape$IntLineConsumer.class */
    public interface IntLineConsumer {
        void consume(int i, int i2, int i3, int i4, int i5, int i6);
    }

    public abstract boolean isFull(int i, int i2, int i3);

    public abstract void setFull(int i, int i2, int i3, boolean z, boolean z2);

    public abstract int firstFull(Direction.Axis axis);

    public abstract int lastFull(Direction.Axis axis);

    protected DiscreteVoxelShape(int i, int i2, int i3) {
        this.xSize = i;
        this.ySize = i2;
        this.zSize = i3;
    }

    public boolean isFullWide(AxisCycle axisCycle, int i, int i2, int i3) {
        return isFullWide(axisCycle.cycle(i, i2, i3, Direction.Axis.X), axisCycle.cycle(i, i2, i3, Direction.Axis.Y), axisCycle.cycle(i, i2, i3, Direction.Axis.Z));
    }

    public boolean isFullWide(int i, int i2, int i3) {
        if (i < 0 || i2 < 0 || i3 < 0 || i >= this.xSize || i2 >= this.ySize || i3 >= this.zSize) {
            return false;
        }
        return isFull(i, i2, i3);
    }

    public boolean isFull(AxisCycle axisCycle, int i, int i2, int i3) {
        return isFull(axisCycle.cycle(i, i2, i3, Direction.Axis.X), axisCycle.cycle(i, i2, i3, Direction.Axis.Y), axisCycle.cycle(i, i2, i3, Direction.Axis.Z));
    }

    public boolean isEmpty() {
        for (Direction.Axis axis : AXIS_VALUES) {
            if (firstFull(axis) >= lastFull(axis)) {
                return true;
            }
        }
        return false;
    }

    public int lastFull(Direction.Axis axis, int i, int i2) {
        if (i < 0 || i2 < 0) {
            return 0;
        }
        Direction.Axis cycle = AxisCycle.FORWARD.cycle(axis);
        Direction.Axis cycle2 = AxisCycle.BACKWARD.cycle(axis);
        if (i >= getSize(cycle) || i2 >= getSize(cycle2)) {
            return 0;
        }
        int size = getSize(axis);
        AxisCycle between = AxisCycle.between(Direction.Axis.X, axis);
        for (int i3 = size - 1; i3 >= 0; i3--) {
            if (isFull(between, i3, i, i2)) {
                return i3 + 1;
            }
        }
        return 0;
    }

    public int getSize(Direction.Axis axis) {
        return axis.choose(this.xSize, this.ySize, this.zSize);
    }

    public int getXSize() {
        return getSize(Direction.Axis.X);
    }

    public int getYSize() {
        return getSize(Direction.Axis.Y);
    }

    public int getZSize() {
        return getSize(Direction.Axis.Z);
    }

    public void forAllEdges(IntLineConsumer intLineConsumer, boolean z) {
        forAllAxisEdges(intLineConsumer, AxisCycle.NONE, z);
        forAllAxisEdges(intLineConsumer, AxisCycle.FORWARD, z);
        forAllAxisEdges(intLineConsumer, AxisCycle.BACKWARD, z);
    }

    private void forAllAxisEdges(IntLineConsumer intLineConsumer, AxisCycle axisCycle, boolean z) {
        AxisCycle inverse = axisCycle.inverse();
        int size = getSize(inverse.cycle(Direction.Axis.X));
        int size2 = getSize(inverse.cycle(Direction.Axis.Y));
        int size3 = getSize(inverse.cycle(Direction.Axis.Z));
        for (int i = 0; i <= size; i++) {
            for (int i2 = 0; i2 <= size2; i2++) {
                int i3 = -1;
                for (int i4 = 0; i4 <= size3; i4++) {
                    int i5 = 0;
                    int i6 = 0;
                    for (int i7 = 0; i7 <= 1; i7++) {
                        for (int i8 = 0; i8 <= 1; i8++) {
                            if (isFullWide(inverse, (i + i7) - 1, (i2 + i8) - 1, i4)) {
                                i5++;
                                i6 ^= i7 ^ i8;
                            }
                        }
                    }
                    if (i5 == 1 || i5 == 3 || (i5 == 2 && (i6 & 1) == 0)) {
                        if (z) {
                            if (i3 == -1) {
                                i3 = i4;
                            }
                        } else {
                            intLineConsumer.consume(inverse.cycle(i, i2, i4, Direction.Axis.X), inverse.cycle(i, i2, i4, Direction.Axis.Y), inverse.cycle(i, i2, i4, Direction.Axis.Z), inverse.cycle(i, i2, i4 + 1, Direction.Axis.X), inverse.cycle(i, i2, i4 + 1, Direction.Axis.Y), inverse.cycle(i, i2, i4 + 1, Direction.Axis.Z));
                        }
                    } else if (i3 != -1) {
                        intLineConsumer.consume(inverse.cycle(i, i2, i3, Direction.Axis.X), inverse.cycle(i, i2, i3, Direction.Axis.Y), inverse.cycle(i, i2, i3, Direction.Axis.Z), inverse.cycle(i, i2, i4, Direction.Axis.X), inverse.cycle(i, i2, i4, Direction.Axis.Y), inverse.cycle(i, i2, i4, Direction.Axis.Z));
                        i3 = -1;
                    }
                }
            }
        }
    }

    protected boolean isZStripFull(int i, int i2, int i3, int i4) {
        for (int i5 = i; i5 < i2; i5++) {
            if (!isFullWide(i3, i4, i5)) {
                return false;
            }
        }
        return true;
    }

    protected void setZStrip(int i, int i2, int i3, int i4, boolean z) {
        for (int i5 = i; i5 < i2; i5++) {
            setFull(i3, i4, i5, false, z);
        }
    }

    protected boolean isXZRectangleFull(int i, int i2, int i3, int i4, int i5) {
        for (int i6 = i; i6 < i2; i6++) {
            if (!isZStripFull(i3, i4, i6, i5)) {
                return false;
            }
        }
        return true;
    }

    public void forAllBoxes(IntLineConsumer intLineConsumer, boolean z) {
        DiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(this);
        for (int i = 0; i <= this.xSize; i++) {
            for (int i2 = 0; i2 <= this.ySize; i2++) {
                int i3 = -1;
                for (int i4 = 0; i4 <= this.zSize; i4++) {
                    if (bitSetDiscreteVoxelShape.isFullWide(i, i2, i4)) {
                        if (z) {
                            if (i3 == -1) {
                                i3 = i4;
                            }
                        } else {
                            intLineConsumer.consume(i, i2, i4, i + 1, i2 + 1, i4 + 1);
                        }
                    } else if (i3 != -1) {
                        int i5 = i;
                        int i6 = i;
                        int i7 = i2;
                        int i8 = i2;
                        bitSetDiscreteVoxelShape.setZStrip(i3, i4, i, i2, false);
                        while (bitSetDiscreteVoxelShape.isZStripFull(i3, i4, i5 - 1, i7)) {
                            bitSetDiscreteVoxelShape.setZStrip(i3, i4, i5 - 1, i7, false);
                            i5--;
                        }
                        while (bitSetDiscreteVoxelShape.isZStripFull(i3, i4, i6 + 1, i7)) {
                            bitSetDiscreteVoxelShape.setZStrip(i3, i4, i6 + 1, i7, false);
                            i6++;
                        }
                        while (bitSetDiscreteVoxelShape.isXZRectangleFull(i5, i6 + 1, i3, i4, i7 - 1)) {
                            for (int i9 = i5; i9 <= i6; i9++) {
                                bitSetDiscreteVoxelShape.setZStrip(i3, i4, i9, i7 - 1, false);
                            }
                            i7--;
                        }
                        while (bitSetDiscreteVoxelShape.isXZRectangleFull(i5, i6 + 1, i3, i4, i8 + 1)) {
                            for (int i10 = i5; i10 <= i6; i10++) {
                                bitSetDiscreteVoxelShape.setZStrip(i3, i4, i10, i8 + 1, false);
                            }
                            i8++;
                        }
                        intLineConsumer.consume(i5, i7, i3, i6 + 1, i8 + 1, i4);
                        i3 = -1;
                    }
                }
            }
        }
    }

    public void forAllFaces(IntFaceConsumer intFaceConsumer) {
        forAllAxisFaces(intFaceConsumer, AxisCycle.NONE);
        forAllAxisFaces(intFaceConsumer, AxisCycle.FORWARD);
        forAllAxisFaces(intFaceConsumer, AxisCycle.BACKWARD);
    }

    private void forAllAxisFaces(IntFaceConsumer intFaceConsumer, AxisCycle axisCycle) {
        AxisCycle inverse = axisCycle.inverse();
        Direction.Axis cycle = inverse.cycle(Direction.Axis.Z);
        int size = getSize(inverse.cycle(Direction.Axis.X));
        int size2 = getSize(inverse.cycle(Direction.Axis.Y));
        int size3 = getSize(cycle);
        Direction fromAxisAndDirection = Direction.fromAxisAndDirection(cycle, Direction.AxisDirection.NEGATIVE);
        Direction fromAxisAndDirection2 = Direction.fromAxisAndDirection(cycle, Direction.AxisDirection.POSITIVE);
        for (int i = 0; i < size; i++) {
            for (int i2 = 0; i2 < size2; i2++) {
                boolean z = false;
                int i3 = 0;
                while (i3 <= size3) {
                    boolean z2 = i3 != size3 && isFull(inverse, i, i2, i3);
                    if (!z && z2) {
                        intFaceConsumer.consume(fromAxisAndDirection, inverse.cycle(i, i2, i3, Direction.Axis.X), inverse.cycle(i, i2, i3, Direction.Axis.Y), inverse.cycle(i, i2, i3, Direction.Axis.Z));
                    }
                    if (z && !z2) {
                        intFaceConsumer.consume(fromAxisAndDirection2, inverse.cycle(i, i2, i3 - 1, Direction.Axis.X), inverse.cycle(i, i2, i3 - 1, Direction.Axis.Y), inverse.cycle(i, i2, i3 - 1, Direction.Axis.Z));
                    }
                    z = z2;
                    i3++;
                }
            }
        }
    }
}
