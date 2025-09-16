package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BeehiveBlockEntity.class */
public class BeehiveBlockEntity extends BlockEntity implements TickableBlockEntity {
    private final List<BeeData> stored;

    @Nullable
    private BlockPos savedFlowerPos;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BeehiveBlockEntity$BeeReleaseStatus.class */
    public enum BeeReleaseStatus {
        HONEY_DELIVERED,
        BEE_RELEASED,
        EMERGENCY
    }

    public BeehiveBlockEntity() {
        super(BlockEntityType.BEEHIVE);
        this.stored = Lists.newArrayList();
        this.savedFlowerPos = null;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void setChanged() {
        if (isFireNearby()) {
            emptyAllLivingFromHive(null, this.level.getBlockState(getBlockPos()), BeeReleaseStatus.EMERGENCY);
        }
        super.setChanged();
    }

    public boolean isFireNearby() {
        if (this.level == null) {
            return false;
        }
        Iterator<BlockPos> it = BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1)).iterator();
        while (it.hasNext()) {
            if (this.level.getBlockState(it.next()).getBlock() instanceof FireBlock) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.stored.isEmpty();
    }

    public boolean isFull() {
        return this.stored.size() == 3;
    }

    public void emptyAllLivingFromHive(@Nullable Player player, BlockState blockState, BeeReleaseStatus beeReleaseStatus) {
        List<Entity> releaseAllOccupants = releaseAllOccupants(blockState, beeReleaseStatus);
        if (player != null) {
            for (Entity entity : releaseAllOccupants) {
                if (entity instanceof Bee) {
                    Bee bee = (Bee) entity;
                    if (player.position().distanceToSqr(entity.position()) <= 16.0d) {
                        if (!isSedated()) {
                            bee.setTarget(player);
                        } else {
                            bee.setStayOutOfHiveCountdown(400);
                        }
                    }
                }
            }
        }
    }

    private List<Entity> releaseAllOccupants(BlockState blockState, BeeReleaseStatus beeReleaseStatus) {
        List<Entity> newArrayList = Lists.newArrayList();
        this.stored.removeIf(beeData -> {
            return releaseOccupant(blockState, beeData, newArrayList, beeReleaseStatus);
        });
        return newArrayList;
    }

    public void addOccupant(Entity entity, boolean z) {
        addOccupantWithPresetTicks(entity, z, 0);
    }

    public int getOccupantCount() {
        return this.stored.size();
    }

    public static int getHoneyLevel(BlockState blockState) {
        return ((Integer) blockState.getValue(BeehiveBlock.HONEY_LEVEL)).intValue();
    }

    public boolean isSedated() {
        return CampfireBlock.isSmokeyPos(this.level, getBlockPos());
    }

    protected void sendDebugPackets() {
        DebugPackets.sendHiveInfo(this);
    }

