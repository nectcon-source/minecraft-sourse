package net.minecraft.world.scores;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.scores.Team;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/scores/PlayerTeam.class */
public class PlayerTeam extends Team {
    private final Scoreboard scoreboard;
    private final String name;
    private Component displayName;
    private final Style displayNameStyle;
    private final Set<String> players = Sets.newHashSet();
    private Component playerPrefix = TextComponent.EMPTY;
    private Component playerSuffix = TextComponent.EMPTY;
    private boolean allowFriendlyFire = true;
    private boolean seeFriendlyInvisibles = true;
    private Team.Visibility nameTagVisibility = Team.Visibility.ALWAYS;
    private Team.Visibility deathMessageVisibility = Team.Visibility.ALWAYS;
    private ChatFormatting color = ChatFormatting.RESET;
    private Team.CollisionRule collisionRule = Team.CollisionRule.ALWAYS;

    public PlayerTeam(Scoreboard scoreboard, String str) {
        this.scoreboard = scoreboard;
        this.name = str;
        this.displayName = new TextComponent(str);
        this.displayNameStyle = Style.EMPTY.withInsertion(str).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(str)));
    }

    @Override // net.minecraft.world.scores.Team
    public String getName() {
        return this.name;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public MutableComponent getFormattedDisplayName() {
        MutableComponent wrapInSquareBrackets = ComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle(this.displayNameStyle));
        ChatFormatting color = getColor();
        if (color != ChatFormatting.RESET) {
            wrapInSquareBrackets.withStyle(color);
        }
        return wrapInSquareBrackets;
    }

    public void setDisplayName(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.displayName = component;
        this.scoreboard.onTeamChanged(this);
    }

    public void setPlayerPrefix(@Nullable Component component) {
        this.playerPrefix = component == null ? TextComponent.EMPTY : component;
        this.scoreboard.onTeamChanged(this);
    }

    public Component getPlayerPrefix() {
        return this.playerPrefix;
    }

    public void setPlayerSuffix(@Nullable Component component) {
        this.playerSuffix = component == null ? TextComponent.EMPTY : component;
        this.scoreboard.onTeamChanged(this);
    }

    public Component getPlayerSuffix() {
        return this.playerSuffix;
    }

    @Override // net.minecraft.world.scores.Team
    public Collection<String> getPlayers() {
        return this.players;
    }

    @Override // net.minecraft.world.scores.Team
    public MutableComponent getFormattedName(Component component) {
        MutableComponent append = new TextComponent("").append(this.playerPrefix).append(component).append(this.playerSuffix);
        ChatFormatting color = getColor();
        if (color != ChatFormatting.RESET) {
            append.withStyle(color);
        }
        return append;
    }

    public static MutableComponent formatNameForTeam(@Nullable Team team, Component component) {
        if (team == null) {
            return component.copy();
        }
        return team.getFormattedName(component);
    }

    @Override // net.minecraft.world.scores.Team
    public boolean isAllowFriendlyFire() {
        return this.allowFriendlyFire;
    }

    public void setAllowFriendlyFire(boolean z) {
        this.allowFriendlyFire = z;
        this.scoreboard.onTeamChanged(this);
    }

    @Override // net.minecraft.world.scores.Team
    public boolean canSeeFriendlyInvisibles() {
        return this.seeFriendlyInvisibles;
    }

    public void setSeeFriendlyInvisibles(boolean z) {
        this.seeFriendlyInvisibles = z;
        this.scoreboard.onTeamChanged(this);
    }

    @Override // net.minecraft.world.scores.Team
    public Team.Visibility getNameTagVisibility() {
        return this.nameTagVisibility;
    }

    @Override // net.minecraft.world.scores.Team
    public Team.Visibility getDeathMessageVisibility() {
        return this.deathMessageVisibility;
    }

    public void setNameTagVisibility(Team.Visibility visibility) {
        this.nameTagVisibility = visibility;
        this.scoreboard.onTeamChanged(this);
    }

    public void setDeathMessageVisibility(Team.Visibility visibility) {
        this.deathMessageVisibility = visibility;
        this.scoreboard.onTeamChanged(this);
    }

    @Override // net.minecraft.world.scores.Team
    public Team.CollisionRule getCollisionRule() {
        return this.collisionRule;
    }

    public void setCollisionRule(Team.CollisionRule collisionRule) {
        this.collisionRule = collisionRule;
        this.scoreboard.onTeamChanged(this);
    }

    public int packOptions() {
        int i = 0;
        if (isAllowFriendlyFire()) {
            i = 0 | 1;
        }
        if (canSeeFriendlyInvisibles()) {
            i |= 2;
        }
        return i;
    }

    public void unpackOptions(int i) {
        setAllowFriendlyFire((i & 1) > 0);
        setSeeFriendlyInvisibles((i & 2) > 0);
    }

    public void setColor(ChatFormatting chatFormatting) {
        this.color = chatFormatting;
        this.scoreboard.onTeamChanged(this);
    }

    @Override // net.minecraft.world.scores.Team
    public ChatFormatting getColor() {
        return this.color;
    }
}
