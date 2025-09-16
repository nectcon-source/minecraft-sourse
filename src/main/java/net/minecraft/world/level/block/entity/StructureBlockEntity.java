package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/StructureBlockEntity.class */
public class StructureBlockEntity extends BlockEntity {
    private ResourceLocation structureName;
    private String author;
    private String metaData;
    private BlockPos structurePos;
    private BlockPos structureSize;
    private Mirror mirror;
    private Rotation rotation;
    private StructureMode mode;
    private boolean ignoreEntities;
    private boolean powered;
    private boolean showAir;
    private boolean showBoundingBox;
    private float integrity;
    private long seed;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/StructureBlockEntity$UpdateType.class */
    public enum UpdateType {
        UPDATE_DATA,
        SAVE_AREA,
        LOAD_AREA,
        SCAN_AREA
    }

    public StructureBlockEntity() {
        super(BlockEntityType.STRUCTURE_BLOCK);
        this.author = "";
        this.metaData = "";
        this.structurePos = new BlockPos(0, 1, 0);
        this.structureSize = BlockPos.ZERO;
        this.mirror = Mirror.NONE;
        this.rotation = Rotation.NONE;
        this.mode = StructureMode.DATA;
        this.ignoreEntities = true;
        this.showBoundingBox = true;
        this.integrity = 1.0f;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public double getViewDistance() {
        return 96.0d;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.putString("name", getStructureName());
        compoundTag.putString("author", this.author);
        compoundTag.putString("metadata", this.metaData);
        compoundTag.putInt("posX", this.structurePos.getX());
        compoundTag.putInt("posY", this.structurePos.getY());
        compoundTag.putInt("posZ", this.structurePos.getZ());
        compoundTag.putInt("sizeX", this.structureSize.getX());
        compoundTag.putInt("sizeY", this.structureSize.getY());
        compoundTag.putInt("sizeZ", this.structureSize.getZ());
        compoundTag.putString("rotation", this.rotation.toString());
        compoundTag.putString("mirror", this.mirror.toString());
        compoundTag.putString("mode", this.mode.toString());
        compoundTag.putBoolean("ignoreEntities", this.ignoreEntities);
        compoundTag.putBoolean("powered", this.powered);
        compoundTag.putBoolean("showair", this.showAir);
        compoundTag.putBoolean("showboundingbox", this.showBoundingBox);
        compoundTag.putFloat("integrity", this.integrity);
        compoundTag.putLong("seed", this.seed);
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        setStructureName(compoundTag.getString("name"));
        this.author = compoundTag.getString("author");
        this.metaData = compoundTag.getString("metadata");
        this.structurePos = new BlockPos(Mth.clamp(compoundTag.getInt("posX"), -48, 48), Mth.clamp(compoundTag.getInt("posY"), -48, 48), Mth.clamp(compoundTag.getInt("posZ"), -48, 48));
        this.structureSize = new BlockPos(Mth.clamp(compoundTag.getInt("sizeX"), 0, 48), Mth.clamp(compoundTag.getInt("sizeY"), 0, 48), Mth.clamp(compoundTag.getInt("sizeZ"), 0, 48));
        try {
            this.rotation = Rotation.valueOf(compoundTag.getString("rotation"));
        } catch (IllegalArgumentException e) {
            this.rotation = Rotation.NONE;
        }
        try {
            this.mirror = Mirror.valueOf(compoundTag.getString("mirror"));
        } catch (IllegalArgumentException e2) {
            this.mirror = Mirror.NONE;
        }
        try {
            this.mode = StructureMode.valueOf(compoundTag.getString("mode"));
        } catch (IllegalArgumentException e3) {
            this.mode = StructureMode.DATA;
        }
        this.ignoreEntities = compoundTag.getBoolean("ignoreEntities");
        this.powered = compoundTag.getBoolean("powered");
        this.showAir = compoundTag.getBoolean("showair");
        this.showBoundingBox = compoundTag.getBoolean("showboundingbox");
        if (compoundTag.contains("integrity")) {
            this.integrity = compoundTag.getFloat("integrity");
        } else {
            this.integrity = 1.0f;
        }
        this.seed = compoundTag.getLong("seed");
        updateBlockState();
    }

    private void updateBlockState() {
        if (this.level == null) {
            return;
        }
        BlockPos blockPos = getBlockPos();
        BlockState blockState = this.level.getBlockState(blockPos);
        if (blockState.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock(blockPos, (BlockState) blockState.setValue(StructureBlock.MODE, this.mode), 2);
        }
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 7, getUpdateTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    public boolean usedBy(Player player) {
        if (!player.canUseGameMasterBlocks()) {
            return false;
        }
        if (player.getCommandSenderWorld().isClientSide) {
            player.openStructureBlock(this);
            return true;
        }
        return true;
    }

    public String getStructureName() {
        return this.structureName == null ? "" : this.structureName.toString();
    }

    public String getStructurePath() {
        return this.structureName == null ? "" : this.structureName.getPath();
    }

    public boolean hasStructureName() {
        return this.structureName != null;
    }

    public void setStructureName(@Nullable String str) {
        setStructureName(StringUtil.isNullOrEmpty(str) ? null : ResourceLocation.tryParse(str));
    }

    public void setStructureName(@Nullable ResourceLocation resourceLocation) {
        this.structureName = resourceLocation;
    }

    public void createdBy(LivingEntity livingEntity) {
        this.author = livingEntity.getName().getString();
    }

    public BlockPos getStructurePos() {
        return this.structurePos;
    }

    public void setStructurePos(BlockPos blockPos) {
        this.structurePos = blockPos;
    }

    public BlockPos getStructureSize() {
        return this.structureSize;
    }

    public void setStructureSize(BlockPos blockPos) {
        this.structureSize = blockPos;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public void setMirror(Mirror mirror) {
        this.mirror = mirror;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public String getMetaData() {
        return this.metaData;
    }

    public void setMetaData(String str) {
        this.metaData = str;
    }

    public StructureMode getMode() {
        return this.mode;
    }

    public void setMode(StructureMode structureMode) {
        this.mode = structureMode;
        BlockState blockState = this.level.getBlockState(getBlockPos());
        if (blockState.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock(getBlockPos(), (BlockState) blockState.setValue(StructureBlock.MODE, structureMode), 2);
        }
    }

    public void nextMode() {
        switch (getMode()) {
            case SAVE:
                setMode(StructureMode.LOAD);
                break;
            case LOAD:
                setMode(StructureMode.CORNER);
                break;
            case CORNER:
                setMode(StructureMode.DATA);
                break;
            case DATA:
                setMode(StructureMode.SAVE);
                break;
        }
    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    public void setIgnoreEntities(boolean z) {
        this.ignoreEntities = z;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public void setIntegrity(float f) {
        this.integrity = f;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long j) {
        this.seed = j;
    }

    public boolean detectSize() {
        if (this.mode != StructureMode.SAVE) {
            return false;
        }
        BlockPos blockPos = getBlockPos();
        List<StructureBlockEntity> filterRelatedCornerBlocks = filterRelatedCornerBlocks(getNearbyCornerBlocks(new BlockPos(blockPos.getX() - 80, 0, blockPos.getZ() - 80), new BlockPos(blockPos.getX() + 80, 255, blockPos.getZ() + 80)));
        if (filterRelatedCornerBlocks.size() < 1) {
            return false;
        }
        BoundingBox calculateEnclosingBoundingBox = calculateEnclosingBoundingBox(blockPos, filterRelatedCornerBlocks);
        if (calculateEnclosingBoundingBox.x1 - calculateEnclosingBoundingBox.x0 > 1 && calculateEnclosingBoundingBox.y1 - calculateEnclosingBoundingBox.y0 > 1 && calculateEnclosingBoundingBox.z1 - calculateEnclosingBoundingBox.z0 > 1) {
            this.structurePos = new BlockPos((calculateEnclosingBoundingBox.x0 - blockPos.getX()) + 1, (calculateEnclosingBoundingBox.y0 - blockPos.getY()) + 1, (calculateEnclosingBoundingBox.z0 - blockPos.getZ()) + 1);
            this.structureSize = new BlockPos((calculateEnclosingBoundingBox.x1 - calculateEnclosingBoundingBox.x0) - 1, (calculateEnclosingBoundingBox.y1 - calculateEnclosingBoundingBox.y0) - 1, (calculateEnclosingBoundingBox.z1 - calculateEnclosingBoundingBox.z0) - 1);
            setChanged();
            BlockState blockState = this.level.getBlockState(blockPos);
            this.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            return true;
        }
        return false;
    }

    private List<StructureBlockEntity> filterRelatedCornerBlocks(List<StructureBlockEntity> list) {
        return (List) list.stream().filter(structureBlockEntity -> {
            return structureBlockEntity.mode == StructureMode.CORNER && Objects.equals(this.structureName, structureBlockEntity.structureName);
        }).collect(Collectors.toList());
    }

    private List<StructureBlockEntity> getNearbyCornerBlocks(BlockPos blockPos, BlockPos blockPos2) {
        BlockEntity blockEntity;
        List<StructureBlockEntity> newArrayList = Lists.newArrayList();
        for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
            if (this.level.getBlockState(blockPos3).is(Blocks.STRUCTURE_BLOCK) && (blockEntity = this.level.getBlockEntity(blockPos3)) != null && (blockEntity instanceof StructureBlockEntity)) {
                newArrayList.add((StructureBlockEntity) blockEntity);
            }
        }
        return newArrayList;
    }

    private BoundingBox calculateEnclosingBoundingBox(BlockPos blockPos, List<StructureBlockEntity> list) {
        BoundingBox boundingBox;
        if (list.size() > 1) {
            BlockPos blockPos2 = list.get(0).getBlockPos();
            boundingBox = new BoundingBox(blockPos2, blockPos2);
        } else {
            boundingBox = new BoundingBox(blockPos, blockPos);
        }
        Iterator<StructureBlockEntity> it = list.iterator();
        while (it.hasNext()) {
            BlockPos blockPos3 = it.next().getBlockPos();
            if (blockPos3.getX() < boundingBox.x0) {
                boundingBox.x0 = blockPos3.getX();
            } else if (blockPos3.getX() > boundingBox.x1) {
                boundingBox.x1 = blockPos3.getX();
            }
            if (blockPos3.getY() < boundingBox.y0) {
                boundingBox.y0 = blockPos3.getY();
            } else if (blockPos3.getY() > boundingBox.y1) {
                boundingBox.y1 = blockPos3.getY();
            }
            if (blockPos3.getZ() < boundingBox.z0) {
                boundingBox.z0 = blockPos3.getZ();
            } else if (blockPos3.getZ() > boundingBox.z1) {
                boundingBox.z1 = blockPos3.getZ();
            }
        }
        return boundingBox;
    }

    public boolean saveStructure() {
        return saveStructure(true);
    }

    public boolean saveStructure(boolean z) {
        if (this.mode != StructureMode.SAVE || this.level.isClientSide || this.structureName == null) {
            return false;
        }
        BlockPos offset = getBlockPos().offset(this.structurePos);
        StructureManager structureManager = ((ServerLevel) this.level).getStructureManager();
        try {
            StructureTemplate orCreate = structureManager.getOrCreate(this.structureName);
            orCreate.fillFromWorld(this.level, offset, this.structureSize, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
            orCreate.setAuthor(this.author);
            if (z) {
                try {
                    return structureManager.save(this.structureName);
                } catch (ResourceLocationException e) {
                    return false;
                }
            }
            return true;
        } catch (ResourceLocationException e2) {
            return false;
        }
    }

    public boolean loadStructure(ServerLevel serverLevel) {
        return loadStructure(serverLevel, true);
    }

    private static Random createRandom(long j) {
        if (j == 0) {
            return new Random(Util.getMillis());
        }
        return new Random(j);
    }

    public boolean loadStructure(ServerLevel serverLevel, boolean z) {
        if (this.mode != StructureMode.LOAD || this.structureName == null) {
            return false;
        }
        try {
            StructureTemplate structureTemplate = serverLevel.getStructureManager().get(this.structureName);
            if (structureTemplate == null) {
                return false;
            }
            return loadStructure(serverLevel, z, structureTemplate);
        } catch (ResourceLocationException e) {
            return false;
        }
    }

    public boolean loadStructure(ServerLevel serverLevel, boolean z, StructureTemplate structureTemplate) {
        BlockPos blockPos = getBlockPos();
        if (!StringUtil.isNullOrEmpty(structureTemplate.getAuthor())) {
            this.author = structureTemplate.getAuthor();
        }
        BlockPos size = structureTemplate.getSize();
        boolean equals = this.structureSize.equals(size);
        if (!equals) {
            this.structureSize = size;
            setChanged();
            BlockState blockState = serverLevel.getBlockState(blockPos);
            serverLevel.sendBlockUpdated(blockPos, blockState, blockState, 3);
        }
        if (!z || equals) {
            StructurePlaceSettings chunkPos = new StructurePlaceSettings().setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities).setChunkPos(null);
            if (this.integrity < 1.0f) {
                chunkPos.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0f, 1.0f))).setRandom(createRandom(this.seed));
            }
            structureTemplate.placeInWorldChunk(serverLevel, blockPos.offset(this.structurePos), chunkPos, createRandom(this.seed));
            return true;
        }
        return false;
    }

    public void unloadStructure() {
        if (this.structureName == null) {
            return;
        }
        ((ServerLevel) this.level).getStructureManager().remove(this.structureName);
    }

    public boolean isStructureLoadable() {
        if (this.mode != StructureMode.LOAD || this.level.isClientSide || this.structureName == null) {
            return false;
        }
        try {
            return ((ServerLevel) this.level).getStructureManager().get(this.structureName) != null;
        } catch (ResourceLocationException e) {
            return false;
        }
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean z) {
        this.powered = z;
    }

    public boolean getShowAir() {
        return this.showAir;
    }

    public void setShowAir(boolean z) {
        this.showAir = z;
    }

    public boolean getShowBoundingBox() {
        return this.showBoundingBox;
    }

    public void setShowBoundingBox(boolean z) {
        this.showBoundingBox = z;
    }
}
