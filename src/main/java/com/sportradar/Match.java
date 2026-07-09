package com.sportradar;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a sports match with home and away teams.
 */
public class Match {
    private final String id;
    private final String homeTeam;
    private final String awayTeam;
    private int homeScore;
    private int awayScore;
    private final LocalDateTime startTime;

    /**
     * Creates a new match.
     *
     * @param id unique identifier for the match
     * @param homeTeam name of the home team
     * @param awayTeam name of the away team
     * @param startTime when the match was started
     */
    public Match(String id, String homeTeam, String awayTeam, LocalDateTime startTime) {
        this.id = Objects.requireNonNull(id, "Match ID cannot be null");
        this.homeTeam = Objects.requireNonNull(homeTeam, "Home team name cannot be null");
        this.awayTeam = Objects.requireNonNull(awayTeam, "Away team name cannot be null");
        this.startTime = Objects.requireNonNull(startTime, "Start time cannot be null");
        this.homeScore = 0;
        this.awayScore = 0;
    }

    /**
     * Gets the match ID.
     */
    public String getId() {
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
     * Gets when the match was started.
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Updates the score for this match.
     *
     * @param homeScore new home team score
     * @param awayScore new away team score
     * @throws IllegalArgumentException if scores are negative
     */
    public void updateScore(int homeScore, int awayScore) {
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Scores cannot be negative");
        }
        this.homeScore = homeScore;
        this.awayScore = awayScore;
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
        return Objects.equals(id, match.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
