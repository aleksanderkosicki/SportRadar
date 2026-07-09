### Short summary of how AI tools were used

AI was used to generate the initial version of codebase

### Prompt history and other contextual information

#### Github copilot prompt history for the initial code:

- Can you help me with coding?
- The tech stack is pure java 17 and maven.
- It's a live event tracker library that would track matches. It allows to
    * Start a new match
    * Update the score
    * Finish a match
    * Get a summary of matches in progress
- Wait a moment. I need to tell you more details about " Get a summary of matches in progress" operation
- It should return the matches in progress ordered by:
    * Total score (descending)
    * If tied → most recently started match first
      For example if the following matches are started in the specified order and updated with these scores:
    * Mexico 0 – Canada 5
    * Spain 10 – Brazil 2
    * Germany 2 – France 2
    * Uruguay 6 – Italy 6
    * Argentina 3 – Australia 1
      Expected summary ordering:
    * Uruguay 6 – Italy 6
    * Spain 10 – Brazil 2
    * Mexico 0 – Canada 5
    * Argentina 3 – Australia 1
    * Germany 2 – France 2
- If the total scores are equal the most recently started match should come first. Both "Spain 10 – Brazil 2" and "Uruguay 6 – Italy 6" have the same total score, but the "Uruguay 6 – Italy 6" was started most recently and so it should come first.
- One more remark, we do not need to track FINISHED matches. They can be removed when "Finish a match" operation is called
- ok, can you create a separate interface for the main four library operations?
- 1) Can we have a factory that would return us the MatchTracker interface?
2) Can we hide the implementation details from the library user?
- Ok,I am now fine with all the code, please commit it
- Can we now focus on the MatchTracker interface. We would need to change it a little.
- Can you make the match immutable? Changing score would just create a new Match.
- please remove the first parameter (String id) from the startMatch method in the MatchTracker interface. It should be assigned internally in the SportRada from thread safe atomic variable
- let's make the MatchTracker interface simpler
    1) startMatch method should not return Match object
    2) in the methods updateScore and finishMatch we should use homeTeam and awayTeam instead of the matchID
    3) the matchId should not be visible to the library user
- Could you please update tests accordingly?


#### Github copilot prompts for the synchronization discussion:

- can you check if the SportRadar class code is multi-thread safe?
- but then we would have the race condition if one update happens during another
- How would you do the ConcurrentHashMap with optimistic locking?
- please do not change the sorting behaviour
- that sorting is broken, only one reversed() is needed there
- at which point snapshot in the getMatchesSummary() method is created?
- You have convinced me. Please replace the SportRadar with the one you suggested (i.e. without explicit 'synchronized' keywords and with the synchronized collection)
- Please do not cast the id to String
- one more remark regarding the updateScore. We would like to retry only if the totalScore we are trying to write down is larger the the one updated by other thread. Please fix that
- please commit the changes
- scan all the classes for errors now
- please use the Match id for sorting, not the startTime
- it's better, also please remove the unused methods (getMatch and getAllMatches)

End of monthly quota for the copilot here happened.

### artifacts that guided the implementation