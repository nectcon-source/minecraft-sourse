package net.minecraft.world.item.alchemy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/alchemy/Potion.class */
public class Potion {
    private final String name;
    private final ImmutableList<MobEffectInstance> effects;

    public static Potion byName(String str) {
        return Registry.POTION.get(ResourceLocation.tryParse(str));
    }

    public Potion(MobEffectInstance... mobEffectInstanceArr) {
        this(null, mobEffectInstanceArr);
    }

    public Potion(@Nullable String str, MobEffectInstance... mobEffectInstanceArr) {
        this.name = str;
        this.effects = ImmutableList.copyOf(mobEffectInstanceArr);
    }

    public String getName(String str) {
        return str + (this.name == null ? Registry.POTION.getKey(this).getPath() : this.name);
    }

    public List<MobEffectInstance> getEffects() {
        return this.effects;
    }

    public boolean hasInstantEffects() {
        if (!this.effects.isEmpty()) {
            UnmodifiableIterator it = this.effects.iterator();
            while (it.hasNext()) {
                if (((MobEffectInstance) it.next()).getEffect().isInstantenous()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
}
