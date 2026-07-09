package com.sportradar;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SportRadar is a live event tracker library for sports matches.
 * It allows you to:
 * - Start a new match
 * - Update the score
 * - Finish a match (removes it from tracking)
 * - Get a summary of matches in progress
 */
class SportRadar implements MatchTracker {
    private final Map<String, Match> matches = new ConcurrentHashMap<>();
    private final AtomicLong matchIdCounter = new AtomicLong(0);

    /**
     * Starts a new match.
     * The match ID is generated internally using a thread-safe atomic counter.
     *
     * @param homeTeam name of the home team
     * @param awayTeam name of the away team
     * @return the created match
     */
    @Override
    public Match startMatch(String homeTeam, String awayTeam) {
        String id = String.valueOf(matchIdCounter.incrementAndGet());
        Match match = new Match(id, homeTeam, awayTeam, LocalDateTime.now());
        matches.put(id, match);
        return match;
    }

    /**
     * Updates the score of an in-progress match.
     * Returns a new Match instance with updated scores and stores it.
     *
     * @param matchId the ID of the match to update
     * @param homeScore the home team's new score
     * @param awayScore the away team's new score
     * @throws IllegalArgumentException if the match does not exist
     */
    @Override
    public void updateScore(String matchId, int homeScore, int awayScore) {
        Match match = matches.get(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match with ID " + matchId + " not found");
        }
        Match updatedMatch = match.updateScore(homeScore, awayScore);
        matches.put(matchId, updatedMatch);
    }

    /**
     * Finishes a match and removes it from tracking.
     *
     * @param matchId the ID of the match to finish
     * @throws IllegalArgumentException if the match does not exist
     */
    @Override
    public void finishMatch(String matchId) {
        if (!matches.containsKey(matchId)) {
            throw new IllegalArgumentException("Match with ID " + matchId + " not found");
        }
        matches.remove(matchId);
    }

    /**
     * Gets a summary of all matches in progress, ordered by:
     * 1. Total score (descending) - highest total first
     * 2. Start time (descending) - most recently started first
     *
     * @return list of matches in progress, ordered as specified
     */
    @Override
    public List<Match> getMatchesSummary() {
        return matches.values().stream()
                .sorted(Comparator
                        .comparingInt(Match::getTotalScore).reversed()
                        .thenComparing(Match::getStartTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Gets a specific match by ID.
     *
     * @param matchId the ID of the match to retrieve
     * @return the match, or null if not found
     */
    public Match getMatch(String matchId) {
        return matches.get(matchId);
    }

    /**
     * Gets all matches currently being tracked.
     *
     * @return collection of all matches in progress
     */
    public Collection<Match> getAllMatches() {
        return Collections.unmodifiableCollection(matches.values());
    }
}
