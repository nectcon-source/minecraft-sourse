package net.minecraft.world.scores;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/scores/Team.class */
public abstract class Team {
    public abstract String getName();

    public abstract MutableComponent getFormattedName(Component component);

    public abstract boolean canSeeFriendlyInvisibles();

    public abstract boolean isAllowFriendlyFire();

    public abstract Visibility getNameTagVisibility();

    public abstract ChatFormatting getColor();

    public abstract Collection<String> getPlayers();

    public abstract Visibility getDeathMessageVisibility();

    public abstract CollisionRule getCollisionRule();

    public boolean isAlliedTo(@Nullable Team team) {
        if (team != null && this == team) {
            return true;
        }
        return false;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/scores/Team$Visibility.class */
    public enum Visibility {
        ALWAYS("always", 0),
        NEVER("never", 1),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

        private static final Map<String, Visibility> BY_NAME =  Arrays.stream(values()).collect(Collectors.toMap(visibility -> {
            return visibility.name;
        }, visibility2 -> {
            return visibility2;
        }));
        public final String name;

        /* renamed from: id */
        public final int id;

        @Nullable
        public static Visibility byName(String str) {
            return BY_NAME.get(str);
        }

        Visibility(String str, int i) {
            this.name = str;
            this.id = i;
        }

        public Component getDisplayName() {
            return new TranslatableComponent("team.visibility." + this.name);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/scores/Team$CollisionRule.class */
    public enum CollisionRule {
        ALWAYS("always", 0),
        NEVER("never", 1),
        PUSH_OTHER_TEAMS("pushOtherTeams", 2),
        PUSH_OWN_TEAM("pushOwnTeam", 3);

        private static final Map<String, CollisionRule> BY_NAME =  Arrays.stream(values()).collect(Collectors.toMap(collisionRule -> {
            return collisionRule.name;
        }, collisionRule2 -> {
            return collisionRule2;
        }));
        public final String name;

        /* renamed from: id */
        public final int id;

        @Nullable
        public static CollisionRule byName(String str) {
            return BY_NAME.get(str);
        }

        CollisionRule(String str, int i) {
            this.name = str;
            this.id = i;
        }

        public Component getDisplayName() {
            return new TranslatableComponent("team.collision." + this.name);
        }
    }
}
