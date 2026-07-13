package com.sportradar;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SportRadar is a live event tracker library for sports matches.
 * It allows you to:
 * - Start a new match
 * - Update the score
 * - Finish a match (removes it from tracking)
 * - Get a summary of matches in progress
 * 
 * Uses optimistic locking with ConcurrentHashMap for better concurrency.
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
     * @throws IllegalArgumentException if the match has already been started
     */
    @Override
    public void startMatch(String homeTeam, String awayTeam) {
        String key = getMatchKey(homeTeam, awayTeam);
        
        // Use putIfAbsent for atomic insert-if-absent
        long id = matchIdCounter.incrementAndGet();
        Match newMatch = new Match(id, homeTeam, awayTeam);
        Match existing = matches.putIfAbsent(key, newMatch);
        
        if (existing != null) {
            throw new IllegalArgumentException("Match between " + homeTeam + " and " + awayTeam + " already started");
        }
    }

    /**
     * Updates the score of an in-progress match.
     * Uses optimistic locking with compare-and-swap to handle concurrent updates.
     * Retries only if another thread's update resulted in a lower total score.
     *
     * @param homeTeam name of the home team
     * @param awayTeam name of the away team
     * @param homeScore the home team's new score
     * @param awayScore the away team's new score
     * @throws IllegalArgumentException if the match does not exist
     */
    @Override
    public void updateScore(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        String key = getMatchKey(homeTeam, awayTeam);
        int newTotalScore = homeScore + awayScore;
        
        while (true) {
            Match oldMatch = matches.get(key);
            if (oldMatch == null) {
                throw new IllegalArgumentException("Match between " + homeTeam + " and " + awayTeam + " not found");
            }
            
            // Create updated match (immutable, so new instance)
            Match newMatch = oldMatch.updateScore(homeScore, awayScore);
            
            // Try to replace - only succeeds if oldMatch is still in the map (compare-and-swap)
            if (matches.replace(key, oldMatch, newMatch)) {
                return; // Success!
            }
            
            // Another thread updated the match. Check if we should retry.
            Match currentMatch = matches.get(key);
            if (currentMatch != null && currentMatch.getTotalScore() < newTotalScore) {
                // Current total score is lower, retry to apply our update
                continue;
            }
            
            // Current total score is higher or equal, accept the other thread's update
            return;
        }
    }

    /**
     * Finishes a match and removes it from tracking.
     *
     * @param homeTeam name of the home team
     * @param awayTeam name of the away team
     * @throws IllegalArgumentException if the match does not exist
     */
    @Override
    public void finishMatch(String homeTeam, String awayTeam) {
        String key = getMatchKey(homeTeam, awayTeam);
        Match match = matches.remove(key);
        
        if (match == null) {
            throw new IllegalArgumentException("Match between " + homeTeam + " and " + awayTeam + " not found");
        }
    }

    /**
     * Gets a summary of all matches in progress, ordered by:
     * 1. Total score (descending) - highest total first
     * 2. Start time (descending) - most recently started first
     *
     * @return list of matches in progress, ordered as specified
     */
    @Override
    public List<String> getMatchesSummary() {
        // Create explicit snapshot to protect against concurrent modifications during sorting
        List<Match> snapshot = new ArrayList<>(matches.values());
        
        return snapshot.stream()
                .sorted(Comparator
                        .comparingInt(Match::getTotalScore)
                        .thenComparingLong(Match::getId)
                        .reversed())
                .map(m -> m.getHomeScore() + " " + m.getHomeTeam() + " - " + m.getAwayTeam() + " " +m.getAwayScore())
                .collect(Collectors.toList());
    }

    /**
     * Gets three teams with the highest score.
     * Highest score teams go first. Teams ex aequo are sorted alphabetically
     *
     * Notice there might be more results than three if there are teams ex aequo or
     * there might be fewer results if there is only one or zero matches ongoing
     * Teams ex aequo are sorted alphabetically
     *
     * @return teams names with assigned scores
     */
    @Override
    public List<String> getTopThreeTeams() {
        // Create explicit snapshot to protect against concurrent modifications during sorting
        List<Match> snapshot = new ArrayList<>(matches.values());

        TreeMap<Integer, SortedSet<String>> scoreTable = new TreeMap<>();
        for (Match match : snapshot){
            addScores(scoreTable, match.getHomeScore(), match.getHomeTeam());
            addScores(scoreTable, match.getAwayScore(), match.getAwayTeam());
        }

        List<String> result = new LinkedList<>();

        int limit = 0;
        for (Integer score: scoreTable.descendingKeySet()) {
            SortedSet<String> teams = scoreTable.get(score);
            for (String team : teams) {
                result.add(team + " " + score);
            }

            limit += teams.size();
            if (limit >= 3) {
                break;
            }
        }
        return result;
    }

    private static void addScores(TreeMap<Integer, SortedSet<String>> scoreTable, int score, String team) {
        SortedSet<String> teams = scoreTable.get(score);
        if (teams == null) {
            teams = new TreeSet<>();
        }
        teams.add(team);
        scoreTable.put(score, teams);
    }

    /**
     * Generates a unique key for a match based on team names.
     * (Package-private helper method)
     */
    private String getMatchKey(String homeTeam, String awayTeam) {
        return homeTeam + " vs " + awayTeam;
    }
}
