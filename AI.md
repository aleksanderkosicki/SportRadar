### Short summary of how AI tools were used

AI was used to generate the initial version of codebase

### Prompt history and other contextual information

Github copilot prompt history for the initial code:

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

### artifacts that guided the implementation