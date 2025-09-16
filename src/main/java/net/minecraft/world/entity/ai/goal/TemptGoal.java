package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/TemptGoal.class */
public class TemptGoal extends Goal {
    private static final TargetingConditions TEMP_TARGETING = new TargetingConditions().range(10.0d).allowInvulnerable().allowSameTeam().allowNonAttackable().allowUnseeable();
    protected final PathfinderMob mob;
    private final double speedModifier;

    /* renamed from: px */
    private double f437px;

    /* renamed from: py */
    private double f438py;

    /* renamed from: pz */
    private double f439pz;
    private double pRotX;
    private double pRotY;
    protected Player player;
    private int calmDown;
    private boolean isRunning;
    private final Ingredient items;
    private final boolean canScare;

    public TemptGoal(PathfinderMob pathfinderMob, double d, Ingredient ingredient, boolean z) {
        this(pathfinderMob, d, z, ingredient);
    }

    public TemptGoal(PathfinderMob pathfinderMob, double d, boolean z, Ingredient ingredient) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.items = ingredient;
        this.canScare = z;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(pathfinderMob.getNavigation() instanceof GroundPathNavigation) && !(pathfinderMob.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for TemptGoal");
        }
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.calmDown > 0) {
            this.calmDown--;
            return false;
        }
        this.player = this.mob.level.getNearestPlayer(TEMP_TARGETING, this.mob);
        if (this.player == null) {
            return false;
        }
        return shouldFollowItem(this.player.getMainHandItem()) || shouldFollowItem(this.player.getOffhandItem());
    }

    protected boolean shouldFollowItem(ItemStack itemStack) {
        return this.items.test(itemStack);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        if (canScare()) {
            if (this.mob.distanceToSqr(this.player) >= 36.0d) {
                this.f437px = this.player.getX();
                this.f438py = this.player.getY();
                this.f439pz = this.player.getZ();
            } else if (this.player.distanceToSqr(this.f437px, this.f438py, this.f439pz) > 0.010000000000000002d || Math.abs(this.player.xRot - this.pRotX) > 5.0d || Math.abs(this.player.yRot - this.pRotY) > 5.0d) {
                return false;
            }
            this.pRotX = this.player.xRot;
            this.pRotY = this.player.yRot;
        }
        return canUse();
    }

    protected boolean canScare() {
        return this.canScare;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.f437px = this.player.getX();
        this.f438py = this.player.getY();
        this.f439pz = this.player.getZ();
        this.isRunning = true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.player = null;
        this.mob.getNavigation().stop();
        this.calmDown = 100;
        this.isRunning = false;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.mob.getLookControl().setLookAt(this.player, this.mob.getMaxHeadYRot() + 20, this.mob.getMaxHeadXRot());
        if (this.mob.distanceToSqr(this.player) < 6.25d) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(this.player, this.speedModifier);
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
