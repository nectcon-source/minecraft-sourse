package net.minecraft.world.level.storage;

import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import org.apache.commons.lang3.StringUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/LevelSummary.class */
public class LevelSummary implements Comparable<LevelSummary> {
    private final LevelSettings settings;
    private final LevelVersion levelVersion;
    private final String levelId;
    private final boolean requiresConversion;
    private final boolean locked;
    private final File icon;

    @Nullable
    private Component info;

    public LevelSummary(LevelSettings levelSettings, LevelVersion levelVersion, String str, boolean z, boolean z2, File file) {
        this.settings = levelSettings;
        this.levelVersion = levelVersion;
        this.levelId = str;
        this.locked = z2;
        this.icon = file;
        this.requiresConversion = z;
    }

    public String getLevelId() {
        return this.levelId;
    }

    public String getLevelName() {
        return StringUtils.isEmpty(this.settings.levelName()) ? this.levelId : this.settings.levelName();
    }

    public File getIcon() {
        return this.icon;
    }

    public boolean isRequiresConversion() {
        return this.requiresConversion;
    }

    public long getLastPlayed() {
        return this.levelVersion.lastPlayed();
    }

    @Override // java.lang.Comparable
    public int compareTo(LevelSummary levelSummary) {
        if (this.levelVersion.lastPlayed() < levelSummary.levelVersion.lastPlayed()) {
            return 1;
        }
        if (this.levelVersion.lastPlayed() > levelSummary.levelVersion.lastPlayed()) {
            return -1;
        }
        return this.levelId.compareTo(levelSummary.levelId);
    }

    public GameType getGameMode() {
        return this.settings.gameType();
    }

    public boolean isHardcore() {
        return this.settings.hardcore();
    }

    public boolean hasCheats() {
        return this.settings.allowCommands();
    }

    public MutableComponent getWorldVersionName() {
        if (StringUtil.isNullOrEmpty(this.levelVersion.minecraftVersionName())) {
            return new TranslatableComponent("selectWorld.versionUnknown");
        }
        return new TextComponent(this.levelVersion.minecraftVersionName());
    }

    public LevelVersion levelVersion() {
        return this.levelVersion;
    }

    public boolean markVersionInList() {
        return askToOpenWorld() || !(SharedConstants.getCurrentVersion().isStable() || this.levelVersion.snapshot()) || shouldBackup();
    }

    public boolean askToOpenWorld() {
        return this.levelVersion.minecraftVersion() > SharedConstants.getCurrentVersion().getWorldVersion();
    }

    public boolean shouldBackup() {
        return this.levelVersion.minecraftVersion() < SharedConstants.getCurrentVersion().getWorldVersion();
    }

    public boolean isLocked() {
        return this.locked;
    }

    public Component getInfo() {
        if (this.info == null) {
            this.info = createInfo();
        }
        return this.info;
    }

    private Component createInfo() {
        MutableComponent translatableComponent;
        if (isLocked()) {
            return new TranslatableComponent("selectWorld.locked").withStyle(ChatFormatting.RED);
        }
        if (isRequiresConversion()) {
            return new TranslatableComponent("selectWorld.conversion");
        }
        if (isHardcore()) {
            translatableComponent = new TextComponent("").append(new TranslatableComponent("gameMode.hardcore").withStyle(ChatFormatting.DARK_RED));
        } else {
            translatableComponent = new TranslatableComponent("gameMode." + getGameMode().getName());
        }
        MutableComponent mutableComponent = translatableComponent;
        if (hasCheats()) {
            mutableComponent.append(", ").append(new TranslatableComponent("selectWorld.cheats"));
        }
        MutableComponent worldVersionName = getWorldVersionName();
        MutableComponent append = new TextComponent(", ").append(new TranslatableComponent("selectWorld.version")).append(" ");
        if (markVersionInList()) {
            append.append(worldVersionName.withStyle(askToOpenWorld() ? ChatFormatting.RED : ChatFormatting.ITALIC));
        } else {
            append.append(worldVersionName);
        }
        mutableComponent.append(append);
        return mutableComponent;
    }
}
