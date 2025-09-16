package net.minecraft.world.entity.ai.village;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/village/ReputationEventType.class */
public interface ReputationEventType {
    public static final ReputationEventType ZOMBIE_VILLAGER_CURED = register("zombie_villager_cured");
    public static final ReputationEventType GOLEM_KILLED = register("golem_killed");
    public static final ReputationEventType VILLAGER_HURT = register("villager_hurt");
    public static final ReputationEventType VILLAGER_KILLED = register("villager_killed");
    public static final ReputationEventType TRADE = register("trade");

    static ReputationEventType register(final String str) {
        return new ReputationEventType() { // from class: net.minecraft.world.entity.ai.village.ReputationEventType.1
            public String toString() {
                return str;
            }
        };
    }
}
