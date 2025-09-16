package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/ClipContext.class */
public class ClipContext {
    private final Vec3 from;

    /* renamed from: to */
    private final Vec3 to;
    private final Block block;
    private final Fluid fluid;
    private final CollisionContext collisionContext;



    public ClipContext(Vec3 vec3, Vec3 vec32, Block block, Fluid fluid, Entity entity) {
        this.from = vec3;
        this.to = vec32;
        this.block = block;
        this.fluid = fluid;
        this.collisionContext = CollisionContext.of(entity);
    }

    public Vec3 getTo() {
        return this.to;
    }

    public Vec3 getFrom() {
        return this.from;
    }

    public VoxelShape getBlockShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return this.block.get(blockState, blockGetter, blockPos, this.collisionContext);
    }

    public VoxelShape getFluidShape(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        return this.fluid.canPick(fluidState) ? fluidState.getShape(blockGetter, blockPos) : Shapes.empty();
    }

    public static enum Block implements ShapeGetter {
        COLLIDER(BlockBehaviour.BlockStateBase::getCollisionShape),
        OUTLINE(BlockBehaviour.BlockStateBase::getShape),
        VISUAL(BlockBehaviour.BlockStateBase::getVisualShape);

        private final ShapeGetter shapeGetter;

        private Block(ShapeGetter var3) {
            this.shapeGetter = var3;
        }

        @Override
        public VoxelShape get(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
            return this.shapeGetter.get(var1, var2, var3, var4);
        }
    }


    public static enum Fluid {
        NONE((var0) -> false),
        SOURCE_ONLY(FluidState::isSource),
        ANY((var0) -> !var0.isEmpty());

        private final Predicate<FluidState> canPick;

        private Fluid(Predicate<FluidState> var3) {
            this.canPick = var3;
        }

        public boolean canPick(FluidState var1) {
            return this.canPick.test(var1);
        }
    }

    public interface ShapeGetter {
        VoxelShape get(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4);
    }
}
