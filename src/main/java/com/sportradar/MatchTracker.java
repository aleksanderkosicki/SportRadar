package com.sportradar;

import java.util.List;

/**
 * Interface for tracking live sports matches.
 * Provides operations to manage match lifecycle and retrieve match summaries.
 */
public interface MatchTracker {
    /**
     * Starts a new match.
     *
     * @param id unique identifier for the match
     * @param homeTeam name of the home team
     * @param awayTeam name of the away team
     * @return the created match
     * @throws IllegalArgumentException if a match with the same ID already exists
     */
    Match startMatch(String id, String homeTeam, String awayTeam);

    /**
     * Updates the score of an in-progress match.
     *
     * @param matchId the ID of the match to update
     * @param homeScore the home team's new score
     * @param awayScore the away team's new score
     * @throws IllegalArgumentException if the match does not exist
     */
    void updateScore(String matchId, int homeScore, int awayScore);

    /**
     * Finishes a match and removes it from tracking.
     *
     * @param matchId the ID of the match to finish
     * @throws IllegalArgumentException if the match does not exist
     */
    void finishMatch(String matchId);

    /**
     * Gets a summary of all matches in progress, ordered by:
     * 1. Total score (descending) - highest total first
     * 2. Start time (descending) - most recently started first
     *
     * @return list of matches in progress, ordered as specified
     */
    List<Match> getMatchesSummary();
}
