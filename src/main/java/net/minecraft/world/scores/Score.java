package net.minecraft.world.scores;

import java.util.Comparator;
import javax.annotation.Nullable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/scores/Score.class */
public class Score {
    public static final Comparator<Score> SCORE_COMPARATOR = (score, score2) -> {
        if (score.getScore() > score2.getScore()) {
            return 1;
        }
        if (score.getScore() < score2.getScore()) {
            return -1;
        }
        return score2.getOwner().compareToIgnoreCase(score.getOwner());
    };
    private final Scoreboard scoreboard;

    @Nullable
    private final Objective objective;
    private final String owner;
    private int count;
    private boolean locked = true;
    private boolean forceUpdate = true;

    public Score(Scoreboard scoreboard, Objective objective, String str) {
        this.scoreboard = scoreboard;
        this.objective = objective;
        this.owner = str;
    }

    public void add(int i) {
        if (this.objective.getCriteria().isReadOnly()) {
            throw new IllegalStateException("Cannot modify read-only score");
        }
        setScore(getScore() + i);
    }

    public void increment() {
        add(1);
    }

    public int getScore() {
        return this.count;
    }

    public void reset() {
        setScore(0);
    }

    public void setScore(int i) {
        int i2 = this.count;
        this.count = i;
        if (i2 != i || this.forceUpdate) {
            this.forceUpdate = false;
            getScoreboard().onScoreChanged(this);
        }
    }

    @Nullable
    public Objective getObjective() {
        return this.objective;
    }

    public String getOwner() {
        return this.owner;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean z) {
        this.locked = z;
    }
}
