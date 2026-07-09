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
        Match match = matchTracker.startMatch("match1", "Mexico", "Canada");
        assertNotNull(match);
        assertEquals("Mexico", match.getHomeTeam());
        assertEquals("Canada", match.getAwayTeam());
        assertEquals(0, match.getHomeScore());
        assertEquals(0, match.getAwayScore());
    }

    @Test
    void testStartMatchDuplicateId() {
        matchTracker.startMatch("match1", "Mexico", "Canada");
        assertThrows(IllegalArgumentException.class, () ->
                matchTracker.startMatch("match1", "Spain", "Brazil")
        );
    }

    @Test
    void testUpdateScore() {
        matchTracker.startMatch("match1", "Mexico", "Canada");
        matchTracker.updateScore("match1", 0, 5);
        Match match = matchTracker.startMatch("match1", "Mexico", "Canada");
        assertEquals(0, match.getHomeScore());
        assertEquals(5, match.getAwayScore());
    }

    @Test
    void testUpdateScoreNegative() {
        matchTracker.startMatch("match1", "Mexico", "Canada");
        assertThrows(IllegalArgumentException.class, () ->
                matchTracker.updateScore("match1", -1, 5)
        );
    }

    @Test
    void testUpdateScoreNonExistentMatch() {
        assertThrows(IllegalArgumentException.class, () ->
                matchTracker.updateScore("nonexistent", 1, 1)
        );
    }

    @Test
    void testFinishMatch() {
        matchTracker.startMatch("match1", "Mexico", "Canada");
        matchTracker.updateScore("match1", 0, 5);
        
        // Finish the match
        matchTracker.finishMatch("match1");
        
        // Verify match is removed from summary
        List<Match> summary = matchTracker.getMatchesSummary();
        assertFalse(summary.stream().anyMatch(m -> m.getId().equals("match1")));
    }

    @Test
    void testFinishNonExistentMatch() {
        assertThrows(IllegalArgumentException.class, () ->
                matchTracker.finishMatch("nonexistent")
        );
    }

    @Test
    void testGetMatchesSummaryOrdering() throws InterruptedException {
        // Create matches in the specified order
        matchTracker.startMatch("match1", "Mexico", "Canada");
        Thread.sleep(10); // Small delay to ensure different timestamps
        matchTracker.startMatch("match2", "Spain", "Brazil");
        Thread.sleep(10);
        matchTracker.startMatch("match3", "Germany", "France");
        Thread.sleep(10);
        matchTracker.startMatch("match4", "Uruguay", "Italy");
        Thread.sleep(10);
        matchTracker.startMatch("match5", "Argentina", "Australia");

        // Update scores
        matchTracker.updateScore("match1", 0, 5);     // Total: 5
        matchTracker.updateScore("match2", 10, 2);    // Total: 12
        matchTracker.updateScore("match3", 2, 2);     // Total: 4
        matchTracker.updateScore("match4", 6, 6);     // Total: 12
        matchTracker.updateScore("match5", 3, 1);     // Total: 4

        List<Match> summary = matchTracker.getMatchesSummary();

        // Verify ordering
        assertEquals(5, summary.size());
        assertEquals("match4", summary.get(0).getId()); // Uruguay 6-6 (total 12, started 4th)
        assertEquals("match2", summary.get(1).getId()); // Spain 10-2 (total 12, started 2nd)
        assertEquals("match1", summary.get(2).getId()); // Mexico 0-5 (total 5)
        assertEquals("match5", summary.get(3).getId()); // Argentina 3-1 (total 4, started 5th)
        assertEquals("match3", summary.get(4).getId()); // Germany 2-2 (total 4, started 3rd)
    }

    @Test
    void testGetMatchesSummaryFinishedMatchesRemoved() {
        matchTracker.startMatch("match1", "Mexico", "Canada");
        matchTracker.startMatch("match2", "Spain", "Brazil");
        matchTracker.updateScore("match1", 0, 5);
        matchTracker.updateScore("match2", 10, 2);
        matchTracker.finishMatch("match1");

        List<Match> summary = matchTracker.getMatchesSummary();

        // Only match2 should be in the summary
        assertEquals(1, summary.size());
        assertEquals("match2", summary.get(0).getId());
    }
}
