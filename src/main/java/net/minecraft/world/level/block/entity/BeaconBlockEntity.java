package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BeaconBlockEntity.class */
public class BeaconBlockEntity extends BlockEntity implements MenuProvider, TickableBlockEntity {
    public static final MobEffect[][] BEACON_EFFECTS = {new MobEffect[]{MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED}, new MobEffect[]{MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP}, new MobEffect[]{MobEffects.DAMAGE_BOOST}, new MobEffect[]{MobEffects.REGENERATION}};
    private static final Set<MobEffect> VALID_EFFECTS = (Set) Arrays.stream(BEACON_EFFECTS).flatMap((v0) -> {
        return Arrays.stream(v0);
    }).collect(Collectors.toSet());
    private List<BeaconBeamSection> beamSections;
    private List<BeaconBeamSection> checkingBeamSections;
    private int levels;
    private int lastCheckY;

    @Nullable
    private MobEffect primaryPower;

    @Nullable
    private MobEffect secondaryPower;

    @Nullable
    private Component name;
    private LockCode lockKey;
    private final ContainerData dataAccess;

    public BeaconBlockEntity() {
        super(BlockEntityType.BEACON);
        this.beamSections = Lists.newArrayList();
        this.checkingBeamSections = Lists.newArrayList();
        this.lastCheckY = -1;
        this.lockKey = LockCode.NO_LOCK;
        this.dataAccess = new ContainerData() { // from class: net.minecraft.world.level.block.entity.BeaconBlockEntity.1
            @Override // net.minecraft.world.inventory.ContainerData
            public int get(int i) {
                switch (i) {
                    case 0:
                        return BeaconBlockEntity.this.levels;
                    case 1:
                        return MobEffect.getId(BeaconBlockEntity.this.primaryPower);
                    case 2:
                        return MobEffect.getId(BeaconBlockEntity.this.secondaryPower);
                    default:
                        return 0;
                }
            }

            @Override // net.minecraft.world.inventory.ContainerData
            public void set(int i, int i2) {
                switch (i) {
                    case 0:
                        BeaconBlockEntity.this.levels = i2;
                        break;
                    case 1:
                        if (!BeaconBlockEntity.this.level.isClientSide && !BeaconBlockEntity.this.beamSections.isEmpty()) {
                            BeaconBlockEntity.this.playSound(SoundEvents.BEACON_POWER_SELECT);
                        }
                        BeaconBlockEntity.this.primaryPower = BeaconBlockEntity.getValidEffectById(i2);
                        break;
                    case 2:
                        BeaconBlockEntity.this.secondaryPower = BeaconBlockEntity.getValidEffectById(i2);
                        break;
                }
            }

            @Override // net.minecraft.world.inventory.ContainerData
            public int getCount() {
                return 3;
            }
        };
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        BlockPos blockPos;
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        if (this.lastCheckY < y) {
            blockPos = this.worldPosition;
            this.checkingBeamSections = Lists.newArrayList();
            this.lastCheckY = blockPos.getY() - 1;
        } else {
            blockPos = new BlockPos(x, this.lastCheckY + 1, z);
        }
        BeaconBeamSection beaconBeamSection = this.checkingBeamSections.isEmpty() ? null : this.checkingBeamSections.get(this.checkingBeamSections.size() - 1);
        int height = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        for (int i = 0; i < 10 && blockPos.getY() <= height; i++) {
            BlockState blockState = this.level.getBlockState(blockPos);
            ItemLike block = blockState.getBlock();
            if (!(block instanceof BeaconBeamBlock)) {
                if (beaconBeamSection != null && (blockState.getLightBlock(this.level, blockPos) < 15 || block == Blocks.BEDROCK)) {
                    beaconBeamSection.increaseHeight();
                } else {
                    this.checkingBeamSections.clear();
                    this.lastCheckY = height;
                    break;
                }
            } else {
                float[] textureDiffuseColors = ((BeaconBeamBlock) block).getColor().getTextureDiffuseColors();
                if (this.checkingBeamSections.size() <= 1) {
                    beaconBeamSection = new BeaconBeamSection(textureDiffuseColors);
                    this.checkingBeamSections.add(beaconBeamSection);
                } else if (beaconBeamSection != null) {
                    if (Arrays.equals(textureDiffuseColors, beaconBeamSection.color)) {
                        beaconBeamSection.increaseHeight();
                    } else {
                        beaconBeamSection = new BeaconBeamSection(new float[]{(beaconBeamSection.color[0] + textureDiffuseColors[0]) / 2.0f, (beaconBeamSection.color[1] + textureDiffuseColors[1]) / 2.0f, (beaconBeamSection.color[2] + textureDiffuseColors[2]) / 2.0f});
                        this.checkingBeamSections.add(beaconBeamSection);
                    }
                }
            }
            blockPos = blockPos.above();
            this.lastCheckY++;
        }
        int i2 = this.levels;
        if (this.level.getGameTime() % 80 == 0) {
            if (!this.beamSections.isEmpty()) {
                updateBase(x, y, z);
            }
            if (this.levels > 0 && !this.beamSections.isEmpty()) {
                applyEffects();
                playSound(SoundEvents.BEACON_AMBIENT);
            }
        }
        if (this.lastCheckY >= height) {
            this.lastCheckY = -1;
            boolean z2 = i2 > 0;
            this.beamSections = this.checkingBeamSections;
            if (!this.level.isClientSide) {
                boolean z3 = this.levels > 0;
                if (!z2 && z3) {
                    playSound(SoundEvents.BEACON_ACTIVATE);
                    Iterator it = this.level.getEntitiesOfClass(ServerPlayer.class, new AABB(x, y, z, x, y - 4, z).inflate(10.0d, 5.0d, 10.0d)).iterator();
                    while (it.hasNext()) {
                        CriteriaTriggers.CONSTRUCT_BEACON.trigger((ServerPlayer) it.next(), this);
                    }
                    return;
                }
                if (z2 && !z3) {
                    playSound(SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }
    }

    private void updateBase(int i, int i2, int i3) {
        int i4;
        this.levels = 0;
        for (int i5 = 1; i5 <= 4 && (i4 = i2 - i5) >= 0; i5++) {
            boolean z = true;
            for (int i6 = i - i5; i6 <= i + i5 && z; i6++) {
                int i7 = i3 - i5;
                while (true) {
                    if (i7 > i3 + i5) {
                        break;
                    }
                    if (this.level.getBlockState(new BlockPos(i6, i4, i7)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                        i7++;
                    } else {
                        z = false;
                        break;
                    }
                }
            }
            if (z) {
                this.levels = i5;
            } else {
                return;
            }
        }
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void setRemoved() {
        playSound(SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    private void applyEffects() {
        if (this.level.isClientSide || this.primaryPower == null) {
            return;
        }
        double d = (this.levels * 10) + 10;
        int i = 0;
        if (this.levels >= 4 && this.primaryPower == this.secondaryPower) {
            i = 1;
        }
        int i2 = (9 + (this.levels * 2)) * 20;
        List<Player> entitiesOfClass = this.level.getEntitiesOfClass(Player.class, new AABB(this.worldPosition).inflate(d).expandTowards(0.0d, this.level.getMaxBuildHeight(), 0.0d));
        Iterator<Player> it = entitiesOfClass.iterator();
        while (it.hasNext()) {
            it.next().addEffect(new MobEffectInstance(this.primaryPower, i2, i, true, true));
        }
        if (this.levels >= 4 && this.primaryPower != this.secondaryPower && this.secondaryPower != null) {
            Iterator<Player> it2 = entitiesOfClass.iterator();
            while (it2.hasNext()) {
                it2.next().addEffect(new MobEffectInstance(this.secondaryPower, i2, 0, true, true));
            }
        }
    }

    public void playSound(SoundEvent soundEvent) {
        this.level.playSound((Player) null, this.worldPosition, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    public List<BeaconBeamSection> getBeamSections() {
        return this.levels == 0 ? ImmutableList.of() : this.beamSections;
    }

    public int getLevels() {
        return this.levels;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, getUpdateTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public double getViewDistance() {
        return 256.0d;
    }

    /* JADX INFO: Access modifiers changed from: private */
    @Nullable
    public static MobEffect getValidEffectById(int i) {
        MobEffect byId = MobEffect.byId(i);
        if (VALID_EFFECTS.contains(byId)) {
            return byId;
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.primaryPower = getValidEffectById(compoundTag.getInt("Primary"));
        this.secondaryPower = getValidEffectById(compoundTag.getInt("Secondary"));
        if (compoundTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
        this.lockKey = LockCode.fromTag(compoundTag);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.putInt("Primary", MobEffect.getId(this.primaryPower));
        compoundTag.putInt("Secondary", MobEffect.getId(this.secondaryPower));
        compoundTag.putInt("Levels", this.levels);
        if (this.name != null) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
        this.lockKey.addToTag(compoundTag);
        return compoundTag;
    }

    public void setCustomName(@Nullable Component component) {
        this.name = component;
    }

    @Override // net.minecraft.world.inventory.MenuConstructor
    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (BaseContainerBlockEntity.canUnlock(player, this.lockKey, getDisplayName())) {
            return new BeaconMenu(i, inventory, this.dataAccess, ContainerLevelAccess.create(this.level, getBlockPos()));
        }
        return null;
    }

    @Override // net.minecraft.world.MenuProvider
    public Component getDisplayName() {
        return this.name != null ? this.name : new TranslatableComponent("container.beacon");
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BeaconBlockEntity$BeaconBeamSection.class */
    public static class BeaconBeamSection {
        private final float[] color;
        private int height = 1;

        public BeaconBeamSection(float[] fArr) {
            this.color = fArr;
        }

        protected void increaseHeight() {
            this.height++;
        }

        public float[] getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}
