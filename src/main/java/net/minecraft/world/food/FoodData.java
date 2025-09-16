package net.minecraft.world.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/food/FoodData.class */
public class FoodData {
    private float exhaustionLevel;
    private int tickTimer;
    private int foodLevel = 20;
    private int lastFoodLevel = 20;
    private float saturationLevel = 5.0f;

    public void eat(int i, float f) {
        this.foodLevel = Math.min(i + this.foodLevel, 20);
        this.saturationLevel = Math.min(this.saturationLevel + (i * f * 2.0f), this.foodLevel);
    }

    public void eat(Item item, ItemStack itemStack) {
        if (item.isEdible()) {
            FoodProperties foodProperties = item.getFoodProperties();
            eat(foodProperties.getNutrition(), foodProperties.getSaturationModifier());
        }
    }

    public void tick(Player player) {
        Difficulty difficulty = player.level.getDifficulty();
        this.lastFoodLevel = this.foodLevel;
        if (this.exhaustionLevel > 4.0f) {
            this.exhaustionLevel -= 4.0f;
            if (this.saturationLevel > 0.0f) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0f, 0.0f);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }
        boolean z = player.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
        if (z && this.saturationLevel > 0.0f && player.isHurt() && this.foodLevel >= 20) {
            this.tickTimer++;
            if (this.tickTimer >= 10) {
                float min = Math.min(this.saturationLevel, 6.0f);
                player.heal(min / 6.0f);
                addExhaustion(min);
                this.tickTimer = 0;
                return;
            }
            return;
        }
        if (z && this.foodLevel >= 18 && player.isHurt()) {
            this.tickTimer++;
            if (this.tickTimer >= 80) {
                player.heal(1.0f);
                addExhaustion(6.0f);
                this.tickTimer = 0;
                return;
            }
            return;
        }
        if (this.foodLevel <= 0) {
            this.tickTimer++;
            if (this.tickTimer >= 80) {
                if (player.getHealth() > 10.0f || difficulty == Difficulty.HARD || (player.getHealth() > 1.0f && difficulty == Difficulty.NORMAL)) {
                    player.hurt(DamageSource.STARVE, 1.0f);
                }
                this.tickTimer = 0;
                return;
            }
            return;
        }
        this.tickTimer = 0;
    }

    public void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("foodLevel", 99)) {
            this.foodLevel = compoundTag.getInt("foodLevel");
            this.tickTimer = compoundTag.getInt("foodTickTimer");
            this.saturationLevel = compoundTag.getFloat("foodSaturationLevel");
            this.exhaustionLevel = compoundTag.getFloat("foodExhaustionLevel");
        }
    }

    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("foodLevel", this.foodLevel);
        compoundTag.putInt("foodTickTimer", this.tickTimer);
        compoundTag.putFloat("foodSaturationLevel", this.saturationLevel);
        compoundTag.putFloat("foodExhaustionLevel", this.exhaustionLevel);
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public boolean needsFood() {
        return this.foodLevel < 20;
    }

    public void addExhaustion(float f) {
        this.exhaustionLevel = Math.min(this.exhaustionLevel + f, 40.0f);
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public void setFoodLevel(int i) {
        this.foodLevel = i;
    }

    public void setSaturation(float f) {
        this.saturationLevel = f;
    }
}
