package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/pathfinder/NodeEvaluator.class */
public abstract class NodeEvaluator {
    protected PathNavigationRegion level;
    protected Mob mob;
    protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap();
    protected int entityWidth;
    protected int entityHeight;
    protected int entityDepth;
    protected boolean canPassDoors;
    protected boolean canOpenDoors;
    protected boolean canFloat;

    public abstract Node getStart();

    public abstract Target getGoal(double d, double d2, double d3);

    public abstract int getNeighbors(Node[] nodeArr, Node node);

    public abstract BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i2, int i3, Mob mob, int i4, int i5, int i6, boolean z, boolean z2);

    public abstract BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i2, int i3);

    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        this.level = pathNavigationRegion;
        this.mob = mob;
        this.nodes.clear();
        this.entityWidth = Mth.floor(mob.getBbWidth() + 1.0f);
        this.entityHeight = Mth.floor(mob.getBbHeight() + 1.0f);
        this.entityDepth = Mth.floor(mob.getBbWidth() + 1.0f);
    }

    public void done() {
        this.level = null;
        this.mob = null;
    }

    protected Node getNode(BlockPos blockPos) {
        return getNode(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    protected Node getNode(int i, int i2, int i3) {
        return (Node) this.nodes.computeIfAbsent(Node.createHash(i, i2, i3), i4 -> {
            return new Node(i, i2, i3);
        });
    }

    public void setCanPassDoors(boolean z) {
        this.canPassDoors = z;
    }

    public void setCanOpenDoors(boolean z) {
        this.canOpenDoors = z;
    }

    public void setCanFloat(boolean z) {
        this.canFloat = z;
    }

    public boolean canPassDoors() {
        return this.canPassDoors;
    }

    public boolean canOpenDoors() {
        return this.canOpenDoors;
    }

    public boolean canFloat() {
        return this.canFloat;
    }
}
