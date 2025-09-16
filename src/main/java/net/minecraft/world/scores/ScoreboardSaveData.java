package net.minecraft.world.scores;

import java.util.Iterator;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/scores/ScoreboardSaveData.class */
public class ScoreboardSaveData extends SavedData {
    private static final Logger LOGGER = LogManager.getLogger();
    private Scoreboard scoreboard;
    private CompoundTag delayLoad;

    public ScoreboardSaveData() {
        super("scoreboard");
    }

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        if (this.delayLoad != null) {
            load(this.delayLoad);
        }
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public void load(CompoundTag compoundTag) {
        if (this.scoreboard == null) {
            this.delayLoad = compoundTag;
            return;
        }
        loadObjectives(compoundTag.getList("Objectives", 10));
        this.scoreboard.loadPlayerScores(compoundTag.getList("PlayerScores", 10));
        if (compoundTag.contains("DisplaySlots", 10)) {
            loadDisplaySlots(compoundTag.getCompound("DisplaySlots"));
        }
        if (compoundTag.contains("Teams", 9)) {
            loadTeams(compoundTag.getList("Teams", 10));
        }
    }

    protected void loadTeams(ListTag listTag) {
        Team.CollisionRule byName;
        Team.Visibility byName2;
        Team.Visibility byName3;
        Component fromJson;
        Component fromJson2;
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compound = listTag.getCompound(i);
            String string = compound.getString("Name");
            if (string.length() > 16) {
                string = string.substring(0, 16);
            }
            PlayerTeam addPlayerTeam = this.scoreboard.addPlayerTeam(string);
            Component fromJson3 = Component.Serializer.fromJson(compound.getString("DisplayName"));
            if (fromJson3 != null) {
                addPlayerTeam.setDisplayName(fromJson3);
            }
            if (compound.contains("TeamColor", 8)) {
                addPlayerTeam.setColor(ChatFormatting.getByName(compound.getString("TeamColor")));
            }
            if (compound.contains("AllowFriendlyFire", 99)) {
                addPlayerTeam.setAllowFriendlyFire(compound.getBoolean("AllowFriendlyFire"));
            }
            if (compound.contains("SeeFriendlyInvisibles", 99)) {
                addPlayerTeam.setSeeFriendlyInvisibles(compound.getBoolean("SeeFriendlyInvisibles"));
            }
            if (compound.contains("MemberNamePrefix", 8) && (fromJson2 = Component.Serializer.fromJson(compound.getString("MemberNamePrefix"))) != null) {
                addPlayerTeam.setPlayerPrefix(fromJson2);
            }
            if (compound.contains("MemberNameSuffix", 8) && (fromJson = Component.Serializer.fromJson(compound.getString("MemberNameSuffix"))) != null) {
                addPlayerTeam.setPlayerSuffix(fromJson);
            }
            if (compound.contains("NameTagVisibility", 8) && (byName3 = Team.Visibility.byName(compound.getString("NameTagVisibility"))) != null) {
                addPlayerTeam.setNameTagVisibility(byName3);
            }
            if (compound.contains("DeathMessageVisibility", 8) && (byName2 = Team.Visibility.byName(compound.getString("DeathMessageVisibility"))) != null) {
                addPlayerTeam.setDeathMessageVisibility(byName2);
            }
            if (compound.contains("CollisionRule", 8) && (byName = Team.CollisionRule.byName(compound.getString("CollisionRule"))) != null) {
                addPlayerTeam.setCollisionRule(byName);
            }
            loadTeamPlayers(addPlayerTeam, compound.getList("Players", 8));
        }
    }

    protected void loadTeamPlayers(PlayerTeam playerTeam, ListTag listTag) {
        for (int i = 0; i < listTag.size(); i++) {
            this.scoreboard.addPlayerToTeam(listTag.getString(i), playerTeam);
        }
    }

    protected void loadDisplaySlots(CompoundTag compoundTag) {
        for (int i = 0; i < 19; i++) {
            if (compoundTag.contains("slot_" + i, 8)) {
                this.scoreboard.setDisplayObjective(i, this.scoreboard.getObjective(compoundTag.getString("slot_" + i)));
            }
        }
    }

    protected void loadObjectives(ListTag listTag) {
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compound = listTag.getCompound(i);
            ObjectiveCriteria.byName(compound.getString("CriteriaName")).ifPresent(objectiveCriteria -> {
                String string = compound.getString("Name");
                if (string.length() > 16) {
                    string = string.substring(0, 16);
                }
                this.scoreboard.addObjective(string, objectiveCriteria, Component.Serializer.fromJson(compound.getString("DisplayName")), ObjectiveCriteria.RenderType.byId(compound.getString("RenderType")));
            });
        }
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public CompoundTag save(CompoundTag compoundTag) {
        if (this.scoreboard == null) {
            LOGGER.warn("Tried to save scoreboard without having a scoreboard...");
            return compoundTag;
        }
        compoundTag.put("Objectives", saveObjectives());
        compoundTag.put("PlayerScores", this.scoreboard.savePlayerScores());
        compoundTag.put("Teams", saveTeams());
        saveDisplaySlots(compoundTag);
        return compoundTag;
    }

    protected ListTag saveTeams() {
        ListTag listTag = new ListTag();
        for (PlayerTeam playerTeam : this.scoreboard.getPlayerTeams()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("Name", playerTeam.getName());
            compoundTag.putString("DisplayName", Component.Serializer.toJson(playerTeam.getDisplayName()));
            if (playerTeam.getColor().getId() >= 0) {
                compoundTag.putString("TeamColor", playerTeam.getColor().getName());
            }
            compoundTag.putBoolean("AllowFriendlyFire", playerTeam.isAllowFriendlyFire());
            compoundTag.putBoolean("SeeFriendlyInvisibles", playerTeam.canSeeFriendlyInvisibles());
            compoundTag.putString("MemberNamePrefix", Component.Serializer.toJson(playerTeam.getPlayerPrefix()));
            compoundTag.putString("MemberNameSuffix", Component.Serializer.toJson(playerTeam.getPlayerSuffix()));
            compoundTag.putString("NameTagVisibility", playerTeam.getNameTagVisibility().name);
            compoundTag.putString("DeathMessageVisibility", playerTeam.getDeathMessageVisibility().name);
            compoundTag.putString("CollisionRule", playerTeam.getCollisionRule().name);
            ListTag listTag2 = new ListTag();
            Iterator<String> it = playerTeam.getPlayers().iterator();
            while (it.hasNext()) {
                listTag2.add(StringTag.valueOf(it.next()));
            }
            compoundTag.put("Players", listTag2);
            listTag.add(compoundTag);
        }
        return listTag;
    }

    protected void saveDisplaySlots(CompoundTag compoundTag) {
        CompoundTag compoundTag2 = new CompoundTag();
        boolean z = false;
        for (int i = 0; i < 19; i++) {
            Objective displayObjective = this.scoreboard.getDisplayObjective(i);
            if (displayObjective != null) {
                compoundTag2.putString("slot_" + i, displayObjective.getName());
                z = true;
            }
        }
        if (z) {
            compoundTag.put("DisplaySlots", compoundTag2);
        }
    }

    protected ListTag saveObjectives() {
        ListTag listTag = new ListTag();
        for (Objective objective : this.scoreboard.getObjectives()) {
            if (objective.getCriteria() != null) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("Name", objective.getName());
                compoundTag.putString("CriteriaName", objective.getCriteria().getName());
                compoundTag.putString("DisplayName", Component.Serializer.toJson(objective.getDisplayName()));
                compoundTag.putString("RenderType", objective.getRenderType().getId());
                listTag.add(compoundTag);
            }
        }
        return listTag;
    }
}
