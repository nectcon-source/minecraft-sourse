package net.minecraft.world;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/BossEvent.class */
public abstract class BossEvent {

    /* renamed from: id */
    private final UUID id;
    protected Component name;
    protected float percent = 1.0f;
    protected BossBarColor color;
    protected BossBarOverlay overlay;
    protected boolean darkenScreen;
    protected boolean playBossMusic;
    protected boolean createWorldFog;

    public BossEvent(UUID uuid, Component component, BossBarColor bossBarColor, BossBarOverlay bossBarOverlay) {
        this.id = uuid;
        this.name = component;
        this.color = bossBarColor;
        this.overlay = bossBarOverlay;
    }

    public UUID getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }

    public void setName(Component component) {
        this.name = component;
    }

    public float getPercent() {
        return this.percent;
    }

    public void setPercent(float f) {
        this.percent = f;
    }

    public BossBarColor getColor() {
        return this.color;
    }

    public void setColor(BossBarColor bossBarColor) {
        this.color = bossBarColor;
    }

    public BossBarOverlay getOverlay() {
        return this.overlay;
    }

    public void setOverlay(BossBarOverlay bossBarOverlay) {
        this.overlay = bossBarOverlay;
    }

    public boolean shouldDarkenScreen() {
        return this.darkenScreen;
    }

    public BossEvent setDarkenScreen(boolean z) {
        this.darkenScreen = z;
        return this;
    }

    public boolean shouldPlayBossMusic() {
        return this.playBossMusic;
    }

    public BossEvent setPlayBossMusic(boolean z) {
        this.playBossMusic = z;
        return this;
    }

    public BossEvent setCreateWorldFog(boolean z) {
        this.createWorldFog = z;
        return this;
    }

    public boolean shouldCreateWorldFog() {
        return this.createWorldFog;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/BossEvent$BossBarColor.class */
    public enum BossBarColor {
        PINK("pink", ChatFormatting.RED),
        BLUE("blue", ChatFormatting.BLUE),
        RED("red", ChatFormatting.DARK_RED),
        GREEN("green", ChatFormatting.GREEN),
        YELLOW("yellow", ChatFormatting.YELLOW),
        PURPLE("purple", ChatFormatting.DARK_BLUE),
        WHITE("white", ChatFormatting.WHITE);

        private final String name;
        private final ChatFormatting formatting;

        BossBarColor(String str, ChatFormatting chatFormatting) {
            this.name = str;
            this.formatting = chatFormatting;
        }

        public ChatFormatting getFormatting() {
            return this.formatting;
        }

        public String getName() {
            return this.name;
        }

        public static BossBarColor byName(String str) {
            for (BossBarColor bossBarColor : values()) {
                if (bossBarColor.name.equals(str)) {
                    return bossBarColor;
                }
            }
            return WHITE;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/BossEvent$BossBarOverlay.class */
    public enum BossBarOverlay {
        PROGRESS("progress"),
        NOTCHED_6("notched_6"),
        NOTCHED_10("notched_10"),
        NOTCHED_12("notched_12"),
        NOTCHED_20("notched_20");

        private final String name;

        BossBarOverlay(String str) {
            this.name = str;
        }

        public String getName() {
            return this.name;
        }

        public static BossBarOverlay byName(String str) {
            for (BossBarOverlay bossBarOverlay : values()) {
                if (bossBarOverlay.name.equals(str)) {
                    return bossBarOverlay;
                }
            }
            return PROGRESS;
        }
    }
}
