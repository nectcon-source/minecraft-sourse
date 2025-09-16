package net.minecraft.world.level.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/pathfinder/Node.class */
public class Node {

    /* renamed from: x */
    public final int x;

    /* renamed from: y */
    public final int y;

    /* renamed from: z */
    public final int z;
    private final int hash;

    /* renamed from: g */
    public float g;

    /* renamed from: h */
    public float h;

    /* renamed from: f */
    public float f;
    public Node cameFrom;
    public boolean closed;
    public float walkedDistance;
    public float costMalus;
    public int heapIdx = -1;
    public BlockPathTypes type = BlockPathTypes.BLOCKED;

    public Node(int i, int i2, int i3) {
        this.x = i;
        this.y = i2;
        this.z = i3;
        this.hash = createHash(i, i2, i3);
    }

    public Node cloneAndMove(int i, int i2, int i3) {
        Node node = new Node(i, i2, i3);
        node.heapIdx = this.heapIdx;
        node.g = this.g;
        node.h = this.h;
        node.f = this.f;
        node.cameFrom = this.cameFrom;
        node.closed = this.closed;
        node.walkedDistance = this.walkedDistance;
        node.costMalus = this.costMalus;
        node.type = this.type;
        return node;
    }

    public static int createHash(int i, int i2, int i3) {
        return (i2 & 255) | ((i & 32767) << 8) | ((i3 & 32767) << 24) | (i < 0 ? Integer.MIN_VALUE : 0) | (i3 < 0 ? 32768 : 0);
    }

    public float distanceTo(Node node) {
        float f = node.x - this.x;
        float f2 = node.y - this.y;
        float f3 = node.z - this.z;
        return Mth.sqrt((f * f) + (f2 * f2) + (f3 * f3));
    }

    public float distanceToSqr(Node node) {
        float f = node.x - this.x;
        float f2 = node.y - this.y;
        float f3 = node.z - this.z;
        return (f * f) + (f2 * f2) + (f3 * f3);
    }

    public float distanceManhattan(Node node) {
        return Math.abs(node.x - this.x) + Math.abs(node.y - this.y) + Math.abs(node.z - this.z);
    }

    public float distanceManhattan(BlockPos blockPos) {
        return Math.abs(blockPos.getX() - this.x) + Math.abs(blockPos.getY() - this.y) + Math.abs(blockPos.getZ() - this.z);
    }

    public BlockPos asBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            Node node = (Node) obj;
            return this.hash == node.hash && this.x == node.x && this.y == node.y && this.z == node.z;
        }
        return false;
    }

    public int hashCode() {
        return this.hash;
    }

    public boolean inOpenSet() {
        return this.heapIdx >= 0;
    }

    public String toString() {
        return "Node{x=" + this.x + ", y=" + this.y + ", z=" + this.z + '}';
    }

    public static Node createFromStream(FriendlyByteBuf friendlyByteBuf) {
        Node node = new Node(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
        node.walkedDistance = friendlyByteBuf.readFloat();
        node.costMalus = friendlyByteBuf.readFloat();
        node.closed = friendlyByteBuf.readBoolean();
        node.type = BlockPathTypes.values()[friendlyByteBuf.readInt()];
        node.f = friendlyByteBuf.readFloat();
        return node;
    }
}
