package com.sportradar;

/**
 * Factory for creating MatchTracker instances.
 * This is the main entry point for users of the SportRadar library.
 */
public final class MatchTrackerFactory {
    private MatchTrackerFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a new MatchTracker instance.
     *
     * @return a new MatchTracker implementation
     */
    public static MatchTracker createMatchTracker() {
        return new SportRadar();
    }
}
