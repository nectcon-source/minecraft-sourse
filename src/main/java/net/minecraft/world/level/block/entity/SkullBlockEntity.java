package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/SkullBlockEntity.class */
public class SkullBlockEntity extends BlockEntity implements TickableBlockEntity {

    @Nullable
    private static GameProfileCache profileCache;

    @Nullable
    private static MinecraftSessionService sessionService;

    @Nullable
    private GameProfile owner;
    private int mouthTickCount;
    private boolean isMovingMouth;

    public SkullBlockEntity() {
        super(BlockEntityType.SKULL);
    }

    public static void setProfileCache(GameProfileCache gameProfileCache) {
        profileCache = gameProfileCache;
    }

    public static void setSessionService(MinecraftSessionService minecraftSessionService) {
        sessionService = minecraftSessionService;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (this.owner != null) {
            CompoundTag compoundTag2 = new CompoundTag();
            NbtUtils.writeGameProfile(compoundTag2, this.owner);
            compoundTag.put("SkullOwner", compoundTag2);
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        if (compoundTag.contains("SkullOwner", 10)) {
            setOwner(NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner")));
        } else if (compoundTag.contains("ExtraType", 8)) {
            String string = compoundTag.getString("ExtraType");
            if (!StringUtil.isNullOrEmpty(string)) {
                setOwner(new GameProfile((UUID) null, string));
            }
        }
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        BlockState blockState = getBlockState();
        if (blockState.is(Blocks.DRAGON_HEAD) || blockState.is(Blocks.DRAGON_WALL_HEAD)) {
            if (this.level.hasNeighborSignal(this.worldPosition)) {
                this.isMovingMouth = true;
                this.mouthTickCount++;
            } else {
                this.isMovingMouth = false;
            }
        }
    }

    public float getMouthAnimation(float f) {
        if (this.isMovingMouth) {
            return this.mouthTickCount + f;
        }
        return this.mouthTickCount;
    }

    @Nullable
    public GameProfile getOwnerProfile() {
        return this.owner;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 4, getUpdateTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    public void setOwner(@Nullable GameProfile gameProfile) {
        this.owner = gameProfile;
        updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        this.owner = updateGameprofile(this.owner);
        setChanged();
    }

    @Nullable
    public static GameProfile updateGameprofile(@Nullable GameProfile gameProfile) {
        if (gameProfile == null || StringUtil.isNullOrEmpty(gameProfile.getName())) {
            return gameProfile;
        }
        if (gameProfile.isComplete() && gameProfile.getProperties().containsKey("textures")) {
            return gameProfile;
        }
        if (profileCache == null || sessionService == null) {
            return gameProfile;
        }
        GameProfile gameProfile2 = profileCache.get(gameProfile.getName());
        if (gameProfile2 == null) {
            return gameProfile;
        }
        if (((Property) Iterables.getFirst(gameProfile2.getProperties().get("textures"),  null)) == null) {
            gameProfile2 = sessionService.fillProfileProperties(gameProfile2, true);
        }
        return gameProfile2;
    }
}
