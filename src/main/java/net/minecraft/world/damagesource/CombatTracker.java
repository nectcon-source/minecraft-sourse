package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/damagesource/CombatTracker.class */
public class CombatTracker {
    private final List<CombatEntry> entries = Lists.newArrayList();
    private final LivingEntity mob;
    private int lastDamageTime;
    private int combatStartTime;
    private int combatEndTime;
    private boolean inCombat;
    private boolean takingDamage;
    private String nextLocation;

    public CombatTracker(LivingEntity livingEntity) {
        this.mob = livingEntity;
    }

    public void prepareForDamage() {
        resetPreparedStatus();
        Optional<BlockPos> lastClimbablePos = this.mob.getLastClimbablePos();
        if (!lastClimbablePos.isPresent()) {
            if (this.mob.isInWater()) {
                this.nextLocation = "water";
                return;
            }
            return;
        }
        BlockState blockState = this.mob.level.getBlockState(lastClimbablePos.get());
        if (blockState.is(Blocks.LADDER) || blockState.is(BlockTags.TRAPDOORS)) {
            this.nextLocation = "ladder";
            return;
        }
        if (blockState.is(Blocks.VINE)) {
            this.nextLocation = "vines";
            return;
        }
        if (blockState.is(Blocks.WEEPING_VINES) || blockState.is(Blocks.WEEPING_VINES_PLANT)) {
            this.nextLocation = "weeping_vines";
            return;
        }
        if (blockState.is(Blocks.TWISTING_VINES) || blockState.is(Blocks.TWISTING_VINES_PLANT)) {
            this.nextLocation = "twisting_vines";
        } else if (blockState.is(Blocks.SCAFFOLDING)) {
            this.nextLocation = "scaffolding";
        } else {
            this.nextLocation = "other_climbable";
        }
    }

    public void recordDamage(DamageSource damageSource, float f, float f2) {
        recheckStatus();
        prepareForDamage();
        CombatEntry combatEntry = new CombatEntry(damageSource, this.mob.tickCount, f, f2, this.nextLocation, this.mob.fallDistance);
        this.entries.add(combatEntry);
        this.lastDamageTime = this.mob.tickCount;
        this.takingDamage = true;
        if (combatEntry.isCombatRelated() && !this.inCombat && this.mob.isAlive()) {
            this.inCombat = true;
            this.combatStartTime = this.mob.tickCount;
            this.combatEndTime = this.combatStartTime;
            this.mob.onEnterCombat();
        }
    }

    public Component getDeathMessage() {
        Component localizedDeathMessage;
        if (this.entries.isEmpty()) {
            return new TranslatableComponent("death.attack.generic", this.mob.getDisplayName());
        }
        CombatEntry mostSignificantFall = getMostSignificantFall();
        CombatEntry combatEntry = this.entries.get(this.entries.size() - 1);
        Component attackerName = combatEntry.getAttackerName();
        Entity entity = combatEntry.getSource().getEntity();
        if (mostSignificantFall != null && combatEntry.getSource() == DamageSource.FALL) {
            Component attackerName2 = mostSignificantFall.getAttackerName();
            if (mostSignificantFall.getSource() == DamageSource.FALL || mostSignificantFall.getSource() == DamageSource.OUT_OF_WORLD) {
                localizedDeathMessage = new TranslatableComponent("death.fell.accident." + getFallLocation(mostSignificantFall), this.mob.getDisplayName());
            } else if (attackerName2 != null && (attackerName == null || !attackerName2.equals(attackerName))) {
                Entity entity2 = mostSignificantFall.getSource().getEntity();
                ItemStack mainHandItem = entity2 instanceof LivingEntity ? ((LivingEntity) entity2).getMainHandItem() : ItemStack.EMPTY;
                if (!mainHandItem.isEmpty() && mainHandItem.hasCustomHoverName()) {
                    localizedDeathMessage = new TranslatableComponent("death.fell.assist.item", this.mob.getDisplayName(), attackerName2, mainHandItem.getDisplayName());
                } else {
                    localizedDeathMessage = new TranslatableComponent("death.fell.assist", this.mob.getDisplayName(), attackerName2);
                }
            } else if (attackerName != null) {
                ItemStack mainHandItem2 = entity instanceof LivingEntity ? ((LivingEntity) entity).getMainHandItem() : ItemStack.EMPTY;
                if (!mainHandItem2.isEmpty() && mainHandItem2.hasCustomHoverName()) {
                    localizedDeathMessage = new TranslatableComponent("death.fell.finish.item", this.mob.getDisplayName(), attackerName, mainHandItem2.getDisplayName());
                } else {
                    localizedDeathMessage = new TranslatableComponent("death.fell.finish", this.mob.getDisplayName(), attackerName);
                }
            } else {
                localizedDeathMessage = new TranslatableComponent("death.fell.killer", this.mob.getDisplayName());
            }
        } else {
            localizedDeathMessage = combatEntry.getSource().getLocalizedDeathMessage(this.mob);
        }
        return localizedDeathMessage;
    }

