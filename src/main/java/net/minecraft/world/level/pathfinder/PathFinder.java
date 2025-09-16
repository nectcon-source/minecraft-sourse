package net.minecraft.world.level.pathfinder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/pathfinder/PathFinder.class */
public class PathFinder {
    private final int maxVisitedNodes;
    private final NodeEvaluator nodeEvaluator;
    private final Node[] neighbors = new Node[32];
    private final BinaryHeap openSet = new BinaryHeap();

    public PathFinder(NodeEvaluator nodeEvaluator, int i) {
        this.nodeEvaluator = nodeEvaluator;
        this.maxVisitedNodes = i;
    }

    @Nullable
    public Path findPath(PathNavigationRegion pathNavigationRegion, Mob mob, Set<BlockPos> set, float f, int i, float f2) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(pathNavigationRegion, mob);
        Path findPath = findPath(this.nodeEvaluator.getStart(), (Map) set.stream().collect(Collectors.toMap(blockPos -> {
            return this.nodeEvaluator.getGoal(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }, Function.identity())), f, i, f2);
        this.nodeEvaluator.done();
        return findPath;
    }

    @Nullable
    private Path findPath(Node node, Map<Target, BlockPos> map, float f, int i, float f2) {
        Set<Target> var6 = map.keySet();
        node.g = 0.0F;
        node.h = this.getBestH(node, var6);
        node.f = node.h;
        this.openSet.clear();
        this.openSet.insert(node);
        Set<Node> var7 = ImmutableSet.of();
        int var8 = 0;
        Set<Target> var9 = Sets.newHashSetWithExpectedSize(var6.size());
        int var10 = (int)((float)this.maxVisitedNodes * f2);

        while(!this.openSet.isEmpty()) {
            ++var8;
            if (var8 >= var10) {
                break;
            }

            Node var11 = this.openSet.pop();
            var11.closed = true;

            for(Target var13 : var6) {
                if (var11.distanceManhattan(var13) <= (float)i) {
                    var13.setReached();
                    var9.add(var13);
                }
            }

            if (!var9.isEmpty()) {
                break;
            }

            if (!(var11.distanceTo(node) >= f)) {
                int var18 = this.nodeEvaluator.getNeighbors(this.neighbors, var11);

                for(int var20 = 0; var20 < var18; ++var20) {
                    Node var14 = this.neighbors[var20];
                    float var15 = var11.distanceTo(var14);
                    var14.walkedDistance = var11.walkedDistance + var15;
                    float var16 = var11.g + var15 + var14.costMalus;
                    if (var14.walkedDistance < f && (!var14.inOpenSet() || var16 < var14.g)) {
                        var14.cameFrom = var11;
                        var14.g = var16;
                        var14.h = this.getBestH(var14, var6) * 1.5F;
                        if (var14.inOpenSet()) {
                            this.openSet.changeCost(var14, var14.g + var14.h);
                        } else {
                            var14.f = var14.g + var14.h;
                            this.openSet.insert(var14);
                        }
                    }
                }
            }
        }

        Optional<Path> var17 = !var9.isEmpty() ? var9.stream().map((var2x) -> this.reconstructPath(var2x.getBestNode(), (BlockPos)map.get(var2x), true)).min(Comparator.comparingInt(Path::getNodeCount)) : var6.stream().map((var2x) -> this.reconstructPath(var2x.getBestNode(), (BlockPos)map.get(var2x), false)).min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
        if (!var17.isPresent()) {
            return null;
        } else {
            Path var19 = (Path)var17.get();
            return var19;
        }
    }


    private float getBestH(Node node, Set<Target> set) {
        float f = Float.MAX_VALUE;
        for (Target target : set) {
            float distanceTo = node.distanceTo(target);
            target.updateBest(distanceTo, node);
            f = Math.min(distanceTo, f);
        }
        return f;
    }

    private Path reconstructPath(Node node, BlockPos blockPos, boolean z) {
        List<Node> newArrayList = Lists.newArrayList();
        Node node2 = node;
        newArrayList.add(0, node2);
        while (node2.cameFrom != null) {
            node2 = node2.cameFrom;
            newArrayList.add(0, node2);
        }
        return new Path(newArrayList, blockPos, z);
    }
}
