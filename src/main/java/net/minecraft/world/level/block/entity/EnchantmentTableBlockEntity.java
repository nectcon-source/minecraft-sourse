package net.minecraft.world.level.block.entity;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/EnchantmentTableBlockEntity.class */
public class EnchantmentTableBlockEntity extends BlockEntity implements Nameable, TickableBlockEntity {
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final Random RANDOM = new Random();
    private Component name;

    public EnchantmentTableBlockEntity() {
        super(BlockEntityType.ENCHANTING_TABLE);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (hasCustomName()) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        float f;
        this.oOpen = this.open;
        this.oRot = this.rot;
        Player nearestPlayer = this.level.getNearestPlayer(this.worldPosition.getX() + 0.5d, this.worldPosition.getY() + 0.5d, this.worldPosition.getZ() + 0.5d, 3.0d, false);
        if (nearestPlayer != null) {
            this.tRot = (float) Mth.atan2(nearestPlayer.getZ() - (this.worldPosition.getZ() + 0.5d), nearestPlayer.getX() - (this.worldPosition.getX() + 0.5d));
            this.open += 0.1f;
            if (this.open < 0.5f || RANDOM.nextInt(40) == 0) {
                float f2 = this.flipT;
                do {
                    this.flipT += RANDOM.nextInt(4) - RANDOM.nextInt(4);
                } while (f2 == this.flipT);
            }
        } else {
            this.tRot += 0.02f;
            this.open -= 0.1f;
        }
        while (this.rot >= 3.1415927f) {
            this.rot -= 6.2831855f;
        }
        while (this.rot < -3.1415927f) {
            this.rot += 6.2831855f;
        }
        while (this.tRot >= 3.1415927f) {
            this.tRot -= 6.2831855f;
        }
        while (this.tRot < -3.1415927f) {
            this.tRot += 6.2831855f;
        }
        float f3 = this.tRot;
        float f4 = this.rot;
        while (true) {
            f = f3 - f4;
            if (f < 3.1415927f) {
                break;
            }
            f3 = f;
            f4 = 6.2831855f;
        }
        while (f < -3.1415927f) {
            f += 6.2831855f;
        }
        this.rot += f * 0.4f;
        this.open = Mth.clamp(this.open, 0.0f, 1.0f);
        this.time++;
        this.oFlip = this.flip;
        this.flipA += (Mth.clamp((this.flipT - this.flip) * 0.4f, -0.2f, 0.2f) - this.flipA) * 0.9f;
        this.flip += this.flipA;
    }

    @Override // net.minecraft.world.Nameable
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return new TranslatableComponent("container.enchant");
    }

    public void setCustomName(@Nullable Component component) {
        this.name = component;
    }

    @Override // net.minecraft.world.Nameable
    @Nullable
    public Component getCustomName() {
        return this.name;
    }
}