    @Nullable
    public LivingEntity getKiller() {
        LivingEntity livingEntity = null;
        Player player = null;
        float f = 0.0f;
        float f2 = 0.0f;
        for (CombatEntry combatEntry : this.entries) {
            if ((combatEntry.getSource().getEntity() instanceof Player) && (player == null || combatEntry.getDamage() > f2)) {
                f2 = combatEntry.getDamage();
                player = (Player) combatEntry.getSource().getEntity();
            }
            if ((combatEntry.getSource().getEntity() instanceof LivingEntity) && (livingEntity == null || combatEntry.getDamage() > f)) {
                f = combatEntry.getDamage();
                livingEntity = (LivingEntity) combatEntry.getSource().getEntity();
            }
        }
        if (player != null && f2 >= f / 3.0f) {
            return player;
        }
        return livingEntity;
    }

    @Nullable
    private CombatEntry getMostSignificantFall() {
        CombatEntry combatEntry = null;
        CombatEntry combatEntry2 = null;
        float f = 0.0f;
        float f2 = 0.0f;
        int i = 0;
        while (i < this.entries.size()) {
            CombatEntry combatEntry3 = this.entries.get(i);
            CombatEntry combatEntry4 = i > 0 ? this.entries.get(i - 1) : null;
            if ((combatEntry3.getSource() == DamageSource.FALL || combatEntry3.getSource() == DamageSource.OUT_OF_WORLD) && combatEntry3.getFallDistance() > 0.0f && (combatEntry == null || combatEntry3.getFallDistance() > f2)) {
                if (i > 0) {
                    combatEntry = combatEntry4;
                } else {
                    combatEntry = combatEntry3;
                }
                f2 = combatEntry3.getFallDistance();
            }
            if (combatEntry3.getLocation() != null && (combatEntry2 == null || combatEntry3.getDamage() > f)) {
                combatEntry2 = combatEntry3;
                f = combatEntry3.getDamage();
            }
            i++;
        }
        if (f2 > 5.0f && combatEntry != null) {
            return combatEntry;
        }
        if (f > 5.0f && combatEntry2 != null) {
            return combatEntry2;
        }
        return null;
    }

    private String getFallLocation(CombatEntry combatEntry) {
        return combatEntry.getLocation() == null ? "generic" : combatEntry.getLocation();
    }

    public int getCombatDuration() {
        if (this.inCombat) {
            return this.mob.tickCount - this.combatStartTime;
        }
        return this.combatEndTime - this.combatStartTime;
    }

    private void resetPreparedStatus() {
        this.nextLocation = null;
    }

    public void recheckStatus() {
        int i = this.inCombat ? 300 : 100;
        if (this.takingDamage) {
            if (!this.mob.isAlive() || this.mob.tickCount - this.lastDamageTime > i) {
                boolean z = this.inCombat;
                this.takingDamage = false;
                this.inCombat = false;
                this.combatEndTime = this.mob.tickCount;
                if (z) {
                    this.mob.onLeaveCombat();
                }
                this.entries.clear();
            }
        }
    }

    public LivingEntity getMob() {
        return this.mob;
    }
}
