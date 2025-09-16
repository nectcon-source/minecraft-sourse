package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/scores/Scoreboard.class */
public class Scoreboard {
    private final Map<String, Objective> objectivesByName = Maps.newHashMap();
    private final Map<ObjectiveCriteria, List<Objective>> objectivesByCriteria = Maps.newHashMap();
    private final Map<String, Map<Objective, Score>> playerScores = Maps.newHashMap();
    private final Objective[] displayObjectives = new Objective[19];
    private final Map<String, PlayerTeam> teamsByName = Maps.newHashMap();
    private final Map<String, PlayerTeam> teamsByPlayer = Maps.newHashMap();
    private static String[] displaySlotNames;

    public boolean hasObjective(String str) {
        return this.objectivesByName.containsKey(str);
    }

    public Objective getOrCreateObjective(String str) {
        return this.objectivesByName.get(str);
    }

    @Nullable
    public Objective getObjective(@Nullable String str) {
        return this.objectivesByName.get(str);
    }

    public Objective addObjective(String str, ObjectiveCriteria objectiveCriteria, Component component, ObjectiveCriteria.RenderType renderType) {
        if (str.length() > 16) {
            throw new IllegalArgumentException("The objective name '" + str + "' is too long!");
        }
        if (this.objectivesByName.containsKey(str)) {
            throw new IllegalArgumentException("An objective with the name '" + str + "' already exists!");
        }
        Objective objective = new Objective(this, str, objectiveCriteria, component, renderType);
        this.objectivesByCriteria.computeIfAbsent(objectiveCriteria, objectiveCriteria2 -> {
            return Lists.newArrayList();
        }).add(objective);
        this.objectivesByName.put(str, objective);
        onObjectiveAdded(objective);
        return objective;
    }

    public final void forAllObjectives(ObjectiveCriteria objectiveCriteria, String str, Consumer<Score> consumer) {
        this.objectivesByCriteria.getOrDefault(objectiveCriteria, Collections.emptyList()).forEach(objective -> {
            consumer.accept(getOrCreatePlayerScore(str, objective));
        });
    }

    public boolean hasPlayerScore(String str, Objective objective) {
        Map<Objective, Score> map = this.playerScores.get(str);
        return (map == null || map.get(objective) == null) ? false : true;
    }

    public Score getOrCreatePlayerScore(String str, Objective objective) {
        if (str.length() > 40) {
            throw new IllegalArgumentException("The player name '" + str + "' is too long!");
        }
        return this.playerScores.computeIfAbsent(str, str2 -> {
            return Maps.newHashMap();
        }).computeIfAbsent(objective, objective2 -> {
            Score score = new Score(this, objective2, str);
            score.setScore(0);
            return score;
        });
    }

    public Collection<Score> getPlayerScores(Objective objective) {
        List<Score> newArrayList = Lists.newArrayList();
        Iterator<Map<Objective, Score>> it = this.playerScores.values().iterator();
        while (it.hasNext()) {
            Score score = it.next().get(objective);
            if (score != null) {
                newArrayList.add(score);
            }
        }
        newArrayList.sort(Score.SCORE_COMPARATOR);
        return newArrayList;
    }

    public Collection<Objective> getObjectives() {
        return this.objectivesByName.values();
    }

    public Collection<String> getObjectiveNames() {
        return this.objectivesByName.keySet();
    }

    public Collection<String> getTrackedPlayers() {
        return Lists.newArrayList(this.playerScores.keySet());
    }

    public void resetPlayerScore(String str, @Nullable Objective objective) {
        if (objective == null) {
            if (this.playerScores.remove(str) != null) {
                onPlayerRemoved(str);
                return;
            }
            return;
        }
        Map<Objective, Score> map = this.playerScores.get(str);
        if (map != null) {
            Score remove = map.remove(objective);
            if (map.size() < 1) {
                if (this.playerScores.remove(str) != null) {
                    onPlayerRemoved(str);
                }
            } else if (remove != null) {
                onPlayerScoreRemoved(str, objective);
            }
        }
    }

    public Map<Objective, Score> getPlayerScores(String str) {
        Map<Objective, Score> map = this.playerScores.get(str);
        if (map == null) {
            map = Maps.newHashMap();
        }
        return map;
    }

    public void removeObjective(Objective objective) {
        this.objectivesByName.remove(objective.getName());
        for (int i = 0; i < 19; i++) {
            if (getDisplayObjective(i) == objective) {
                setDisplayObjective(i, null);
            }
        }
        List<Objective> list = this.objectivesByCriteria.get(objective.getCriteria());
        if (list != null) {
            list.remove(objective);
        }
        Iterator<Map<Objective, Score>> it = this.playerScores.values().iterator();
        while (it.hasNext()) {
            it.next().remove(objective);
        }
        onObjectiveRemoved(objective);
    }

    public void setDisplayObjective(int i, @Nullable Objective objective) {
        this.displayObjectives[i] = objective;
    }

    @Nullable
    public Objective getDisplayObjective(int i) {
        return this.displayObjectives[i];
    }

    public PlayerTeam getPlayerTeam(String str) {
        return this.teamsByName.get(str);
    }

