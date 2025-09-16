package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.IntArrayTag;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/BoundingBox.class */
public class BoundingBox {

    /* renamed from: x0 */
    public int x0;

    /* renamed from: y0 */
    public int y0;

    /* renamed from: z0 */
    public int z0;

    /* renamed from: x1 */
    public int x1;

    /* renamed from: y1 */
    public int y1;

    /* renamed from: z1 */
    public int z1;

    public BoundingBox() {
    }

    public BoundingBox(int[] iArr) {
        if (iArr.length == 6) {
            this.x0 = iArr[0];
            this.y0 = iArr[1];
            this.z0 = iArr[2];
            this.x1 = iArr[3];
            this.y1 = iArr[4];
            this.z1 = iArr[5];
        }
    }

    public static BoundingBox getUnknownBox() {
        return new BoundingBox(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public static BoundingBox infinite() {
        return new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static BoundingBox orientBox(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, Direction direction) {
        switch (direction) {
            case NORTH:
                return new BoundingBox(i + i4, i2 + i5, (i3 - i9) + 1 + i6, ((i + i7) - 1) + i4, ((i2 + i8) - 1) + i5, i3 + i6);
            case SOUTH:
                return new BoundingBox(i + i4, i2 + i5, i3 + i6, ((i + i7) - 1) + i4, ((i2 + i8) - 1) + i5, ((i3 + i9) - 1) + i6);
            case WEST:
                return new BoundingBox((i - i9) + 1 + i6, i2 + i5, i3 + i4, i + i6, ((i2 + i8) - 1) + i5, ((i3 + i7) - 1) + i4);
            case EAST:
                return new BoundingBox(i + i6, i2 + i5, i3 + i4, ((i + i9) - 1) + i6, ((i2 + i8) - 1) + i5, ((i3 + i7) - 1) + i4);
            default:
                return new BoundingBox(i + i4, i2 + i5, i3 + i6, ((i + i7) - 1) + i4, ((i2 + i8) - 1) + i5, ((i3 + i9) - 1) + i6);
        }
    }

    public static BoundingBox createProper(int i, int i2, int i3, int i4, int i5, int i6) {
        return new BoundingBox(Math.min(i, i4), Math.min(i2, i5), Math.min(i3, i6), Math.max(i, i4), Math.max(i2, i5), Math.max(i3, i6));
    }

    public BoundingBox(BoundingBox boundingBox) {
        this.x0 = boundingBox.x0;
        this.y0 = boundingBox.y0;
        this.z0 = boundingBox.z0;
        this.x1 = boundingBox.x1;
        this.y1 = boundingBox.y1;
        this.z1 = boundingBox.z1;
    }

    public BoundingBox(int i, int i2, int i3, int i4, int i5, int i6) {
        this.x0 = i;
        this.y0 = i2;
        this.z0 = i3;
        this.x1 = i4;
        this.y1 = i5;
        this.z1 = i6;
    }

    public BoundingBox(Vec3i vec3i, Vec3i vec3i2) {
        this.x0 = Math.min(vec3i.getX(), vec3i2.getX());
        this.y0 = Math.min(vec3i.getY(), vec3i2.getY());
        this.z0 = Math.min(vec3i.getZ(), vec3i2.getZ());
        this.x1 = Math.max(vec3i.getX(), vec3i2.getX());
        this.y1 = Math.max(vec3i.getY(), vec3i2.getY());
        this.z1 = Math.max(vec3i.getZ(), vec3i2.getZ());
    }

    public BoundingBox(int i, int i2, int i3, int i4) {
        this.x0 = i;
        this.z0 = i2;
        this.x1 = i3;
        this.z1 = i4;
        this.y0 = 1;
        this.y1 = 512;
    }

    public boolean intersects(BoundingBox boundingBox) {
        return this.x1 >= boundingBox.x0 && this.x0 <= boundingBox.x1 && this.z1 >= boundingBox.z0 && this.z0 <= boundingBox.z1 && this.y1 >= boundingBox.y0 && this.y0 <= boundingBox.y1;
    }

    public boolean intersects(int i, int i2, int i3, int i4) {
        return this.x1 >= i && this.x0 <= i3 && this.z1 >= i2 && this.z0 <= i4;
    }

    public void expand(BoundingBox boundingBox) {
        this.x0 = Math.min(this.x0, boundingBox.x0);
        this.y0 = Math.min(this.y0, boundingBox.y0);
        this.z0 = Math.min(this.z0, boundingBox.z0);
        this.x1 = Math.max(this.x1, boundingBox.x1);
        this.y1 = Math.max(this.y1, boundingBox.y1);
        this.z1 = Math.max(this.z1, boundingBox.z1);
    }

    public void move(int i, int i2, int i3) {
        this.x0 += i;
        this.y0 += i2;
        this.z0 += i3;
        this.x1 += i;
        this.y1 += i2;
        this.z1 += i3;
    }

    public BoundingBox moved(int i, int i2, int i3) {
        return new BoundingBox(this.x0 + i, this.y0 + i2, this.z0 + i3, this.x1 + i, this.y1 + i2, this.z1 + i3);
    }

    public void move(Vec3i vec3i) {
        move(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public boolean isInside(Vec3i vec3i) {
        return vec3i.getX() >= this.x0 && vec3i.getX() <= this.x1 && vec3i.getZ() >= this.z0 && vec3i.getZ() <= this.z1 && vec3i.getY() >= this.y0 && vec3i.getY() <= this.y1;
    }

    public Vec3i getLength() {
        return new Vec3i(this.x1 - this.x0, this.y1 - this.y0, this.z1 - this.z0);
    }

    public int getXSpan() {
        return (this.x1 - this.x0) + 1;
    }

    public int getYSpan() {
        return (this.y1 - this.y0) + 1;
    }

    public int getZSpan() {
        return (this.z1 - this.z0) + 1;
    }

    public Vec3i getCenter() {
        return new BlockPos(this.x0 + (((this.x1 - this.x0) + 1) / 2), this.y0 + (((this.y1 - this.y0) + 1) / 2), this.z0 + (((this.z1 - this.z0) + 1) / 2));
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("x0", this.x0).add("y0", this.y0).add("z0", this.z0).add("x1", this.x1).add("y1", this.y1).add("z1", this.z1).toString();
    }

    public IntArrayTag createTag() {
        return new IntArrayTag(new int[]{this.x0, this.y0, this.z0, this.x1, this.y1, this.z1});
    }
}
