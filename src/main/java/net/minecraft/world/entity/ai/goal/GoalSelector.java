package net.minecraft.world.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/GoalSelector.class */
public class GoalSelector {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final WrappedGoal NO_GOAL = new WrappedGoal(Integer.MAX_VALUE, new Goal() { // from class: net.minecraft.world.entity.ai.goal.GoalSelector.1
        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return false;
        }
    }) { // from class: net.minecraft.world.entity.ai.goal.GoalSelector.2
        @Override // net.minecraft.world.entity.p000ai.goal.WrappedGoal
        public boolean isRunning() {
            return false;
        }
    };
    private final Supplier<ProfilerFiller> profiler;
    private final Map<Goal.Flag, WrappedGoal> lockedFlags = new EnumMap(Goal.Flag.class);
    private final Set<WrappedGoal> availableGoals = Sets.newLinkedHashSet();
    private final EnumSet<Goal.Flag> disabledFlags = EnumSet.noneOf(Goal.Flag.class);
    private int newGoalRate = 3;

    public GoalSelector(Supplier<ProfilerFiller> supplier) {
        this.profiler = supplier;
    }

    public void addGoal(int i, Goal goal) {
        this.availableGoals.add(new WrappedGoal(i, goal));
    }

    public void removeGoal(Goal goal) {
        this.availableGoals.stream().filter(wrappedGoal -> {
            return wrappedGoal.getGoal() == goal;
        }).filter((v0) -> {
            return v0.isRunning();
        }).forEach((v0) -> {
            v0.stop();
        });
        this.availableGoals.removeIf(wrappedGoal2 -> {
            return wrappedGoal2.getGoal() == goal;
        });
    }

    public void tick() {
        ProfilerFiller var1 = (ProfilerFiller)this.profiler.get();
        var1.push("goalCleanup");
        this.getRunningGoals().filter((var1x) -> {
            boolean var2;
            if (var1x.isRunning()) {
                if (!var1x.getFlags().stream().anyMatch(this.disabledFlags::contains) && var1x.canContinueToUse()) {
                    var2 = false;
                    return var2;
                }
            }

            var2 = true;
            return var2;
        }).forEach(Goal::stop);
        this.lockedFlags.forEach((var1x, var2) -> {
            if (!var2.isRunning()) {
                this.lockedFlags.remove(var1x);
            }

        });
        var1.pop();
        var1.push("goalUpdate");
        this.availableGoals.stream().filter((var0) -> !var0.isRunning()).filter((var1x) -> {
            return var1x.getFlags().stream().noneMatch(this.disabledFlags::contains);
        }).filter((var1x) -> var1x.getFlags().stream().allMatch((var2) -> ((WrappedGoal)this.lockedFlags.getOrDefault(var2, NO_GOAL)).canBeReplacedBy(var1x))).filter(WrappedGoal::canUse).forEach((var1x) -> {
            var1x.getFlags().forEach((var2) -> {
                WrappedGoal var3 = (WrappedGoal)this.lockedFlags.getOrDefault(var2, NO_GOAL);
                var3.stop();
                this.lockedFlags.put(var2, var1x);
            });
            var1x.start();
        });
        var1.pop();
        var1.push("goalTick");
        this.getRunningGoals().forEach(WrappedGoal::tick);
        var1.pop();
    }

    public Stream<WrappedGoal> getRunningGoals() {
        return this.availableGoals.stream().filter((v0) -> {
            return v0.isRunning();
        });
    }

    public void disableControlFlag(Goal.Flag flag) {
        this.disabledFlags.add(flag);
    }

    public void enableControlFlag(Goal.Flag flag) {
        this.disabledFlags.remove(flag);
    }

    public void setControlFlag(Goal.Flag flag, boolean z) {
        if (z) {
            enableControlFlag(flag);
        } else {
            disableControlFlag(flag);
        }
    }
}
