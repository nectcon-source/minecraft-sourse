package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ItemCooldowns.class */
public class ItemCooldowns {
    private final Map<Item, CooldownInstance> cooldowns = Maps.newHashMap();
    private int tickCount;

    public boolean isOnCooldown(Item item) {
        return getCooldownPercent(item, 0.0f) > 0.0f;
    }

    public float getCooldownPercent(Item var1, float var2) {
        CooldownInstance var3 = (CooldownInstance)this.cooldowns.get(var1);
        if (var3 != null) {
            float var4 = (float)(var3.endTime - var3.startTime);
            float var5 = (float)var3.endTime - ((float)this.tickCount + var2);
            return Mth.clamp(var5 / var4, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public void tick() {
        this.tickCount++;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Map.Entry<Item, CooldownInstance>> it = this.cooldowns.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Item, CooldownInstance> next = it.next();
                if (next.getValue().endTime <= this.tickCount) {
                    it.remove();
                    onCooldownEnded(next.getKey());
                }
            }
        }
    }

    public void addCooldown(Item item, int i) {
        this.cooldowns.put(item, new CooldownInstance(this.tickCount, this.tickCount + i));
        onCooldownStarted(item, i);
    }

    public void removeCooldown(Item item) {
        this.cooldowns.remove(item);
        onCooldownEnded(item);
    }

    protected void onCooldownStarted(Item item, int i) {
    }

    protected void onCooldownEnded(Item item) {
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ItemCooldowns$CooldownInstance.class */
    class CooldownInstance {
        private final int startTime;
        private final int endTime;

        private CooldownInstance(int i, int i2) {
            this.startTime = i;
            this.endTime = i2;
        }
    }
}