    public PlayerTeam addPlayerTeam(String str) {
        if (str.length() > 16) {
            throw new IllegalArgumentException("The team name '" + str + "' is too long!");
        }
        if (getPlayerTeam(str) != null) {
            throw new IllegalArgumentException("A team with the name '" + str + "' already exists!");
        }
        PlayerTeam playerTeam = new PlayerTeam(this, str);
        this.teamsByName.put(str, playerTeam);
        onTeamAdded(playerTeam);
        return playerTeam;
    }

    public void removePlayerTeam(PlayerTeam playerTeam) {
        this.teamsByName.remove(playerTeam.getName());
        Iterator<String> it = playerTeam.getPlayers().iterator();
        while (it.hasNext()) {
            this.teamsByPlayer.remove(it.next());
        }
        onTeamRemoved(playerTeam);
    }

    public boolean addPlayerToTeam(String str, PlayerTeam playerTeam) {
        if (str.length() > 40) {
            throw new IllegalArgumentException("The player name '" + str + "' is too long!");
        }
        if (getPlayersTeam(str) != null) {
            removePlayerFromTeam(str);
        }
        this.teamsByPlayer.put(str, playerTeam);
        return playerTeam.getPlayers().add(str);
    }

    public boolean removePlayerFromTeam(String str) {
        PlayerTeam playersTeam = getPlayersTeam(str);
        if (playersTeam != null) {
            removePlayerFromTeam(str, playersTeam);
            return true;
        }
        return false;
    }

    public void removePlayerFromTeam(String str, PlayerTeam playerTeam) {
        if (getPlayersTeam(str) != playerTeam) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + playerTeam.getName() + "'.");
        }
        this.teamsByPlayer.remove(str);
        playerTeam.getPlayers().remove(str);
    }

    public Collection<String> getTeamNames() {
        return this.teamsByName.keySet();
    }

    public Collection<PlayerTeam> getPlayerTeams() {
        return this.teamsByName.values();
    }

    @Nullable
    public PlayerTeam getPlayersTeam(String str) {
        return this.teamsByPlayer.get(str);
    }

    public void onObjectiveAdded(Objective objective) {
    }

    public void onObjectiveChanged(Objective objective) {
    }

    public void onObjectiveRemoved(Objective objective) {
    }

    public void onScoreChanged(Score score) {
    }

    public void onPlayerRemoved(String str) {
    }

    public void onPlayerScoreRemoved(String str, Objective objective) {
    }

    public void onTeamAdded(PlayerTeam playerTeam) {
    }

    public void onTeamChanged(PlayerTeam playerTeam) {
    }

    public void onTeamRemoved(PlayerTeam playerTeam) {
    }

    public static String getDisplaySlotName(int i) {
        ChatFormatting byId;
        switch (i) {
            case 0:
                return "list";
            case 1:
                return "sidebar";
            case 2:
                return "belowName";
            default:
                if (i >= 3 && i <= 18 && (byId = ChatFormatting.getById(i - 3)) != null && byId != ChatFormatting.RESET) {
                    return "sidebar.team." + byId.getName();
                }
                return null;
        }
    }

    public static int getDisplaySlotByName(String str) {
        ChatFormatting byName;
        if ("list".equalsIgnoreCase(str)) {
            return 0;
        }
        if ("sidebar".equalsIgnoreCase(str)) {
            return 1;
        }
        if ("belowName".equalsIgnoreCase(str)) {
            return 2;
        }
        if (str.startsWith("sidebar.team.") && (byName = ChatFormatting.getByName(str.substring("sidebar.team.".length()))) != null && byName.getId() >= 0) {
            return byName.getId() + 3;
        }
        return -1;
    }

    public static String[] getDisplaySlotNames() {
        if (displaySlotNames == null) {
            displaySlotNames = new String[19];
            for (int i = 0; i < 19; i++) {
                displaySlotNames[i] = getDisplaySlotName(i);
            }
        }
        return displaySlotNames;
    }

    public void entityRemoved(Entity entity) {
        if (entity == null || (entity instanceof Player) || entity.isAlive()) {
            return;
        }
        String stringUUID = entity.getStringUUID();
        resetPlayerScore(stringUUID, null);
        removePlayerFromTeam(stringUUID);
    }

    protected ListTag savePlayerScores() {
        ListTag listTag = new ListTag();
        this.playerScores.values().stream().map((v0) -> {
            return v0.values();
        }).forEach(collection -> {
            collection.stream().filter(score -> {
                return score.getObjective() != null;
            }).forEach(score2 -> {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("Name", score2.getOwner());
                compoundTag.putString("Objective", score2.getObjective().getName());
                compoundTag.putInt("Score", score2.getScore());
                compoundTag.putBoolean("Locked", score2.isLocked());
                listTag.add(compoundTag);
            });
        });
        return listTag;
    }

    protected void loadPlayerScores(ListTag listTag) {
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compound = listTag.getCompound(i);
            Objective orCreateObjective = getOrCreateObjective(compound.getString("Objective"));
            String string = compound.getString("Name");
            if (string.length() > 40) {
                string = string.substring(0, 40);
            }
            Score orCreatePlayerScore = getOrCreatePlayerScore(string, orCreateObjective);
            orCreatePlayerScore.setScore(compound.getInt("Score"));
            if (compound.contains("Locked")) {
                orCreatePlayerScore.setLocked(compound.getBoolean("Locked"));
            }
        }
    }
}
