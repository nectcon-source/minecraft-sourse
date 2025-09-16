package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/pathfinder/Path.class */
public class Path {
    private final List<Node> nodes;
    private Node[] openSet = new Node[0];
    private Node[] closedSet = new Node[0];
    private Set<Target> targetNodes;
    private int nextNodeIndex;
    private final BlockPos target;
    private final float distToTarget;
    private final boolean reached;

    public Path(List<Node> list, BlockPos blockPos, boolean z) {
        this.nodes = list;
        this.target = blockPos;
        this.distToTarget = list.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).distanceManhattan(this.target);
        this.reached = z;
    }

    public void advance() {
        this.nextNodeIndex++;
    }

    public boolean notStarted() {
        return this.nextNodeIndex <= 0;
    }

    public boolean isDone() {
        return this.nextNodeIndex >= this.nodes.size();
    }

    @Nullable
    public Node getEndNode() {
        if (!this.nodes.isEmpty()) {
            return this.nodes.get(this.nodes.size() - 1);
        }
        return null;
    }

    public Node getNode(int i) {
        return this.nodes.get(i);
    }

    public void truncateNodes(int i) {
        if (this.nodes.size() > i) {
            this.nodes.subList(i, this.nodes.size()).clear();
        }
    }

    public void replaceNode(int i, Node node) {
        this.nodes.set(i, node);
    }

    public int getNodeCount() {
        return this.nodes.size();
    }

    public int getNextNodeIndex() {
        return this.nextNodeIndex;
    }

    public void setNextNodeIndex(int i) {
        this.nextNodeIndex = i;
    }

    public Vec3 getEntityPosAtNode(Entity entity, int i) {
        Node node = this.nodes.get(i);
        return new Vec3(node.x + (((int) (entity.getBbWidth() + 1.0f)) * 0.5d), node.y, node.z + (((int) (entity.getBbWidth() + 1.0f)) * 0.5d));
    }

    public BlockPos getNodePos(int i) {
        return this.nodes.get(i).asBlockPos();
    }

    public Vec3 getNextEntityPos(Entity entity) {
        return getEntityPosAtNode(entity, this.nextNodeIndex);
    }

    public BlockPos getNextNodePos() {
        return this.nodes.get(this.nextNodeIndex).asBlockPos();
    }

    public Node getNextNode() {
        return this.nodes.get(this.nextNodeIndex);
    }

    @Nullable
    public Node getPreviousNode() {
        if (this.nextNodeIndex > 0) {
            return this.nodes.get(this.nextNodeIndex - 1);
        }
        return null;
    }

    public boolean sameAs(@Nullable Path path) {
        if (path == null || path.nodes.size() != this.nodes.size()) {
            return false;
        }
        for (int i = 0; i < this.nodes.size(); i++) {
            Node node = this.nodes.get(i);
            Node node2 = path.nodes.get(i);
            if (node.x != node2.x || node.y != node2.y || node.z != node2.z) {
                return false;
            }
        }
        return true;
    }

    public boolean canReach() {
        return this.reached;
    }

    public Node[] getOpenSet() {
        return this.openSet;
    }

    public Node[] getClosedSet() {
        return this.closedSet;
    }

    public static Path createFromStream(FriendlyByteBuf friendlyByteBuf) {
        boolean readBoolean = friendlyByteBuf.readBoolean();
        int readInt = friendlyByteBuf.readInt();
        int readInt2 = friendlyByteBuf.readInt();
        Set<Target> newHashSet = Sets.newHashSet();
        for (int i = 0; i < readInt2; i++) {
            newHashSet.add(Target.createFromStream(friendlyByteBuf));
        }
        BlockPos blockPos = new BlockPos(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
        List<Node> newArrayList = Lists.newArrayList();
        int readInt3 = friendlyByteBuf.readInt();
        for (int i2 = 0; i2 < readInt3; i2++) {
            newArrayList.add(Node.createFromStream(friendlyByteBuf));
        }
        Node[] nodeArr = new Node[friendlyByteBuf.readInt()];
        for (int i3 = 0; i3 < nodeArr.length; i3++) {
            nodeArr[i3] = Node.createFromStream(friendlyByteBuf);
        }
        Node[] nodeArr2 = new Node[friendlyByteBuf.readInt()];
        for (int i4 = 0; i4 < nodeArr2.length; i4++) {
            nodeArr2[i4] = Node.createFromStream(friendlyByteBuf);
        }
        Path path = new Path(newArrayList, blockPos, readBoolean);
        path.openSet = nodeArr;
        path.closedSet = nodeArr2;
        path.targetNodes = newHashSet;
        path.nextNodeIndex = readInt;
        return path;
    }

    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public float getDistToTarget() {
        return this.distToTarget;
    }
}
