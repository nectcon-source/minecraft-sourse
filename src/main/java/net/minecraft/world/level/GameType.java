package net.minecraft.world.level;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Abilities;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/GameType.class */
public enum GameType {
    NOT_SET(-1, ""),
    SURVIVAL(0, "survival"),
    CREATIVE(1, "creative"),
    ADVENTURE(2, "adventure"),
    SPECTATOR(3, "spectator");


    /* renamed from: id */
    private final int id;
    private final String name;

    GameType(int i, String str) {
        this.id = i;
        this.name = str;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Component getDisplayName() {
        return new TranslatableComponent("gameMode." + this.name);
    }

    public void updatePlayerAbilities(Abilities abilities) {
        if (this == CREATIVE) {
            abilities.mayfly = true;
            abilities.instabuild = true;
            abilities.invulnerable = true;
        } else if (this == SPECTATOR) {
            abilities.mayfly = true;
            abilities.instabuild = false;
            abilities.invulnerable = true;
            abilities.flying = true;
        } else {
            abilities.mayfly = false;
            abilities.instabuild = false;
            abilities.invulnerable = false;
            abilities.flying = false;
        }
        abilities.mayBuild = !isBlockPlacingRestricted();
    }

    public boolean isBlockPlacingRestricted() {
        return this == ADVENTURE || this == SPECTATOR;
    }

    public boolean isCreative() {
        return this == CREATIVE;
    }

    public boolean isSurvival() {
        return this == SURVIVAL || this == ADVENTURE;
    }

    public static GameType byId(int i) {
        return byId(i, SURVIVAL);
    }

    public static GameType byId(int i, GameType gameType) {
        for (GameType gameType2 : values()) {
            if (gameType2.id == i) {
                return gameType2;
            }
        }
        return gameType;
    }

    public static GameType byName(String str) {
        return byName(str, SURVIVAL);
    }

    public static GameType byName(String str, GameType gameType) {
        for (GameType gameType2 : values()) {
            if (gameType2.name.equals(str)) {
                return gameType2;
            }
        }
        return gameType;
    }
}
