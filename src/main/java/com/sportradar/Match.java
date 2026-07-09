package com.sportradar;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an immutable sports match with home and away teams.
 * To update the score, use the updateScore method which returns a new Match instance.
 */
public final class Match {
    private final long id;
    private final String homeTeam;
    private final String awayTeam;
    private final int homeScore;
    private final int awayScore;

    /**
     * Creates a new match.
     *
     * @param id unique identifier for the match
     * @param homeTeam name of the home team
     * @param awayTeam name of the away team
     */
    public Match(long id, String homeTeam, String awayTeam) {
        this(id, homeTeam, awayTeam, 0, 0);
    }

    /**
     * Creates a new match with specified scores.
     * (Package-private constructor used internally)
     */
    Match(long id, String homeTeam, String awayTeam, int homeScore, int awayScore) {
        this.id = Objects.requireNonNull(id, "Match ID cannot be null");
        this.homeTeam = Objects.requireNonNull(homeTeam, "Home team name cannot be null");
        this.awayTeam = Objects.requireNonNull(awayTeam, "Away team name cannot be null");
        
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Scores cannot be negative");
        }
        
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    /**
     * Gets the match ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the home team name.
     */
    public String getHomeTeam() {
        return homeTeam;
    }

    /**
     * Gets the away team name.
     */
    public String getAwayTeam() {
        return awayTeam;
    }

    /**
     * Gets the home team's current score.
     */
    public int getHomeScore() {
        return homeScore;
    }

    /**
     * Gets the away team's current score.
     */
    public int getAwayScore() {
        return awayScore;
    }

    /**
     * Gets the total score of the match.
     */
    public int getTotalScore() {
        return homeScore + awayScore;
    }

    /**
     * Returns a new Match with updated scores.
     * This Match instance remains unchanged.
     *
     * @param homeScore new home team score
     * @param awayScore new away team score
     * @return a new Match instance with updated scores
     * @throws IllegalArgumentException if scores are negative
     */
    public Match updateScore(int homeScore, int awayScore) {
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Scores cannot be negative");
        }
        return new Match(this.id, this.homeTeam, this.awayTeam, homeScore, awayScore);
    }

    @Override
    public String toString() {
        return homeTeam + " " + homeScore + " – " + awayTeam + " " + awayScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return id == match.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
