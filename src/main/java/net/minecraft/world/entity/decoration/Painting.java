package net.minecraft.world.entity.decoration;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/decoration/Painting.class */
public class Painting extends HangingEntity {
    public Motive motive;

    public Painting(EntityType<? extends Painting> entityType, Level level) {
        super(entityType, level);
    }

    public Painting(Level level, BlockPos blockPos, Direction direction) {
        super(EntityType.PAINTING, level, blockPos);
        List<Motive> newArrayList = Lists.newArrayList();
        int i = 0;
        Iterator<Motive> it = Registry.MOTIVE.iterator();
        while (it.hasNext()) {
            Motive next = it.next();
            this.motive = next;
            setDirection(direction);
            if (survives()) {
                newArrayList.add(next);
                int width = next.getWidth() * next.getHeight();
                if (width > i) {
                    i = width;
                }
            }
        }
        if (!newArrayList.isEmpty()) {
            Iterator<Motive> it2 = newArrayList.iterator();
            while (it2.hasNext()) {
                Motive next2 = it2.next();
                if (next2.getWidth() * next2.getHeight() < i) {
                    it2.remove();
                }
            }
            this.motive = newArrayList.get(this.random.nextInt(newArrayList.size()));
        }
        setDirection(direction);
    }

    public Painting(Level level, BlockPos blockPos, Direction direction, Motive motive) {
        this(level, blockPos, direction);
        this.motive = motive;
        setDirection(direction);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putString("Motive", Registry.MOTIVE.getKey(this.motive).toString());
        compoundTag.putByte("Facing", (byte) this.direction.get2DDataValue());
        super.addAdditionalSaveData(compoundTag);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.motive = Registry.MOTIVE.get(ResourceLocation.tryParse(compoundTag.getString("Motive")));
        this.direction = Direction.from2DDataValue(compoundTag.getByte("Facing"));
        super.readAdditionalSaveData(compoundTag);
        setDirection(this.direction);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public int getWidth() {
        if (this.motive == null) {
            return 1;
        }
        return this.motive.getWidth();
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public int getHeight() {
        if (this.motive == null) {
            return 1;
        }
        return this.motive.getHeight();
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public void dropItem(@Nullable Entity entity) {
        if (!this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        playSound(SoundEvents.PAINTING_BREAK, 1.0f, 1.0f);
        if ((entity instanceof Player) && ((Player) entity).abilities.instabuild) {
            return;
        }
        spawnAtLocation(Items.PAINTING);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public void playPlacementSound() {
        playSound(SoundEvents.PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override // net.minecraft.world.entity.Entity
    public void moveTo(double d, double d2, double d3, float f, float f2) {
        setPos(d, d2, d3);
    }

    @Override // net.minecraft.world.entity.Entity
    public void lerpTo(double d, double d2, double d3, float f, float f2, int i, boolean z) {
        BlockPos offset = this.pos.offset(d - getX(), d2 - getY(), d3 - getZ());
        setPos(offset.getX(), offset.getY(), offset.getZ());
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddPaintingPacket(this);
    }
}
