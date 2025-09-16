package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/JigsawBlockEntity.class */
public class JigsawBlockEntity extends BlockEntity {
    private ResourceLocation name;
    private ResourceLocation target;
    private ResourceLocation pool;
    private JointType joint;
    private String finalState;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/JigsawBlockEntity$JointType.class */
    public enum JointType implements StringRepresentable {
        ROLLABLE("rollable"),
        ALIGNED("aligned");

        private final String name;

        JointType(String str) {
            this.name = str;
        }

        @Override // net.minecraft.util.StringRepresentable
        public String getSerializedName() {
            return this.name;
        }

        public static Optional<JointType> byName(String str) {
            return Arrays.stream(values()).filter(jointType -> {
                return jointType.getSerializedName().equals(str);
            }).findFirst();
        }
    }

    public JigsawBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
        this.name = new ResourceLocation("empty");
        this.target = new ResourceLocation("empty");
        this.pool = new ResourceLocation("empty");
        this.joint = JointType.ROLLABLE;
        this.finalState = "minecraft:air";
    }

    public JigsawBlockEntity() {
        this(BlockEntityType.JIGSAW);
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public ResourceLocation getTarget() {
        return this.target;
    }

    public ResourceLocation getPool() {
        return this.pool;
    }

    public String getFinalState() {
        return this.finalState;
    }

    public JointType getJoint() {
        return this.joint;
    }

    public void setName(ResourceLocation resourceLocation) {
        this.name = resourceLocation;
    }

    public void setTarget(ResourceLocation resourceLocation) {
        this.target = resourceLocation;
    }

    public void setPool(ResourceLocation resourceLocation) {
        this.pool = resourceLocation;
    }

    public void setFinalState(String str) {
        this.finalState = str;
    }

    public void setJoint(JointType jointType) {
        this.joint = jointType;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.putString("name", this.name.toString());
        compoundTag.putString("target", this.target.toString());
        compoundTag.putString("pool", this.pool.toString());
        compoundTag.putString("final_state", this.finalState);
        compoundTag.putString("joint", this.joint.getSerializedName());
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.name = new ResourceLocation(compoundTag.getString("name"));
        this.target = new ResourceLocation(compoundTag.getString("target"));
        this.pool = new ResourceLocation(compoundTag.getString("pool"));
        this.finalState = compoundTag.getString("final_state");
        this.joint = JointType.byName(compoundTag.getString("joint")).orElseGet(() -> {
            return JigsawBlock.getFrontFacing(blockState).getAxis().isHorizontal() ? JointType.ALIGNED : JointType.ROLLABLE;
        });
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 12, getUpdateTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    public void generate(ServerLevel serverLevel, int i, boolean z) {
        ChunkGenerator generator = serverLevel.getChunkSource().getGenerator();
        StructureManager structureManager = serverLevel.getStructureManager();
        StructureFeatureManager structureFeatureManager = serverLevel.structureFeatureManager();
        Random random = serverLevel.getRandom();
        BlockPos blockPos = getBlockPos();
        List<PoolElementStructurePiece> newArrayList = Lists.newArrayList();
        StructureTemplate structureTemplate = new StructureTemplate();
        structureTemplate.fillFromWorld(serverLevel, blockPos, new BlockPos(1, 1, 1), false, null);
        JigsawPlacement.addPieces(serverLevel.registryAccess(), new PoolElementStructurePiece(structureManager, new SinglePoolElement(structureTemplate), blockPos, 1, Rotation.NONE, new BoundingBox(blockPos, blockPos)), i, PoolElementStructurePiece::new, generator, structureManager, newArrayList, random);
        Iterator<PoolElementStructurePiece> it = newArrayList.iterator();
        while (it.hasNext()) {
            it.next().place(serverLevel, structureFeatureManager, generator, random, BoundingBox.infinite(), blockPos, z);
        }
    }
}
