package net.minecraft.world.entity.ambient;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ambient/AmbientCreature.class */
public abstract class AmbientCreature extends Mob {
    protected AmbientCreature(EntityType<? extends AmbientCreature> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canBeLeashed(Player player) {
        return false;
    }
}
