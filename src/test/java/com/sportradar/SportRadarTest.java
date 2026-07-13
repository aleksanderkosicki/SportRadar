package com.sportradar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;

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
        List<String> summary = matchTracker.getMatchesSummary();
        assertEquals(1, summary.size());
        String match = summary.get(0);
        assertEquals("0 Mexico - Canada 0", match);
    }

    @Test
    void testUpdateScore() {
        matchTracker.startMatch("Mexico", "Canada");
        matchTracker.updateScore("Mexico", "Canada", 0, 5);
        
        List<String> summary = matchTracker.getMatchesSummary();
        assertEquals(1, summary.size());
        String match = summary.get(0);
        assertEquals("0 Mexico - Canada 5", match);
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
                matchTracker.updateScore("NonExistent", "Teams", 0, 0)
        );
    }

    @Test
    void testFinishMatch() {
        matchTracker.startMatch("Mexico", "Canada");
        matchTracker.updateScore("Mexico", "Canada", 0, 5);
        
        // Finish the match
        matchTracker.finishMatch("Mexico", "Canada");
        
        // Verify match is removed from summary
        List<String> summary = matchTracker.getMatchesSummary();
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
        matchTracker.startMatch("Spain", "Brazil");
        matchTracker.startMatch("Germany", "France");
        matchTracker.startMatch("Uruguay", "Italy");
        matchTracker.startMatch("Argentina", "Australia");

        // Update scores
        matchTracker.updateScore("Mexico", "Canada", 0, 5);       // Total: 5
        matchTracker.updateScore("Spain", "Brazil", 10, 2);       // Total: 12
        matchTracker.updateScore("Germany", "France", 2, 2);      // Total: 4
        matchTracker.updateScore("Uruguay", "Italy", 6, 6);       // Total: 12
        matchTracker.updateScore("Argentina", "Australia", 3, 1); // Total: 4

        List<String> summary = matchTracker.getMatchesSummary();

        // Verify ordering
        assertEquals(5, summary.size());
        assertEquals(summary.get(0), "6 Uruguay - Italy 6"); // Uruguay 6-6 (total 12, started 4th)
        assertEquals(summary.get(1), "10 Spain - Brazil 2"); // Spain 10-2 (total 12, started 2nd)
        assertEquals(summary.get(2), "0 Mexico - Canada 5"); // Mexico 0-5 (total 5)
        assertEquals(summary.get(3), "3 Argentina - Australia 1"); // Argentina 3-1 (total 4, started 5th)
        assertEquals(summary.get(4), "2 Germany - France 2"); // Germany 2-2 (total 4, started 3rd)
    }

    @Test
    void testGetMatchesSummaryFinishedMatchesRemoved() {
        matchTracker.startMatch("Mexico", "Canada");
        matchTracker.startMatch("Spain", "Brazil");
        matchTracker.updateScore("Mexico", "Canada", 0, 5);
        matchTracker.updateScore("Spain", "Brazil", 10, 2);
        matchTracker.finishMatch("Mexico", "Canada");

        List<String> summary = matchTracker.getMatchesSummary();

        // Only Spain vs Brazil should be in the summary
        assertEquals(1, summary.size());
        assertEquals(summary.get(0), "10 Spain - Brazil 2");
    }

    @Test
    void multithreadingTest() {
        final int THREAD_NUM = 100;
        final CountDownLatch step1 = new CountDownLatch(THREAD_NUM);
        final CountDownLatch step2 = new CountDownLatch(THREAD_NUM);
        for (int i = 0; i < THREAD_NUM; i++) {
            final int threadNum = i;
            new Thread(()-> {
                Random rn = new Random();
                matchTracker.startMatch("A" + threadNum, "B"+ threadNum);
                matchTracker.updateScore("A" + threadNum, "B"+ threadNum,
                        rn.nextInt(3), rn.nextInt(3));

                step1.countDown();
                try {
                    step1.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (int k = 0; k < 10; k++) {
                    int teamNum = rn.nextInt(THREAD_NUM);
                    int homeScore = 3 + rn.nextInt(k+1);
                    int awayScore = 3 + rn.nextInt(k+1);
                    matchTracker.updateScore("A" + teamNum, "B" + teamNum,
                            homeScore, awayScore);
                }

                try {
                    step2.countDown();
                    step2.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (threadNum == 0) {
                    List<String> matches = matchTracker.getMatchesSummary();
                    assertEquals(100, matches.size());
                    assertMatchesInOrder(matches);
                }
            }).start();
        }
    }

    private void assertMatchesInOrder(List<String> matches) {
        for(int i = 0; i < matches.size(); i++) {
            for (int j = i + 1; j < matches.size(); j++) {
                String matchA = matches.get(i);
                int totalScoreA = getTotalScore(matchA);
                String matchB = matches.get(j);
                int totalScoreB = getTotalScore(matchB);
                assertTrue(totalScoreA >= totalScoreB);
            }
        }
    }

    private static int getTotalScore(String match) {
        String parts[] = match.split(" ");
        return Integer.parseInt(parts[0]) + Integer.parseInt(parts[4]);
    }

    @Test
    void testGetTopThreeTeams() throws InterruptedException {
        // Create matches in the specified order
        matchTracker.startMatch("Mexico", "Canada");
        matchTracker.startMatch("Spain", "Brazil");
        matchTracker.startMatch("Germany", "France");
        matchTracker.startMatch("Uruguay", "Italy");
        matchTracker.startMatch("Argentina", "Australia");

        // Update scores
        matchTracker.updateScore("Mexico", "Canada", 0, 5);       // Total: 5
        matchTracker.updateScore("Spain", "Brazil", 10, 2);       // Total: 12
        matchTracker.updateScore("Germany", "France", 2, 2);      // Total: 4
        matchTracker.updateScore("Uruguay", "Italy", 6, 6);       // Total: 12
        matchTracker.updateScore("Argentina", "Australia", 3, 1); // Total: 4

        List<String> topThree = matchTracker.getTopThreeTeams();

        assertEquals(3, topThree.size());
        assertEquals(topThree.get(0),"Spain 10");
        assertEquals(topThree.get(1),"Italy 6");
        assertEquals(topThree.get(2),"Uruguay 6");
    }

    @Test
    void testGetTopThreeTeams_OnlyOneMatch() throws InterruptedException {
        // Create matches in the specified order
        matchTracker.startMatch("Mexico", "Canada");

        // Update scores
        matchTracker.updateScore("Mexico", "Canada", 0, 5);       // Total: 5

        List<String> topThree = matchTracker.getTopThreeTeams();

        assertEquals(2, topThree.size());
        assertEquals(topThree.get(0),"Canada 5");
        assertEquals(topThree.get(1),"Mexico 0");
    }

    @Test
    void testGetTopThreeTeams_moreThanThree() throws InterruptedException {
        // Create matches in the specified order
        matchTracker.startMatch("Mexico", "Canada");
        matchTracker.startMatch("Spain", "Brazil");
        matchTracker.startMatch("Germany", "France");
        matchTracker.startMatch("Uruguay", "Italy");
        matchTracker.startMatch("Argentina", "Australia");

        // Update scores
        matchTracker.updateScore("Mexico", "Canada", 0, 5);       // Total: 5
        matchTracker.updateScore("Spain", "Brazil", 10, 2);       // Total: 12
        matchTracker.updateScore("Germany", "France", 6, 2);      // Total: 4
        matchTracker.updateScore("Uruguay", "Italy", 6, 6);       // Total: 12
        matchTracker.updateScore("Argentina", "Australia", 3, 1); // Total: 4

        List<String> topThree = matchTracker.getTopThreeTeams();

        assertEquals(4, topThree.size());
        assertEquals(topThree.get(0),"Spain 10");
        assertEquals(topThree.get(1),"Germany 6");
        assertEquals(topThree.get(2),"Italy 6");
        assertEquals(topThree.get(3),"Uruguay 6");
    }
}
