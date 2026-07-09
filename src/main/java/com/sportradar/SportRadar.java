package com.sportradar;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
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
    private final Map<String, Match> matches = new HashMap<>();
    private final AtomicLong matchIdCounter = new AtomicLong(0);

    /**
     * Starts a new match.
     * The match ID is generated internally using a thread-safe atomic counter.
     *
     * @param homeTeam name of the home team
     * @param awayTeam name of the away team
     */
    @Override
    public synchronized void startMatch(String homeTeam, String awayTeam) {
        String key = getMatchKey(homeTeam, awayTeam);
        if (matches.containsKey(key)) {
            throw new IllegalArgumentException("Match between " + homeTeam + " and " + awayTeam + " already started");
        }

        long id = matchIdCounter.incrementAndGet();
        Match match = new Match(id, homeTeam, awayTeam);
        matches.put(getMatchKey(homeTeam, awayTeam), match);
    }

    /**
     * Updates the score of an in-progress match.
     * Returns a new Match instance with updated scores and stores it.
     *
     * @param homeTeam name of the home team
     * @param awayTeam name of the away team
     * @param homeScore the home team's new score
     * @param awayScore the away team's new score
     * @throws IllegalArgumentException if the match does not exist
     */
    @Override
    public synchronized void updateScore(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        String key = getMatchKey(homeTeam, awayTeam);

        Match match = matches.get(key);
        if (match == null) {
            throw new IllegalArgumentException("Match between " + homeTeam + " and " + awayTeam + " not found");
        }
        Match updatedMatch = match.updateScore(homeScore, awayScore);
        matches.put(key, updatedMatch);
    }

    /**
     * Finishes a match and removes it from tracking.
     *
     * @param homeTeam name of the home team
     * @param awayTeam name of the away team
     * @throws IllegalArgumentException if the match does not exist
     */
    @Override
    public synchronized void finishMatch(String homeTeam, String awayTeam) {
        String key = getMatchKey(homeTeam, awayTeam);
        if (!matches.containsKey(key)) {
            throw new IllegalArgumentException("Match between " + homeTeam + " and " + awayTeam + " not found");
        }
        matches.remove(key);
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
        List<Match> matchesList;
        synchronized(this) {
            matchesList = new ArrayList<>(matches.values());
        }
        return matchesList.stream()
                .sorted(Comparator
                        .comparingInt(Match::getTotalScore)
                        .thenComparingLong(Match::getId).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Generates a unique key for a match based on team names.
     * (Package-private helper method)
     */
    private String getMatchKey(String homeTeam, String awayTeam) {
        return homeTeam + " vs " + awayTeam;
    }
}
