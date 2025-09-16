package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/Sensing.class */
public class Sensing {
    private final Mob mob;
    private final List<Entity> seen = Lists.newArrayList();
    private final List<Entity> unseen = Lists.newArrayList();

    public Sensing(Mob mob) {
        this.mob = mob;
    }

    public void tick() {
        this.seen.clear();
        this.unseen.clear();
    }

    public boolean canSee(Entity entity) {
        if (this.seen.contains(entity)) {
            return true;
        }
        if (this.unseen.contains(entity)) {
            return false;
        }
        this.mob.level.getProfiler().push("canSee");
        boolean canSee = this.mob.canSee(entity);
        this.mob.level.getProfiler().pop();
        if (canSee) {
            this.seen.add(entity);
        } else {
            this.unseen.add(entity);
        }
        return canSee;
    }
}
