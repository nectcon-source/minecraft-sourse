package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/WaterAvoidingRandomFlyingGoal.class */
public class WaterAvoidingRandomFlyingGoal extends WaterAvoidingRandomStrollGoal {
    public WaterAvoidingRandomFlyingGoal(PathfinderMob pathfinderMob, double d) {
        super(pathfinderMob, d);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.WaterAvoidingRandomStrollGoal, net.minecraft.world.entity.p000ai.goal.RandomStrollGoal
    @Nullable
    protected Vec3 getPosition() {
        Vec3 vec3 = null;
        if (this.mob.isInWater()) {
            vec3 = RandomPos.getLandPos(this.mob, 15, 15);
        }
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            vec3 = getTreePos();
        }
        return vec3 == null ? super.getPosition() : vec3;
    }

    @Nullable
    private Vec3 getTreePos() {
        BlockPos blockPosition = this.mob.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (BlockPos blockPos : BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 3.0d), Mth.floor(this.mob.getY() - 6.0d), Mth.floor(this.mob.getZ() - 3.0d), Mth.floor(this.mob.getX() + 3.0d), Mth.floor(this.mob.getY() + 6.0d), Mth.floor(this.mob.getZ() + 3.0d))) {
            if (!blockPosition.equals(blockPos)) {
                Block block = this.mob.level.getBlockState(mutableBlockPos2.setWithOffset(blockPos, Direction.DOWN)).getBlock();
                if (((block instanceof LeavesBlock) || block.is(BlockTags.LOGS)) && this.mob.level.isEmptyBlock(blockPos) && this.mob.level.isEmptyBlock(mutableBlockPos.setWithOffset(blockPos, Direction.UP))) {
                    return Vec3.atBottomCenterOf(blockPos);
                }
            }
        }
        return null;
    }
}
