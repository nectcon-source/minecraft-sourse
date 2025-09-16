package net.minecraft.world.level.pathfinder;

import net.minecraft.network.FriendlyByteBuf;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/pathfinder/Target.class */
public class Target extends Node {
    private float bestHeuristic;
    private Node bestNode;
    private boolean reached;

    public Target(Node node) {
        super(node.x, node.y, node.z);
        this.bestHeuristic = Float.MAX_VALUE;
    }

    public Target(int i, int i2, int i3) {
        super(i, i2, i3);
        this.bestHeuristic = Float.MAX_VALUE;
    }

    public void updateBest(float f, Node node) {
        if (f < this.bestHeuristic) {
            this.bestHeuristic = f;
            this.bestNode = node;
        }
    }

    public Node getBestNode() {
        return this.bestNode;
    }

    public void setReached() {
        this.reached = true;
    }

    public static Target createFromStream(FriendlyByteBuf friendlyByteBuf) {
        Target target = new Target(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
        target.walkedDistance = friendlyByteBuf.readFloat();
        target.costMalus = friendlyByteBuf.readFloat();
        target.closed = friendlyByteBuf.readBoolean();
        target.type = BlockPathTypes.values()[friendlyByteBuf.readInt()];
        target.f = friendlyByteBuf.readFloat();
        return target;
    }
}
