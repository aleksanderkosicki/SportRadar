package com.sportradar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SportRadarTest {
    private MatchTracker matchTracker;

    @BeforeEach
    void setUp() {
        matchTracker = new SportRadar();
    }

    @Test
    void testStartMatch() {
        matchTracker.startMatch("Mexico", "Canada");
        
        // Verify match exists in summary
        List<Match> summary = matchTracker.getMatchesSummary();
        assertEquals(1, summary.size());
        Match match = summary.get(0);
        assertEquals("Mexico", match.getHomeTeam());
        assertEquals("Canada", match.getAwayTeam());
        assertEquals(0, match.getHomeScore());
        assertEquals(0, match.getAwayScore());
    }

    @Test
    void testUpdateScore() {
        matchTracker.startMatch("Mexico", "Canada");
        matchTracker.updateScore("Mexico", "Canada", 0, 5);
        
        List<Match> summary = matchTracker.getMatchesSummary();
        Match match = summary.get(0);
        assertEquals(0, match.getHomeScore());
        assertEquals(5, match.getAwayScore());
    }

    @Test
    void testUpdateScoreNegative() {
        matchTracker.startMatch("Mexico", "Canada");
        assertThrows(IllegalArgumentException.class, () ->
                matchTracker.updateScore("Mexico", "Canada", -1, 5)
        );
    }

    @Test
    void testUpdateScoreNonExistentMatch() {
        assertThrows(IllegalArgumentException.class, () ->
                matchTracker.updateScore("NonExistent", "Teams")
        );
    }

    @Test
    void testFinishMatch() {
        matchTracker.startMatch("Mexico", "Canada");
        matchTracker.updateScore("Mexico", "Canada", 0, 5);
        
        // Finish the match
        matchTracker.finishMatch("Mexico", "Canada");
        
        // Verify match is removed from summary
        List<Match> summary = matchTracker.getMatchesSummary();
        assertEquals(0, summary.size());
    }

    @Test
    void testFinishNonExistentMatch() {
        assertThrows(IllegalArgumentException.class, () ->
                matchTracker.finishMatch("NonExistent", "Teams")
        );
    }

    @Test
    void testGetMatchesSummaryOrdering() throws InterruptedException {
        // Create matches in the specified order
        matchTracker.startMatch("Mexico", "Canada");
        Thread.sleep(10); // Small delay to ensure different timestamps
        matchTracker.startMatch("Spain", "Brazil");
        Thread.sleep(10);
        matchTracker.startMatch("Germany", "France");
        Thread.sleep(10);
        matchTracker.startMatch("Uruguay", "Italy");
        Thread.sleep(10);
        matchTracker.startMatch("Argentina", "Australia");

        // Update scores
        matchTracker.updateScore("Mexico", "Canada", 0, 5);       // Total: 5
        matchTracker.updateScore("Spain", "Brazil", 10, 2);       // Total: 12
        matchTracker.updateScore("Germany", "France", 2, 2);      // Total: 4
        matchTracker.updateScore("Uruguay", "Italy", 6, 6);       // Total: 12
        matchTracker.updateScore("Argentina", "Australia", 3, 1); // Total: 4

        List<Match> summary = matchTracker.getMatchesSummary();

        // Verify ordering
        assertEquals(5, summary.size());
        assertEquals("Uruguay", summary.get(0).getHomeTeam());    // Uruguay 6-6 (total 12, started 4th)
        assertEquals("Italy", summary.get(0).getAwayTeam());
        assertEquals("Spain", summary.get(1).getHomeTeam());      // Spain 10-2 (total 12, started 2nd)
        assertEquals("Brazil", summary.get(1).getAwayTeam());
        assertEquals("Mexico", summary.get(2).getHomeTeam());     // Mexico 0-5 (total 5)
        assertEquals("Canada", summary.get(2).getAwayTeam());
        assertEquals("Argentina", summary.get(3).getHomeTeam());  // Argentina 3-1 (total 4, started 5th)
        assertEquals("Australia", summary.get(3).getAwayTeam());
        assertEquals("Germany", summary.get(4).getHomeTeam());    // Germany 2-2 (total 4, started 3rd)
        assertEquals("France", summary.get(4).getAwayTeam());
    }

    @Test
    void testGetMatchesSummaryFinishedMatchesRemoved() {
        matchTracker.startMatch("Mexico", "Canada");
        matchTracker.startMatch("Spain", "Brazil");
        matchTracker.updateScore("Mexico", "Canada", 0, 5);
        matchTracker.updateScore("Spain", "Brazil", 10, 2);
        matchTracker.finishMatch("Mexico", "Canada");

        List<Match> summary = matchTracker.getMatchesSummary();

        // Only Spain vs Brazil should be in the summary
        assertEquals(1, summary.size());
        assertEquals("Spain", summary.get(0).getHomeTeam());
        assertEquals("Brazil", summary.get(0).getAwayTeam());
    }
}