    public void addOccupantWithPresetTicks(Entity entity, boolean z, int i) {
        if (this.stored.size() >= 3) {
            return;
        }
        entity.stopRiding();
        entity.ejectPassengers();
        CompoundTag compoundTag = new CompoundTag();
        entity.save(compoundTag);
        this.stored.add(new BeeData(compoundTag, i, z ? 2400 : 600));
        if (this.level != null) {
            if (entity instanceof Bee) {
                Bee bee = (Bee) entity;
                if (bee.hasSavedFlowerPos() && (!hasSavedFlowerPos() || this.level.random.nextBoolean())) {
                    this.savedFlowerPos = bee.getSavedFlowerPos();
                }
            }
            BlockPos blockPos = getBlockPos();
            this.level.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        entity.remove();
    }

    private boolean releaseOccupant(BlockState blockState, BeeData beeData, @Nullable List<Entity> list, BeeReleaseStatus beeReleaseStatus) {
        if ((this.level.isNight() || this.level.isRaining()) && beeReleaseStatus != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
            return false;
        } else {
            BlockPos var5 = this.getBlockPos();
            CompoundTag var6 = beeData.entityData;
            var6.remove("Passengers");
            var6.remove("Leash");
            var6.remove("UUID");
            Direction var7 = (Direction)blockState.getValue(BeehiveBlock.FACING);
            BlockPos var8 = var5.relative(var7);
            boolean var9 = !this.level.getBlockState(var8).getCollisionShape(this.level, var8).isEmpty();
            if (var9 && beeReleaseStatus != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
                return false;
            } else {
                Entity var10 = EntityType.loadEntityRecursive(var6, this.level, (var0) -> var0);
                if (var10 != null) {
                    if (!var10.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
                        return false;
                    } else {
                        if (var10 instanceof Bee) {
                            Bee var11 = (Bee)var10;
                            if (this.hasSavedFlowerPos() && !var11.hasSavedFlowerPos() && this.level.random.nextFloat() < 0.9F) {
                                var11.setSavedFlowerPos(this.savedFlowerPos);
                            }

                            if (beeReleaseStatus == BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED) {
                                var11.dropOffNectar();
                                if (blockState.getBlock().is(BlockTags.BEEHIVES)) {
                                    int var12 = getHoneyLevel(blockState);
                                    if (var12 < 5) {
                                        int var13 = this.level.random.nextInt(100) == 0 ? 2 : 1;
                                        if (var12 + var13 > 5) {
                                            --var13;
                                        }

                                        this.level.setBlockAndUpdate(this.getBlockPos(), (BlockState)blockState.setValue(BeehiveBlock.HONEY_LEVEL, var12 + var13));
                                    }
                                }
                            }

                            this.setBeeReleaseData(beeData.ticksInHive, var11);
                            if (list != null) {
                                list.add(var11);
                            }

                            float var21 = var10.getBbWidth();
                            double var22 = var9 ? (double)0.0F : 0.55 + (double)(var21 / 2.0F);
                            double var15 = (double)var5.getX() + (double)0.5F + var22 * (double)var7.getStepX();
                            double var17 = (double)var5.getY() + (double)0.5F - (double)(var10.getBbHeight() / 2.0F);
                            double var19 = (double)var5.getZ() + (double)0.5F + var22 * (double)var7.getStepZ();
                            var10.moveTo(var15, var17, var19, var10.yRot, var10.xRot);
                        }

                        this.level.playSound(null, var5, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return this.level.addFreshEntity(var10);
                    }
                } else {
                    return false;
                }
            }
        }
    }

    private void setBeeReleaseData(int i, Bee bee) {
        int age = bee.getAge();
        if (age < 0) {
            bee.setAge(Math.min(0, age + i));
        } else if (age > 0) {
            bee.setAge(Math.max(0, age - i));
        }
        bee.setInLoveTime(Math.max(0, bee.getInLoveTime() - i));
        bee.resetTicksWithoutNectarSinceExitingHive();
    }

    private boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    private void tickOccupants() {
        Iterator<BeeData> it = this.stored.iterator();
        BlockState blockState = getBlockState();
        while (it.hasNext()) {
            BeeData next = it.next();
            if (next.ticksInHive > next.minOccupationTicks) {
                if (releaseOccupant(blockState, next, null, next.entityData.getBoolean("HasNectar") ? BeeReleaseStatus.HONEY_DELIVERED : BeeReleaseStatus.BEE_RELEASED)) {
                    it.remove();
                }
            }
            BeeData.access$208(next);
        }
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        if (this.level.isClientSide) {
            return;
        }
        tickOccupants();
        BlockPos blockPos = getBlockPos();
        if (this.stored.size() > 0 && this.level.getRandom().nextDouble() < 0.005d) {
            this.level.playSound(null, blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        sendDebugPackets();
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.stored.clear();
        ListTag list = compoundTag.getList("Bees", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag compound = list.getCompound(i);
            this.stored.add(new BeeData(compound.getCompound("EntityData"), compound.getInt("TicksInHive"), compound.getInt("MinOccupationTicks")));
        }
        this.savedFlowerPos = null;
        if (compoundTag.contains("FlowerPos")) {
            this.savedFlowerPos = NbtUtils.readBlockPos(compoundTag.getCompound("FlowerPos"));
        }
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.put("Bees", writeBees());
        if (hasSavedFlowerPos()) {
            compoundTag.put("FlowerPos", NbtUtils.writeBlockPos(this.savedFlowerPos));
        }
        return compoundTag;
    }

    public ListTag writeBees() {
        ListTag listTag = new ListTag();
        for (BeeData beeData : this.stored) {
            beeData.entityData.remove("UUID");
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("EntityData", beeData.entityData);
            compoundTag.putInt("TicksInHive", beeData.ticksInHive);
            compoundTag.putInt("MinOccupationTicks", beeData.minOccupationTicks);
            listTag.add(compoundTag);
        }
        return listTag;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BeehiveBlockEntity$BeeData.class */
    static class BeeData {
        private final CompoundTag entityData;
        private int ticksInHive;
        private final int minOccupationTicks;

        static /* synthetic */ int access$208(BeeData beeData) {
            int i = beeData.ticksInHive;
            beeData.ticksInHive = i + 1;
            return i;
        }

        private BeeData(CompoundTag compoundTag, int i, int i2) {
            compoundTag.remove("UUID");
            this.entityData = compoundTag;
            this.ticksInHive = i;
            this.minOccupationTicks = i2;
        }
    }
}
